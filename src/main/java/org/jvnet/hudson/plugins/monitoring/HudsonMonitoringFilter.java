package org.jvnet.hudson.plugins.monitoring;

import net.bull.javamelody.MonitoringFilter;
import hudson.model.Hudson;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * Filter of monitoring JavaMelody with security check for Hudson administrator.
 * @author Emeric Vernat
 */
public class HudsonMonitoringFilter extends MonitoringFilter {
    /** {@inheritDoc} */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		if (!(request instanceof HttpServletRequest)) {
			super.doFilter(request, response, chain);
			return;
		}
		final HttpServletRequest httpRequest = (HttpServletRequest) request;

		if (httpRequest.getRequestURI().equals(getMonitoringUrl(httpRequest))) {
			// only the hudson administrator can view the monitoring report
			Hudson.getInstance().checkPermission(Hudson.ADMINISTER);
		}
		super.doFilter(request, response, chain);
	}
}
