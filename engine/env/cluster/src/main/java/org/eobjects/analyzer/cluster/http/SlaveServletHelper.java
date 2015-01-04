/**
 * AnalyzerBeans
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
package org.eobjects.analyzer.cluster.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.SerializationUtils;
import org.eobjects.analyzer.cluster.SlaveAnalysisRunner;
import org.eobjects.analyzer.cluster.SlaveJobInterceptor;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.JaxbJobReader;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.runner.AnalysisListener;
import org.eobjects.analyzer.job.runner.AnalysisResultFuture;
import org.eobjects.analyzer.job.runner.AnalysisRunner;
import org.eobjects.analyzer.result.SimpleAnalysisResult;
import org.apache.metamodel.util.FileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper method for handling servlet requests and responses according to the
 * requests sent by the master node if it is using a {@link HttpClusterManager}.
 */
public class SlaveServletHelper {

    private static final Logger logger = LoggerFactory.getLogger(SlaveServletHelper.class);

    private final AnalyzerBeansConfiguration _configuration;
    private final SlaveJobInterceptor _jobInterceptor;
    private final ConcurrentMap<String, AnalysisResultFuture> _runningJobs;

    /**
     * 
     * @param configuration
     * @deprecated Use
     *             {@link #SlaveServletHelper(AnalyzerBeansConfiguration, Map)}
     *             instead.
     */
    @Deprecated
    public SlaveServletHelper(AnalyzerBeansConfiguration configuration) {
        this(configuration, new ConcurrentHashMap<String, AnalysisResultFuture>());
    }

    /**
     * Creates a {@link SlaveServletHelper}.
     * 
     * @param configuration
     * @param runningJobsMap
     *            a map to be used for internal book-keeping of running jobs.
     *            This parameter is required so that multiple
     *            {@link SlaveServletHelper}s can share the same running jobs
     *            state.
     */
    public SlaveServletHelper(AnalyzerBeansConfiguration configuration,
            ConcurrentMap<String, AnalysisResultFuture> runningJobsMap) {
        this(configuration, null, runningJobsMap);
    }

    /**
     * 
     * @param configuration
     * @param jobInterceptor
     * 
     * @deprecated use
     *             {@link #SlaveServletHelper(AnalyzerBeansConfiguration, SlaveJobInterceptor, ConcurrentMap)}
     *             instead
     */
    @Deprecated
    public SlaveServletHelper(AnalyzerBeansConfiguration configuration, SlaveJobInterceptor jobInterceptor) {
        this(configuration, jobInterceptor, new ConcurrentHashMap<String, AnalysisResultFuture>());
    }

    /**
     * Creates a {@link SlaveServletHelper}.
     * 
     * @param configuration
     *            the slave's {@link AnalyzerBeansConfiguration}.
     * @param jobInterceptor
     *            an optional interceptor
     * @param runningJobsMap
     *            a map to be used for internal book-keeping of running jobs.
     *            This parameter is required so that multiple
     *            {@link SlaveServletHelper}s can share the same running jobs
     *            state.
     */
    public SlaveServletHelper(AnalyzerBeansConfiguration configuration, SlaveJobInterceptor jobInterceptor,
            ConcurrentMap<String, AnalysisResultFuture> runningJobsMap) {
        if (configuration == null) {
            throw new IllegalArgumentException("AnalyzerBeansConfiguration cannot be null");
        }
        _configuration = configuration;
        _jobInterceptor = jobInterceptor;
        _runningJobs = runningJobsMap;
    }
    
    /**
     * Completely handles a HTTP request and response. This method is
     * functionally equivalent of calling these methods in sequence:
     * 
     * {@link #readJob(HttpServletRequest)}
     * 
     * {@link #runJob(AnalysisJob, String)}
     * 
     * {@link #serializeResult(AnalysisResultFuture, String)}
     * 
     * {@link #sendResponse(HttpServletResponse, Serializable)}
     * 
     * @param request
     * @param response
     * @throws IOException
     */
    public void handleRequest(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        handleRequest(request, response, new AnalysisListener[0]);
    }

