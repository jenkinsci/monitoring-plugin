/*
 * Copyright 2008-2017 by Emeric Vernat
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

import java.io.File;
import java.util.Arrays;
import java.util.logging.LogRecord;

import javax.servlet.ServletContext;

import hudson.Plugin;
import hudson.util.PluginServletFilter;
import jenkins.model.Jenkins;

/**
 * Entry point of the plugin.
 * 
 * <p>
 * There must be one {@link Plugin} class in each plugin. See javadoc of
 * {@link Plugin} for more about what can be done on this class.
 * 
 * @author Emeric Vernat
 */
@SuppressWarnings("deprecation")
public class PluginImpl extends Plugin {
	private ServletContext context;
	private HudsonMonitoringFilter filter;

	/** {@inheritDoc} */
	@Override
	public void start() throws Exception {
		super.start();

		// get the servletContext in Jenkins instead of overriding Plugin.setServletContext
		final Jenkins jenkins = Jenkins.getInstance();
		if (jenkins != null) {
			this.context = jenkins.servletContext;
		}

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
		// le repertoire de stockage est dans le repertoire de Hudson/Jenkins au lieu
		// d'etre dans le repertoire temporaire
		// ("/" initial necessaire sous windows pour javamelody v1.8.1)
		if (isParameterUndefined("javamelody.storage-directory")) {
			System.setProperty("javamelody.storage-directory",
					"/" + new File(jenkins.getRootDir(), "monitoring").getAbsolutePath());
		}
		// google-analytics pour connaitre le nombre d'installations actives et
		// pour connaitre les fonctions les plus utilisees
		if (isParameterUndefined("javamelody.analytics-disabled")
				&& isParameterUndefined("javamelody.analytics-id")) {
			System.setProperty("javamelody.analytics-id", "UA-1335263-7");
		}
		// http-transform-pattern pour agreger les requetes contenant des
		// parties "dynamiques" comme des numeros des builds,
		// les fichiers dans job/<name>/site/, javadoc/, ws/, cobertura/,
		// testReport/, violations/file/
		// ou les utilisateurs dans user/
		// ou les fichiers dans /static/abcdef123/ et dans /adjuncts/abcdef123/
		// ou les renders ajax lors de l'ajout de build step dans /$stapler/bound/c285ac3d-39c1-4515-86aa-0b42d75212b3/render
		if (isParameterUndefined("javamelody.http-transform-pattern")) {
			System.setProperty("javamelody.http-transform-pattern",
					"/\\d+/|/site/.+|avadoc/.+|/ws/.+|obertura/.+|estReport/.+|iolations/file/.+|/user/.+|/static/\\w+/|/adjuncts/\\w+/|/bound/[\\w\\-]+");
		}

		// custom reports (v1.50+)
		if (isParameterUndefined("javamelody.custom-reports")) {
			System.setProperty("javamelody.custom-reports", "Jenkins Info,About Monitoring");
			System.setProperty("javamelody.Jenkins Info", "/systemInfo");
			System.setProperty("javamelody.About Monitoring",
					"https://wiki.jenkins-ci.org/display/JENKINS/Monitoring");
		}

		// fix for JENKINS-14050: Unreadable HTML response for the monitoring reports
		if (isParameterUndefined("javamelody.gzip-compression-disabled")) {
			System.setProperty("javamelody.gzip-compression-disabled", "true");
		}

		if (isParameterUndefined("javamelody.maven-repositories")) {
			// add jenkins maven public repository for jenkins and plugins sources
			final String mavenRepositories = System.getProperty("user.home")
					+ "/.m2/repository,http://repo1.maven.org/maven2,http://repo.jenkins-ci.org/public";
			System.setProperty("javamelody.maven-repositories", mavenRepositories);
		}

		// we could set "javamelody.admin-emails" with
		// ((Mailer.DescriptorImpl) Jenkins.getInstance().getDescriptorByType(
		// hudson.tasks.Mailer.DescriptorImpl.class)).getAdminAddress();
		// but the admin-emails property is better next to the mail session

		// try to fix https://issues.jenkins-ci.org/browse/JENKINS-23442 (ClassCircularityError: java/util/logging/LogRecord)
		// by preloading the java.util.logging.LogRecord class
		Arrays.hashCode(new Class<?>[] { LogRecord.class });

		this.filter = new HudsonMonitoringFilter();
		PluginServletFilter.addFilter(filter);
	}

	private boolean isParameterUndefined(String key) {
		return System.getProperty(key) == null && context != null
				&& context.getInitParameter(key) == null;
	}

	HudsonMonitoringFilter getFilter() {
		return filter;
	}

	/** {@inheritDoc} */
	@Override
	public void postInitialize() throws Exception {
		super.postInitialize();
		if (filter == null) {
			throw new Exception("Post-initialization hook has been called before the plugin start. "
					+ "Filters are not available");
		}
		filter.getNodesCollector().init();

		// replaced by @Extension in NodesListener: new NodesListener(filter.getNodesCollector()).register();

		// replaced by @Extension in CounterRunListener: new CounterRunListener().register();
	}

	/** {@inheritDoc} */
	@Override
	public void stop() throws Exception {
		if (filter != null && filter.getNodesCollector() != null) {
			filter.getNodesCollector().stop();
		}
		super.stop();
	}
}
