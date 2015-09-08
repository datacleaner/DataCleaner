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
package org.datacleaner.job.runner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.metamodel.schema.Table;
import org.datacleaner.api.Analyzer;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.InjectionManager;
import org.datacleaner.configuration.InjectionManagerFactory;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.FilterOutcome;
import org.datacleaner.job.FilterOutcomes;
import org.datacleaner.job.concurrent.SingleThreadedTaskRunner;
import org.datacleaner.job.concurrent.TaskListener;
import org.datacleaner.job.tasks.Task;
import org.datacleaner.lifecycle.LifeCycleHelper;
import org.datacleaner.util.SourceColumnFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Object that can handle the task of consuming a number of rows. The
 * {@link ConsumeRowHandler} is internally used to execute all necesary
 * components for every record, but it can also be used as a utility if
 * DataCleaner jobs are being embedded or applied in externally.
 */
public class ConsumeRowHandler {

    private static final Logger logger = LoggerFactory.getLogger(ConsumeRowHandler.class);

    private final List<RowProcessingConsumer> _consumers;
    private final Collection<? extends FilterOutcome> _alwaysSatisfiedOutcomes;

    public static class Configuration {
        public boolean includeNonDistributedTasks = true;
        public AnalysisListener analysisListener = new InfoLoggingAnalysisListener();
        public boolean includeAnalyzers = true;
        public Collection<? extends FilterOutcome> alwaysSatisfiedOutcomes;
        public Table table;
    }

    /**
     * Builds a {@link ConsumeRowHandler} based on a job, and the configuration
     * to read the job's consumers
     * 
     * @param job
     * @param configuration
     * @param rowConsumerConfiguration
     */
    public ConsumeRowHandler(AnalysisJob job, DataCleanerConfiguration configuration,
            Configuration rowConsumerConfiguration) {
        _consumers = extractConsumers(job, configuration, rowConsumerConfiguration);
        _alwaysSatisfiedOutcomes = rowConsumerConfiguration.alwaysSatisfiedOutcomes;
    }

    /**
     * Builds a {@link ConsumeRowHandler} based on a list of consumers.
     * 
     * @param consumers
     */
    public ConsumeRowHandler(List<RowProcessingConsumer> consumers) {
        this(consumers, null);
    }

    /**
     * Builds a {@link ConsumeRowHandler} based on a list of consumers as well
     * as a collection of always-satisfied outcomes.
     * 
     * @param consumers
     * @param alwaysSatisfiedOutcomes
     */
    public ConsumeRowHandler(List<RowProcessingConsumer> consumers,
            Collection<? extends FilterOutcome> alwaysSatisfiedOutcomes) {
        _consumers = consumers;
        _alwaysSatisfiedOutcomes = alwaysSatisfiedOutcomes;
    }

    /**
     * Gets the {@link RowProcessingConsumer}s that this handler is working on.
     * 
     * @return
     */
    public List<RowProcessingConsumer> getConsumers() {
        return _consumers;
    }

    /**
     * Gets the output columns produced by all the consumers of this
     * {@link ConsumeRowHandler}.
     * 
     * @return
     */
    public List<InputColumn<?>> getOutputColumns() {
        final List<InputColumn<?>> result = new ArrayList<InputColumn<?>>();
        for (final RowProcessingConsumer consumer : _consumers) {
            final InputColumn<?>[] outputColumns = consumer.getOutputColumns();
            for (final InputColumn<?> outputColumn : outputColumns) {
                result.add(outputColumn);
            }
        }
        return result;
    }

    /**
     * @deprecated use {@link #consumeRow(InputRow)} instead
     */
    @Deprecated
    public List<InputRow> consume(final InputRow row) {
        final ConsumeRowResult result = consumeRow(row);
        return result.getRows();
    }

