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

import org.apache.metamodel.util.HdfsResource;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.JaxbConfigurationReader;
import org.datacleaner.connection.Datastore;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.JaxbJobReader;

public class SparkJobLauncher {

    private final DataCleanerConfiguration _dataCleanerConfiguration;

    public SparkJobLauncher(DataCleanerConfiguration dataCleanerConfiguration) {
        _dataCleanerConfiguration = dataCleanerConfiguration;
    }
    
    public SparkJobLauncher(String confXmlPath) {
        _dataCleanerConfiguration = readDataCleanerConfiguration(new HdfsResource(confXmlPath));
    }
    
    public void launchJob(AnalysisJob analysisJob) {
        final String datastoreName = analysisJob.getDatastore().getName();

        final Datastore datastore = _dataCleanerConfiguration.getDatastoreCatalog().getDatastore(datastoreName);
        if (datastore == null) {
            throw new IllegalStateException("Datastore referred by the job (" + datastoreName
                    + ") has not been found in the specified DataCleanerConfiguration");
        } else {
            // TODO: Initialize JavaSparkContext and read the input datastore file
        }
    }

    public void launchJob(String analysisJobXmlPath) {
        final AnalysisJob analysisJob = readAnalysisJob(new HdfsResource(analysisJobXmlPath));
        launchJob(analysisJob);
    }

    private static DataCleanerConfiguration readDataCleanerConfiguration(HdfsResource confXmlHdfsResource) {
        final InputStream confXmlInputStream = confXmlHdfsResource.read();
        final JaxbConfigurationReader confReader = new JaxbConfigurationReader();
        final DataCleanerConfiguration dataCleanerConfiguration = confReader.create(confXmlInputStream);

        return dataCleanerConfiguration;
    }

    private AnalysisJob readAnalysisJob(HdfsResource analysisJobXmlHdfsResource) {
        final InputStream analysisJobXmlInputStream = analysisJobXmlHdfsResource.read();
        final JaxbJobReader jobReader = new JaxbJobReader(_dataCleanerConfiguration);
        final AnalysisJob analysisJob = jobReader.read(analysisJobXmlInputStream);

        return analysisJob;
    }
    
    public DataCleanerConfiguration getDataCleanerConfiguration() {
        return _dataCleanerConfiguration;
    }

}
