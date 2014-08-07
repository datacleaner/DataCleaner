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
package org.eobjects.datacleaner.monitor.server.job;

import java.util.List;

import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.job.JobContext;
import org.eobjects.datacleaner.monitor.job.JobEngine;
import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.eobjects.datacleaner.repository.RepositoryFolder;
import org.apache.metamodel.util.CollectionUtils;
import org.apache.metamodel.util.Func;
import org.apache.metamodel.util.HasNameMapper;

/**
 * Abstract helper implementation of some of the {@link JobEngine} methods
 */
public abstract class AbstractJobEngine<T extends JobContext> implements JobEngine<T> {

    private final String _fileExtension;

    public AbstractJobEngine(String fileExtension) {
        _fileExtension = fileExtension;
    }

    /**
     * Utility method to get the filename of a job.
     * 
     * @param jobName
     * @return
     */
    protected final String getJobFilename(String jobName) {
        if (!jobName.endsWith(_fileExtension)) {
            jobName = jobName + _fileExtension;
        }
        return jobName;
    }

    @Override
    public T getJobContext(TenantContext tenantContext, JobIdentifier job) {
        final String jobName = job.getName();
        final String jobFilename = getJobFilename(jobName);
        final RepositoryFile file = tenantContext.getJobFolder().getFile(jobFilename);
        if (file == null) {
            throw new IllegalArgumentException("No such job: " + jobName);
        }
        return getJobContext(tenantContext, file);
    }

    protected abstract T getJobContext(TenantContext tenantContext, RepositoryFile file);

    @Override
    public final List<JobIdentifier> getJobs(TenantContext tenantContext) {
        final RepositoryFolder jobsFolder = tenantContext.getJobFolder();
        final List<RepositoryFile> files = jobsFolder.getFiles(null, _fileExtension);
        final List<String> filenames = CollectionUtils.map(files, new HasNameMapper());
        final List<JobIdentifier> jobs = CollectionUtils.map(filenames, new Func<String, JobIdentifier>() {
            @Override
            public JobIdentifier eval(String filename) {
                String jobName = filename.substring(0, filename.length() - _fileExtension.length());
                return new JobIdentifier(jobName, getJobType());
            }
        });
        return jobs;
    }

    @Override
    public final boolean containsJob(TenantContext tenantContext, String jobName) {
        if (!jobName.endsWith(_fileExtension)) {
            jobName = jobName + _fileExtension;
        }
        final RepositoryFile file = tenantContext.getJobFolder().getFile(jobName);
        if (file == null) {
            return false;
        }
        return true;
    }
}
