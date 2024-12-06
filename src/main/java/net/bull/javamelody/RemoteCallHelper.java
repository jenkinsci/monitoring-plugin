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

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import hudson.model.Computer;
import hudson.remoting.Callable;
import hudson.remoting.Future;
import jenkins.model.Jenkins;
import jenkins.security.MasterToSlaveCallable;
import net.bull.javamelody.internal.common.I18N;
import net.bull.javamelody.internal.model.Action;
import net.bull.javamelody.internal.model.HeapHistogram;
import net.bull.javamelody.internal.model.JavaInformations;
import net.bull.javamelody.internal.model.MBeanNode;
import net.bull.javamelody.internal.model.MBeans;
import net.bull.javamelody.internal.model.ProcessInformations;
import net.bull.javamelody.internal.model.VirtualMachine;

final class RemoteCallHelper {
	private static final MasterToSlaveCallable<JavaInformations, Throwable> JAVA_INFORMATIONS_TASK = new JavaInformationsTask();
	private static final MasterToSlaveCallable<HeapHistogram, Throwable> HEAP_HISTOGRAM_TASK = new HeapHistogramTask();
	private static final MasterToSlaveCallable<List<ProcessInformations>, Throwable> PROCESS_INFORMATIONS_TASK = new ProcessInformationsTask();
	private static final MasterToSlaveCallable<List<MBeanNode>, Throwable> MBEANS_TASK = new MBeansTask();

	private static final class MBeansTask
			extends MasterToSlaveCallable<List<MBeanNode>, Throwable> {
		private static final long serialVersionUID = 7010512609895185019L;

		MBeansTask() {
			super();
		}

		@Override
		public List<MBeanNode> call() throws Throwable {
			return MBeans.getAllMBeanNodes();
		}
	}

	private static final class ProcessInformationsTask
			extends MasterToSlaveCallable<List<ProcessInformations>, Throwable> {
		private static final long serialVersionUID = -4653173833541398792L;

		ProcessInformationsTask() {
			super();
		}

		@Override
		public List<ProcessInformations> call() throws Throwable {
			return ProcessInformations.buildProcessInformations();
		}
	}

	private static final class HeapHistogramTask
			extends MasterToSlaveCallable<HeapHistogram, Throwable> {
		private static final long serialVersionUID = -3978979765596110525L;

		HeapHistogramTask() {
			super();
		}

		@Override
		public HeapHistogram call() throws Throwable {
			if (VirtualMachine.isSupported()) {
				return VirtualMachine.createHeapHistogram();
			}
			return null;
		}
	}

	private static final class JavaInformationsTask
			extends MasterToSlaveCallable<JavaInformations, Throwable> {
		private static final long serialVersionUID = 4778731836785411552L;

		JavaInformationsTask() {
			super();
		}

		@Override
		public JavaInformations call() throws Throwable {
			// otherwise static values of the Hudson/Jenkins master are used, but web.xml does not exist
			// on the slaves (pom.xml exists and will not be displayed without dependencies)
			JavaInformations.setWebXmlExistsAndPomXmlExists(false, true);
			Parameter.NO_DATABASE.setValue("true");
			return new JavaInformations(null, true);
		}
	}

	private static final class ActionTask extends MasterToSlaveCallable<String, Throwable> {
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

	private static class JmxValueTask extends MasterToSlaveCallable<String, Throwable> {
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

	private static class DelegatingTask<T> extends MasterToSlaveCallable<T, Throwable> {
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
			throws IOException {
		final Jenkins jenkins = Jenkins.get();
		final Computer[] computers = jenkins.getComputers();
		final Map<String, Future<T>> futuresByNodeName = new LinkedHashMap<>(computers.length);
		final DelegatingTask<T> delegatingTask = new DelegatingTask<>(task);
		for (final Computer c : computers) {
			if (c.isOnline() && (nodeName == null || nodeName.equals(c.getName()))) {
				futuresByNodeName.put(c.getName(), c.getChannel().callAsync(delegatingTask));
			}
		}
		final long now = System.currentTimeMillis();
		// timeout dans 59 secondes
		final long end = now + TimeUnit.SECONDS.toMillis(59);

		final Map<String, T> result = new LinkedHashMap<>(futuresByNodeName.size());
		for (final Map.Entry<String, Future<T>> entry : futuresByNodeName.entrySet()) {
			final String node = entry.getKey();
			final Future<T> future = entry.getValue();
			final long timeout = Math.max(0, end - System.currentTimeMillis());
			try {
				result.put(node, future.get(timeout, TimeUnit.MILLISECONDS));
			} catch (final TimeoutException e) {
				continue;
			} catch (final Throwable e) {
				// JENKINS-45963 (FreeBSD): if collect fails for one node, continue with others
				continue;
			}
		}
		return result;
	}

	Map<String, JavaInformations> collectJavaInformationsListByName() throws IOException {
		return collectDataByNodeName(JAVA_INFORMATIONS_TASK);
	}

	List<String> collectJmxValues(String jmxValueParameter) throws IOException {
		return new ArrayList<>(collectDataByNodeName(new JmxValueTask(jmxValueParameter)).values());
	}

	Map<String, List<MBeanNode>> collectMBeanNodesByNodeName() throws IOException {
		return collectDataByNodeName(MBEANS_TASK);
	}

	Map<String, List<ProcessInformations>> collectProcessInformationsByNodeName()
			throws IOException {
		return collectDataByNodeName(PROCESS_INFORMATIONS_TASK);
	}

	HeapHistogram collectGlobalHeapHistogram() throws IOException {
		final Map<String, HeapHistogram> heapHistograms = collectDataByNodeName(
				HEAP_HISTOGRAM_TASK);
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
			String cacheId) throws IOException {
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
