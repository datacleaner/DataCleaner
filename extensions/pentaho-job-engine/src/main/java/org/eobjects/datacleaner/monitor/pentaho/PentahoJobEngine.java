/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.eobjects.datacleaner.monitor.pentaho;

import java.io.OutputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.SerializationUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.job.ExecutionLogger;
import org.eobjects.datacleaner.monitor.job.JobEngine;
import org.eobjects.datacleaner.monitor.pentaho.jaxb.PentahoJobType;
import org.eobjects.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.eobjects.datacleaner.monitor.server.job.AbstractJobEngine;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.eobjects.datacleaner.util.FileFilters;
import org.eobjects.metamodel.util.Action;
import org.springframework.stereotype.Component;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A {@link JobEngine} for running Pentaho Data Integration (aka. Kettle) jobs
 * in within DataCleaner.
 */
@Component
public class PentahoJobEngine extends AbstractJobEngine<PentahoJobContext> {

    public static final String EXTENSION = ".pentaho.job.xml";

    /**
     * Defines the interval duration when polling for updates from the
     * transformation
     */
    private static final int POLL_INTERVAL_MILLIS = 800;

    /**
     * Defines the duration between adding a progress update in the execution
     * log.
     */
    private static final int PROGRESS_UPDATE_INTERVAL_MILLIS = 10000;

    public PentahoJobEngine() {
        super(EXTENSION);
    }

    @Override
    public String getJobType() {
        return "PentahoJob";
    }

    @Override
    public void executeJob(TenantContext tenantContext, ExecutionLog execution, ExecutionLogger executionLogger,
            Map<String, String> variables) throws Exception {
        final PentahoJobContext jobContext = getJobContext(tenantContext, execution.getJob());
        final PentahoJobType pentahoJobType = jobContext.getPentahoJobType();

        final PentahoCarteClient carteClient = new PentahoCarteClient(pentahoJobType);

        final boolean ready = fillMissingDetails(carteClient, pentahoJobType, executionLogger);
        if (!ready) {
            return;
        }

        final boolean started = startTrans(carteClient, pentahoJobType, executionLogger);

        if (!started) {
            return;
        }

        long lastStatusUpdateTime = System.currentTimeMillis();
        boolean running = true;
        while (running) {
            Thread.sleep(POLL_INTERVAL_MILLIS);

            final boolean progressUpdate;
            if (System.currentTimeMillis() - lastStatusUpdateTime > PROGRESS_UPDATE_INTERVAL_MILLIS) {
                lastStatusUpdateTime = System.currentTimeMillis();
                progressUpdate = true;
            } else {
                progressUpdate = false;
            }

            running = transStatus(carteClient, pentahoJobType, executionLogger, tenantContext, execution,
                    progressUpdate);

            if (!running) {
                break;
            }
        }
    }

    /**
     * Fills in any missing details of the request
     * 
     * @param carteClient
     * @param pentahoJobType
     * @param executionLogger
     * @return true if the job should continue, or false if it has been
     *         aborted/failed.
     * @throws Exception
     */
    private boolean fillMissingDetails(PentahoCarteClient carteClient, PentahoJobType pentahoJobType,
            ExecutionLogger executionLogger) throws Exception {
        final String queriedTransformationId = pentahoJobType.getTransformationId();
        final String queriedTransformationName = pentahoJobType.getTransformationName();
        if (!StringUtils.isNullOrEmpty(queriedTransformationId) && StringUtils.isNullOrEmpty(queriedTransformationName)) {
            // both 'id' and 'name' of transformation is filled already.
            return true;
        }

        final List<PentahoTransformation> availableTransformations = carteClient.getAvailableTransformations();
        for (PentahoTransformation candidate : availableTransformations) {
            if (candidate.matches(queriedTransformationId, queriedTransformationName)) {
                pentahoJobType.setTransformationId(candidate.getId());
                pentahoJobType.setTransformationName(candidate.getName());
                executionLogger.log("Identified transformation: name=" + candidate.getName() + ", id="
                        + candidate.getId());
                return true;
            }
        }

        executionLogger.setStatusFailed(null, null, new PentahoJobException(
                "Carte did not present any transformations with id='" + queriedTransformationId + "' or name='"
                        + queriedTransformationName + "'"));
        return false;
    }

