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
package org.datacleaner.configuration;

import org.datacleaner.job.AnalysisJob;

/**
 * Factory for {@link InjectionManager} instances. Since such instances
 * represent job-level context, they much be created individually for each job
 * execution.
 */
public interface InjectionManagerFactory {

    /**
     * Gets an {@link InjectionManager} (typically new) for the execution of an
     * {@link AnalysisJob}
     * 
     * @param configuration
     * @param job
     * @return
     */
    public InjectionManager getInjectionManager(DataCleanerConfiguration configuration, AnalysisJob job);

    /**
     * Gets an {@link InjectionManager} for a {@link DataCleanerConfiguration}.
     * Since no job is in scope, certain injections may not be possible .
     * 
     * @param configuration
     * @return
     */
    public InjectionManager getInjectionManager(DataCleanerConfiguration configuration);
}
