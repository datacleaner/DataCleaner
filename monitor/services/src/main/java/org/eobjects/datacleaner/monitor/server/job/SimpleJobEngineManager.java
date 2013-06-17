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

import java.util.Arrays;
import java.util.Collection;

import org.eobjects.analyzer.util.ReflectionUtils;
import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.job.JobContext;
import org.eobjects.datacleaner.monitor.job.JobEngine;
import org.eobjects.datacleaner.monitor.job.JobEngineManager;

/**
 * Simple {@link JobEngineManager} implementation
 */
public class SimpleJobEngineManager implements JobEngineManager {

    private final Collection<JobEngine<?>> _jobEngines;

    public SimpleJobEngineManager(JobEngine<?>... jobEngines) {
        _jobEngines = Arrays.asList(jobEngines);
    }

    public SimpleJobEngineManager(Collection<JobEngine<?>> jobEngines) {
        _jobEngines = jobEngines;
    }

    @Override
    public Collection<JobEngine<?>> getJobEngines() {
        return _jobEngines;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends JobContext> JobEngine<? extends T> getJobEngine(T jobContext) {
        return (JobEngine<? extends T>) getJobEngine(jobContext.getClass());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends JobContext> JobEngine<? extends T> getJobEngine(Class<T> jobContext) {
        final Collection<JobEngine<?>> jobEngines = getJobEngines();
        for (JobEngine<?> jobEngine : jobEngines) {
            final Class<?> jobEngineTypeParameter = ReflectionUtils.getTypeParameter(jobEngine.getClass(), JobEngine.class, 0);
            if (ReflectionUtils.is(jobContext, jobEngineTypeParameter)) {
                return (JobEngine<? extends T>) jobEngine;
            }
        }
        return null;
    }

    @Override
    public JobEngine<?> getJobEngine(TenantContext tenantContext, String jobName) {
        final Collection<JobEngine<?>> jobEngines = getJobEngines();
        for (JobEngine<?> jobEngine : jobEngines) {
            if (jobEngine.containsJob(tenantContext, jobName)) {
                return jobEngine;
            }
        }
        return null;
    }

}
