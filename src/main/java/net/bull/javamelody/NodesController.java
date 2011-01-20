/*
 * Copyright 2008-2011 by Emeric Vernat
 */
package net.bull.javamelody;

import static net.bull.javamelody.HttpParameters.ACTION_PARAMETER;
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
 * Controller between data and presentation for Hudson's nodes (slaves in
 * general)
 * 
 * @author Emeric Vernat
 */
public class NodesController {
	private final Collector collector;
	private final List<JavaInformations> lastJavaInformationsList;
	private final HttpCookieManager httpCookieManager = new HttpCookieManager();

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
					final String messageForReport;
					if (Action.valueOfIgnoreCase(actionParameter) != Action.CLEAR_COUNTER) {
						// on forwarde l'action (gc ou heap dump) sur l'application monitorée
						// et on récupère les informations à jour (notamment mémoire)
						final String actionName = req.getParameter(ACTION_PARAMETER);
						final String sessionId = req.getParameter(SESSION_ID_PARAMETER);
						final String threadId = req.getParameter(THREAD_ID_PARAMETER);
						final String jobId = req.getParameter(JOB_ID_PARAMETER);
						messageForReport = RemoteCallHelper.forwardAction(actionName, sessionId,
								threadId, jobId);
						writeMessage(resp, messageForReport, partParameter);
					} else {
						// nécessaire si action clear_counter
						messageForReport = monitoringController.executeActionIfNeeded(req);
						writeMessage(resp, messageForReport, null);
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
					doCompressedPart(req, resp, monitoringController, partParameter);
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
		// la période n'a pas d'importance pour writeMessageIfNotNull
		new HtmlReport(collector, null, lastJavaInformationsList, Period.TOUT, writer)
				.writeMessageIfNotNull(message, partToRedirectTo);
		writer.close();
	}

	private void doPdf(HttpServletRequest req, HttpServletResponse resp,
			MonitoringController monitoringController) throws IOException, InterruptedException,
			ExecutionException {
		if (PROCESSES_PART.equalsIgnoreCase(req.getParameter(PART_PARAMETER))) {
			monitoringController.addPdfContentTypeAndDisposition(req, resp);
			try {
				doPdfProcesses(resp);
			} finally {
				resp.getOutputStream().flush();
			}
		} else {
			monitoringController.doReport(req, resp, lastJavaInformationsList);
		}
	}

