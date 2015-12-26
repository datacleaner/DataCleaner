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
package org.datacleaner.spark.functions;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.apache.metamodel.csv.CsvConfiguration;
import org.apache.metamodel.util.FileResource;
import org.apache.metamodel.util.HdfsResource;
import org.apache.metamodel.util.Resource;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.AnalyzerResultFuture;
import org.datacleaner.api.HasAnalyzerResult;
import org.datacleaner.api.InputRow;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.connection.CsvDatastore;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.JsonDatastore;
import org.datacleaner.connection.ResourceDatastore;
import org.datacleaner.connection.UpdateableDatastore;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.extension.output.CreateCsvFileAnalyzer;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.job.runner.ActiveOutputDataStream;
import org.datacleaner.job.runner.ConsumeRowHandler;
import org.datacleaner.job.runner.RowProcessingConsumer;
import org.datacleaner.lifecycle.LifeCycleHelper;
import org.datacleaner.spark.NamedAnalyzerResult;
import org.datacleaner.spark.SparkAnalysisRunner;
import org.datacleaner.spark.SparkJobContext;
import org.datacleaner.spark.utils.HdfsHelper;
import org.datacleaner.util.HadoopResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.Tuple2;

/**
 * The main Spark function which applies the DataCleaner row processing
 * framework onto RDDs of InputRows.
 * 
 * The main vehicle used to do this is the {@link ConsumeRowHandler}.
 * 
 * This class implements two interfaces because it has two (quite similar)
 * styles of usages in the {@link SparkAnalysisRunner}.
 */
