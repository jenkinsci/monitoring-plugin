package org.jvnet.hudson.plugins.monitoring;

import net.bull.javamelody.MonitoringFilter;
import net.bull.javamelody.SessionListener;
import hudson.model.Hudson;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
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
	public void init(FilterConfig config) throws ServletException {
		// Rq: avec hudson, on ne peut pas ajouter un SessionListener comme dans un web.xml, sauf si api servlet 3.0
		// sauf que cela ne marche dans Hudson avec Tomcat 7.0.0
		// if (config.getServletContext().getMajorVersion() >= 3) {
		//	config.getServletContext().addListener(SessionListener.class);
		// }
		super.init(config);
	}
	
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
