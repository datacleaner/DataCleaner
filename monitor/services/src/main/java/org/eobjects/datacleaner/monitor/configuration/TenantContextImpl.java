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
package org.eobjects.datacleaner.monitor.configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.InjectionManagerFactory;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.monitor.job.JobContext;
import org.eobjects.datacleaner.monitor.job.JobEngine;
import org.eobjects.datacleaner.monitor.job.JobEngineManager;
import org.eobjects.datacleaner.monitor.job.MetricJobContext;
import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.repository.Repository;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.eobjects.datacleaner.repository.RepositoryFolder;
import org.eobjects.datacleaner.util.FileFilters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Default implementation of {@link TenantContext}.
 */
public class TenantContextImpl implements TenantContext {

    private static final Logger logger = LoggerFactory.getLogger(TenantContextImpl.class);

    private static final String PATH_TIMELINES = "timelines";
    private static final String PATH_JOBS = "jobs";
    private static final String PATH_RESULTS = "results";
    private static final String EXTENSION_RESULT = FileFilters.ANALYSIS_RESULT_SER.getExtension();

    private final String _tenantId;
    private final Repository _repository;
    private final InjectionManagerFactory _injectionManagerFactory;
    private final ConfigurationCache _configurationCache;
    private final JobEngineManager _jobEngineManager;
    private final LoadingCache<JobIdentifier, JobContext> _jobCache;

    public TenantContextImpl(String tenantId, Repository repository, InjectionManagerFactory injectionManagerFactory,
            JobEngineManager jobEngineManager) {
        _tenantId = tenantId;
        _repository = repository;
        _injectionManagerFactory = injectionManagerFactory;
        _jobEngineManager = jobEngineManager;
        if (jobEngineManager == null) {
            throw new IllegalArgumentException("JobEngineManager cannot be null");
        }
        _configurationCache = new ConfigurationCache(tenantId, getTenantRootFolder(), _injectionManagerFactory);
        _jobCache = buildJobCache();
    }

    private LoadingCache<JobIdentifier, JobContext> buildJobCache() {
        final LoadingCache<JobIdentifier, JobContext> cache = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.SECONDS)
                .build(new CacheLoader<JobIdentifier, JobContext>() {
                    @Override
                    public JobContext load(JobIdentifier job) throws Exception {
                        final String jobName = job.getName();
                        if (StringUtils.isNullOrEmpty(jobName)) {
                            throw new NoSuchObjectException();
                        }

                        final TenantContext tenantContext = TenantContextImpl.this;
                        final JobEngine<?> jobEngine = _jobEngineManager.getJobEngine(tenantContext, jobName);
                        if (jobEngine == null) {
                            throw new NoSuchObjectException();
                        }
                        final JobContext result = jobEngine.getJobContext(tenantContext, job);
                        return result;
                    }
                });
        return cache;
    }

    @Override
    public List<JobIdentifier> getJobs() {
        final List<JobIdentifier> jobs = new ArrayList<JobIdentifier>();

        final Collection<JobEngine<?>> jobEngines = _jobEngineManager.getJobEngines();
        for (JobEngine<?> jobEngine : jobEngines) {
            final List<JobIdentifier> jobEngineJobs = jobEngine.getJobs(this);
            jobs.addAll(jobEngineJobs);
        }

        return jobs;
    }

    @Override
    public JobContext getJob(String jobName) {
        return getJob(new JobIdentifier(jobName));
    }

    @Override
    public JobContext getJob(JobIdentifier jobIdentifier) throws IllegalArgumentException {
        if (jobIdentifier == null) {
            throw new IllegalArgumentException("JobIdentifier cannot be null");
        }

        try {
            return _jobCache.get(jobIdentifier);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof NoSuchObjectException) {
                // expected exception at this point
                return null;
            }
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new IllegalStateException(e);
        }
    }

    @Override
    public AnalyzerBeansConfiguration getConfiguration() {
        return _configurationCache.getAnalyzerBeansConfiguration();
    }

    @Override
    public RepositoryFolder getTenantRootFolder() {
        RepositoryFolder tenantFolder = _repository.getFolder(_tenantId);
        if (tenantFolder == null) {
            logger.info("Creating tenant folder: {}", _tenantId);
            tenantFolder = _repository.createFolder(_tenantId);
            tenantFolder.createFolder(PATH_JOBS);
            tenantFolder.createFolder(PATH_RESULTS);
            tenantFolder.createFolder(PATH_TIMELINES);
        }
        return tenantFolder;
    }

    @Override
    public RepositoryFolder getJobFolder() {
        final RepositoryFolder tenantFolder = getTenantRootFolder();
        final RepositoryFolder jobsFolder = tenantFolder.getFolder(PATH_JOBS);
        if (jobsFolder == null) {
            throw new IllegalArgumentException("No job folder for tenant: " + _tenantId);
        }
        return jobsFolder;
    }

    @Override
    public String getTenantId() {
        return _tenantId;
    }

    @Override
    public RepositoryFolder getResultFolder() {
        final RepositoryFolder tenantFolder = getTenantRootFolder();
        final RepositoryFolder resultsFolder = tenantFolder.getFolder(PATH_RESULTS);
        if (resultsFolder == null) {
            throw new IllegalArgumentException("No result folder for tenant: " + _tenantId);
        }
        return resultsFolder;
    }

    @Override
    public RepositoryFolder getTimelineFolder() {
        final RepositoryFolder tenantFolder = getTenantRootFolder();
        final RepositoryFolder timelinesFolder = tenantFolder.getFolder(PATH_TIMELINES);
        if (timelinesFolder == null) {
            throw new IllegalArgumentException("No timeline folder for tenant: " + _tenantId);
        }
        return timelinesFolder;
    }

    @Override
    public boolean containsJob(String jobName) {
        try {
            JobContext job = getJob(jobName);
            return job != null;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public ResultContext getLatestResult(MetricJobContext job) {
        final String jobName = job.getName();
        final RepositoryFolder resultFolder = getResultFolder();
        final RepositoryFile resultFile = resultFolder.getLatestFile(jobName, EXTENSION_RESULT);
        return getResult(resultFile);
    }

    private ResultContext getResult(RepositoryFile resultFile) {
        if (resultFile == null) {
            return null;
        }
        return new DefaultResultContext(this, resultFile);
    }

    @Override
    public ResultContext getResult(String resultFilename) {
        if (StringUtils.isNullOrEmpty(resultFilename)) {
            return null;
        }
        if (!resultFilename.endsWith(EXTENSION_RESULT)) {
            resultFilename = resultFilename + EXTENSION_RESULT;
        }

        final RepositoryFolder resultFolder = getResultFolder();

        final RepositoryFile resultFile;
        if (resultFilename.endsWith("-latest" + EXTENSION_RESULT)) {
            final String jobName = resultFilename.substring(0, resultFilename.length() - ("-latest" + EXTENSION_RESULT).length());
            resultFile = resultFolder.getLatestFile(jobName, EXTENSION_RESULT);
        } else {
            resultFile = resultFolder.getFile(resultFilename);
        }

        return getResult(resultFile);
    }

    @Override
    public RepositoryFile getConfigurationFile() {
        return _configurationCache.getConfigurationFile();
    }

}
