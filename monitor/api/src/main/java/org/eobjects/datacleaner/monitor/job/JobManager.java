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
package org.eobjects.datacleaner.monitor.job;

import java.util.List;

import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.scheduling.model.ExecutionLog;

/**
 * A component responsible for jobs of a particular type.
 * 
 * @param <T>
 */
public interface JobManager {

    /**
     * Gets the display name of a particular job type, such as 'DataCleaner
     * analysis job', 'Custom Java class' or other names of the job type.
     * 
     * @return
     */
    public String getJobTypeDisplayName();

    /**
     * Gets all job names of a particular tenant.
     * 
     * @param tenantContext
     * @return
     */
    public List<String> getJobNames(TenantContext tenantContext);

    /**
     * Executes a job
     * 
     * @param context
     * @param execution
     * @param executionLogger
     */
    public void executeJob(TenantContext context, ExecutionLog execution, ExecutionLogger executionLogger);
}
