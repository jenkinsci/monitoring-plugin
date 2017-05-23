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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import jenkins.model.Jenkins;

/**
 * Collector of data for Hudson/Jenkins' nodes (slaves in general)
 * 
 * @author Emeric Vernat
 */
public class NodesCollector {
	private final boolean monitoringDisabled;
	private final Timer timer;
	private final Collector collector;
	private Map<String, JavaInformations> lastJavaInformationsList;

	private static class RemoteCollector extends Collector {
		RemoteCollector(String application, List<Counter> counters) {
			super(application, counters);
		}

		/** {@inheritDoc} */
		@Override
		void collectLocalContextWithoutErrors() {
			// no local collect
		}
	}

	/**
	 * Constructor.
	 * 
	 * @param filter Http filter to get the scheduling timer
	 */
	public NodesCollector(MonitoringFilter filter) {
		super();
		this.monitoringDisabled = Boolean.parseBoolean(Parameters.getParameter(Parameter.DISABLED));
		if (!monitoringDisabled) {
			this.timer = filter.getFilterContext().getTimer();
			final List<Counter> counters = Collections
					.singletonList(CounterRunListener.getBuildCounter());
			this.collector = new RemoteCollector("nodes", counters);
		} else {
			this.timer = null;
			this.collector = null;
		}
	}

	/**
	 * Initialization.
	 */
	public void init() {
		if (monitoringDisabled) {
			return;
		}
		final int periodMillis = Parameters.getResolutionSeconds() * 1000;
		// schedule of a background task, with an asynchronous execution now to
		// initialize the data
		final TimerTask collectTask = new TimerTask() {
			/** {@inheritDoc} */
			@Override
			public void run() {
				// errors must not happen in this task
				collectWithoutErrors();
			}
		};
		timer.schedule(collectTask, 5000, periodMillis);

		// schedule to send reports by email
		if (Parameters.getParameter(Parameter.MAIL_SESSION) != null
				&& Parameters.getParameter(Parameter.ADMIN_EMAILS) != null) {
			scheduleReportMailForSlaves();
		}
	}

	/**
	 * Schedule a collect now (used to collect data on new online nodes)
	 */
	public void scheduleCollectNow() {
		if (monitoringDisabled) {
			return;
		}
		final TimerTask collectTask = new TimerTask() {
			/** {@inheritDoc} */
			@Override
			public void run() {
				// errors must not happen in this task
				collectWithoutErrors();
			}
		};
		timer.schedule(collectTask, 0);
	}

	/**
	 * Stop the collector.
	 */
	public void stop() {
		if (monitoringDisabled) {
			return;
		}
		timer.cancel();
		collector.stop();
	}

	boolean isNodesMonitoringDisabled() {
		// if system property "javamelody.nodes-monitoring-disabled" is true,
		// then no periodic monitoring for nodes (only for master and nodes when nodes report requested)
		return Boolean.parseBoolean(System.getProperty("javamelody.nodes-monitoring-disabled"));
	}

	/**
	 * Collect the data (and never throws any exception).
	 */
	public void collectWithoutErrors() {
		try {
			// reevaluate each time to disable or reenable at runtime
			if (isNodesMonitoringDisabled()) {
				return;
			}
			collectWithoutErrorsNow();
		} catch (final Throwable t) { // NOPMD
			LOG.warn("exception while collecting data", t);
		}
	}

	/**
	 * Collect the data (and never throws any exception).
	 */
	public void collectWithoutErrorsNow() {
		try {
			lastJavaInformationsList = new RemoteCallHelper(null)
					.collectJavaInformationsListByName();

			// inspired by https://github.com/jenkinsci/jenkins/blob/master/core/src/main/java/hudson/model/LoadStatistics.java#L197
			// (note: jobs in quiet period are not counted)
			final Jenkins jenkins = Jenkins.getInstance();
			if (jenkins != null) {
				final int queueLength = jenkins.getQueue().getBuildableItems().size();
				// note: this BUILD_QUEUE_LENGTH needs at least javamelody-core 1.35.0-SNAPSHOT
				// including values for buildQueueLength in translations*.properties
				JdbcWrapper.BUILD_QUEUE_LENGTH.set(queueLength);
			}

			final List<JavaInformations> javaInformations = new ArrayList<>(
					getLastJavaInformationsList().values());
			collector.collectWithoutErrors(javaInformations);
		} catch (final Throwable t) { // NOPMD
			LOG.warn("exception while collecting data", t);
		}
	}

	private void scheduleReportMailForSlaves() {
		for (final Period period : MailReport.getMailPeriods()) {
			scheduleReportMailForSlaves(period);
		}
	}

	void scheduleReportMailForSlaves(final Period period) {
		assert period != null;
		final TimerTask task = new TimerTask() {
			/** {@inheritDoc} */
			@Override
			public void run() {
				try {
					// send the report
					final List<JavaInformations> javaInformations = new ArrayList<>(
							getLastJavaInformationsList().values());
					new MailReport().sendReportMail(getCollector(), true, javaInformations, period);
				} catch (final Throwable t) { // NOPMD
					// no error in this task
					LOG.warn("sending mail report failed", t);
				}
				// schedule again at the same hour next day or next week without
				// using a fixed period, because some days have 23h or 25h and
				// we do not want to have a change in the hour for sending the
				// report
				scheduleReportMailForSlaves(period);
			}
		};

		// schedule the task once
		timer.schedule(task, MailReport.getNextExecutionDate(period));
	}

	Collector getCollector() {
		return collector;
	}

	Map<String, JavaInformations> getLastJavaInformationsList() {
		return lastJavaInformationsList;
	}

	/**
	 * Is the monitoring disabled?
	 * @return boolean
	 */
	public boolean isMonitoringDisabled() {
		return monitoringDisabled;
	}
}
