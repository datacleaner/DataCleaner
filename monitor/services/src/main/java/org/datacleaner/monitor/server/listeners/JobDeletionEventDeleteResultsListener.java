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
package org.datacleaner.monitor.server.listeners;

import java.util.List;

import org.datacleaner.monitor.events.JobDeletionEvent;
import org.datacleaner.monitor.server.dao.ResultDao;
import org.datacleaner.monitor.shared.model.JobIdentifier;
import org.datacleaner.monitor.shared.model.TenantIdentifier;
import org.datacleaner.repository.RepositoryFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * Listener that deletes results pertaining to a deleted job from the
 * repository.
 */
@Component
public class JobDeletionEventDeleteResultsListener implements ApplicationListener<JobDeletionEvent> {

    @Autowired
    ResultDao resultDao;

    @Override
    public void onApplicationEvent(final JobDeletionEvent event) {
        final TenantIdentifier tenantIdentifier = new TenantIdentifier(event.getTenant());
        final JobIdentifier jobIdentifier = new JobIdentifier(event.getJobName());

        final List<RepositoryFile> results = resultDao.getResultsForJob(tenantIdentifier, jobIdentifier);
        for (final RepositoryFile repositoryFile : results) {
            repositoryFile.delete();
        }
    }
}