    /**
     * Consumes a {@link InputRow} by applying all transformations etc. to it,
     * returning a result of transformed rows and their {@link FilterOutcomes}s.
     * 
     * @param row
     * @return
     */
    public ConsumeRowResult consumeRow(final InputRow row) {
        final FilterOutcomes outcomes = new FilterOutcomesImpl(_alwaysSatisfiedOutcomes);
        final ConsumeRowHandlerDelegate delegate = new ConsumeRowHandlerDelegate(_consumers, row, 0, outcomes);
        final ConsumeRowResult result = delegate.consume();
        return result;
    }

    private List<RowProcessingConsumer> extractConsumers(AnalysisJob analysisJob,
            DataCleanerConfiguration configuration, Configuration rowConsumeConfiguration) {
        final InjectionManagerFactory injectionManagerFactory = configuration.getEnvironment()
                .getInjectionManagerFactory();
        final InjectionManager injectionManager = injectionManagerFactory.getInjectionManager(configuration,
                analysisJob);
        final LifeCycleHelper lifeCycleHelper = new LifeCycleHelper(injectionManager,
                rowConsumeConfiguration.includeNonDistributedTasks);
        SourceColumnFinder sourceColumnFinder = new SourceColumnFinder();
        sourceColumnFinder.addSources(analysisJob);

        /**
         * Use a single threaded task runner since this handler is invoked in a
         * blocking way - the calling code may itself be multithreaded without
         * issues.
         */
        final SingleThreadedTaskRunner taskRunner = new SingleThreadedTaskRunner();

        final AnalysisListener analysisListener = rowConsumeConfiguration.analysisListener;
        final RowProcessingPublishers rowProcessingPublishers = new RowProcessingPublishers(analysisJob,
                analysisListener, taskRunner, lifeCycleHelper, sourceColumnFinder);

        final RowProcessingPublisher publisher;
        if (rowConsumeConfiguration.table != null) {
            @SuppressWarnings("deprecation")
            final RowProcessingPublisher tablePublisher = rowProcessingPublishers
                    .getRowProcessingPublisher(rowConsumeConfiguration.table);
            if (tablePublisher == null) {
                throw new IllegalArgumentException("Job does not consume records from table: "
                        + rowConsumeConfiguration.table);
            }
            publisher = tablePublisher;
        } else {
            Collection<RowProcessingPublisher> publishers = rowProcessingPublishers.getRowProcessingPublishers();
            publisher = publishers.iterator().next();
            for (RowProcessingPublisher aPublisher : publishers) {
                if (aPublisher != publisher) {
                    if (aPublisher.getStream().isSourceTable()) {
                        throw new IllegalArgumentException(
                                "Job consumes multiple source tables, but ConsumeRowHandler can only handle a single table's components. Please specify a Table constructor argument.");
                    }
                }
            }
        }

        final AtomicReference<Throwable> errorReference = new AtomicReference<Throwable>();

        publisher.initializeConsumers(new TaskListener() {
            @Override
            public void onError(Task task, Throwable throwable) {
                logger.error("Exception thrown while initializing consumers.", throwable);
                errorReference.compareAndSet(null, throwable);
            }

            @Override
            public void onComplete(Task task) {
                logger.info("Consumers initialized successfully.");
            }

            @Override
            public void onBegin(Task task) {
                logger.info("Beginning the process of initializing consumers.");
            }
        });

        final Throwable throwable = errorReference.get();
        if (throwable != null) {
            if (throwable instanceof RuntimeException) {

            }
        }

        List<RowProcessingConsumer> consumers = publisher.getConsumers();
        if (!rowConsumeConfiguration.includeAnalyzers) {
            consumers = removeAnalyzers(consumers);
        }

        consumers = RowProcessingPublisher.sortConsumers(consumers);
        return consumers;
    }

    private List<RowProcessingConsumer> removeAnalyzers(List<RowProcessingConsumer> consumers) {
        final List<RowProcessingConsumer> result = new ArrayList<RowProcessingConsumer>();
        for (RowProcessingConsumer rowProcessingConsumer : consumers) {
            final Object component = rowProcessingConsumer.getComponent();
            if (!(component instanceof Analyzer<?>)) {
                result.add(rowProcessingConsumer);
            }
        }
        return result;
    }
}
