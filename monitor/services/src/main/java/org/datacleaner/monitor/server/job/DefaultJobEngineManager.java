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
import java.util.Collection;
import java.util.Map;

import org.datacleaner.monitor.job.JobEngine;
import org.datacleaner.monitor.job.JobEngineManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Default {@link JobEngineManager} implementation.
 */
@Component
public class DefaultJobEngineManager extends SimpleJobEngineManager {

    private final ApplicationContext _applicationContext;

    @Autowired
    public DefaultJobEngineManager(ApplicationContext applicationContext) {
        super();
        _applicationContext = applicationContext;
    }

    @Override
    public Collection<JobEngine<?>> getJobEngines() {
        @SuppressWarnings("rawtypes")
        final Map<String, JobEngine> beans = _applicationContext.getBeansOfType(JobEngine.class);
        final Collection<JobEngine<?>> result = new ArrayList<JobEngine<?>>(beans.size());
        for (JobEngine<?> jobEngine : beans.values()) {
            result.add(jobEngine);
        }
        return result;
    }

}
