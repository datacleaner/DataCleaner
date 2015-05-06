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
package org.datacleaner.api;

import org.apache.metamodel.query.Query;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.HasName;
import org.datacleaner.connection.PerformanceCharacteristics;

/**
 * Interface that describes the metadata of an output data set. See
 * {@link HasOutputDataSets} for details on how the metadata relates to actual
 * data.
 */
public interface OutputDataSet extends HasName {

    /**
     * Gets the name of the output data set, as presented to the user and
     * referenced to in analysis job files etc.
     */
    @Override
    public String getName();

    /**
     * Gets the logical {@link Table} objects that represent the format of the
     * data that will be made available by the {@link HasOutputDataSets}
     * 
     * @return
     */
    public Table getTable();

    /**
     * Gets performance characteristics of the output data set. This may
     * influence the {@link Query} posted to consume the data. See
     * {@link HasOutputDataSets#initializeOutputDataSet(OutputDataSetMetadata, org.apache.metamodel.query.Query, OutputRowCollector)}
     * for details on usage.
     * 
     * @return
     */
    public PerformanceCharacteristics getPerformanceCharacteristics();
}