    /**
     * Completely handles a HTTP request and response. This method is
     * functionally equivalent of calling these methods in sequence:
     * 
     * {@link #readJob(HttpServletRequest)}
     * 
     * {@link #runJob(AnalysisJob, String, AnalysisListener...)
     * 
     * {@link #serializeResult(AnalysisResultFuture, String)}
     * 
     * {@link #sendResponse(HttpServletResponse, Serializable)}
     * 
     * @param request
     * @param response
     * @param analysisListeners
     * @throws IOException
     */
    public void handleRequest(final HttpServletRequest request, final HttpServletResponse response, final AnalysisListener ... analysisListeners) throws IOException {
        final String jobId = request.getParameter(HttpClusterManager.HTTP_PARAM_SLAVE_JOB_ID);
        final String action = request.getParameter(HttpClusterManager.HTTP_PARAM_ACTION);

        if (HttpClusterManager.ACTION_CANCEL.equals(action)) {
            logger.info("Handling 'cancel' request: {}", jobId);
            cancelJob(jobId);
            return;
        }

        if (HttpClusterManager.ACTION_RUN.equals(action)) {
            logger.info("Handling 'run' request: {}", jobId);

            final AnalysisJob job;
            try {
                job = readJob(request);
            } catch (IOException e) {
                logger.error("Failed to read job definition from HTTP request", e);
                throw e;
            }

            final Serializable resultObject;
            try {
                final AnalysisResultFuture resultFuture = runJob(job, jobId, analysisListeners);
                resultObject = serializeResult(resultFuture, jobId);
            } catch (RuntimeException e) {
                logger.error("Unexpected error occurred while running slave job", e);
                throw e;
            }

            try {
                sendResponse(response, resultObject);
            } catch (IOException e) {
                logger.error("Failed to send job result through HTTP response", e);
                throw e;
            }

            return;
        }

        logger.warn("Unspecified action request: {}", jobId);
    }

    /**
     * 
     * @param resultFuture
     * @return
     * @deprecated use {@link #serializeResult(AnalysisResultFuture, String)}
     *             instead.
     */
    @Deprecated
    public Serializable serializeResult(AnalysisResultFuture resultFuture) {
        return serializeResult(resultFuture, null);
    }

    public Serializable serializeResult(AnalysisResultFuture resultFuture, String slaveJobId) {
        try {
            // wait for result to be ready
            resultFuture.await();

            final Serializable resultObject;
            if (resultFuture.isSuccessful()) {
                resultObject = new SimpleAnalysisResult(resultFuture.getResultMap());
            } else {
                resultObject = new ArrayList<Throwable>(resultFuture.getErrors());
            }

            return resultObject;
        } finally {
            if (slaveJobId != null) {
                _runningJobs.remove(slaveJobId);
            }
        }
    }

    public AnalysisJob readJob(HttpServletRequest request) throws IOException {
        final JaxbJobReader reader = new JaxbJobReader(_configuration);
        final String jobDefinition = request.getParameter(HttpClusterManager.HTTP_PARAM_JOB_DEF);

        final InputStream inputStream;
        if (jobDefinition == null) {
            // backwards compatibility node
            inputStream = request.getInputStream();
        } else {
            inputStream = new ByteArrayInputStream(jobDefinition.getBytes());
        }

        try {
            final AnalysisJobBuilder jobBuilder = reader.create(inputStream);
            if (_jobInterceptor != null) {
                _jobInterceptor.intercept(jobBuilder, _configuration);
            }
            final AnalysisJob job = jobBuilder.toAnalysisJob();
            return job;
        } finally {
            FileHelper.safeClose(inputStream);
        }
    }

    /**
     * Runs a slave job
     * 
     * @param job
     * @return
     * @deprecated use {@link #runJob(AnalysisJob, String)} instead
     */
    @Deprecated
    public AnalysisResultFuture runJob(AnalysisJob job) {
        return runJob(job, null);
    }

    /**
     * Runs a slave job
     * 
     * @param job
     * @param slaveJobId
     *            a unique ID for the slave job.
     * @return
     */
    public AnalysisResultFuture runJob(AnalysisJob job, String slaveJobId) {
        return runJob(job, slaveJobId, new AnalysisListener[0]);
    }

    /**
     * Runs a slave job
     * 
     * @param job
     * @param slaveJobId
     * @param analysisListeners
     * @return
     */
    public AnalysisResultFuture runJob(AnalysisJob job, String slaveJobId, AnalysisListener... analysisListeners) {
        final AnalysisRunner runner = new SlaveAnalysisRunner(_configuration, analysisListeners);
        final AnalysisResultFuture resultFuture = runner.run(job);
        if (slaveJobId != null) {
            _runningJobs.put(slaveJobId, resultFuture);
        }
        return resultFuture;
    }

    /**
     * Cancels a slave job, referred by it's id.
     * 
     * @param slaveJobId
     * @return whether or not the job was (found and) cancelled.
     */
    public boolean cancelJob(String slaveJobId) {
        final AnalysisResultFuture resultFuture = _runningJobs.remove(slaveJobId);
        if (resultFuture != null) {
            resultFuture.cancel();
            return true;
        }
        return false;
    }

    public void sendResponse(HttpServletResponse response, Serializable object) throws IOException {
        ServletOutputStream outputStream = response.getOutputStream();
        try {
            SerializationUtils.serialize(object, outputStream);
        } finally {
            outputStream.flush();
        }
    }
}
