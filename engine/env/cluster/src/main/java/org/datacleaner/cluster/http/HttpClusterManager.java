/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Neopost - Customer Information Management
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
package org.datacleaner.cluster.http;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.metamodel.util.Action;
import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.LazyRef;
import org.datacleaner.cluster.ClusterManager;
import org.datacleaner.cluster.DistributedJobContext;
import org.datacleaner.cluster.FixedDivisionsCountJobDivisionManager;
import org.datacleaner.cluster.JobDivisionManager;
import org.datacleaner.cluster.LazyRefAnalysisResultFuture;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.JaxbJobWriter;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.result.AnalysisResult;
import org.datacleaner.util.ChangeAwareObjectInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A cluster manager that uses HTTP servlet transport to communicate between
 * nodes.
 */
public class HttpClusterManager implements ClusterManager {

    private static final Logger logger = LoggerFactory.getLogger(HttpClusterManager.class);

    public static final String HTTP_PARAM_SLAVE_JOB_ID = "slave-job-id";
    public static final String HTTP_PARAM_ACTION = "action";
    public static final String HTTP_PARAM_JOB_DEF = "job-def";

    public static final String ACTION_RUN = "run";
    public static final String ACTION_CANCEL = "cancel";

    private final HttpClient _httpClient;
    private final List<String> _slaveEndpoints;
    private final HttpClientContext _httpClientContext;

    /**
     * Creates a new HTTP cluster manager
     * 
     * @param slaveEndpoints
     *            the endpoint URLs of the slaves
     */
    public HttpClusterManager(List<String> slaveEndpoints) {
        this(HttpClients.custom().useSystemProperties().setConnectionManager(new PoolingHttpClientConnectionManager())
                .build(), HttpClientContext.create(), slaveEndpoints);
    }

    /**
     * Create a new HTTP cluster manager
     * 
     * @param httpClient
     *            http client to use for invoking slave endpoints. Must be
     *            capable of executing multiple requests at the same time (see
     *            {@link PoolingClientConnectionManager}).
     * @param context
     * @param slaveEndpoints
     *            the endpoint URLs of the slaves
     */
    public HttpClusterManager(HttpClient httpClient, HttpClientContext context, List<String> slaveEndpoints) {
        _httpClient = httpClient;
        _httpClientContext = context;
        _slaveEndpoints = slaveEndpoints;
    }

    @Override
    public JobDivisionManager getJobDivisionManager() {
        return new FixedDivisionsCountJobDivisionManager(_slaveEndpoints.size());
    }

    @Override
    public AnalysisResultFuture dispatchJob(AnalysisJob job, DistributedJobContext context) throws Exception {
        // determine endpoint url
        final int index = context.getJobDivisionIndex();
        final String slaveEndpoint = _slaveEndpoints.get(index);

        // write the job as XML
        final JaxbJobWriter jobWriter = new JaxbJobWriter(context.getMasterConfiguration());
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        jobWriter.write(job, baos);
        final byte[] bytes = baos.toByteArray();

        // send the request in another thread
        final List<Throwable> errors = new LinkedList<Throwable>();

        final String slaveJobUuid = UUID.randomUUID().toString();

        final LazyRef<AnalysisResult> resultRef = sendExecuteRequest(slaveEndpoint, bytes, errors, slaveJobUuid);
        resultRef.requestLoad(new Action<Throwable>() {
            @Override
            public void run(Throwable error) throws Exception {
                errors.add(error);
            }
        });

        return new LazyRefAnalysisResultFuture(resultRef, errors) {
            @Override
            public void cancel() {
                sendCancelRequest(slaveEndpoint, slaveJobUuid);
            }
        };
    }

    private LazyRef<AnalysisResult> sendExecuteRequest(final String slaveEndpoint, final byte[] bytes,
            final List<Throwable> errors, final String slaveJobId) {
        return new LazyRef<AnalysisResult>() {
            @Override
            protected AnalysisResult fetch() throws Throwable {
                // send the HTTP request
                final HttpPost request = new HttpPost(slaveEndpoint);

                final List<NameValuePair> parameters = new ArrayList<NameValuePair>();
                parameters.add(new BasicNameValuePair(HTTP_PARAM_SLAVE_JOB_ID, slaveJobId));
                parameters.add(new BasicNameValuePair(HTTP_PARAM_ACTION, ACTION_RUN));
                parameters.add(new BasicNameValuePair(HTTP_PARAM_JOB_DEF, new String(bytes)));

                final UrlEncodedFormEntity entity = new UrlEncodedFormEntity(parameters);
                request.setEntity(entity);

                logger.info("Firing run request to slave server '{}' for job id '{}'", slaveEndpoint, slaveJobId);

                final HttpResponse response = _httpClient.execute(request, _httpClientContext);

                // handle the response
                final StatusLine statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() != 200) {
                    throw new IllegalStateException("Slave server '" + slaveEndpoint
                            + "' responded with an error to 'run' request: " + statusLine.getReasonPhrase() + " ("
                            + statusLine.getStatusCode() + ")");
                }

                final InputStream inputStream = response.getEntity().getContent();
                try {
                    AnalysisResult result = readResult(inputStream, errors);
                    return result;
                } finally {
                    FileHelper.safeClose(inputStream);
                }
            }
        };
    }

    private void sendCancelRequest(String slaveEndpoint, String slaveJobId) {
        RequestBuilder rb = RequestBuilder.post(slaveEndpoint);
        rb.addParameter(HTTP_PARAM_SLAVE_JOB_ID, slaveJobId);
        rb.addParameter(HTTP_PARAM_ACTION, ACTION_CANCEL);

        try {
            final HttpResponse response = _httpClient.execute(rb.build(), _httpClientContext);

            // handle the response
            final StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() != 200) {
                throw new IllegalStateException("Slave server '" + slaveEndpoint
                        + "' responded with an error to 'cancel' request: " + statusLine.getReasonPhrase() + " ("
                        + statusLine.getStatusCode() + ")");
            }

        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new IllegalStateException("Failed to fire cancel request to slave server '" + slaveEndpoint
                    + "' for job id '" + slaveJobId + "'", e);
        }
    }

    protected AnalysisResult readResult(InputStream inputStream, List<Throwable> errors) throws Exception {
        final ChangeAwareObjectInputStream changeAwareObjectInputStream = new ChangeAwareObjectInputStream(inputStream);
        final Object object = changeAwareObjectInputStream.readObject();
        changeAwareObjectInputStream.close();
        if (object instanceof AnalysisResult) {
            // response carries a result
            return (AnalysisResult) object;
        } else if (object instanceof List) {
            // response carries a list of errors
            @SuppressWarnings("unchecked")
            List<Throwable> slaveErrors = (List<Throwable>) object;
            errors.addAll(slaveErrors);
            return null;
        } else {
            throw new IllegalStateException("Unexpected response payload: " + object);
        }
    }
}
