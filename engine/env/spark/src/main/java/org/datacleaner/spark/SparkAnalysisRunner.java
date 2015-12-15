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

import java.util.Collections;
import java.util.List;

import org.apache.metamodel.csv.CsvConfiguration;
import org.apache.metamodel.util.Resource;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.InputRow;
import org.datacleaner.connection.CsvDatastore;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.JsonDatastore;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.job.runner.AnalysisRunner;
import org.datacleaner.spark.functions.AnalyzerResultReduceFunction;
import org.datacleaner.spark.functions.CsvParserFunction;
import org.datacleaner.spark.functions.ExtractAnalyzerResultFunction;
import org.datacleaner.spark.functions.JsonParserFunction;
import org.datacleaner.spark.functions.RowProcessingFunction;
import org.datacleaner.spark.functions.TuplesToTuplesFunction;
import org.datacleaner.spark.functions.ValuesToInputRowFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.Tuple2;

public class SparkAnalysisRunner implements AnalysisRunner {

    private static final Logger logger = LoggerFactory.getLogger(SparkAnalysisRunner.class);

    private final SparkJobContext _sparkJobContext;
    private final JavaSparkContext _sparkContext;

    private final Integer _minPartitions;

    public SparkAnalysisRunner(JavaSparkContext sparkContext, SparkJobContext sparkJobContext) {
        this(sparkContext, sparkJobContext, null);
    }

    public SparkAnalysisRunner(JavaSparkContext sparkContext, SparkJobContext sparkJobContext, Integer minPartitions) {
        _sparkContext = sparkContext;
        _sparkJobContext = sparkJobContext;
        if (minPartitions != null) {
            if (minPartitions > 0) {
                _minPartitions = minPartitions;
            } else {
                logger.warn(
                        "Minimum number of partitions needs to be a positive number, but specified: {}. Disregarding the value and inferring the number of partitions automatically",
                        minPartitions);
                _minPartitions = null;
            }
        } else {
            _minPartitions = null;
        }
    }

    @Override
    public AnalysisResultFuture run(AnalysisJob job) {
        return run();
    }

    public AnalysisResultFuture run() {
        _sparkJobContext.triggerOnJobStart();
        final AnalysisJob analysisJob = _sparkJobContext.getAnalysisJob();
        final Datastore datastore = analysisJob.getDatastore();

        final JavaRDD<InputRow> inputRowsRDD = openSourceDatastore(datastore);

        final JavaPairRDD<String, NamedAnalyzerResult> namedAnalyzerResultsRDD;
        if (_sparkJobContext.getAnalysisJobBuilder().isDistributable()) {
            logger.info("Running the job in distributed mode");

            // TODO: We have yet to get more experience with this setting - do a
            // benchmark of what works best, true or false.
            final boolean preservePartitions = true;

            final JavaRDD<Tuple2<String, NamedAnalyzerResult>> processedTuplesRdd = inputRowsRDD
                    .mapPartitionsWithIndex(new RowProcessingFunction(_sparkJobContext), preservePartitions);
            
            if (_sparkJobContext.isResultEnabled()) {
                final JavaPairRDD<String, NamedAnalyzerResult> partialNamedAnalyzerResultsRDD = processedTuplesRdd
                        .mapPartitionsToPair(new TuplesToTuplesFunction<String, NamedAnalyzerResult>(), preservePartitions);
                
                namedAnalyzerResultsRDD = partialNamedAnalyzerResultsRDD.reduceByKey(new AnalyzerResultReduceFunction(
                        _sparkJobContext));
            } else {
                // call count() to block and wait for RDD to be fully processed
                processedTuplesRdd.count();
                namedAnalyzerResultsRDD = null;
            }
        } else {
            logger.warn("Running the job in non-distributed mode");
            final JavaRDD<InputRow> coalescedInputRowsRDD = inputRowsRDD.coalesce(1);
            namedAnalyzerResultsRDD = coalescedInputRowsRDD.mapPartitionsToPair(new RowProcessingFunction(
                    _sparkJobContext));
            
            if (!_sparkJobContext.isResultEnabled()) {
                // call count() to block and wait for RDD to be fully processed
                namedAnalyzerResultsRDD.count();
            }
        }
        
        if (!_sparkJobContext.isResultEnabled()) {
            final List<Tuple2<String, AnalyzerResult>> results = Collections.emptyList();
            return new SparkAnalysisResultFuture(results, _sparkJobContext);
        }

        assert namedAnalyzerResultsRDD != null;
        final JavaPairRDD<String, AnalyzerResult> finalAnalyzerResultsRDD = namedAnalyzerResultsRDD
                .mapValues(new ExtractAnalyzerResultFunction());

        // log analyzer results
        final List<Tuple2<String, AnalyzerResult>> results = finalAnalyzerResultsRDD.collect();

        logger.info("Finished! Number of AnalyzerResult objects: {}", results.size());
        for (Tuple2<String, AnalyzerResult> analyzerResultTuple : results) {
            final String key = analyzerResultTuple._1;
            final AnalyzerResult result = analyzerResultTuple._2;
            logger.info("AnalyzerResult (" + key + "):\n\n" + result + "\n");
        }

        _sparkJobContext.triggerOnJobEnd();
        return new SparkAnalysisResultFuture(results, _sparkJobContext);
    }

    private JavaRDD<InputRow> openSourceDatastore(Datastore datastore) {
        if (datastore instanceof CsvDatastore) {
            final CsvDatastore csvDatastore = (CsvDatastore) datastore;
            final Resource resource = csvDatastore.getResource();
            assert resource != null;
            final String datastorePath = resource.getQualifiedPath();

            final CsvConfiguration csvConfiguration = csvDatastore.getCsvConfiguration();

            final JavaRDD<String> rawInput;
            if (_minPartitions != null) {
                rawInput = _sparkContext.textFile(datastorePath, _minPartitions);
            } else {
                rawInput = _sparkContext.textFile(datastorePath);
            }
            final JavaRDD<Object[]> parsedInput = rawInput.map(new CsvParserFunction(csvConfiguration));

            JavaPairRDD<Object[], Long> zipWithIndex = parsedInput.zipWithIndex();

            if (csvConfiguration.getColumnNameLineNumber() != CsvConfiguration.NO_COLUMN_NAME_LINE) {
                zipWithIndex = zipWithIndex.filter(new SkipHeaderLineFunction(csvConfiguration
                        .getColumnNameLineNumber()));
            }

            final JavaRDD<InputRow> inputRowsRDD = zipWithIndex.map(new ValuesToInputRowFunction(_sparkJobContext));

            return inputRowsRDD;
        } else if (datastore instanceof JsonDatastore) {
            final JsonDatastore jsonDatastore = (JsonDatastore) datastore;
            final String datastorePath = jsonDatastore.getResource().getQualifiedPath();
            final JavaRDD<String> rawInput;
            if (_minPartitions != null) {
                rawInput = _sparkContext.textFile(datastorePath, _minPartitions);
            } else {
                rawInput = _sparkContext.textFile(datastorePath);
            }

            final JavaRDD<Object[]> parsedInput = rawInput.map(new JsonParserFunction(jsonDatastore));
            final JavaPairRDD<Object[], Long> zipWithIndex = parsedInput.zipWithIndex();
            final JavaRDD<InputRow> inputRowsRDD = zipWithIndex.map(new ValuesToInputRowFunction(_sparkJobContext));
            return inputRowsRDD;
        }

        throw new UnsupportedOperationException("Unsupported datastore type or configuration: " + datastore);
    }
}
