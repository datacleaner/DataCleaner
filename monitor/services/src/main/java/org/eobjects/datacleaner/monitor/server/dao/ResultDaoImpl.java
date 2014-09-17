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
package org.eobjects.datacleaner.monitor.server.dao;

import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import org.eobjects.analyzer.result.AnalysisResult;
import org.eobjects.analyzer.result.SimpleAnalysisResult;
import org.eobjects.datacleaner.monitor.configuration.ResultContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContextFactory;
import org.eobjects.datacleaner.monitor.events.ResultModificationEvent;
import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.eobjects.datacleaner.repository.RepositoryFolder;
import org.eobjects.datacleaner.util.FileFilters;
import org.apache.metamodel.util.Action;
import org.apache.metamodel.util.CollectionUtils;
import org.apache.metamodel.util.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Default implementation of {@link ResultDao}
 */
@Component
public class ResultDaoImpl implements ResultDao {

    private final TenantContextFactory _tenantContextFactory;
    private final ApplicationEventPublisher _eventPublisher;

    @Autowired
    public ResultDaoImpl(TenantContextFactory tenantContextFactory, ApplicationEventPublisher eventPublisher) {
        _tenantContextFactory = tenantContextFactory;
        _eventPublisher = eventPublisher;
    }

    @Override
    public List<RepositoryFile> getResultsForJob(TenantIdentifier tenantIdentifier, JobIdentifier job) {
        final TenantContext context = _tenantContextFactory.getContext(tenantIdentifier.getId());
        final RepositoryFolder resultsFolder = context.getResultFolder();
        final String jobName = job.getName();

        return getResultsForJob(jobName, resultsFolder);
    }

    @Override
    public ResultContext getLatestResult(TenantIdentifier tenantIdentifier, JobIdentifier job) {
        final TenantContext context = _tenantContextFactory.getContext(tenantIdentifier.getId());
        final RepositoryFolder resultsFolder = context.getResultFolder();
        final String jobName = job.getName();

        final RepositoryFile resultFile = resultsFolder.getLatestFile(jobName,
                FileFilters.ANALYSIS_RESULT_SER.getExtension());

        if (resultFile == null) {
            return null;
        }

        return context.getResult(resultFile.getName());
    }

    protected static List<RepositoryFile> getResultsForJob(String jobName, RepositoryFolder resultsFolder) {

        final String extension = FileFilters.ANALYSIS_RESULT_SER.getExtension();
        final String prefix = jobName + "-";

        final List<RepositoryFile> candidatesByFilename = resultsFolder.getFiles(prefix, extension);

        final List<RepositoryFile> files = CollectionUtils.filter(candidatesByFilename,
                new Predicate<RepositoryFile>() {
                    @Override
                    public Boolean eval(RepositoryFile file) {
                        // check that the remainding part of the file is ONLY a
                        // timestamp - or else it might be a name conflict
                        // between similarly named jobs.
                        String timestampPart = file.getName();
                        timestampPart = timestampPart.substring(prefix.length());
                        timestampPart = timestampPart.substring(0, timestampPart.length() - extension.length());
                        try {
                            Long.parseLong(timestampPart);
                            return true;
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    }
                });

        return files;
    }

    @Override
    public ResultContext getResult(TenantIdentifier tenant, RepositoryFile resultFile) {
        if (resultFile == null) {
            return null;
        }
        TenantContext context = _tenantContextFactory.getContext(tenant);
        return context.getResult(resultFile.getName());
    }

    @Override
    public ResultContext updateResult(TenantIdentifier tenantIdentifier, RepositoryFile resultFile,
            JobIdentifier newJob, Date newTimestamp) {
        ResultContext result = getResult(tenantIdentifier, resultFile);
        return updateResult(tenantIdentifier, result, newJob, newTimestamp);
    }

    @Override
    public ResultContext updateResult(TenantIdentifier tenantIdentifier, ResultContext result, JobIdentifier newJob,
            Date newDate) {
        final TenantContext tenantContext = _tenantContextFactory.getContext(tenantIdentifier);

        final RepositoryFile existingFile = result.getResultFile();

        final long newTimestamp;
        final AnalysisResult newAnalysisResult;
        if (newDate == null) {
            newAnalysisResult = result.getAnalysisResult();
            newTimestamp = newAnalysisResult.getCreationDate().getTime();
        } else {
            final AnalysisResult existinAnalysisResult = result.getAnalysisResult();
            newAnalysisResult = new SimpleAnalysisResult(existinAnalysisResult.getResultMap(), newDate);

            newTimestamp = newDate.getTime();
        }

        // we assume a filename pattern like this:
        // {job}-{timestamp}.{extension}
        final String oldFilename = existingFile.getName();
        final int lastIndexOfDash = oldFilename.lastIndexOf('-');
        assert lastIndexOfDash != -1;

        int extensionStartIndex = oldFilename.indexOf('.', lastIndexOfDash);
        assert extensionStartIndex != -1;
        final String extension = oldFilename.substring(extensionStartIndex);

        final String newJobName;
        if (newJob == null) {
            newJobName = oldFilename.substring(0, lastIndexOfDash);
        } else {
            newJobName = newJob.getName();
        }

        final String newFilename = newJobName + '-' + newTimestamp + extension;

        final RepositoryFolder resultFolder = tenantContext.getResultFolder();
        RepositoryFile newFile = resultFolder.getFile(newFilename);

        final Action<OutputStream> writeAction = new Action<OutputStream>() {
            @Override
            public void run(OutputStream out) throws Exception {
                final ObjectOutputStream oos = new ObjectOutputStream(out);
                oos.writeObject(newAnalysisResult);
            }
        };

        if (newFile == null) {
            newFile = resultFolder.createFile(newFilename, writeAction);
        } else {
            newFile.writeFile(writeAction);
        }
        existingFile.delete();

        // notify listeners
        if (_eventPublisher != null) {
            _eventPublisher.publishEvent(new ResultModificationEvent(this, tenantContext.getTenantId(), oldFilename,
                    newFilename, newJobName, newTimestamp));
        }

        final ResultContext newResult = getResult(tenantIdentifier, newFile);
        return newResult;
    }
}