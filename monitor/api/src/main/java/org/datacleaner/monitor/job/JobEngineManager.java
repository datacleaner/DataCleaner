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
package org.datacleaner.monitor.job;

import java.util.Collection;

import org.datacleaner.monitor.configuration.TenantContext;

/**
 * Manager component of all {@link JobEngine}s in the application.
 */
public interface JobEngineManager {

    /**
     * Gets all {@link JobEngine}s in the application.
     *
     * @return
     */
    Collection<JobEngine<?>> getJobEngines();

    JobEngine<?> getJobEngine(TenantContext tenantContext, String jobName);

    <T extends JobContext> JobEngine<? extends T> getJobEngine(T jobContext);

    <T extends JobContext> JobEngine<? extends T> getJobEngine(Class<T> jobContext);

    <E extends JobEngine<?>> E getJobEngineOfType(Class<E> class1);
}
