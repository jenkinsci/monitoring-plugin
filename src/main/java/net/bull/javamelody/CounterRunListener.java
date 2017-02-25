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
package net.bull.javamelody;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;

/**
 * Listener de debut et de fin de builds pour alimenter les tableaux des builds en cours,
 * le graphique du nombre de builds en cours et les statistiques des temps des builds.
 * @author Emeric Vernat
 */
@Extension
@SuppressWarnings("rawtypes")
public class CounterRunListener extends RunListener<AbstractBuild> {
	private static final Counter BUILD_COUNTER = new Counter(Counter.BUILDS_COUNTER_NAME,
			"jobs.png");
	private static final boolean COUNTER_HIDDEN = Parameters
			.isCounterHidden(BUILD_COUNTER.getName());
	private static final boolean DISABLED = Boolean
			.parseBoolean(Parameters.getParameter(Parameter.DISABLED));

	/**
	 * Constructor.
	 */
	public CounterRunListener() {
		super(AbstractBuild.class);
		// le compteur est affiche sauf si le parametre displayed-counters dit
		// le contraire
		BUILD_COUNTER.setDisplayed(!COUNTER_HIDDEN);
	}

	static Counter getBuildCounter() {
		return BUILD_COUNTER;
	}

	/** {@inheritDoc} */
	@Override
	public void onStarted(AbstractBuild r, TaskListener listener) {
		super.onStarted(r, listener);

		if (DISABLED || !BUILD_COUNTER.isDisplayed()) {
			return;
		}
		if (isMavenModuleBuild(r)) {
			// si job maven, alors ok pour MavenModuleSetBuild,
			// mais pas ok pour le MavenBuild de chaque module Maven,
			// car onStarted et onCompleted seraient appelees sur des threads differents
			// et des builds resteraient affiches "en cours"
			return;
		}
		final String name = r.getProject().getName();
		BUILD_COUNTER.bindContextIncludingCpu(name);
		JdbcWrapper.RUNNING_BUILD_COUNT.incrementAndGet();
	}

	/** {@inheritDoc} */
	@Override
	public void onCompleted(AbstractBuild r, TaskListener listener) {
		super.onCompleted(r, listener);

		if (DISABLED || !BUILD_COUNTER.isDisplayed()) {
			return;
		}
		if (isMavenModuleBuild(r)) {
			return;
		}
		JdbcWrapper.RUNNING_BUILD_COUNT.decrementAndGet();
		final boolean error = Result.FAILURE.equals(r.getResult());
		BUILD_COUNTER.addRequestForCurrentContext(error);
	}

	private boolean isMavenModuleBuild(AbstractBuild r) {
		return "hudson.maven.MavenBuild".equals(r.getClass().getName());
	}
}
