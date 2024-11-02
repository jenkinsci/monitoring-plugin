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
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import net.bull.javamelody.internal.common.HttpParameter;
import net.bull.javamelody.internal.common.HttpPart;
import net.bull.javamelody.internal.common.I18N;
import net.bull.javamelody.internal.model.Action;
import net.bull.javamelody.internal.model.Collector;
import net.bull.javamelody.internal.model.HeapHistogram;
import net.bull.javamelody.internal.model.JavaInformations;
import net.bull.javamelody.internal.model.MBeanNode;
import net.bull.javamelody.internal.model.Period;
import net.bull.javamelody.internal.model.ProcessInformations;
import net.bull.javamelody.internal.model.Range;
import net.bull.javamelody.internal.model.ThreadInformations;
import net.bull.javamelody.internal.model.TransportFormat;
import net.bull.javamelody.internal.web.HtmlController;
import net.bull.javamelody.internal.web.HttpCookieManager;
import net.bull.javamelody.internal.web.MonitoringController;
import net.bull.javamelody.internal.web.SerializableController;
import net.bull.javamelody.internal.web.html.HtmlReport;
import net.bull.javamelody.internal.web.pdf.PdfOtherReport;

/**
 * Controller between data and presentation for Hudson/Jenkins' nodes (slaves in
 * general)
 * @author Emeric Vernat
 */
public class NodesController {
	/**
	 * For HudsonMonitoringFilter.
	 */
	public static final String SESSION_REMOTE_USER = SessionListener.SESSION_REMOTE_USER;

	private final Collector collector;
	private final String nodeName;
	private final List<JavaInformations> lastJavaInformationsList;
	private final HttpCookieManager httpCookieManager = new HttpCookieManager();

	/**
	 * Constructor.
	 * @param nodesCollector NodesCollector
	 * @param nodeName Nom du node
	 */
	public NodesController(NodesCollector nodesCollector, String nodeName) {
		super();
		this.collector = nodesCollector.getCollector();
		this.nodeName = nodeName;
		if (nodeName == null) {
			this.lastJavaInformationsList = new ArrayList<>(
					nodesCollector.getLastJavaInformationsList().values());
		} else {
			this.lastJavaInformationsList = Collections
					.singletonList(nodesCollector.getLastJavaInformationsList().get(nodeName));
		}
	}

