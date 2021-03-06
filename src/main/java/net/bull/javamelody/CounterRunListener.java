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
package net.bull.javamelody;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import net.bull.javamelody.internal.common.Parameters;
import net.bull.javamelody.internal.model.Counter;

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
	private static final boolean DISABLED = Parameter.DISABLED.getValueAsBoolean();

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
