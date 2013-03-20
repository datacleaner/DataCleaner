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

import java.util.Map;

import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.job.ExecutionLogger;
import org.eobjects.datacleaner.monitor.job.JobEngine;
import org.eobjects.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.springframework.stereotype.Component;

/**
 * A {@link JobEngine} implementation for running custom Java-based jobs
 */
@Component
public class CustomJavaJobEngine extends AbstractJobEngine<CustomJavaJobContext> {

    public static final String EXTENSION = ".java.job.xml";

    public CustomJavaJobEngine() {
        super(EXTENSION);
    }

    @Override
    public String getJobTypeDisplayName() {
        return "Custom Java job";
    }

    @Override
    protected CustomJavaJobContext getJobContext(TenantContext tenantContext, RepositoryFile file) {
        return new CustomJavaJobContext(file);
    }

    @Override
    public void executeJob(TenantContext context, ExecutionLog execution, ExecutionLogger executionLogger,
            Map<String, String> variables) throws Exception {
        // TODO : Implement job execution here.
        Exception error = new UnsupportedOperationException("Custom Java jobs are not yet implemented");
        executionLogger.setStatusFailed(this, null, error);
    }

}