	/**
	 * Generate a report
	 * @param req Http request
	 * @param resp Http response
	 * @throws IOException e
	 */
	public void doMonitoring(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		if (lastJavaInformationsList != null && !lastJavaInformationsList.isEmpty()) {
			try {
				// preferred language of the browser, getLocale can't be null
				I18N.bindLocale(req.getLocale());

				final MonitoringController monitoringController = new MonitoringController(
						collector, null);
				final String partParameter = HttpParameter.PART.getParameterFrom(req);
				final String actionParameter = HttpParameter.ACTION.getParameterFrom(req);
				if (actionParameter != null) {
					final Action action = Action.valueOfIgnoreCase(actionParameter);
					final String messageForReport;
					if (action != Action.CLEAR_COUNTER && action != Action.PURGE_OBSOLETE_FILES
							&& action != Action.LOGOUT) {
						// on forwarde l'action (gc ou heap dump) sur le(s) node(s)
						// et on recupere les informations a jour (notamment memoire)
						final String actionName = HttpParameter.ACTION.getParameterFrom(req);
						final String sessionId = HttpParameter.SESSION_ID.getParameterFrom(req);
						final String threadId = HttpParameter.THREAD_ID.getParameterFrom(req);
						final String jobId = HttpParameter.JOB_ID.getParameterFrom(req);
						final String cacheId = HttpParameter.CACHE_ID.getParameterFrom(req);
						messageForReport = getRemoteCallHelper().forwardAction(actionName,
								sessionId, threadId, jobId, cacheId);
					} else {
						// necessaire si action clear_counter
						messageForReport = monitoringController.executeActionIfNeeded(req);
					}
					if (TransportFormat
							.isATransportFormat(HttpParameter.FORMAT.getParameterFrom(req))) {
						final SerializableController serializableController = new SerializableController(
								collector);
						final Range range = serializableController.getRangeForSerializable(req);
						final List<JavaInformations> javaInformationsList = new ArrayList<>(
								getRemoteCallHelper().collectJavaInformationsListByName().values());
						final Serializable serializable = serializableController
								.createDefaultSerializable(javaInformationsList, range,
										messageForReport);
						monitoringController.doCompressedSerializable(req, resp, serializable);
					} else {
						writeMessage(resp, messageForReport, partParameter);
					}
					return;
				}

				final String formatParameter = HttpParameter.FORMAT.getParameterFrom(req);
				if (HttpParameter.JMX_VALUE.getParameterFrom(req) != null) {
					final List<String> jmxValues = getRemoteCallHelper()
							.collectJmxValues(HttpParameter.JMX_VALUE.getParameterFrom(req));
					doJmxValue(resp, jmxValues);
				} else if (TransportFormat
						.isATransportFormat(HttpParameter.FORMAT.getParameterFrom(req))) {
					doCompressedSerializable(req, resp, monitoringController);
				} else if ("pdf".equalsIgnoreCase(formatParameter)) {
					doPdf(req, resp, monitoringController);
				} else if (partParameter == null) {
					monitoringController.doReport(req, resp, lastJavaInformationsList);
				} else {
					doPart(req, resp, monitoringController);
				}
			} catch (final Throwable e) { // NOPMD
				writeMessage(resp, e.getMessage(), null);
			} finally {
				I18N.unbindLocale();
			}
		} else {
			MonitoringController.noCache(resp);
			final PrintWriter writer = resp.getWriter();
			writer.write("<html><body>");
			writer.write("<a href='javascript:history.back()'>Back</a>");
			writer.write("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
			writer.write("<a href='?'>Update</a>");
			writer.write("<br/><br/>No slaves online, try again in a minute.");
			writer.write("</body></html>");
			writer.close();
		}
	}

	private void writeMessage(HttpServletResponse resp, String message, String partToRedirectTo)
			throws IOException {
		MonitoringController.noCache(resp);
		final PrintWriter writer = createWriterFromOutputStream(resp);
		// la periode n'a pas d'importance pour writeMessageIfNotNull
		new HtmlReport(collector, null, lastJavaInformationsList, Period.TOUT, writer)
				.writeMessageIfNotNull(message, partToRedirectTo);
		writer.close();
	}

	private void doPdf(HttpServletRequest req, HttpServletResponse resp,
			MonitoringController monitoringController) throws IOException, ServletException {
		if (HttpPart.PROCESSES.isPart(req)) {
			monitoringController.addPdfContentTypeAndDisposition(req, resp);
			final Map<String, List<ProcessInformations>> processInformationsByNodeName = getRemoteCallHelper()
					.collectProcessInformationsByNodeName();
			try {
				doPdfProcesses(resp, processInformationsByNodeName);
			} finally {
				resp.getOutputStream().flush();
			}
		} else if (HttpPart.MBEANS.isPart(req)) {
			monitoringController.addPdfContentTypeAndDisposition(req, resp);
			final Map<String, List<MBeanNode>> mbeanNodesByNodeName = getRemoteCallHelper()
					.collectMBeanNodesByNodeName();
			try {
				doPdfMBeans(resp, mbeanNodesByNodeName);
			} finally {
				resp.getOutputStream().flush();
			}
		} else {
			monitoringController.doReport(req, resp, lastJavaInformationsList);
		}
	}

	private void doPdfProcesses(HttpServletResponse resp,
			Map<String, List<ProcessInformations>> processInformationsByNodeName)
			throws IOException {
		final String title = I18N.getString("Processus");
		final Map<String, List<ProcessInformations>> processInformationsByTitle = convertMapByNodeToMapByTitle(
				processInformationsByNodeName, title);
		new PdfOtherReport(collector.getApplication(), resp.getOutputStream())
				.writeProcessInformations(processInformationsByTitle);
	}

	private void doPdfMBeans(HttpServletResponse resp,
			Map<String, List<MBeanNode>> mbeanNodesByNodeName) throws IOException {
		final String title = I18N.getString("MBeans");
		final Map<String, List<MBeanNode>> mbeanNodesByTitle = convertMapByNodeToMapByTitle(
				mbeanNodesByNodeName, title);
		new PdfOtherReport(collector.getApplication(), resp.getOutputStream())
				.writeMBeans(mbeanNodesByTitle);
	}

	private void doJmxValue(HttpServletResponse resp, List<String> jmxValues) throws IOException {
		MonitoringController.noCache(resp);
		resp.setContentType("text/plain");
		final PrintWriter writer = resp.getWriter();
		boolean first = true;
		for (final String jmxValue : jmxValues) {
			if (first) {
				first = false;
			} else {
				writer.write('|');
				writer.write('|');
			}
			writer.write(jmxValue);
		}
		writer.close();
	}

	private void doPart(HttpServletRequest req, HttpServletResponse resp,
			MonitoringController monitoringController) throws IOException, ServletException {
		// ici, ni web.xml ni pom.xml
		if (HttpPart.MBEANS.isPart(req)) {
			final Map<String, List<MBeanNode>> mbeanNodesByNodeName = getRemoteCallHelper()
					.collectMBeanNodesByNodeName();
			doMBeans(req, resp, mbeanNodesByNodeName);
		} else if (HttpPart.PROCESSES.isPart(req)) {
			final Map<String, List<ProcessInformations>> processInformationsByNodeName = getRemoteCallHelper()
					.collectProcessInformationsByNodeName();
			doProcesses(req, resp, processInformationsByNodeName);
		} else if (HttpPart.HEAP_HISTO.isPart(req)) {
			final HeapHistogram heapHistoTotal = getRemoteCallHelper().collectGlobalHeapHistogram();
			doHeapHisto(req, resp, heapHistoTotal, monitoringController);
		} else {
			monitoringController.doReport(req, resp, lastJavaInformationsList);
		}
	}

	private void doProcesses(HttpServletRequest req, HttpServletResponse resp,
			Map<String, List<ProcessInformations>> processListByNodeName) throws IOException {
		final PrintWriter writer = createWriterFromOutputStream(resp);
		final HtmlReport htmlReport = createHtmlReport(req, resp, writer);
		final String title = I18N.getString("Processus");
		final Map<String, List<ProcessInformations>> processListByTitle = convertMapByNodeToMapByTitle(
				processListByNodeName, title);
		htmlReport.writeProcesses(processListByTitle);
		writer.close();
	}

	private void doMBeans(HttpServletRequest req, HttpServletResponse resp,
			Map<String, List<MBeanNode>> mbeanNodesByNodeName) throws IOException {
		final PrintWriter writer = createWriterFromOutputStream(resp);
		final HtmlReport htmlReport = createHtmlReport(req, resp, writer);
		final String title = I18N.getString("MBeans");
		final Map<String, List<MBeanNode>> mbeanNodesByTitle = convertMapByNodeToMapByTitle(
				mbeanNodesByNodeName, title);
		htmlReport.writeMBeans(mbeanNodesByTitle);
		writer.close();
	}

	private <T> Map<String, T> convertMapByNodeToMapByTitle(Map<String, T> mapByNodeName,
			final String title) {
		final Map<String, T> mapByTitle = new LinkedHashMap<>(mapByNodeName.size());
		for (final Map.Entry<String, T> entry : mapByNodeName.entrySet()) {
			final String name = entry.getKey();
			if (name != null && name.length() != 0) {
				mapByTitle.put(title + " (" + entry.getKey() + ")", entry.getValue());
			} else {
				mapByTitle.put(title, entry.getValue());
			}
		}
		return mapByTitle;
	}

	private void doHeapHisto(HttpServletRequest req, HttpServletResponse resp,
			HeapHistogram heapHistogram, MonitoringController monitoringController)
			throws IOException {
		if ("pdf".equalsIgnoreCase(HttpParameter.FORMAT.getParameterFrom(req))) {
			monitoringController.addPdfContentTypeAndDisposition(req, resp);
			try {
				final PdfOtherReport pdfOtherReport = new PdfOtherReport(collector.getApplication(),
						resp.getOutputStream());
				pdfOtherReport.writeHeapHistogram(heapHistogram);
			} finally {
				resp.getOutputStream().flush();
			}
		} else {
			final PrintWriter writer = createWriterFromOutputStream(resp);
			final HtmlReport htmlReport = createHtmlReport(req, resp, writer);
			htmlReport.writeHtmlHeader();
			htmlReport.writeHeapHistogram(heapHistogram, null, HttpPart.HEAP_HISTO.getName());
			htmlReport.writeHtmlFooter();
			writer.close();
		}
	}

	private void doCompressedSerializable(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, MonitoringController monitoringController)
			throws IOException {
		Serializable serializable;
		try {
			serializable = createSerializable(httpRequest);
		} catch (final Exception e) {
			serializable = e;
		}
		monitoringController.doCompressedSerializable(httpRequest, httpResponse, serializable);
	}

	private Serializable createSerializable(HttpServletRequest httpRequest) throws Exception { // NOPMD
		if (HttpPart.MBEANS.isPart(httpRequest)) {
			return new LinkedHashMap<>(getRemoteCallHelper().collectMBeanNodesByNodeName());
		} else if (HttpPart.PROCESSES.isPart(httpRequest)) {
			return new LinkedHashMap<>(
					getRemoteCallHelper().collectProcessInformationsByNodeName());
		} else if (HttpPart.HEAP_HISTO.isPart(httpRequest)) {
			return getRemoteCallHelper().collectGlobalHeapHistogram();
		} else if (HttpPart.JVM.isPart(httpRequest)) {
			return new ArrayList<>(lastJavaInformationsList);
		} else if (HttpPart.THREADS.isPart(httpRequest)) {
			final ArrayList<List<ThreadInformations>> result = new ArrayList<>();
			for (final JavaInformations javaInformations : lastJavaInformationsList) {
				result.add(new ArrayList<>(javaInformations.getThreadInformationsList()));
			}
			return result;
		}

		// utile pour JROBINS_PART, OTHER_JROBINS_PART, SESSIONS_PART et
		// defaultSerializable notamment
		final SerializableController serializableController = new SerializableController(collector);
		return serializableController.createSerializable(httpRequest, lastJavaInformationsList,
				null);
	}

	private HtmlReport createHtmlReport(HttpServletRequest req, HttpServletResponse resp,
			PrintWriter writer) {
		final Range range = httpCookieManager.getRange(req, resp);
		return new HtmlReport(collector, null, lastJavaInformationsList, range, writer);
	}

	private static PrintWriter createWriterFromOutputStream(HttpServletResponse httpResponse)
			throws IOException {
		MonitoringController.noCache(httpResponse);
		httpResponse.setContentType("text/html; charset=UTF-8");
		return new PrintWriter(HtmlController.getWriter(httpResponse));
	}

	private RemoteCallHelper getRemoteCallHelper() {
		return new RemoteCallHelper(nodeName);
	}

	/**
	 * Is it necessary to collect java informations for this monitoring request?
	 * @param httpRequest HttpServletRequest
	 * @return boolean
	 */
	public static boolean isJavaInformationsNeeded(HttpServletRequest httpRequest) {
		return MonitoringController.isJavaInformationsNeeded(httpRequest);
	}
}
