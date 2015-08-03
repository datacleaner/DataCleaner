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
import org.apache.metamodel.csv.CsvDataContext;
import org.apache.metamodel.util.HdfsResource;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.JaxbConfigurationReader;
import org.datacleaner.connection.Datastore;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.JaxbJobReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SparkJobLauncher {
    
    private static final Logger logger = LoggerFactory.getLogger(SparkJobLauncher.class);

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
            JavaSparkContext sc = null;
            try {
                DataContext dataContext = datastore.openConnection().getDataContext(); 
                if (dataContext instanceof CsvDataContext) {
                    CsvDataContext csvDataContext = (CsvDataContext) dataContext;
                    String datastoreFilePath = csvDataContext.getResource().getQualifiedPath();
            
                    SparkConf conf = new SparkConf().setAppName("DataCleaner-spark");
                    sc = new JavaSparkContext(conf);

                    JavaRDD<String> instructionsRDD = sc.textFile(datastoreFilePath);
                    long lineCount = instructionsRDD.count();
        
                    logger.info("Line count: " + lineCount);
                }
            } finally {
                if (sc != null) {
                    sc.stop();
                }
            }
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
    
    public static void main(String[] args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("The number of arguments is incorrect. Usage:\n <path_to_conf_xml_file_in_hdfs> <path_to_analysis_job_xml_in_hdfs>");
        }
        
        String confXmlPath = args[0];
        String analysisJobXmlPath = args[1];
        SparkJobLauncher sparkJobLauncher = new SparkJobLauncher(confXmlPath);
        sparkJobLauncher.launchJob(analysisJobXmlPath);
    }

}
