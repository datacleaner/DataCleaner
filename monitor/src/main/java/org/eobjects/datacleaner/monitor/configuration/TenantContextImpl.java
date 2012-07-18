/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.repository.Repository;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.eobjects.datacleaner.repository.RepositoryFolder;
import org.eobjects.datacleaner.util.FileFilters;
import org.eobjects.metamodel.util.CollectionUtils;
import org.eobjects.metamodel.util.Func;
import org.eobjects.metamodel.util.HasNameMapper;

public class TenantContextImpl implements TenantContext {

    private static final String PATH_TIMELINES = "timelines";
    private static final String PATH_JOBS = "jobs";
    private static final String PATH_RESULTS = "results";
    private static final String EXTENSION_JOB = FileFilters.ANALYSIS_XML.getExtension();

    private final String _tenantId;
    private final Repository _repository;
    private final ConfigurationCache _configurationCache;
    private final ConcurrentHashMap<String, JobContext> _jobCache;

    public TenantContextImpl(String tenantId, Repository repository) {
        _tenantId = tenantId;
        _repository = repository;
        _configurationCache = new ConfigurationCache(tenantId, repository);
        _jobCache = new ConcurrentHashMap<String, JobContext>();
    }

    @Override
    public List<String> getJobNames() {
        final RepositoryFolder jobsFolder = getJobFolder();
        final List<RepositoryFile> files = jobsFolder.getFiles(null, EXTENSION_JOB);
        final List<String> filenames = CollectionUtils.map(files, new HasNameMapper());
        final List<String> jobNames = CollectionUtils.map(filenames, new Func<String, String>() {
            @Override
            public String eval(String filename) {
                return filename.substring(0, filename.length() - EXTENSION_JOB.length());
            }
        });
        return jobNames;
    }

    @Override
    public JobContext getJob(String jobName) throws IllegalArgumentException {
        if (StringUtils.isNullOrEmpty(jobName)) {
            return null;
        }
        JobContext job = _jobCache.get(jobName);
        if (job == null) {
            if (!jobName.endsWith(EXTENSION_JOB)) {
                jobName = jobName + EXTENSION_JOB;
            }

            final RepositoryFile file = getJobFolder().getFile(jobName);
            if (file == null) {
                throw new IllegalArgumentException("No such job: " + jobName);
            }
            final JobContext newJob = new DefaultJobContext(this, file);
            job = _jobCache.putIfAbsent(jobName, newJob);
            if (job == null) {
                job = newJob;
            }
        }
        return job;
    }

    @Override
    public AnalyzerBeansConfiguration getConfiguration() {
        return _configurationCache.getAnalyzerBeansConfiguration();
    }

    @Override
    public JobContext getJob(JobIdentifier jobIdentifier) {
        if (jobIdentifier == null) {
            return null;
        }
        return getJob(jobIdentifier.getName());
    }

    @Override
    public RepositoryFolder getJobFolder() {
        final RepositoryFolder tenantFolder = _repository.getFolder(_tenantId);
        if (tenantFolder == null) {
            throw new IllegalArgumentException("No such tenant: " + _tenantId);
        }
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
        final RepositoryFolder tenantFolder = _repository.getFolder(_tenantId);
        if (tenantFolder == null) {
            throw new IllegalArgumentException("No such tenant: " + _tenantId);
        }
        final RepositoryFolder resultsFolder = tenantFolder.getFolder(PATH_RESULTS);
        if (resultsFolder == null) {
            throw new IllegalArgumentException("No result folder for tenant: " + _tenantId);
        }
        return resultsFolder;
    }

    @Override
    public RepositoryFolder getTimelineFolder() {
        final RepositoryFolder tenantFolder = _repository.getFolder(_tenantId);
        if (tenantFolder == null) {
            throw new IllegalArgumentException("No such tenant: " + _tenantId);
        }
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

}
