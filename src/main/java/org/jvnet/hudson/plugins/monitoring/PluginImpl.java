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

import java.io.File;
import java.util.Arrays;
import java.util.logging.LogRecord;

import javax.servlet.ServletContext;

import hudson.Plugin;
import hudson.init.InitMilestone;
import hudson.util.PluginServletFilter;
import jenkins.model.Jenkins;
import net.bull.javamelody.Parameter;
import net.bull.javamelody.internal.common.Parameters;

/**
 * Entry point of the plugin.
 * <p>
 * There must be one {@link Plugin} class in each plugin. See javadoc of
 * {@link Plugin} for more about what can be done on this class.
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
		this.context = jenkins.servletContext;

		// jenkins.isUseCrumbs() is always false here because it's too early
		// and we can't use @Initializer(after = InitMilestone.COMPLETED)
		// because of https://issues.jenkins-ci.org/browse/JENKINS-37807
		// so check when jenkins is initialized
		final Thread thread = new Thread("javamelody-initializer") {
			@Override
			public void run() {
				while (jenkins.getInitLevel() != InitMilestone.COMPLETED) {
					try {
						Thread.sleep(1000);
					} catch (final InterruptedException e) {
						// RAS
					}
				}
				if (jenkins.isUseCrumbs()) {
					Parameter.CSRF_PROTECTION_ENABLED.setValue("true");
				}
			}
		};
		thread.setDaemon(true);
		thread.start();

		// on active les actions systemes (gc, heap dump, histogramme memoire,
		// processus...), sauf si l'administrateur a dit differemment
		if (isParameterUndefined(Parameter.SYSTEM_ACTIONS_ENABLED)) {
			Parameter.SYSTEM_ACTIONS_ENABLED.setValue("true");
		}
		// on desactive les graphiques jdbc et statistiques sql puisqu'il n'y en
		// aura pas
		if (isParameterUndefined(Parameter.NO_DATABASE)) {
			Parameter.NO_DATABASE.setValue("true");
		}
		// le repertoire de stockage est dans le repertoire de Hudson/Jenkins au lieu
		// d'etre dans le repertoire temporaire
		// ("/" initial necessaire sous windows pour javamelody v1.8.1)
		if (isParameterUndefined(Parameter.STORAGE_DIRECTORY)) {
			Parameter.STORAGE_DIRECTORY
					.setValue("/" + new File(jenkins.getRootDir(), "monitoring").getAbsolutePath());
		}
		// google-analytics pour connaitre le nombre d'installations actives et
		// pour connaitre les fonctions les plus utilisees
		if (isParameterUndefined("javamelody.analytics-disabled")
				&& isParameterUndefined(Parameter.ANALYTICS_ID)) {
			Parameter.ANALYTICS_ID.setValue("UA-1335263-7");
		}
		// http-transform-pattern pour agreger les requetes contenant des
		// parties "dynamiques" comme des numeros des builds,
		// les fichiers dans job/<name>/site/, javadoc/, ws/, cobertura/,
		// testReport/, violations/file/
		// ou les utilisateurs dans user/
		// ou les fichiers dans /static/abcdef123/ et dans /adjuncts/abcdef123/
		// ou les renders ajax lors de l'ajout de build step dans /$stapler/bound/c285ac3d-39c1-4515-86aa-0b42d75212b3/render
		if (isParameterUndefined(Parameter.HTTP_TRANSFORM_PATTERN)) {
			Parameter.HTTP_TRANSFORM_PATTERN.setValue(
					"/\\d+/|(?<=/static/|/adjuncts/|/bound/)[\\w\\-]+|(?<=/ws/|/user/|/testReport/|/javadoc/|/site/|/violations/file/|/cobertura/).+|(?<=/job/).+(?=/descriptorByName/)");
		}

		// custom reports (v1.50+)
		if (isParameterUndefined(Parameter.CUSTOM_REPORTS)) {
			Parameter.CUSTOM_REPORTS.setValue("Jenkins Info,About Monitoring");
			System.setProperty("javamelody.Jenkins Info", "/systemInfo");
			System.setProperty("javamelody.About Monitoring",
					"https://wiki.jenkins-ci.org/display/JENKINS/Monitoring");
		}

		// fix for JENKINS-14050: Unreadable HTML response for the monitoring reports
		if (isParameterUndefined(Parameter.GZIP_COMPRESSION_DISABLED)) {
			Parameter.GZIP_COMPRESSION_DISABLED.setValue("true");
		}

		if (isParameterUndefined(Parameter.MAVEN_REPOSITORIES)) {
			// add jenkins maven public repository for jenkins and plugins sources
			final String mavenRepositories = System.getProperty("user.home")
					+ "/.m2/repository,http://repo1.maven.org/maven2,http://repo.jenkins-ci.org/public";
			Parameter.MAVEN_REPOSITORIES.setValue(mavenRepositories);
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

	private boolean isParameterUndefined(Parameter parameter) {
		final String key = Parameters.PARAMETER_SYSTEM_PREFIX + parameter.getCode();
		return isParameterUndefined(key);
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
