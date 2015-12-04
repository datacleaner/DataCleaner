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

import java.util.Arrays;
import java.util.Map;

import org.apache.commons.lang.SerializationException;
import org.apache.metamodel.util.HdfsResource;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.job.ComponentJob;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.result.AnalysisResult;
import org.datacleaner.result.save.AnalysisResultSaveHandler;
import org.datacleaner.spark.utils.ResultFilePathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

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
        // get the path of the result file here so that it can fail fast(not
        // after the job has run).
        final String resultJobFilePath = ResultFilePathUtils.getResultFilePath(sparkContext, sparkJobContext);
        logger.info("The result of the job will be written to " + resultJobFilePath);

        final SparkAnalysisRunner sparkAnalysisRunner = new SparkAnalysisRunner(sparkContext, sparkJobContext);
        try {
            final AnalysisResultFuture result = sparkAnalysisRunner.run();
            if (result.isDone()) {
                if (resultJobFilePath != null) {
                    final HdfsResource hdfsResource = new HdfsResource(resultJobFilePath);
                    final AnalysisResultSaveHandler analysisResultSaveHandler = new AnalysisResultSaveHandler(result,
                            hdfsResource);
                    try {
                        analysisResultSaveHandler.saveOrThrow();
                    } catch (SerializationException e) {
                        // attempt to save what we can - and then rethrow
                        final AnalysisResult safeAnalysisResult = analysisResultSaveHandler.createSafeAnalysisResult();
                        if (safeAnalysisResult == null) {
                            logger.error("Serialization of result failed without any safe result elements to persist");
                        } else {
                            final Map<ComponentJob, AnalyzerResult> unsafeResultElements = analysisResultSaveHandler
                                    .getUnsafeResultElements();
                            logger.error("Serialization of result failed with the following unsafe elements: {}",
                                    unsafeResultElements);
                            logger.warn("Partial AnalysisResult will be persisted to filename '{}'", resultJobFilePath);

                            analysisResultSaveHandler.saveWithoutUnsafeResultElements();
                        }

                        // rethrow the exception regardless
                        throw e;
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