public final class RowProcessingFunction implements
        Function2<Integer, Iterator<InputRow>, Iterator<Tuple2<String, NamedAnalyzerResult>>>,
        PairFlatMapFunction<Iterator<InputRow>, String, NamedAnalyzerResult> {

    private static final Logger logger = LoggerFactory.getLogger(RowProcessingFunction.class);

    private static final long serialVersionUID = 1L;
    private final SparkJobContext _sparkJobContext;

    public RowProcessingFunction(final SparkJobContext sparkJobContext) {
        _sparkJobContext = sparkJobContext;
    }

    @Override
    public Iterable<Tuple2<String, NamedAnalyzerResult>> call(Iterator<InputRow> inputRowIterator) throws Exception {
        logger.info("call(Iterator) invoked");

        final AnalysisJob analysisJob = _sparkJobContext.getAnalysisJob();
        final List<Tuple2<String, NamedAnalyzerResult>> analyzerResults = executePartition(inputRowIterator,
                analysisJob);

        logger.info("call(Iterator) finished, returning {} results", analyzerResults.size());

        return analyzerResults;
    }

    @Override
    public Iterator<Tuple2<String, NamedAnalyzerResult>> call(Integer partitionNumber,
            Iterator<InputRow> inputRowIterator) throws Exception {
        logger.info("call({}, Iterator) invoked", partitionNumber);

        final AnalysisJobBuilder jobBuilder = _sparkJobContext.getAnalysisJobBuilder();

        configureComponentsBeforeBuilding(jobBuilder, partitionNumber.intValue());

        final AnalysisJob analysisJob = jobBuilder.toAnalysisJob();

        final List<Tuple2<String, NamedAnalyzerResult>> analyzerResults = executePartition(inputRowIterator,
                analysisJob);

        logger.info("call({}, Iterator) finished, returning {} results", partitionNumber, analyzerResults.size());

        return analyzerResults.iterator();
    }

    /**
     * Applies any partition-specific configuration to the job builder before
     * building it.
     * 
     * @param jobBuilder
     * @param partitionNumber
     */
    private void configureComponentsBeforeBuilding(AnalysisJobBuilder jobBuilder, int partitionNumber) {
        // update datastores and resource properties to point to node-specific
        // targets if possible. This way parallel writing to files on HDFS does
        // not cause any inconsistencies because each node is writing to a
        // separate file.
        for (final ComponentBuilder cb : jobBuilder.getComponentBuilders()) {
            // find any datastore properties that point to HDFS files
            final Set<ConfiguredPropertyDescriptor> targetDatastoreProperties = cb.getDescriptor()
                    .getConfiguredPropertiesByType(UpdateableDatastore.class, false);
            for (final ConfiguredPropertyDescriptor targetDatastoreProperty : targetDatastoreProperties) {
                final Object datastoreObject = cb.getConfiguredProperty(targetDatastoreProperty);
                if (datastoreObject instanceof ResourceDatastore) {
                    final ResourceDatastore resourceDatastore = (ResourceDatastore) datastoreObject;
                    final Resource resource = resourceDatastore.getResource();
                    final Resource replacementResource = createReplacementResource(resource, partitionNumber);
                    if (replacementResource != null) {
                        final ResourceDatastore replacementDatastore = createReplacementDatastore(cb, resourceDatastore,
                                replacementResource);
                        if (replacementDatastore != null) {
                            cb.setConfiguredProperty(targetDatastoreProperty, replacementDatastore);
                        }
                    }
                }
            }

            final Set<ConfiguredPropertyDescriptor> resourceProperties = cb.getDescriptor()
                    .getConfiguredPropertiesByType(Resource.class, false);
            for (final ConfiguredPropertyDescriptor resourceProperty : resourceProperties) {
                final Resource resource = (Resource) cb.getConfiguredProperty(resourceProperty);
                final Resource replacementResource = createReplacementResource(resource, partitionNumber);
                if (replacementResource != null) {
                    cb.setConfiguredProperty(resourceProperty, replacementResource);
                }
            }

            // special handlings of specific component types are handled here
            if (cb.getComponentInstance() instanceof CreateCsvFileAnalyzer) {
                if (partitionNumber > 0) {
                    // ensure header is only created once
                    cb.setConfiguredProperty(CreateCsvFileAnalyzer.PROPERTY_INCLUDE_HEADER, false);
                }
            }
        }

        // recursively apply this function also on output data stream jobs
        final List<AnalysisJobBuilder> children = jobBuilder.getConsumedOutputDataStreamsJobBuilders();
        for (AnalysisJobBuilder childJobBuilder : children) {
            configureComponentsBeforeBuilding(childJobBuilder, partitionNumber);
        }
    }

    /**
     * Creates a {@link Resource} replacement to use for configured properties.
     * 
     * @param resource
     * @param partitionNumber
     * @return a replacement resource, or null if it shouldn't be replaced
     */
    private Resource createReplacementResource(final Resource resource, int partitionNumber) {
        final String formattedPartitionNumber = String.format("%05d", partitionNumber);
        if (resource instanceof HdfsResource || resource instanceof HadoopResource) {
            final String path = resource.getQualifiedPath() + "/part-" + formattedPartitionNumber;
            final URI uri = URI.create(path);
            final Resource replacementResource = HdfsHelper.createHelper().getResourceToUse(uri);
            return replacementResource;
        }
        if (resource instanceof FileResource) {
            final File file = ((FileResource) resource).getFile();
            if (file.exists() && file.isFile()) {
                // a file already exists - we cannot just create a directory
                // then
                return resource;
            }
            if (!file.exists()) {
                file.mkdirs();
            }
            final FileResource fileResource = new FileResource(resource.getQualifiedPath() + "/part-"
                    + formattedPartitionNumber);
            return fileResource;
        }
        return null;
    }

    /**
     * Creates a {@link Datastore} replacement to use for configured properties
     * 
     * @param cb
     * @param datastore
     * @param replacementResource
     * @return a replacement datastore, or null if it shouldn't be replaced
     */
    private ResourceDatastore createReplacementDatastore(ComponentBuilder cb, ResourceDatastore datastore,
            Resource replacementResource) {
        final String name = datastore.getName();
        if (datastore instanceof CsvDatastore) {
            final CsvConfiguration csvConfiguration = ((CsvDatastore) datastore).getCsvConfiguration();
            return new CsvDatastore(name, replacementResource, csvConfiguration);
        }
        if (datastore instanceof JsonDatastore) {
            return new JsonDatastore(name, replacementResource, ((JsonDatastore) datastore).getSchemaBuilder());
        }

        logger.warn("Could not replace datastore '{}' because it is of an unsupported type: ", name, datastore
                .getClass().getSimpleName());
        return datastore;
    }

    private List<Tuple2<String, NamedAnalyzerResult>> executePartition(Iterator<InputRow> inputRowIterator,
            final AnalysisJob analysisJob) {
        _sparkJobContext.triggerOnPartitionProcessingStart();
        final DataCleanerConfiguration configuration = _sparkJobContext.getConfiguration();
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
            logger.debug("Consumed row no. {}", inputRow.getId());
        }

        logger.info("Row processing complete - continuing to fetching results");

        // collect results
        final List<Tuple2<String, NamedAnalyzerResult>> analyzerResults = getAnalyzerResults(consumeRowHandler
                .getConsumers());

        // await any future results
        for (ListIterator<Tuple2<String, NamedAnalyzerResult>> it = analyzerResults.listIterator(); it.hasNext();) {
            final Tuple2<String, NamedAnalyzerResult> tuple = it.next();
            final NamedAnalyzerResult namedAnalyzerResult = tuple._2;
            final AnalyzerResult analyzerResult = namedAnalyzerResult.getAnalyzerResult();
            if (analyzerResult instanceof AnalyzerResultFuture) {
                final AnalyzerResult awaitedResult = ((AnalyzerResultFuture<?>) analyzerResult).get();
                final NamedAnalyzerResult awaitedResultTuple = new NamedAnalyzerResult(namedAnalyzerResult.getName(),
                        awaitedResult);
                it.set(new Tuple2<>(tuple._1, awaitedResultTuple));
            }
        }

        // close components
        final LifeCycleHelper lifeCycleHelper = new LifeCycleHelper(configuration, analysisJob, false);
        for (RowProcessingConsumer consumer : consumeRowHandler.getConsumers()) {
            lifeCycleHelper.close(consumer.getComponentJob().getDescriptor(), consumer.getComponent(), true);
        }
        _sparkJobContext.triggerOnPartitionProcessingEnd();
        return analyzerResults;
    }

    private List<Tuple2<String, NamedAnalyzerResult>> getAnalyzerResults(
            Collection<RowProcessingConsumer> rowProcessingConsumers) {
        final List<Tuple2<String, NamedAnalyzerResult>> analyzerResults = new ArrayList<>();

        for (RowProcessingConsumer consumer : rowProcessingConsumers) {
            if (consumer.isResultProducer()) {
                final HasAnalyzerResult<?> resultProducer = (HasAnalyzerResult<?>) consumer.getComponent();
                final AnalyzerResult analyzerResult = resultProducer.getResult();
                final String key = _sparkJobContext.getComponentKey(consumer.getComponentJob());
                final NamedAnalyzerResult namedAnalyzerResult = new NamedAnalyzerResult(key, analyzerResult);
                final Tuple2<String, NamedAnalyzerResult> tuple = new Tuple2<>(key, namedAnalyzerResult);
                analyzerResults.add(tuple);
            }

            for (ActiveOutputDataStream activeOutputDataStream : consumer.getActiveOutputDataStreams()) {
                List<RowProcessingConsumer> outputDataStreamConsumers = activeOutputDataStream.getPublisher()
                        .getConsumers();
                List<Tuple2<String, NamedAnalyzerResult>> outputDataStreamsAnalyzerResults = getAnalyzerResults(
                        outputDataStreamConsumers);
                analyzerResults.addAll(outputDataStreamsAnalyzerResults);
            }
        }
        return analyzerResults;
    }
}