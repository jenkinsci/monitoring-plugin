/*
 * Copyright 2008-2011 by Emeric Vernat
 *
 *     This file is part of the Monitoring plugin.
 *
 * The Monitoring plugin is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The Monitoring plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the Monitoring plugin.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jvnet.hudson.plugins.monitoring;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Enumeration;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jenkins.model.Jenkins;
import net.bull.javamelody.NodesCollector;
import net.bull.javamelody.NodesController;
import net.bull.javamelody.PluginMonitoringFilter;

/**
 * Filter of monitoring JavaMelody with security check for Hudson/Jenkins administrator.
 *
 * @author Emeric Vernat
 */
public class HudsonMonitoringFilter extends PluginMonitoringFilter {
	private static final boolean PLUGIN_AUTHENTICATION_DISABLED = Boolean.parseBoolean(System
			.getProperty("javamelody.plugin-authentication-disabled"));

	private NodesCollector nodesCollector;

	/** {@inheritDoc} */
	@Override
	public void init(FilterConfig config) throws ServletException {
		super.init(config);

		nodesCollector = new NodesCollector(this);
		// on n'initialize pas nodesCollector tout de suite mais seulement dans
		// PluginImpl.postInitialize
	}

	/** {@inheritDoc} */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)
				|| getNodesCollector().isMonitoringDisabled()) {
			super.doFilter(request, response, chain);
			return;
		}
		final HttpServletRequest httpRequest = (HttpServletRequest) request;

		final String requestURI = httpRequest.getRequestURI();
		final String monitoringUrl = getMonitoringUrl(httpRequest);
		final String monitoringSlavesUrl = monitoringUrl + "/nodes";
		if (!PLUGIN_AUTHENTICATION_DISABLED
				&& (requestURI.equals(monitoringUrl) || requestURI.startsWith(monitoringSlavesUrl))) {
			// only the Hudson/Jenkins administrator can view the monitoring report
			Jenkins.getInstance().checkPermission(Jenkins.ADMINISTER);

			// this check of parameters is not supposed to be needed,
			// but just in case we can check parameters here
			if (hasInvalidParameters(request)) {
				((HttpServletResponse) response).sendError(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}
		}

		if (requestURI.startsWith(monitoringSlavesUrl)) {
			final String nodeName;
			if (requestURI.equals(monitoringSlavesUrl)) {
				nodeName = null;
			} else {
				nodeName = URLDecoder.decode(requestURI.substring(monitoringSlavesUrl.length())
						.replace("/", ""), "UTF-8");
			}
			final HttpServletResponse httpResponse = (HttpServletResponse) response;
			doMonitoring(httpRequest, httpResponse, nodeName);
			return;
		}

		super.doFilter(request, response, chain);
	}

	private boolean hasInvalidParameters(ServletRequest request) {
		final Enumeration<?> parameterNames = request.getParameterNames();
		while (parameterNames.hasMoreElements()) {
			final String parameterName = (String) parameterNames.nextElement();
			for (final String value : request.getParameterValues(parameterName)) {
				if (value.indexOf('"') != -1 || value.indexOf('\'') != -1
						|| value.indexOf('<') != -1 || value.indexOf('&') != -1) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Generate a report
	 *
	 * @param httpRequest Http request
	 * @param httpResponse Http response
	 * @param nodeName nom du node (slave ou "")
	 * @throws IOException e
	 */
	private void doMonitoring(HttpServletRequest httpRequest, HttpServletResponse httpResponse,
			String nodeName) throws IOException {
		if (NodesController.isJavaInformationsNeeded(httpRequest)) {
			getNodesCollector().collectWithoutErrorsNow();
		}
		final NodesController nodesController = new NodesController(getNodesCollector(), nodeName);
		nodesController.doMonitoring(httpRequest, httpResponse);
	}

	NodesCollector getNodesCollector() {
		return nodesCollector;
	}
}
