/*
 * Copyright 2008-2011 by Emeric Vernat
 */
package org.jvnet.hudson.plugins.monitoring;

import hudson.model.Hudson;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.bull.javamelody.NodesCollector;
import net.bull.javamelody.NodesController;
import net.bull.javamelody.PluginMonitoringFilter;

/**
 * Filter of monitoring JavaMelody with security check for Hudson administrator.
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
				&& (requestURI.equals(monitoringUrl) || requestURI.equals(monitoringSlavesUrl))) {
			// only the hudson administrator can view the monitoring report
			Hudson.getInstance().checkPermission(Hudson.ADMINISTER);
		}

		if (requestURI.equals(monitoringSlavesUrl)) {
			final HttpServletResponse httpResponse = (HttpServletResponse) response;
			doMonitoring(httpRequest, httpResponse);
			return;
		}

		super.doFilter(request, response, chain);
	}

	/**
	 * Generate a report
	 * 
	 * @param httpRequest Http request
	 * @param httpResponse Http response
	 * @throws IOException e
	 */
	private void doMonitoring(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
			throws IOException {
		if (NodesController.isJavaInformationsNeeded(httpRequest)) {
			getNodesCollector().collectWithoutErrors();
		}
		final NodesController nodesController = new NodesController(getNodesCollector());
		nodesController.doMonitoring(httpRequest, httpResponse);
	}

	NodesCollector getNodesCollector() {
		return nodesCollector;
	}
}
