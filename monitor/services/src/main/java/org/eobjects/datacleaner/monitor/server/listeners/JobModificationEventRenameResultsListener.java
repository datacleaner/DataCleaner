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
package org.eobjects.datacleaner.monitor.server.listeners;

import java.util.List;

import org.eobjects.datacleaner.monitor.events.JobModificationEvent;
import org.eobjects.datacleaner.monitor.server.dao.ResultDao;
import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * Listener that handles renaming of result files, when a job has been renamed.
 */
@Component
public class JobModificationEventRenameResultsListener implements ApplicationListener<JobModificationEvent> {

    private final ResultDao _resultDao;

    @Autowired
    public JobModificationEventRenameResultsListener(ResultDao resultDao) {
        _resultDao = resultDao;
    }

    @Override
    public void onApplicationEvent(JobModificationEvent event) {
        final String oldJobName = event.getOldJobName();
        final String newJobName = event.getNewJobName();
        if (oldJobName.equals(newJobName)) {
            return;
        }

        final String tenant = event.getTenant();
        final TenantIdentifier tenantIdentifier = new TenantIdentifier(tenant);
        final List<RepositoryFile> oldResultFiles = _resultDao.getResultsForJob(tenantIdentifier, new JobIdentifier(
                oldJobName));

        final JobIdentifier newJob = new JobIdentifier(newJobName);
        for (RepositoryFile repositoryFile : oldResultFiles) {
            _resultDao.updateResult(tenantIdentifier, repositoryFile, newJob, null);
        }
    }
}
