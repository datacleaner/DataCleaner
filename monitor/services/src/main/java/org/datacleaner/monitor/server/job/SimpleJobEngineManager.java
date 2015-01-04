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
package org.datacleaner.monitor.server.job;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.datacleaner.util.ReflectionUtils;
import org.datacleaner.monitor.configuration.TenantContext;
import org.datacleaner.monitor.job.JobContext;
import org.datacleaner.monitor.job.JobEngine;
import org.datacleaner.monitor.job.JobEngineManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple {@link JobEngineManager} implementation
 */
public class SimpleJobEngineManager implements JobEngineManager {

    private static final Logger logger = LoggerFactory.getLogger(SimpleJobEngineManager.class);

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
            final Class<?> jobEngineTypeParameter = ReflectionUtils.getTypeParameter(jobEngine.getClass(),
                    JobEngine.class, 0);
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
        if (jobEngines.isEmpty()) {
            logger.warn("No job engines has been configured, thus no engine found for job: {}", jobName);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E extends JobEngine<?>> E getJobEngineOfType(Class<E> cls) {
        if (cls == null) {
            throw new IllegalArgumentException("JobEngine class cannot be null");
        }
        
        Collection<JobEngine<?>> jobEngines = getJobEngines();
        
        for (JobEngine<?> engine : jobEngines) {
            // look for exact matching classes
            if (engine.getClass() == cls) {
                return (E) engine;
            }
        }
        for (JobEngine<?> engine : jobEngines) {
            // take any sub class
            if (ReflectionUtils.is(engine.getClass(), cls)) {
                return (E) engine;
            }
        }
        
        // build meaningful error message
        final List<String> types = new ArrayList<String>();
        for (JobEngine<?> engine : jobEngines) {
            types.add(engine.getClass().getName());
        }
        throw new UnsupportedOperationException("No job engine available of type: " + cls.getName()
                + ". Available job engine types are: " + types);
    }

}
