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

import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;

import org.apache.commons.lang.SerializationException;
import org.apache.commons.lang.SerializationUtils;
import org.apache.log4j.Logger;
import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.HdfsResource;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.job.ComponentJob;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.result.SimpleAnalysisResult;
import org.datacleaner.spark.utils.ResultFilePathUtils;

public class Main {

    private static final Logger logger = Logger.getLogger(Main.class);
  

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            throw new IllegalArgumentException("The number of arguments is incorrect. Usage:\n"
                    + " <configuration file (conf.xml) path> <job file (.analysis.xml) path> [properties file path]\n"
                    + "Got: " + Arrays.toString(args));
        }

        final SparkConf conf = new SparkConf().setAppName("DataCleaner-spark");
        final JavaSparkContext sparkContext = new JavaSparkContext(conf);

        final String confXmlPath = args[0];
        final String analysisJobXmlPath = args[1];

        final String propertiesPath;
        if (args.length > 2) {
            propertiesPath = args[2];
        } else {
            propertiesPath = null;
        }

        final SparkJobContext sparkJobContext = new SparkJobContext(sparkContext, confXmlPath, analysisJobXmlPath,
                propertiesPath);
        
        // get the path of the result file here so that it can fail fast(not after the job has run). 
        final String resultJobFilePath = ResultFilePathUtils.getResultFilePath(sparkContext, sparkJobContext);
        logger.info("The result of the job will be written to " + resultJobFilePath);
        final SparkAnalysisRunner sparkAnalysisRunner = new SparkAnalysisRunner(sparkContext, sparkJobContext);
        try {
            final AnalysisResultFuture result = sparkAnalysisRunner.run();
            if (result.isDone()) {
                final Map<ComponentJob, AnalyzerResult> resultMap = result.getResultMap();
                final SimpleAnalysisResult simpleAnalysisResult = new SimpleAnalysisResult(resultMap,
                        result.getCreationDate());
                if (resultJobFilePath != null) {
                    final HdfsResource hdfsResource = new HdfsResource(resultJobFilePath);
                    final OutputStream out = hdfsResource.write();
                    try {
                        SerializationUtils.serialize(simpleAnalysisResult, out);
                    } catch (SerializationException e) {
                        logger.error("Error while trying to serialize the job");
                        throw e;
                    } finally {
                        logger.info("The result of the job was written to " + resultJobFilePath);
                        FileHelper.safeClose(out);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Exception " + e.getStackTrace());
            throw e;
        } finally {
            sparkContext.stop();
        }
    }

   
}
