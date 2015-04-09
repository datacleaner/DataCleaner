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

import java.io.Serializable;

import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.reference.ReferenceDataCatalog;

/**
 * Represents the configuration of a DataCleaner instance. Usually there is just
 * one configuration active but it is possible to have multiple configurations,
 * for instance within the same {@link DataCleanerEnvironment}.
 */
public interface DataCleanerConfiguration extends Serializable {

    /**
     * @see DatastoreCatalog
     * @return the datastore catalog defined in this configuration
     */
    public DatastoreCatalog getDatastoreCatalog();

    /**
     * @see ReferenceDataCatalog
     * @return the reference data catalog defined in this configuration
     */
    public ReferenceDataCatalog getReferenceDataCatalog();

    /**
     * Gets the home folder of this configuration.
     * 
     * @return
     */
    public DataCleanerHomeFolder getHomeFolder();

    /**
     * Gets the {@link DataCleanerEnvironment} that this configuration refers
     * to.
     * 
     * @return
     */
    public DataCleanerEnvironment getEnvironment();
}