    /**
     * Fires the HTTP request to the Carte server to get the updated status of
     * the execution
     * 
     * @param carteClient
     * @param pentahoJobType
     * @param executionLogger
     * @param execution
     * @param progressUpdate
     * @return
     * @throws Exception
     */
    private boolean transStatus(PentahoCarteClient carteClient, PentahoJobType pentahoJobType,
            ExecutionLogger executionLogger, TenantContext tenantContext, ExecutionLog execution, boolean progressUpdate)
            throws Exception {
        final String transStatusUrl = carteClient.getUrl("transStatus");
        final HttpGet request = new HttpGet(transStatusUrl);
        try {
            final HttpResponse response = carteClient.execute(request);
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                final Document doc = carteClient.parse(response.getEntity());
                final Element webresultElement = doc.getDocumentElement();

                final String statusDescription = DomUtils.getTextValue(DomUtils.getChildElementByTagName(
                        webresultElement, "status_desc"));
                if ("Running".equalsIgnoreCase(statusDescription)) {
                    // the job is still running

                    if (progressUpdate) {
                        logTransStatus("progress", pentahoJobType, executionLogger, doc);
                    }

                    return true;
                } else if ("Waiting".equalsIgnoreCase(statusDescription)) {
                    // the job has finished - serialize and return succesfully

                    logTransStatus("finished", pentahoJobType, executionLogger, doc);

                    final String documentString;
                    {
                        final Transformer transformer = getTransformer();
                        final Source source = new DOMSource(doc);
                        final StringWriter outText = new StringWriter();
                        final StreamResult target = new StreamResult(outText);
                        transformer.transform(source, target);
                        documentString = outText.toString();
                    }

                    final PentahoJobResult result = new PentahoJobResult(documentString);

                    final String resultFilename = execution.getResultId()
                            + FileFilters.ANALYSIS_RESULT_SER.getExtension();
                    tenantContext.getResultFolder().createFile(resultFilename, new Action<OutputStream>() {
                        @Override
                        public void run(OutputStream out) throws Exception {
                            SerializationUtils.serialize(result, out);
                        }
                    });

                    executionLogger.setStatusSuccess(result);
                    return false;
                } else if ("Paused".equalsIgnoreCase(statusDescription)) {
                    executionLogger.setStatusFailed(null, transStatusUrl, new PentahoJobException(
                            "The transformation was paused by a third-party actor"));
                    return false;
                } else {
                    executionLogger.setStatusFailed(null, transStatusUrl, new PentahoJobException(
                            "Encountered unexpected status_desc from Carte when updating transformation status: "
                                    + statusDescription));
                    return false;
                }
            } else {
                String responseString = EntityUtils.toString(response.getEntity());
                executionLogger.log(responseString);
                executionLogger.setStatusFailed(null, transStatusUrl, new PentahoJobException(
                        "Unexpected response status when updating transformation status: " + statusCode));
                return false;
            }
        } finally {
            request.releaseConnection();
        }
    }

    /**
     * Logs the progress of a job in Carte based on the XML response of a
     * 'transUpdate' call.
     * 
     * @param statusType
     *            the type of status update - expecting a word to put into an
     *            update sentence like 'progress' or 'finished'.
     * @param pentahoJobType
     * @param executionLogger
     * @param document
     */
    private void logTransStatus(String statusType, PentahoJobType pentahoJobType, ExecutionLogger executionLogger,
            Document document) {
        final Element transstatusElement = document.getDocumentElement();
        final Element stepstatuslistElement = DomUtils.getChildElementByTagName(transstatusElement, "stepstatuslist");
        final List<Element> stepstatusElements = DomUtils.getChildElements(stepstatuslistElement);
        for (Element stepstatusElement : stepstatusElements) {
            final String stepName = DomUtils.getChildElementValueByTagName(stepstatusElement, "stepname");
            final String linesInput = DomUtils.getChildElementValueByTagName(stepstatusElement, "linesInput");
            final String linesOutput = DomUtils.getChildElementValueByTagName(stepstatusElement, "linesOutput");
            final String linesRead = DomUtils.getChildElementValueByTagName(stepstatusElement, "linesRead");
            final String linesWritten = DomUtils.getChildElementValueByTagName(stepstatusElement, "linesWritten");
            final String statusDescription = DomUtils.getChildElementValueByTagName(stepstatusElement,
                    "statusDescription");

            final StringBuilder update = new StringBuilder();
            update.append("Step '");
            update.append(stepName);
            update.append("' ");
            update.append(statusType);
            update.append(": status='");
            update.append(statusDescription);
            update.append("'");

            if (!"0".equals(linesRead)) {
                update.append(", linesRead=");
                update.append(linesRead);
            }
            if (!"0".equals(linesWritten)) {
                update.append(", linesWritten=");
                update.append(linesWritten);
            }
            if (!"0".equals(linesInput)) {
                update.append(", linesInput=");
                update.append(linesInput);
            }
            if (!"0".equals(linesOutput)) {
                update.append(", linesOutput=");
                update.append(linesOutput);
            }

            executionLogger.log(update.toString());
        }
        executionLogger.flushLog();
    }

    protected Transformer getTransformer() {
        try {
            final TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            return transformer;
        } catch (TransformerConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Fires the HTTP request to Carte to start processing the transformation.
     * 
     * @param carteClient
     * @param pentahoJobType
     * @param executionLogger
     * @return
     * @throws Exception
     */
    private boolean startTrans(PentahoCarteClient carteClient, PentahoJobType pentahoJobType,
            ExecutionLogger executionLogger) throws Exception {
        final String startTransUrl = carteClient.getUrl("startTrans");
        final HttpGet request = new HttpGet(startTransUrl);
        try {
            final HttpResponse response = carteClient.execute(request);
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                final Document doc = carteClient.parse(response.getEntity());
                final Element webresultElement = doc.getDocumentElement();

                final String message = DomUtils.getTextValue(DomUtils.getChildElementByTagName(webresultElement,
                        "message"));
                if (!StringUtils.isNullOrEmpty(message)) {
                    executionLogger.log(message);
                }
                final String result = DomUtils.getTextValue(DomUtils.getChildElementByTagName(webresultElement,
                        "result"));
                if ("OK".equalsIgnoreCase(result)) {
                    executionLogger.setStatusRunning();
                    executionLogger.flushLog();
                    return true;
                } else if ("ERROR".equalsIgnoreCase(result)) {
                    executionLogger.setStatusFailed(null, startTransUrl, new PentahoJobException(
                            "The Carte service reported an 'ERROR' result when starting transformation"));
                    return false;
                } else {
                    executionLogger.setStatusFailed(null, startTransUrl, new PentahoJobException(
                            "Encountered unexpected result from Carte when starting transformation: " + result));
                    return false;
                }
            } else {
                String responseString = EntityUtils.toString(response.getEntity());
                executionLogger.log(responseString);
                executionLogger.setStatusFailed(null, startTransUrl, new PentahoJobException(
                        "Unexpected response status when starting transformation: " + statusCode));
                return false;
            }
        } finally {
            request.releaseConnection();
        }
    }

    @Override
    protected PentahoJobContext getJobContext(TenantContext tenantContext, RepositoryFile file) {
        return new PentahoJobContext(file);
    }
}
