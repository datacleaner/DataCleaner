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
package org.datacleaner.beans.writers;

import org.datacleaner.beans.api.Distributed;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.connection.FileDatastore;
import org.datacleaner.result.AnalyzerResult;
import org.datacleaner.result.Metric;
import org.apache.metamodel.schema.Table;

/**
 * Represents the result of a Writer analyzer (see {@link WriteDataCategory}).
 * The result will not be an analysis result as such, but a pointer to a written
 * dataset.
 * 
 * 
 */
@Distributed(reducer = WriteDataResultReducer.class)
public interface WriteDataResult extends AnalyzerResult {
    
    /**
     * @return the amount of rows that was written.
     */
    @Metric("Inserts")
    public int getWrittenRowCount();

    /**
     * @return the amount of updates that was executed.
     */
    @Metric("Updates")
    public int getUpdatesCount();

    /**
     * Gets the amount of rows that was errornuosly not written. This will only
     * be non-zero if a error handling strategy has been specified, typically
     * using {@link ErrorHandlingOption}.
     * 
     * @return the amount of rows that was not written.
     */
    @Metric("Errornous rows")
    public int getErrorRowCount();

    /**
     * Gets a reference to a datastore containing error records. Note that the
     * datastore is not nescesarily registered in the {@link DatastoreCatalog}.
     * 
     * @return a {@link FileDatastore} reference or null if no errors occurred.
     */
    public FileDatastore getErrorDatastore();

    /**
     * @param datastoreCatalog
     *            the datastore catalog that the user has configured.
     * @return a datastore that can be used to access the target destination, or
     *         null of it is not available (eg. destination not reachable or no
     *         rows written).
     */
    public Datastore getDatastore(DatastoreCatalog datastoreCatalog);

    /**
     * @param datastore
     *            the datastore that was returned by
     *            {@link #getDatastore(DatastoreCatalog)}.
     * @return a table that can be used for previewing the data written.
     */
    public Table getPreviewTable(Datastore datastore);

}
