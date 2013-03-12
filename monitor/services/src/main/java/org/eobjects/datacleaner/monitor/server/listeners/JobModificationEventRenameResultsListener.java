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
package org.eobjects.datacleaner.monitor.server.listeners;

import java.util.List;

import org.eobjects.datacleaner.monitor.events.JobModificationEvent;
import org.eobjects.datacleaner.monitor.server.controllers.ResultModificationController;
import org.eobjects.datacleaner.monitor.server.controllers.ResultModificationPayload;
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
    private final ResultModificationController _resultModificationController;

    @Autowired
    public JobModificationEventRenameResultsListener(ResultDao resultDao,
            ResultModificationController resultModificationController) {
        _resultDao = resultDao;
        _resultModificationController = resultModificationController;
    }

    @Override
    public void onApplicationEvent(JobModificationEvent event) {
        final String oldJobName = event.getOldJobName();
        final String newJobName = event.getNewJobName();
        if (oldJobName.equals(newJobName)) {
            return;
        }

        final String tenant = event.getTenant();
        final List<RepositoryFile> oldResultFiles = _resultDao.getResultsForJob(new TenantIdentifier(tenant),
                new JobIdentifier(oldJobName));

        for (RepositoryFile repositoryFile : oldResultFiles) {
            ResultModificationPayload modificationInput = new ResultModificationPayload();
            modificationInput.setJob(newJobName);
            modificationInput.setOverwrite(true);
            _resultModificationController.modifyResult(tenant, repositoryFile.getName(), modificationInput);
        }
    }
}
