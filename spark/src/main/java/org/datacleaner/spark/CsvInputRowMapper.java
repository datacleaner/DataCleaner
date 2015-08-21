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
package org.datacleaner.spark;

import org.apache.metamodel.DataContext;
import org.apache.metamodel.csv.CsvConfiguration;
import org.apache.metamodel.csv.CsvDataContext;
import org.apache.spark.api.java.function.Function;
import org.datacleaner.api.InputRow;
import org.datacleaner.connection.Datastore;

public final class CsvInputRowMapper extends AbstractSparkDataCleanerAction implements Function<String, InputRow> {
    private static final long serialVersionUID = 1L;

    private final CsvConfiguration _csvConfiguration;

    public CsvInputRowMapper(final SparkDataCleanerContext sparkDataCleanerContext) {
        super(sparkDataCleanerContext);
        
        final String datastoreName = getAnalysisJob().getDatastore().getName();

        final Datastore datastore = getDataCleanerConfiguration().getDatastoreCatalog().getDatastore(datastoreName);
        if (datastore == null) {
            throw new IllegalStateException("Datastore referred by the job (" + datastoreName
                    + ") has not been found in the specified DataCleanerConfiguration");
        }

        DataContext dataContext = datastore.openConnection().getDataContext();
        if (dataContext instanceof CsvDataContext) {
            CsvDataContext csvDataContext = (CsvDataContext) dataContext;
            _csvConfiguration = csvDataContext.getConfiguration();
        } else {
            throw new IllegalArgumentException("Only CSV datastores are supported");
        }
    }

    @Override
    public InputRow call(String csvLine) throws Exception {
        InputRow inputRow = CsvParser.prepareInputRow(getAnalysisJob().getSourceColumns(), _csvConfiguration, csvLine);
        return inputRow;
    }

}