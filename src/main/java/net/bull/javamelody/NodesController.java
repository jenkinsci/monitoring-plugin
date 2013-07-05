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

import static net.bull.javamelody.HttpParameters.ACTION_PARAMETER;
import static net.bull.javamelody.HttpParameters.CACHE_ID_PARAMETER;
import static net.bull.javamelody.HttpParameters.FORMAT_PARAMETER;
import static net.bull.javamelody.HttpParameters.HEAP_HISTO_PART;
import static net.bull.javamelody.HttpParameters.HTML_CONTENT_TYPE;
import static net.bull.javamelody.HttpParameters.JMX_VALUE;
import static net.bull.javamelody.HttpParameters.JOB_ID_PARAMETER;
import static net.bull.javamelody.HttpParameters.MBEANS_PART;
import static net.bull.javamelody.HttpParameters.PART_PARAMETER;
import static net.bull.javamelody.HttpParameters.PROCESSES_PART;
import static net.bull.javamelody.HttpParameters.SESSION_ID_PARAMETER;
import static net.bull.javamelody.HttpParameters.THREADS_PART;
import static net.bull.javamelody.HttpParameters.THREAD_ID_PARAMETER;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controller between data and presentation for Hudson/Jenkins' nodes (slaves in
 * general)
 * 
 * @author Emeric Vernat
 */
public class NodesController {
	private final Collector collector;
	private final List<JavaInformations> lastJavaInformationsList;
	private final HttpCookieManager httpCookieManager = new HttpCookieManager();

	/**
	 * Constructor.
	 * @param nodesCollector NodesCollector
	 */
	public NodesController(NodesCollector nodesCollector) {
		super();
		this.collector = nodesCollector.getCollector();
		this.lastJavaInformationsList = nodesCollector.getLastJavaInformationsList();
	}

