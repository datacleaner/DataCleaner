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
package org.datacleaner.monitor.configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.InjectionManagerFactory;
import org.datacleaner.monitor.job.JobContext;
import org.datacleaner.monitor.job.JobEngine;
import org.datacleaner.monitor.job.JobEngineManager;
import org.datacleaner.monitor.shared.model.JobIdentifier;
import org.datacleaner.repository.Repository;
import org.datacleaner.repository.RepositoryFile;
import org.datacleaner.repository.RepositoryFolder;
import org.datacleaner.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Default implementation of {@link TenantContext}.
 */
public class TenantContextImpl extends AbstractTenantContext implements TenantContext {

    private static final Logger logger = LoggerFactory.getLogger(TenantContextImpl.class);

    private final String _tenantId;
    private final Repository _repository;
    private final ConfigurationCache _configurationCache;
    private final JobEngineManager _jobEngineManager;
    private final LoadingCache<JobIdentifier, JobContext> _jobCache;

    /**
     * Constructs the {@link TenantContext}.
     * 
     * @param tenantId
     * @param repository
     * @param injectionManagerFactory
     *            the injection manager factory applicable to the whole
     *            application. This injection manager will be decorated/wrapped
     *            with a {@link TenantInjectionManagerFactory} in order to
     *            provide tenant-specific injection options.
     * @param jobEngineManager
     */
    public TenantContextImpl(String tenantId, Repository repository, InjectionManagerFactory injectionManagerFactory,
            JobEngineManager jobEngineManager) {
        _tenantId = tenantId;
        _repository = repository;
        _jobEngineManager = jobEngineManager;
        if (jobEngineManager == null) {
            throw new IllegalArgumentException("JobEngineManager cannot be null");
        }

        final TenantInjectionManagerFactory tenantInjectionManagerFactory = new TenantInjectionManagerFactory(
                injectionManagerFactory, repository, this);

        _configurationCache = new ConfigurationCache(tenantInjectionManagerFactory, this, repository);
        _jobCache = buildJobCache();
    }

    private LoadingCache<JobIdentifier, JobContext> buildJobCache() {
        final LoadingCache<JobIdentifier, JobContext> cache = CacheBuilder.newBuilder()
                .expireAfterWrite(5, TimeUnit.SECONDS).build(new CacheLoader<JobIdentifier, JobContext>() {
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
    public DataCleanerConfiguration getConfiguration() {
        return _configurationCache.getAnalyzerBeansConfiguration();
    }

    @Override
    public RepositoryFolder getTenantRootFolder() {
        RepositoryFolder tenantFolder = _repository.getFolder(_tenantId);
        if (tenantFolder == null) {
            logger.info("Creating tenant folder '{}' for tenant '{}'", _tenantId, _tenantId);
            tenantFolder = _repository.createFolder(_tenantId);
            tenantFolder.createFolder(PATH_JOBS);
            tenantFolder.createFolder(PATH_RESULTS);
            tenantFolder.createFolder(PATH_TIMELINES);
        }
        return tenantFolder;
    }

    @Override
    public String getTenantId() {
        return _tenantId;
    }
    
    @Override
    protected ResultContext getResult(RepositoryFile resultFile) {
        if (resultFile == null) {
            return null;
        }
        return new DefaultResultContext(this, resultFile);
    }

    @Override
    public RepositoryFile getConfigurationFile() {
        return _configurationCache.getConfigurationFile();
    }

    @Override
    public void onConfigurationChanged() {
        logger.debug("onConfigurationChanged() invoked on tenant: {}", _tenantId);
        _configurationCache.clearCache();
        _jobCache.invalidateAll();
    }

}
