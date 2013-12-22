/*
 * Copyright 2008-2011 by Emeric Vernat
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Collector of data for Hudson/Jenkins' nodes (slaves in general)
 * 
 * @author Emeric Vernat
 */
public class NodesCollector {
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

	private final boolean monitoringDisabled;
	private final Timer timer;
	private final Collector collector;
	private Map<String, JavaInformations> lastJavaInformationsList;

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
			final List<Counter> counters = Collections.singletonList(CounterRunListener
					.getBuildCounter());
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

	/**
	 * Collect the data (and never throws any exception).
	 */
	public void collectWithoutErrors() {
		try {
			lastJavaInformationsList = new RemoteCallHelper(null)
					.collectJavaInformationsListByName();

			// inspired by https://github.com/jenkinsci/jenkins/blob/master/core/src/main/java/hudson/model/LoadStatistics.java#L197
			// (note: jobs in quiet period are not counted)
			final int queueLength = hudson.model.Hudson.getInstance().getQueue()
					.getBuildableItems().size();
			// note: this BUILD_QUEUE_LENGTH needs at least javamelody-core 1.35.0-SNAPSHOT
			// including values for buildQueueLength in translations*.properties
			JdbcWrapper.BUILD_QUEUE_LENGTH.set(queueLength);

			final List<JavaInformations> javaInformations = new ArrayList<JavaInformations>(
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
					final List<JavaInformations> javaInformations = new ArrayList<JavaInformations>(
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
