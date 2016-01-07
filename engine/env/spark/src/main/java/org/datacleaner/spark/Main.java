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

import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.ServiceLoader;

import org.apache.commons.lang.SerializationException;
import org.apache.metamodel.util.Resource;
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

        final URI confXmlPath = URI.create(args[0]);
        final URI analysisJobXmlPath = URI.create(args[1]);

        final URI propertiesPath;
        if (args.length > 2) {
            propertiesPath = URI.create(args[2]);
        } else {
            propertiesPath = null;
        }

        final SparkJobContext sparkJobContext = new SparkJobContext(confXmlPath, analysisJobXmlPath, propertiesPath,
                sparkContext);

        final ServiceLoader<SparkJobLifeCycleListener> listenerLoaders =
                ServiceLoader.load(SparkJobLifeCycleListener.class);

        for (SparkJobLifeCycleListener listener : listenerLoaders) {
            sparkJobContext.addSparkJobLifeCycleListener(listener);
        }

        final SparkAnalysisRunner sparkAnalysisRunner = new SparkAnalysisRunner(sparkContext, sparkJobContext);
        try {
            final AnalysisResultFuture result = sparkAnalysisRunner.run();

            result.await();

            if (sparkJobContext.isResultEnabled()) {
                final Resource resultResource = ResultFilePathUtils.getResultResource(sparkContext, sparkJobContext);
                logger.info("DataCleaner result will be written to: {}", resultResource);
                saveResult(result, resultResource);
            } else {
                logger.info("DataCleaner result will not be written - disabled");
            }
        } finally {
            sparkContext.stop();
        }
    }

    private static void saveResult(AnalysisResultFuture result, Resource resultResource) {
        final AnalysisResultSaveHandler analysisResultSaveHandler = new AnalysisResultSaveHandler(result,
                resultResource);
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
                logger.warn("Partial AnalysisResult will be persisted to filename '{}'",
                        resultResource.getQualifiedPath());

                analysisResultSaveHandler.saveWithoutUnsafeResultElements();
            }

            // rethrow the exception regardless
            throw e;
        }
    }
}
