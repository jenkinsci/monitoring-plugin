/*
 * Copyright 2008-2011 by Emeric Vernat
 */
package net.bull.javamelody;

import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.model.AbstractBuild;
import hudson.model.listeners.RunListener;

/**
 * Listener de début et de fin de builds pour alimenter les tableaux des builds en cours
 * et les statistiques des temps des builds.
 * @author Emeric Vernat
 */
@SuppressWarnings("rawtypes")
public class CounterRunListener extends RunListener<AbstractBuild> {
	private static final Counter BUILD_COUNTER = new Counter("builds", "jobs.png");
	private static final boolean COUNTER_HIDDEN = Parameters.isCounterHidden(BUILD_COUNTER
			.getName());
	private static final boolean DISABLED = Boolean.parseBoolean(Parameters
			.getParameter(Parameter.DISABLED));

	public CounterRunListener() {
		super(AbstractBuild.class);
		// le compteur est affiché sauf si le paramètre displayed-counters dit
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
		final String name = r.getProject().getName();
		BUILD_COUNTER.bindContextIncludingCpu(name);
	}

	/** {@inheritDoc} */
	@Override
	public void onCompleted(AbstractBuild r, TaskListener listener) {
		super.onCompleted(r, listener);

		if (DISABLED || !BUILD_COUNTER.isDisplayed()) {
			return;
		}
		final boolean error = Result.FAILURE.equals(r.getResult());
		BUILD_COUNTER.addRequestForCurrentContext(error);
	}
}
