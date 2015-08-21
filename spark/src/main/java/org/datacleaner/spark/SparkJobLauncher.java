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

import java.io.Serializable;
import java.util.List;

import org.apache.metamodel.DataContext;
import org.apache.metamodel.csv.CsvDataContext;
import org.apache.metamodel.util.HdfsResource;
import org.apache.metamodel.util.Resource;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.connection.Datastore;
import org.datacleaner.job.AnalysisJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SparkJobLauncher implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(SparkJobLauncher.class);

    private final DataCleanerConfiguration _dataCleanerConfiguration;
    
    private final String _dataCleanerConfigurationPath;

    public SparkJobLauncher(String confXmlPath) {
        _dataCleanerConfigurationPath = confXmlPath;
        _dataCleanerConfiguration = ConfigurationHelper.readDataCleanerConfiguration(new HdfsResource(confXmlPath));
    }

    public void launchJob(final String analysisJobXmlPath) {
        final AnalysisJob analysisJob = ConfigurationHelper.readAnalysisJob(_dataCleanerConfiguration, new HdfsResource(analysisJobXmlPath));
        
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
                    final Resource resource = csvDataContext.getResource();
                    String datastoreFilePath = resource.getQualifiedPath();
                    
                    SparkConf conf = new SparkConf().setAppName("DataCleaner-spark");
                    sc = new JavaSparkContext(conf);

                    JavaRDD<String> inputRDD = sc.textFile(datastoreFilePath);
                    JavaRDD<InputRow> inputRowRDD = inputRDD.map(new InputRowMapper(_dataCleanerConfigurationPath, analysisJobXmlPath));
                    List<InputRow> inputRows = inputRowRDD.collect();
                    logger.info("Input rows: ");
                    for (InputRow inputRow : inputRows) {
                        logger.info("\tRow id: " + inputRow.getId());
                        for (InputColumn<?> inputColumn: inputRow.getInputColumns()) {
                            logger.info("\t\t" + inputColumn.getName() + ": " + inputRow.getValue(inputColumn));
                        }
                    }
                }
            } finally {
                if (sc != null) {
                    sc.stop();
                }
            }
        }
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