	/**
	 * Generate a report
	 * 
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
				final String partParameter = req.getParameter(PART_PARAMETER);
				final String actionParameter = req.getParameter(ACTION_PARAMETER);
				if (actionParameter != null) {
					final Action action = Action.valueOfIgnoreCase(actionParameter);
					final String messageForReport;
					if (action != Action.CLEAR_COUNTER && action != Action.PURGE_OBSOLETE_FILES) {
						// on forwarde l'action (gc ou heap dump) sur l'application monitoree
						// et on recupere les informations a jour (notamment memoire)
						final String actionName = req.getParameter(ACTION_PARAMETER);
						final String sessionId = req.getParameter(SESSION_ID_PARAMETER);
						final String threadId = req.getParameter(THREAD_ID_PARAMETER);
						final String jobId = req.getParameter(JOB_ID_PARAMETER);
						final String cacheId = req.getParameter(CACHE_ID_PARAMETER);
						messageForReport = RemoteCallHelper.forwardAction(actionName, sessionId,
								threadId, jobId, cacheId);
					} else {
						// necessaire si action clear_counter
						messageForReport = monitoringController.executeActionIfNeeded(req);
					}
					if (TransportFormat.isATransportFormat(req.getParameter(FORMAT_PARAMETER))) {
						final SerializableController serializableController = new SerializableController(
								collector);
						final Range range = serializableController.getRangeForSerializable(req);
						final List<JavaInformations> javaInformationsList = RemoteCallHelper
								.collectJavaInformationsList();
						final Serializable serializable = serializableController
								.createDefaultSerializable(javaInformationsList, range,
										messageForReport);
						monitoringController.doCompressedSerializable(req, resp, serializable);
					} else {
						writeMessage(resp, messageForReport, partParameter);
					}
					return;
				}

				final String formatParameter = req.getParameter(FORMAT_PARAMETER);
				if (req.getParameter(JMX_VALUE) != null) {
					final List<String> jmxValues = RemoteCallHelper.collectJmxValues(req
							.getParameter(JMX_VALUE));
					doJmxValue(resp, jmxValues);
				} else if (TransportFormat.isATransportFormat(req.getParameter(FORMAT_PARAMETER))) {
					doCompressedSerializable(req, resp, monitoringController);
				} else if ("pdf".equalsIgnoreCase(formatParameter)) {
					doPdf(req, resp, monitoringController);
				} else if (partParameter == null) {
					monitoringController.doReport(req, resp, lastJavaInformationsList);
				} else {
					doPart(req, resp, monitoringController, partParameter);
				}
			} catch (final Exception e) {
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
			MonitoringController monitoringController) throws IOException, InterruptedException,
			ExecutionException {
		if (PROCESSES_PART.equalsIgnoreCase(req.getParameter(PART_PARAMETER))) {
			monitoringController.addPdfContentTypeAndDisposition(req, resp);
			final Map<String, List<ProcessInformations>> processInformationsByNodeName = RemoteCallHelper
					.collectProcessInformationsByNodeName();
			try {
				doPdfProcesses(resp, processInformationsByNodeName);
			} finally {
				resp.getOutputStream().flush();
			}
		} else if (MBEANS_PART.equalsIgnoreCase(req.getParameter(PART_PARAMETER))) {
			monitoringController.addPdfContentTypeAndDisposition(req, resp);
			final Map<String, List<MBeanNode>> mbeanNodesByNodeName = RemoteCallHelper
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
			MonitoringController monitoringController, String partParameter) throws IOException,
			InterruptedException, ExecutionException {
		// ici, ni web.xml ni pom.xml
		if (MBEANS_PART.equalsIgnoreCase(partParameter)) {
			final Map<String, List<MBeanNode>> mbeanNodesByNodeName = RemoteCallHelper
					.collectMBeanNodesByNodeName();
			doMBeans(req, resp, mbeanNodesByNodeName);
		} else if (PROCESSES_PART.equalsIgnoreCase(partParameter)) {
			final Map<String, List<ProcessInformations>> processInformationsByNodeName = RemoteCallHelper
					.collectProcessInformationsByNodeName();
			doProcesses(req, resp, processInformationsByNodeName);
		} else if (HEAP_HISTO_PART.equalsIgnoreCase(partParameter)) {
			final HeapHistogram heapHistoTotal = RemoteCallHelper.collectGlobalHeapHistogram();
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
		final Map<String, T> mapByTitle = new LinkedHashMap<String, T>(mapByNodeName.size());
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
		if ("pdf".equalsIgnoreCase(req.getParameter(FORMAT_PARAMETER))) {
			monitoringController.addPdfContentTypeAndDisposition(req, resp);
			try {
				final PdfOtherReport pdfOtherReport = new PdfOtherReport(
						collector.getApplication(), resp.getOutputStream());
				pdfOtherReport.writeHeapHistogram(heapHistogram);
			} finally {
				resp.getOutputStream().flush();
			}
		} else {
			final PrintWriter writer = createWriterFromOutputStream(resp);
			final HtmlReport htmlReport = createHtmlReport(req, resp, writer);
			htmlReport.writeHtmlHeader();
			htmlReport.writeHeapHistogram(heapHistogram, null, HEAP_HISTO_PART);
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
		final String part = httpRequest.getParameter(PART_PARAMETER);
		if (MBEANS_PART.equalsIgnoreCase(part)) {
			return new LinkedHashMap<String, List<MBeanNode>>(
					RemoteCallHelper.collectMBeanNodesByNodeName());
		} else if (PROCESSES_PART.equalsIgnoreCase(part)) {
			return new LinkedHashMap<String, List<ProcessInformations>>(
					RemoteCallHelper.collectProcessInformationsByNodeName());
		} else if (HEAP_HISTO_PART.equalsIgnoreCase(part)) {
			return RemoteCallHelper.collectGlobalHeapHistogram();
		} else if (THREADS_PART.equalsIgnoreCase(part)) {
			final ArrayList<List<ThreadInformations>> result = new ArrayList<List<ThreadInformations>>();
			for (final JavaInformations javaInformations : lastJavaInformationsList) {
				result.add(new ArrayList<ThreadInformations>(javaInformations
						.getThreadInformationsList()));
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
		httpResponse.setContentType(HTML_CONTENT_TYPE);
		return new PrintWriter(MonitoringController.getWriter(httpResponse));
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