	private void doPdfProcesses(HttpServletResponse resp) throws IOException, InterruptedException,
			ExecutionException {
		final Map<String, List<ProcessInformations>> processInformationsByNodeName = RemoteCallHelper
				.collectProcessInformationsByNodeName();
		final String title = I18N.getString("Processus");
		final Map<String, List<ProcessInformations>> processInformationsByTitle = new LinkedHashMap<String, List<ProcessInformations>>();
		for (final Map.Entry<String, List<ProcessInformations>> entry : processInformationsByNodeName
				.entrySet()) {
			processInformationsByTitle.put(title + " (" + entry.getKey() + ')', entry.getValue());
		}
		new PdfOtherReport(collector.getApplication(), resp.getOutputStream())
				.writeProcessInformations(processInformationsByTitle);
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

	private void doCompressedPart(HttpServletRequest httpRequest, HttpServletResponse httpResponse,
			MonitoringController monitoringController, String partParameter) throws IOException,
			InterruptedException, ExecutionException {
		if (MonitoringController.isCompressionSupported(httpRequest)) {
			// comme la page html peut être volumineuse
			// on compresse le flux de réponse en gzip à partir de 4 Ko
			// (à moins que la compression http ne soit pas supportée
			// comme par ex s'il y a un proxy squid qui ne supporte que http
			// 1.0)
			final CompressionServletResponseWrapper wrappedResponse = new CompressionServletResponseWrapper(
					httpResponse, 4096);
			try {
				doPart(httpRequest, wrappedResponse, monitoringController, partParameter);
			} finally {
				wrappedResponse.finishResponse();
			}
		} else {
			doPart(httpRequest, httpResponse, monitoringController, partParameter);
		}
	}

	private void doPart(HttpServletRequest req, HttpServletResponse resp,
			MonitoringController monitoringController, String partParameter) throws IOException,
			InterruptedException, ExecutionException {
		// ici, ni web.xml ni pom.xml
		if (MBEANS_PART.equalsIgnoreCase(partParameter)) {
			final Map<String, String> mbeansHtmlInformations = RemoteCallHelper
					.collectMBeansHtmlInformations();
			doMBeans(req, resp, mbeansHtmlInformations);
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
		htmlReport.writeHtmlHeader();
		writer.write("<div class='noPrint'>");
		I18N.writelnTo(
				"<a href='javascript:history.back()'><img src='?resource=action_back.png' alt='#Retour#'/> #Retour#</a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;",
				writer);
		writer.write("<a href='?part=");
		writer.write(PROCESSES_PART);
		writer.write("'>");
		I18N.writelnTo(
				"<img src='?resource=action_refresh.png' alt='#Actualiser#'/> #Actualiser#</a>",
				writer);
		writer.write("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		I18N.writelnTo("<a href='?part=processes&amp;format=pdf' title='#afficher_PDF#'>", writer);
		I18N.writelnTo("<img src='?resource=pdf.png' alt='#PDF#'/> #PDF#</a>", writer);
		writer.write("</div>");
		final String title = I18N.getString("Processus");
		for (final Map.Entry<String, List<ProcessInformations>> entry : processListByNodeName
				.entrySet()) {
			final String htmlTitle = "<h3><img width='24' height='24' src='?resource=processes.png' alt='"
					+ title
					+ "'/>&nbsp;"
					+ title
					+ " ("
					+ I18N.htmlEncode(entry.getKey(), false)
					+ ")</h3>";
			writer.write(htmlTitle);
			writer.flush();

			new HtmlProcessInformationsReport(entry.getValue(), writer).writeTable();
		}
		htmlReport.writeHtmlFooter();
		writer.close();
	}

	private void doMBeans(HttpServletRequest req, HttpServletResponse resp,
			Map<String, String> mbeansHtmlInformations) throws IOException {
		final PrintWriter writer = createWriterFromOutputStream(resp);
		final HtmlReport htmlReport = createHtmlReport(req, resp, writer);
		htmlReport.writeHtmlHeader();
		writer.write("<div class='noPrint'>");
		I18N.writelnTo(
				"<a href='javascript:history.back()'><img src='?resource=action_back.png' alt='#Retour#'/> #Retour#</a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;",
				writer);
		writer.write("<a href='?part=");
		writer.write(MBEANS_PART);
		writer.write("'>");
		I18N.writelnTo("<img src='?resource=action_refresh.png' alt='#Actualiser#'/> #Actualiser#",
				writer);
		writer.write("</a></div>");
		final String title = I18N.getString("MBeans");
		for (final Map.Entry<String, String> entry : mbeansHtmlInformations.entrySet()) {
			final String htmlTitle = "<h3><img width='24' height='24' src='?resource=mbeans.png' alt='"
					+ title
					+ "'/>&nbsp;"
					+ title
					+ " ("
					+ I18N.htmlEncode(entry.getKey(), false)
					+ ")</h3>";
			writer.write(htmlTitle);
			writer.write(entry.getValue());
		}
		htmlReport.writeHtmlFooter();
		writer.close();
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
			serializable = createSerializable(httpRequest, monitoringController);
		} catch (final Exception e) {
			serializable = e;
		}
		monitoringController.doCompressedSerializable(httpRequest, httpResponse, serializable);
	}

	private Serializable createSerializable(HttpServletRequest httpRequest,
			MonitoringController monitoringController) throws Exception { // NOPMD
		final String part = httpRequest.getParameter(PART_PARAMETER);
		if (HEAP_HISTO_PART.equalsIgnoreCase(part)) {
			return RemoteCallHelper.collectGlobalHeapHistogram();
		} else if (PROCESSES_PART.equalsIgnoreCase(part)) {
			return new LinkedHashMap<String, List<ProcessInformations>>(
					RemoteCallHelper.collectProcessInformationsByNodeName());
		} else if (THREADS_PART.equalsIgnoreCase(part)) {
			final ArrayList<List<ThreadInformations>> result = new ArrayList<List<ThreadInformations>>();
			for (final JavaInformations javaInformations : lastJavaInformationsList) {
				result.add(new ArrayList<ThreadInformations>(javaInformations
						.getThreadInformationsList()));
			}
			return result;
		}

		return monitoringController.createDefaultSerializable(lastJavaInformationsList);
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

	public static boolean isJavaInformationsNeeded(HttpServletRequest httpRequest) {
		return MonitoringController.isJavaInformationsNeeded(httpRequest);
	}
}
