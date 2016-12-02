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

import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.descriptors.DescriptorProvider;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.concurrent.TaskRunner;
import org.datacleaner.reference.ReferenceDataCatalog;
import org.datacleaner.storage.StorageProvider;

/**
 * Represents the configuration of the application. The configuration can
 * provide all the needed providers and catalogs used by DataCleaner to
 * configure and execute jobs.
 *
 * @deprecated use {@link DataCleanerConfiguration} and/or
 *             {@link DataCleanerEnvironment} instead.
 */
@Deprecated
public interface AnalyzerBeansConfiguration extends DataCleanerConfiguration, DataCleanerEnvironment {

    /**
     * @see DatastoreCatalog
     * @return the datastore catalog defined in this configuration
     */
    @Override
    DatastoreCatalog getDatastoreCatalog();

    /**
     * @see ReferenceDataCatalog
     * @return the reference data catalog defined in this configuration
     */
    @Override
    ReferenceDataCatalog getReferenceDataCatalog();

    /**
     * @see DescriptorProvider
     * @return the descriptor provider defined in this configuration
     */
    @Override
    DescriptorProvider getDescriptorProvider();

    /**
     * @see StorageProvider
     * @return the storage provider defined in this configuration
     */
    @Override
    StorageProvider getStorageProvider();

    /**
     * @see InjectionManager
     * @return an injection manager for the job
     */
    InjectionManager getInjectionManager(AnalysisJob job);

    /**
     * @see TaskRunner
     * @return the task runner defined in this configuration
     */
    @Override
    TaskRunner getTaskRunner();
}
