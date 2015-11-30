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
package org.datacleaner.spark.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import org.apache.hadoop.conf.Configuration;
import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaSparkContext;
import org.datacleaner.spark.SparkJobContext;
import org.datacleaner.util.FileFilters;

public class ResultFilePathUtils {

    private static final Logger logger = Logger.getLogger(ResultFilePathUtils.class);

    private static final String DEFAULT_RESULT_PATH = "/datacleaner/results";
    private static final String RESULT_FILE_EXTENSION = FileFilters.ANALYSIS_RESULT_SER.getExtension();

    /**
     * Gets the hdfs path of job's result. The path can be configured in the job
     * properties file with property 'datacleaner.result.hdfs.path'. The path
     * can be absolute('hdfs://[hostname]:[port]/myresults/myjob.analysis.result.dat') or relative('/myresults/myjob.analysis.result.dat'). If no path is set in the properties file and
     * the system is configured to save the results, the default path will be
     * saved to '/datacleaner/results/[jobname]-[timestamp].analysis.result.dat
     * 
     * @retur
     */
    public static String getResultFilePath(final JavaSparkContext sparkContext, final SparkJobContext sparkJobContext) {
        String resultPath = sparkJobContext.getResultPath();
        final Configuration hadoopConfiguration = sparkContext.hadoopConfiguration();
        /** The default value would be read from hadoop configuration at runtime from core-site.xml. It represents the machine's hostname and port. Example: hdfs://bigdatavm:9000 **/
        final String fileSystemPrefix = hadoopConfiguration.get("fs.defaultFS");
        if (resultPath == null || resultPath.isEmpty()) {
            resultPath = createPath(fileSystemPrefix, DEFAULT_RESULT_PATH);
        } else {
            final URI uri = URI.create(resultPath);
            if (!uri.isAbsolute()) {
                resultPath = createPath(fileSystemPrefix, resultPath);
            }
        }

        if (!resultPath.endsWith(RESULT_FILE_EXTENSION)) {
            final String analysisJobXmlName = sparkJobContext.getAnalysisJobName();
            final Date date = new Date();
            final String filename = analysisJobXmlName + "-" + date.getTime() + RESULT_FILE_EXTENSION;
            final String filePath = createPath(resultPath, filename);
            return filePath;
        }
        return resultPath;
    }

    public static String createPath(final String fileSystemPrefix, String resultPath) {

        final URI uri;
        try {
            URI systemPrefix = new URI(fileSystemPrefix);
            if (!resultPath.startsWith("/")) {
                resultPath = "/" + resultPath;
            }
            uri = new URI("hdfs", null, systemPrefix.getHost(), systemPrefix.getPort(), systemPrefix.getPath()
                    + resultPath, null, null);
            return uri.toString();
        } catch (URISyntaxException e) {
            logger.error("Error while trying to create url for saving the job", e);
            return null;
        }
    }

}
