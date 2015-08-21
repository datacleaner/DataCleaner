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
import org.apache.metamodel.util.HdfsResource;
import org.apache.spark.Accumulator;
import org.apache.spark.api.java.function.Function;
import org.datacleaner.api.InputRow;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.connection.Datastore;
import org.datacleaner.job.AnalysisJob;

public final class InputRowMapper implements Function<String, InputRow> {
    private static final long serialVersionUID = 1L;

    private final SparkDataCleanerContext _sparkDataCleanerContext;

    private transient DataCleanerConfiguration _dataCleanerConfiguration;
    private transient AnalysisJob _analysisJob;
    
    private final CsvConfiguration _csvConfiguration;

    public InputRowMapper(final SparkDataCleanerContext sparkDataCleanerContext) {
        _sparkDataCleanerContext = sparkDataCleanerContext;
        
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

    private AnalysisJob getAnalysisJob() {
        if (_analysisJob == null) {
            @SuppressWarnings("unchecked")
            final Accumulator<Integer> dataCleanerConfigurationReadsCounter = (Accumulator<Integer>) _sparkDataCleanerContext
                    .getAccumulator("AnalysisJob reads counter");
            dataCleanerConfigurationReadsCounter.add(1);
            _analysisJob = ConfigurationHelper.readAnalysisJob(getDataCleanerConfiguration(), new HdfsResource(
                    _sparkDataCleanerContext.getAnalysisJobXmlPath()));
        }
        return _analysisJob;
    }

    private DataCleanerConfiguration getDataCleanerConfiguration() {
        if (_dataCleanerConfiguration == null) {
            @SuppressWarnings("unchecked")
            final Accumulator<Integer> analysisJobReadsCounter = (Accumulator<Integer>) _sparkDataCleanerContext
                    .getAccumulator("DataCleanerConfigurationConfiguration reads counter");
            analysisJobReadsCounter.add(1);
            _dataCleanerConfiguration = ConfigurationHelper.readDataCleanerConfiguration(new HdfsResource(
                    _sparkDataCleanerContext.getDataCleanerConfigurationPath()));
        }
        return _dataCleanerConfiguration;
    }

}