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

import java.io.InputStream;

import org.apache.metamodel.DataContext;
import org.apache.metamodel.csv.CsvConfiguration;
import org.apache.metamodel.csv.CsvDataContext;
import org.apache.metamodel.util.Func;
import org.apache.metamodel.util.HdfsResource;
import org.apache.spark.api.java.function.Function;
import org.datacleaner.api.InputRow;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.JaxbConfigurationReader;
import org.datacleaner.connection.Datastore;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.JaxbJobReader;

public final class InputRowMapper implements Function<String, InputRow> {
    private static final long serialVersionUID = 1L;

    private final String _dataCleanerConfigurationPath;
    private final String _analysisJobXmlPath;

    private transient DataCleanerConfiguration _dataCleanerConfiguration;
    private transient AnalysisJob _analysisJob;

    private final CsvConfiguration _csvConfiguration;

    public InputRowMapper(final String dataCleanerConfigurationPath, final String analysisJobXmlPath) {
        _dataCleanerConfigurationPath = dataCleanerConfigurationPath;
        _analysisJobXmlPath = analysisJobXmlPath;

        // _dataCleanerConfiguration = readDataCleanerConfiguration(new
        // HdfsResource(dataCleanerConfigurationPath));
        // _analysisJob = readAnalysisJob(new HdfsResource(analysisJobXmlPath));
        //
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
        // TODO: Check if AnalysisJob is read once per worker or with every
        // record in the map operation
        if (_analysisJob == null) {
            _analysisJob = readAnalysisJob(new HdfsResource(_analysisJobXmlPath));
        }
        return _analysisJob;
    }

    private DataCleanerConfiguration getDataCleanerConfiguration() {
        // TODO: Check if DataCleanerConfiguration is read once per worker or
        // with every record in the map operation
        if (_dataCleanerConfiguration == null) {
            _dataCleanerConfiguration = readDataCleanerConfiguration(new HdfsResource(_dataCleanerConfigurationPath));
        }
        return _dataCleanerConfiguration;
    }

    private static DataCleanerConfiguration readDataCleanerConfiguration(HdfsResource confXmlHdfsResource) {
        final InputStream confXmlInputStream = confXmlHdfsResource.read();
        final JaxbConfigurationReader confReader = new JaxbConfigurationReader();
        final DataCleanerConfiguration dataCleanerConfiguration = confReader.create(confXmlInputStream);

        return dataCleanerConfiguration;
    }

    private AnalysisJob readAnalysisJob(HdfsResource analysisJobXmlHdfsResource) {
        final AnalysisJob analysisJob = analysisJobXmlHdfsResource.read(new Func<InputStream, AnalysisJob>() {

            @Override
            public AnalysisJob eval(InputStream in) {
                final JaxbJobReader jobReader = new JaxbJobReader(getDataCleanerConfiguration());
                final AnalysisJob analysisJob = jobReader.read(in);
                return analysisJob;
            }
        });
        return analysisJob;
    }
}