/*
 * Copyright 2008-2019 by Emeric Vernat
 *
 *     This file is part of the Monitoring plugin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import javax.servlet.http.HttpSession;

import jenkins.model.Jenkins;
import net.bull.javamelody.NodesCollector;
import net.bull.javamelody.NodesController;
import net.bull.javamelody.Parameter;
import net.bull.javamelody.PluginMonitoringFilter;

/**
 * Filter of monitoring JavaMelody with security check for Hudson/Jenkins administrator.
 *
 * @author Emeric Vernat
 */
public class HudsonMonitoringFilter extends PluginMonitoringFilter {
	// TODO since Jenkins 2.2, we could almost extend MonitoringFilter instead of PluginMonitoringFilter
	// by using extension point: https://github.com/jenkinsci/jenkins/blob/master/core/src/main/java/jenkins/util/HttpSessionListener.java
	private static final boolean PLUGIN_AUTHENTICATION_DISABLED = Parameter.PLUGIN_AUTHENTICATION_DISABLED
			.getValueAsBoolean();

	private NodesCollector nodesCollector;

	/** {@inheritDoc} */
	@Override
	public String getApplicationType() {
		return "Jenkins";
	}

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
		final HttpServletResponse httpResponse = (HttpServletResponse) response;

		final String requestURI = httpRequest.getRequestURI();
		final String monitoringUrl = getMonitoringUrl(httpRequest);
		final String monitoringSlavesUrl = monitoringUrl + "/nodes";
		if (requestURI.equals(monitoringUrl) || requestURI.startsWith(monitoringSlavesUrl)) {
			if (isRumMonitoring(httpRequest, httpResponse)) {
				return;
			}
			if (!PLUGIN_AUTHENTICATION_DISABLED) {
				// only the Hudson/Jenkins administrator can view the monitoring report
				Jenkins.getInstance().checkPermission(Jenkins.ADMINISTER);
			}

			// this check of parameters is not supposed to be needed,
			// but just in case we can check parameters here
			if (hasInvalidParameters(request)) {
				httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}
		}

		if (requestURI.startsWith(monitoringSlavesUrl)) {
			final String nodeName;
			if (requestURI.equals(monitoringSlavesUrl)) {
				nodeName = null;
			} else {
				nodeName = URLDecoder.decode(
						requestURI.substring(monitoringSlavesUrl.length()).replace("/", ""),
						"UTF-8");
			}
			doMonitoring(httpRequest, httpResponse, nodeName);
			return;
		}

		try {
			super.doFilter(request, response, chain);
		} finally {
			putUserInfoInSession(httpRequest);
		}
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

	private void putUserInfoInSession(HttpServletRequest httpRequest) {
		final HttpSession session = httpRequest.getSession(false);
		if (session == null) {
			// la session n'est pas encore créée (et ne le sera peut-être jamais)
			return;
		}
		if (session.getAttribute(NodesController.SESSION_REMOTE_USER) == null) {
			// login utilisateur, peut être null
			// dans Jenkins, pas remoteUser = httpRequest.getRemoteUser();
			final String remoteUser = Jenkins.getAuthentication().getName();
			// !anonymous for https://issues.jenkins-ci.org/browse/JENKINS-42112
			if (remoteUser != null && !"anonymous".equals(remoteUser)) {
				session.setAttribute(NodesController.SESSION_REMOTE_USER, remoteUser);
			}
		}
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
