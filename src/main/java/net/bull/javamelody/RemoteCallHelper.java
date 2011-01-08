/*
 * Copyright 2008-2011 by Emeric Vernat
 */
package net.bull.javamelody;

import hudson.model.Computer;
import hudson.model.Hudson;
import hudson.remoting.Callable;
import hudson.remoting.Future;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

final class RemoteCallHelper {
	private static final Callable<JavaInformations, Throwable> JAVA_INFORMATIONS_TASK = new Callable<JavaInformations, Throwable>() {
		private static final long serialVersionUID = 4778731836785411552L;

		@Override
		public JavaInformations call() throws Throwable {
			// otherwise static values of the hudson master are used, but web.xml does not exist
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
	private static final Callable<String, Throwable> MBEANS_HTML_TASK = new Callable<String, Throwable>() {
		private static final long serialVersionUID = 7010512609895185019L;

		@Override
		public String call() throws Throwable {
			final StringWriter writer = new StringWriter();
			new HtmlMBeansReport(writer).writeTree();
			return writer.toString();
		}
	};

	private static final class ActionTask implements Callable<String, Throwable> {
		private static final long serialVersionUID = -3978979765596110525L;
		private final String actionName;
		private final String sessionId;
		private final String threadId;
		private final String jobId;

		ActionTask(String actionName, String sessionId, String threadId, String jobId) {
			super();
			this.actionName = actionName;
			this.sessionId = sessionId;
			this.threadId = threadId;
			this.jobId = jobId;
		}

		@Override
		public String call() throws Throwable {
			final Action action = Action.valueOfIgnoreCase(actionName);
			return action.execute(null, null, sessionId, threadId, jobId);
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

	private RemoteCallHelper() {
		super();
	}

	private static <T> Map<String, T> collectDataByNodeName(Callable<T, Throwable> task)
			throws IOException, InterruptedException, ExecutionException {
		final Computer[] computers = Hudson.getInstance().getComputers();
		final Map<String, Future<T>> futuresByNodeName = new LinkedHashMap<String, Future<T>>(
				computers.length);
		final DelegatingTask<T> delegatingTask = new DelegatingTask<T>(task);
		for (final Computer c : computers) {
			if (c.isOnline()) {
				futuresByNodeName.put(c.getName(), c.getChannel().callAsync(delegatingTask));
			}
		}
		final Map<String, T> result = new LinkedHashMap<String, T>(futuresByNodeName.size());
		for (final Map.Entry<String, Future<T>> entry : futuresByNodeName.entrySet()) {
			result.put(entry.getKey(), entry.getValue().get());
		}
		return result;
	}

	static List<JavaInformations> collectJavaInformationsList() throws IOException,
			InterruptedException, ExecutionException {
		final Map<String, JavaInformations> javaInformationsByNodeName = collectDataByNodeName(JAVA_INFORMATIONS_TASK);
		return new ArrayList<JavaInformations>(javaInformationsByNodeName.values());

	}

	static List<String> collectJmxValues(String jmxValueParameter) throws IOException,
			InterruptedException, ExecutionException {
		return new ArrayList<String>(collectDataByNodeName(new JmxValueTask(jmxValueParameter))
				.values());
	}

	static Map<String, String> collectMBeansHtmlInformations() throws IOException,
			InterruptedException, ExecutionException {
		return collectDataByNodeName(MBEANS_HTML_TASK);
	}

	static Map<String, List<ProcessInformations>> collectProcessInformationsByNodeName()
			throws IOException, InterruptedException, ExecutionException {
		return collectDataByNodeName(PROCESS_INFORMATIONS_TASK);
	}

	static HeapHistogram collectGlobalHeapHistogram() throws IOException, InterruptedException,
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

	static String forwardAction(String actionName, String sessionId, String threadId, String jobId)
			throws IOException, InterruptedException, ExecutionException {
		final ActionTask task = new ActionTask(actionName, sessionId, threadId, jobId);
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
