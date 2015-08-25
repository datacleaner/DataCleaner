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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.AnalyzerResultFuture;
import org.datacleaner.api.HasAnalyzerResult;
import org.datacleaner.api.InputRow;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.ComponentJob;
import org.datacleaner.job.runner.ConsumeRowHandler;
import org.datacleaner.job.runner.RowProcessingConsumer;
import org.datacleaner.lifecycle.LifeCycleHelper;

import scala.Tuple2;

public final class RowProcessingFunction implements
        PairFlatMapFunction<Iterator<InputRow>, String, Tuple2<AnalyzerResult, ComponentJob>> {

    private static final long serialVersionUID = 1L;
    private final SparkJobContext _sparkJobContext;

    public RowProcessingFunction(final SparkJobContext sparkJobContext) {
        _sparkJobContext = sparkJobContext;
    }

    @Override
    public Iterable<Tuple2<String, Tuple2<AnalyzerResult, ComponentJob>>> call(Iterator<InputRow> inputRowIterator)
            throws Exception {
        final DataCleanerConfiguration configuration = _sparkJobContext.getConfiguration();
        final AnalysisJob analysisJob = _sparkJobContext.getAnalysisJob();

        // set up processing stream (this also initializes the components)
        final ConsumeRowHandler consumeRowHandler;
        {
            final ConsumeRowHandler.Configuration handlerConfiguration = new ConsumeRowHandler.Configuration();
            handlerConfiguration.includeAnalyzers = true;
            handlerConfiguration.includeNonDistributedTasks = false;
            consumeRowHandler = new ConsumeRowHandler(analysisJob, configuration, handlerConfiguration);
        }

        // fire row processing on each row
        while (inputRowIterator.hasNext()) {
            final InputRow inputRow = inputRowIterator.next();
            consumeRowHandler.consumeRow(inputRow);
        }

        // collect results
        final List<Tuple2<String, Tuple2<AnalyzerResult, ComponentJob>>> analyzerResults = new ArrayList<>();
        for (RowProcessingConsumer consumer : consumeRowHandler.getConsumers()) {
            if (consumer.isResultProducer()) {
                final HasAnalyzerResult<?> resultProducer = (HasAnalyzerResult<?>) consumer.getComponent();
                final AnalyzerResult analyzerResult = resultProducer.getResult();
                final String key = _sparkJobContext.getComponentKey(consumer.getComponentJob());
                final Tuple2<AnalyzerResult, ComponentJob> analyzerResultTuple = new Tuple2<AnalyzerResult, ComponentJob>(
                        analyzerResult, consumer.getComponentJob());
                final Tuple2<String, Tuple2<AnalyzerResult, ComponentJob>> tuple = new Tuple2<>(key,
                        analyzerResultTuple);
                analyzerResults.add(tuple);
            }
        }

        // await any future results
        for (ListIterator<Tuple2<String, Tuple2<AnalyzerResult, ComponentJob>>> it = analyzerResults.listIterator(); it
                .hasNext();) {
            final Tuple2<String, Tuple2<AnalyzerResult, ComponentJob>> tuple = it.next();
            final Tuple2<AnalyzerResult, ComponentJob> analyzerResultTuple = tuple._2;
            final AnalyzerResult analyzerResult = analyzerResultTuple._1;
            if (analyzerResult instanceof AnalyzerResultFuture) {
                final AnalyzerResult awaitedResult = ((AnalyzerResultFuture<?>) analyzerResult).get();
                final Tuple2<AnalyzerResult, ComponentJob> awaitedResultTuple = new Tuple2<AnalyzerResult, ComponentJob>(
                        awaitedResult, analyzerResultTuple._2);
                it.set(new Tuple2<>(tuple._1, awaitedResultTuple));
            }
        }

        // close components
        final LifeCycleHelper lifeCycleHelper = new LifeCycleHelper(configuration, analysisJob, false);
        for (RowProcessingConsumer consumer : consumeRowHandler.getConsumers()) {
            lifeCycleHelper.close(consumer.getComponentJob().getDescriptor(), consumer.getComponent(), true);
        }

        return analyzerResults;
    }
}