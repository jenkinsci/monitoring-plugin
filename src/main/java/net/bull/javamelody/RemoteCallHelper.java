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

import hudson.model.Computer;
import hudson.model.Hudson;
import hudson.remoting.Callable;
import hudson.remoting.Future;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

final class RemoteCallHelper {
	private static final Callable<JavaInformations, Throwable> JAVA_INFORMATIONS_TASK = new Callable<JavaInformations, Throwable>() {
		private static final long serialVersionUID = 4778731836785411552L;

		@Override
		public JavaInformations call() throws Throwable {
			// otherwise static values of the Hudson/Jenkins master are used, but web.xml does not exist
			// on the slaves (pom.xml exists and will not be displayed without dependencies)
			JavaInformations.setWebXmlExistsAndPomXmlExists(false, true);
			return new JavaInformations(null, true);
		}
	};
	private static final Callable<HeapHistogram, Throwable> HEAP_HISTOGRAM_TASK = new Callable<HeapHistogram, Throwable>() {
		private static final long serialVersionUID = -3978979765596110525L;

		@Override
		public HeapHistogram call() throws Throwable {
			if (VirtualMachine.isSupported()) {
				return VirtualMachine.createHeapHistogram();
			}
			return null;
		}
	};
	private static final Callable<List<ProcessInformations>, Throwable> PROCESS_INFORMATIONS_TASK = new Callable<List<ProcessInformations>, Throwable>() {
		private static final long serialVersionUID = -4653173833541398792L;

		@Override
		public List<ProcessInformations> call() throws Throwable {
			return ProcessInformations.buildProcessInformations();
		}
	};
	private static final Callable<List<MBeanNode>, Throwable> MBEANS_TASK = new Callable<List<MBeanNode>, Throwable>() {
		private static final long serialVersionUID = 7010512609895185019L;

		@Override
		public List<MBeanNode> call() throws Throwable {
			return MBeans.getAllMBeanNodes();
		}
	};

	private static final class ActionTask implements Callable<String, Throwable> {
		private static final long serialVersionUID = -3978979765596110525L;
		private final String actionName;
		private final String sessionId;
		private final String threadId;
		private final String jobId;
		private final String cacheId;

		ActionTask(String actionName, String sessionId, String threadId, String jobId,
				String cacheId) {
			super();
			this.actionName = actionName;
			this.sessionId = sessionId;
			this.threadId = threadId;
			this.jobId = jobId;
			this.cacheId = cacheId;
		}

		@Override
		public String call() throws Throwable {
			final Action action = Action.valueOfIgnoreCase(actionName);
			return action.execute(null, null, null, sessionId, threadId, jobId, cacheId);
		}
	}

	private static class JmxValueTask implements Callable<String, Throwable> {
		private static final long serialVersionUID = -4654080667819214726L;
		private final String jmxValueParameter;

		JmxValueTask(String jmxValueParameter) {
			super();
			this.jmxValueParameter = jmxValueParameter;
		}

		@Override
		public String call() throws Throwable {
			return MBeans.getConvertedAttributes(jmxValueParameter);
		}
	}

	private static class DelegatingTask<T> implements Callable<T, Throwable> {
		private static final long serialVersionUID = -8596757920851396797L;
		private final Callable<T, Throwable> delegate;
		private final Locale locale;

		DelegatingTask(Callable<T, Throwable> delegate) {
			super();
			this.delegate = delegate;
			this.locale = I18N.getCurrentLocale();
		}

		@Override
		public T call() throws Throwable {
			I18N.bindLocale(locale);
			try {
				return delegate.call();
			} finally {
				I18N.unbindLocale();
			}
		}
	}

	private final String nodeName;

	RemoteCallHelper(String nodeName) {
		super();
		this.nodeName = nodeName;
	}

	private <T> Map<String, T> collectDataByNodeName(Callable<T, Throwable> task)
			throws IOException, InterruptedException, ExecutionException {
		final Computer[] computers = Hudson.getInstance().getComputers();
		final Map<String, Future<T>> futuresByNodeName = new LinkedHashMap<String, Future<T>>(
				computers.length);
		final DelegatingTask<T> delegatingTask = new DelegatingTask<T>(task);
		for (final Computer c : computers) {
			if (c.isOnline()) {
				if (nodeName == null || nodeName.equals(c.getName())) {
					futuresByNodeName.put(c.getName(), c.getChannel().callAsync(delegatingTask));
				}
			}
		}
		final long now = System.currentTimeMillis();
		// timeout dans 59 secondes
		final long end = now + TimeUnit.SECONDS.toMillis(59);

		final Map<String, T> result = new LinkedHashMap<String, T>(futuresByNodeName.size());
		for (final Map.Entry<String, Future<T>> entry : futuresByNodeName.entrySet()) {
			final String node = entry.getKey();
			final Future<T> future = entry.getValue();
			final long timeout = Math.max(0, end - System.currentTimeMillis());
			try {
				result.put(node, future.get(timeout, TimeUnit.MILLISECONDS));
			} catch (final TimeoutException e) {
				continue;
			}
		}
		return result;
	}

	Map<String, JavaInformations> collectJavaInformationsListByName() throws IOException,
			InterruptedException, ExecutionException {
		return collectDataByNodeName(JAVA_INFORMATIONS_TASK);
	}

	List<String> collectJmxValues(String jmxValueParameter) throws IOException,
			InterruptedException, ExecutionException {
		return new ArrayList<String>(collectDataByNodeName(new JmxValueTask(jmxValueParameter))
				.values());
	}

	Map<String, List<MBeanNode>> collectMBeanNodesByNodeName() throws IOException,
			InterruptedException, ExecutionException {
		return collectDataByNodeName(MBEANS_TASK);
	}

	Map<String, List<ProcessInformations>> collectProcessInformationsByNodeName()
			throws IOException, InterruptedException, ExecutionException {
		return collectDataByNodeName(PROCESS_INFORMATIONS_TASK);
	}

	HeapHistogram collectGlobalHeapHistogram() throws IOException, InterruptedException,
			ExecutionException {
		final Map<String, HeapHistogram> heapHistograms = collectDataByNodeName(HEAP_HISTOGRAM_TASK);
		HeapHistogram heapHistoTotal = null;
		for (final HeapHistogram heapHisto : heapHistograms.values()) {
			if (heapHistoTotal == null) {
				heapHistoTotal = heapHisto;
			} else if (heapHisto != null) {
				heapHistoTotal.add(heapHisto);
			}
		}
		if (heapHistoTotal == null) {
			throw new IllegalStateException(I18N.getString("heap_histo_non_supporte"));
		}
		return heapHistoTotal;
	}

	String forwardAction(String actionName, String sessionId, String threadId, String jobId,
			String cacheId) throws IOException, InterruptedException, ExecutionException {
		final ActionTask task = new ActionTask(actionName, sessionId, threadId, jobId, cacheId);
		final Map<String, String> messagesByNodeName = collectDataByNodeName(task);
		final StringBuilder sb = new StringBuilder();
		for (final String messageForReport : messagesByNodeName.values()) {
			if (messageForReport != null) {
				sb.append(messageForReport).append('\n');
			}
		}
		final String messageForReport;
		if (sb.length() == 0) {
			messageForReport = null;
		} else {
			messageForReport = sb.toString();
		}
		return messageForReport;
	}
}
