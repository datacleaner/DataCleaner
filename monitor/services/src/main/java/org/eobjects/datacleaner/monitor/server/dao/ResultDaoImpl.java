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
package org.eobjects.datacleaner.monitor.server.dao;

import java.util.List;

import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContextFactory;
import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.eobjects.datacleaner.repository.RepositoryFolder;
import org.eobjects.datacleaner.util.FileFilters;
import org.eobjects.metamodel.util.CollectionUtils;
import org.eobjects.metamodel.util.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Default implementation of {@link ResultDao}
 */
@Component
public class ResultDaoImpl implements ResultDao {

    private final TenantContextFactory _tenantContextFactory;

    @Autowired
    public ResultDaoImpl(TenantContextFactory tenantContextFactory) {
        _tenantContextFactory = tenantContextFactory;
    }

    @Override
    public List<RepositoryFile> getResultsForJob(TenantIdentifier tenantIdentifier, JobIdentifier job) {
        final TenantContext context = _tenantContextFactory.getContext(tenantIdentifier.getId());
        final RepositoryFolder resultsFolder = context.getResultFolder();
        final String jobName = job.getName();

        return getResultsForJob(jobName, resultsFolder);
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
}