/*
 * Copyright 2008-2011 by Emeric Vernat
 */
package org.jvnet.hudson.plugins.monitoring;

import hudson.Plugin;
import hudson.model.Hudson;
import hudson.util.PluginServletFilter;

import java.io.File;

import javax.servlet.ServletContext;

import net.bull.javamelody.CounterRunListener;

/**
 * Entry point of the plugin.
 * 
 * <p>
 * There must be one {@link Plugin} class in each plugin. See javadoc of
 * {@link Plugin} for more about what can be done on this class.
 * 
 * @author Emeric Vernat
 */
public class PluginImpl extends Plugin {
	private ServletContext context;
	private HudsonMonitoringFilter filter;

	/** {@inheritDoc} */
	@Override
	public void setServletContext(ServletContext context) {
		super.setServletContext(context);
		this.context = context;
	}

	/** {@inheritDoc} */
	@Override
	public void start() throws Exception {
		super.start();

		// on active les actions systemes (gc, heap dump, histogramme memoire,
		// processus...), sauf si l'administrateur a dit differemment
		if (isParameterUndefined("javamelody.system-actions-enabled")) {
			System.setProperty("javamelody.system-actions-enabled", "true");
		}
		// on desactive les graphiques jdbc et statistiques sql puisqu'il n'y en
		// aura pas
		if (isParameterUndefined("javamelody.no-database")) {
			System.setProperty("javamelody.no-database", "true");
		}
		// le repertoire de stockage est dans le repertoire de hudson au lieu
		// d'etre dans le repertoire temporaire
		// ("/" initial necessaire sous windows pour javamelody v1.8.1)
		if (isParameterUndefined("javamelody.storage-directory")) {
			System.setProperty("javamelody.storage-directory", "/"
					+ new File(Hudson.getInstance().getRootDir(), "monitoring").getAbsolutePath());
		}
		// google-analytics pour connaitre le nombre d'installations actives et
		// pour connaitre les fonctions les plus utilisees
		if (isParameterUndefined("javamelody.analytics-id")) {
			System.setProperty("javamelody.analytics-id", "UA-1335263-7");
		}
		// http-transform-pattern pour agreger les requêtes contenant des
		// parties "dynamiques" comme des numeros des builds,
		// les fichiers dans job/<name>/site/, javadoc/, ws/, cobertura/,
		// testReport/, violations/file/
		// ou les utilisateurs dans user/ ou les fichiers dans
		// /static/abcdef123/
		if (isParameterUndefined("javamelody.http-transform-pattern")) {
			System.setProperty(
					"javamelody.http-transform-pattern",
					"/\\d+/|/site/.+|avadoc/.+|/ws/.+|obertura/.+|estReport/.+|iolations/file/.+|/user/.+|/static/\\w+/");
		}

		// we could set "javamelody.admin-emails" with
		// ((Mailer.DescriptorImpl) Hudson.getInstance().getDescriptorByType(
		// hudson.tasks.Mailer.DescriptorImpl.class)).getAdminAddress();
		// but the admin-emails property is better next to the mail session

		this.filter = new HudsonMonitoringFilter();
		PluginServletFilter.addFilter(filter);
	}

	private boolean isParameterUndefined(String key) {
		return System.getProperty(key) == null && context.getInitParameter(key) == null;
	}

	/** {@inheritDoc} */
	@SuppressWarnings("deprecation")
	@Override
	public void postInitialize() throws Exception {
		super.postInitialize();
		filter.getNodesCollector().init();

		// I had no success with @Extension twice
		new NodesListener(filter.getNodesCollector()).register();
		new CounterRunListener().register();
	}

	/** {@inheritDoc} */
	@Override
	public void stop() throws Exception {
		filter.getNodesCollector().stop();
		super.stop();
	}
}
