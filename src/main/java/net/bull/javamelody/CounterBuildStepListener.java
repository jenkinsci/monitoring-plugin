/*
 * Copyright 2008-2017 by Emeric Vernat
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
import hudson.model.BuildListener;
import hudson.model.BuildStepListener;
import hudson.tasks.BuildStep;

/**
 * Listener de debut et de fin de build-steps pour alimenter les tableaux des builds en cours,
 * et les statistiques des temps des builds.
 * @author Emeric Vernat
 */
@Extension
public class CounterBuildStepListener extends BuildStepListener {
	private static final Counter BUILD_COUNTER = CounterRunListener.getBuildCounter();
	private static final boolean DISABLED = Boolean
			.parseBoolean(Parameters.getParameter(Parameter.DISABLED));

	/**
	 * Constructor.
	 */
	public CounterBuildStepListener() {
		super();
	}

	/** {@inheritDoc} */
	@SuppressWarnings("rawtypes")
	@Override
	public void started(AbstractBuild build, BuildStep buildStep, BuildListener listener) {
		if (DISABLED || !BUILD_COUNTER.isDisplayed()) {
			return;
		}
		final String jobName = build.getProject().getName();
		final String buildStepName = buildStep.getClass().getSimpleName();
		//		if (bs instanceof Describable) {
		//	phrase générique en anglais (ou français, etc), peu instructive:
		//			buildStepName = ((Describable) bs).getDescriptor().getDisplayName();
		//		}

		// TODO display specifics of builds step depending on type:
		// depending on the instanceof type of buildStep (hudson.tasks.Ant, hudson.tasks.BatchFile, etc),
		// we could cast and get the specifics of the instance of build step (ant targets, batch command, maven goals, etc)
		// instead of just the type of the build step

		final String name = jobName + " / " + buildStepName;
		BUILD_COUNTER.bindContextIncludingCpu(name);
	}

	/** {@inheritDoc} */
	@SuppressWarnings("rawtypes")
	@Override
	public void finished(AbstractBuild build, BuildStep buildStep, BuildListener listener,
			boolean canContinue) {
		if (DISABLED || !BUILD_COUNTER.isDisplayed()) {
			return;
		}
		final boolean error = false; // is there a build step failure result?
		BUILD_COUNTER.addRequestForCurrentContext(error);
	}
}
