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
package org.eobjects.analyzer.job.runner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.metamodel.DataContext;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.jdbc.JdbcDataContext;
import org.apache.metamodel.query.Query;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.CollectionUtils;
import org.apache.metamodel.util.Func;
import org.apache.metamodel.util.LazyRef;
import org.apache.metamodel.util.Predicate;
import org.eobjects.analyzer.beans.api.Analyzer;
import org.eobjects.analyzer.beans.api.Filter;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.configuration.ContextAwareInjectionManager;
import org.eobjects.analyzer.configuration.InjectionManager;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreConnection;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MetaModelInputRow;
import org.eobjects.analyzer.descriptors.ComponentDescriptor;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.job.BeanConfiguration;
import org.eobjects.analyzer.job.ComponentJob;
import org.eobjects.analyzer.job.ConfigurableBeanJob;
import org.eobjects.analyzer.job.FilterJob;
import org.eobjects.analyzer.job.FilterOutcome;
import org.eobjects.analyzer.job.HasFilterOutcomes;
import org.eobjects.analyzer.job.TransformerJob;
import org.eobjects.analyzer.job.concurrent.ForkTaskListener;
import org.eobjects.analyzer.job.concurrent.JoinTaskListener;
import org.eobjects.analyzer.job.concurrent.RunNextTaskTaskListener;
import org.eobjects.analyzer.job.concurrent.TaskListener;
import org.eobjects.analyzer.job.concurrent.TaskRunnable;
import org.eobjects.analyzer.job.concurrent.TaskRunner;
import org.eobjects.analyzer.job.tasks.CloseTaskListener;
import org.eobjects.analyzer.job.tasks.CollectResultsTask;
import org.eobjects.analyzer.job.tasks.ConsumeRowTask;
import org.eobjects.analyzer.job.tasks.InitializeReferenceDataTask;
import org.eobjects.analyzer.job.tasks.InitializeTask;
import org.eobjects.analyzer.job.tasks.RunRowProcessingPublisherTask;
import org.eobjects.analyzer.job.tasks.Task;
import org.eobjects.analyzer.lifecycle.LifeCycleHelper;
import org.eobjects.analyzer.util.SystemProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RowProcessingPublisher {

    private final static Logger logger = LoggerFactory.getLogger(RowProcessingPublisher.class);

    private final RowProcessingPublishers _publishers;
    private final Table _table;
    private final Set<Column> _physicalColumns = new LinkedHashSet<Column>();
    private final List<RowProcessingConsumer> _consumers = new ArrayList<RowProcessingConsumer>();
    private final LazyRef<RowProcessingQueryOptimizer> _queryOptimizerRef;
    private final AtomicBoolean _successful = new AtomicBoolean(true);

    public RowProcessingPublisher(RowProcessingPublishers publishers, Table table) {
        if (publishers == null) {
            throw new IllegalArgumentException("RowProcessingPublishers cannot be null");
        }
        if (table == null) {
            throw new IllegalArgumentException("Table cannot be null");
        }
        _publishers = publishers;
        _table = table;

        _queryOptimizerRef = createQueryOptimizerRef();

        if (!"true".equalsIgnoreCase(SystemProperties.QUERY_SELECTCLAUSE_OPTIMIZE)) {
            final Collection<InputColumn<?>> sourceColumns = publishers.getAnalysisJob().getSourceColumns();
            final List<Column> columns = new ArrayList<Column>();
            for (InputColumn<?> sourceColumn : sourceColumns) {
                Column column = sourceColumn.getPhysicalColumn();
                if (column != null && table.equals(column.getTable())) {
                    columns.add(column);
                }
            }

            addPhysicalColumns(columns.toArray(new Column[columns.size()]));
        }
    }

    /**
     * Gets metrics for this row processing session. Note that consumers are
     * assumed to be initialized at this point. See
     * {@link #initializeConsumers(TaskListener)}.
     * 
     * @return
     */
    public RowProcessingMetrics getRowProcessingMetrics() {
        RowProcessingMetricsImpl metrics = new RowProcessingMetricsImpl(_publishers, this);
        return metrics;
    }

    public Table getTable() {
        return _table;
    }

    /**
     * Inspects the row processed tables primary keys. If all primary keys are
     * in the source columns of the AnalysisJob, they will be added to the
     * physically queried columns.
     * 
     * Adding the primary keys to the query is a trade-off: It helps a lot in
     * making eg. annotated rows referenceable to the source table, but it may
     * also potentially make the job heavier to execute since a lot of (unique)
     * values will be retrieved.
     */
    public void addPrimaryKeysIfSourced() {
        Column[] primaryKeyColumns = _table.getPrimaryKeys();
        if (primaryKeyColumns == null || primaryKeyColumns.length == 0) {
            logger.info("No primary keys defined for table {}, not pre-selecting primary keys", _table.getName());
            return;
        }

        final AnalysisJob analysisJob = _publishers.getAnalysisJob();
        final Collection<InputColumn<?>> sourceInputColumns = analysisJob.getSourceColumns();
        final List<Column> sourceColumns = CollectionUtils.map(sourceInputColumns, new Func<InputColumn<?>, Column>() {
            @Override
            public Column eval(InputColumn<?> inputColumn) {
                return inputColumn.getPhysicalColumn();
            }
        });

        for (Column primaryKeyColumn : primaryKeyColumns) {
            if (!sourceColumns.contains(primaryKeyColumn)) {
                logger.info("Primary key column {} not added to source columns, not pre-selecting primary keys");
                return;
            }
        }

        addPhysicalColumns(primaryKeyColumns);
    }

    private LazyRef<RowProcessingQueryOptimizer> createQueryOptimizerRef() {
        return new LazyRef<RowProcessingQueryOptimizer>() {
            @Override
            protected RowProcessingQueryOptimizer fetch() {
                final Datastore datastore = _publishers.getDatastore();
                try (final DatastoreConnection con = datastore.openConnection()) {
                    final DataContext dataContext = con.getDataContext();

                    final Column[] columnArray = _physicalColumns.toArray(new Column[_physicalColumns.size()]);
                    final Query baseQuery = dataContext.query().from(_table).select(columnArray).toQuery();

                    logger.debug("Base query for row processing: {}", baseQuery);

                    final List<RowProcessingConsumer> sortedConsumers = sortConsumers(_consumers);

                    final RowProcessingQueryOptimizer optimizer = new RowProcessingQueryOptimizer(datastore,
                            sortedConsumers, baseQuery);
                    return optimizer;
                } catch (RuntimeException e) {
                    logger.error("Failed to build query optimizer! {}", e.getMessage(), e);
                    throw e;
                }
            }
        };
    }

    /**
     * Sorts a list of consumers into their execution order
     * 
     * @param consumers
     * @return
     */
    public static List<RowProcessingConsumer> sortConsumers(List<RowProcessingConsumer> consumers) {
        final RowProcessingConsumerSorter sorter = new RowProcessingConsumerSorter(consumers);
        final List<RowProcessingConsumer> sortedConsumers = sorter.createProcessOrderedConsumerList();
        if (logger.isDebugEnabled()) {
            logger.debug("Row processing order ({} consumers):", sortedConsumers.size());
            int i = 1;
            for (RowProcessingConsumer rowProcessingConsumer : sortedConsumers) {
                logger.debug(" {}) {}", i, rowProcessingConsumer);
                i++;
            }
        }
        return sortedConsumers;
    }

    public void initialize() {
        // can safely load query optimizer in separate thread here
        _queryOptimizerRef.requestLoad();
    }

    public void addPhysicalColumns(Column... columns) {
        for (Column column : columns) {
            if (!_table.equals(column.getTable())) {
                throw new IllegalArgumentException("Column does not pertain to the correct table. Expected table: "
                        + _table + ", actual table: " + column.getTable());
            }
            _physicalColumns.add(column);
        }
    }

    public RowProcessingQueryOptimizer getQueryOptimizer() {
        final RowProcessingQueryOptimizer optimizer = _queryOptimizerRef.get();
        if (optimizer == null) {
            final Throwable e = _queryOptimizerRef.getError();
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new IllegalStateException(e);
        }
        return optimizer;
    }

    public Query getQuery() {
        return getQueryOptimizer().getOptimizedQuery();
    }

    /**
     * Fires the actual row processing. This method assumes that consumers have
     * been initialized and the publisher is ready to start processing.
     * 
     * @return true if no errors occurred during processing
     * 
     * @param rowProcessingMetrics
     * 
     * @see #runRowProcessing(Queue, TaskListener)
     */
    public void processRows(RowProcessingMetrics rowProcessingMetrics) {
        final RowProcessingQueryOptimizer queryOptimizer = getQueryOptimizer();
        final Query finalQuery = queryOptimizer.getOptimizedQuery();

        final RowIdGenerator idGenerator;
        if (finalQuery.getFirstRow() == null) {
            idGenerator = new SimpleRowIdGenerator();
        } else {
            idGenerator = new SimpleRowIdGenerator(finalQuery.getFirstRow());
        }

        final AnalysisJob analysisJob = _publishers.getAnalysisJob();
        final AnalysisListener analysisListener = _publishers.getAnalysisListener();
        final TaskRunner taskRunner = _publishers.getTaskRunner();

        for (RowProcessingConsumer rowProcessingConsumer : _consumers) {
            if (rowProcessingConsumer instanceof AnalyzerConsumer) {
                final AnalyzerConsumer analyzerConsumer = (AnalyzerConsumer) rowProcessingConsumer;
                final AnalyzerJob analyzerJob = analyzerConsumer.getComponentJob();
                final AnalyzerMetrics metrics = rowProcessingMetrics.getAnalysisJobMetrics().getAnalyzerMetrics(
                        analyzerJob);
                analysisListener.analyzerBegin(analysisJob, analyzerJob, metrics);
            }

            if (rowProcessingConsumer instanceof TransformerConsumer) {
                ((TransformerConsumer) rowProcessingConsumer).setRowIdGenerator(idGenerator);
            }
        }
        final List<RowProcessingConsumer> consumers = queryOptimizer.getOptimizedConsumers();
        final Collection<? extends FilterOutcome> availableOutcomes = queryOptimizer.getOptimizedAvailableOutcomes();

        analysisListener.rowProcessingBegin(analysisJob, rowProcessingMetrics);

        final RowConsumerTaskListener taskListener = new RowConsumerTaskListener(analysisJob, analysisListener,
                taskRunner);

        final Datastore datastore = _publishers.getDatastore();

        try (final DatastoreConnection con = datastore.openConnection()) {
            final DataContext dataContext = con.getDataContext();

            if (logger.isDebugEnabled()) {
                final String queryString;
                if (dataContext instanceof JdbcDataContext) {
                    final JdbcDataContext jdbcDataContext = (JdbcDataContext) dataContext;
                    queryString = jdbcDataContext.getQueryRewriter().rewriteQuery(finalQuery);
                } else {
                    queryString = finalQuery.toSql();
                }
                logger.debug("Final query: {}", queryString);
                logger.debug("Final query firstRow={}, maxRows={}", finalQuery.getFirstRow(), finalQuery.getMaxRows());
            }

            // represents the distinct count of rows as well as the number of
            // tasks to execute
            int numTasks = 0;

            try (final DataSet dataSet = dataContext.executeQuery(finalQuery)) {
                final ConsumeRowHandler consumeRowHandler = new ConsumeRowHandler(consumers, availableOutcomes);
                while (dataSet.next()) {
                    if (taskListener.isErrornous()) {
                        break;
                    }

                    numTasks++;

                    final Row metaModelRow = dataSet.getRow();
                    final int rowId = idGenerator.nextPhysicalRowId();
                    final MetaModelInputRow inputRow = new MetaModelInputRow(rowId, metaModelRow);

                    final ConsumeRowTask task = new ConsumeRowTask(consumeRowHandler, rowProcessingMetrics, inputRow,
                            analysisListener, numTasks);
                    taskRunner.run(task, taskListener);

                }
            }
            taskListener.awaitTasks(numTasks);
        }

        if (taskListener.isErrornous()) {
            _successful.set(false);
            return;
        }

        analysisListener.rowProcessingSuccess(analysisJob, rowProcessingMetrics);
    }

    public void addAnalyzerBean(Analyzer<?> analyzer, AnalyzerJob analyzerJob, InputColumn<?>[] inputColumns) {
        addConsumer(new AnalyzerConsumer(analyzer, analyzerJob, inputColumns, _publishers));
    }

    public void addTransformerBean(Transformer<?> transformer, TransformerJob transformerJob,
            InputColumn<?>[] inputColumns) {
        addConsumer(new TransformerConsumer(transformer, transformerJob, inputColumns, _publishers));
    }

    public void addFilterBean(final Filter<?> filter, final FilterJob filterJob, final InputColumn<?>[] inputColumns) {
        addConsumer(new FilterConsumer(filter, filterJob, inputColumns, _publishers));
    }

    public boolean containsOutcome(final FilterOutcome prerequisiteOutcome) {
        for (final RowProcessingConsumer consumer : _consumers) {
            final ComponentJob componentJob = consumer.getComponentJob();
            if (componentJob instanceof HasFilterOutcomes) {
                final Collection<FilterOutcome> outcomes = ((HasFilterOutcomes) componentJob).getFilterOutcomes();
                for (FilterOutcome outcome : outcomes) {
                    if (outcome.isEquals(prerequisiteOutcome)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void addConsumer(final RowProcessingConsumer consumer) {
        _consumers.add(consumer);
    }

    public List<RowProcessingConsumer> getConfigurableConsumers() {
        final List<RowProcessingConsumer> configurableConsumers = CollectionUtils.filter(_consumers,
                new Predicate<RowProcessingConsumer>() {
                    @Override
                    public Boolean eval(RowProcessingConsumer input) {
                        return input.getComponentJob() instanceof ConfigurableBeanJob<?>;
                    }
                });
        return configurableConsumers;
    }

    /**
     * Runs the whole row processing logic, start to finish, including
     * initialization, process rows, result collection and cleanup/closing
     * resources.
     * 
     * @param resultQueue
     *            a queue on which to append results
     * @param finishedTaskListener
     *            a task listener which will be invoked once the processing is
     *            done.
     * 
     * @see #processRows(RowProcessingMetrics)
     * @see #initializeConsumers(TaskListener)
     */
    public void runRowProcessing(Queue<JobAndResult> resultQueue, TaskListener finishedTaskListener) {

        final LifeCycleHelper lifeCycleHelper = _publishers.getLifeCycleHelper();
        final TaskRunner taskRunner = _publishers.getTaskRunner();

        final List<RowProcessingConsumer> configurableConsumers = getConfigurableConsumers();

        final int numConsumerTasks = configurableConsumers.size();

        // add tasks for closing components
        final TaskListener closeTaskListener = new JoinTaskListener(numConsumerTasks, finishedTaskListener);
        final List<TaskRunnable> closeTasks = new ArrayList<TaskRunnable>(numConsumerTasks);
        for (RowProcessingConsumer consumer : configurableConsumers) {
            closeTasks.add(createCloseTask(consumer, closeTaskListener));
        }

        final TaskListener getResultCompletionListener = new ForkTaskListener("collect results", taskRunner, closeTasks);

        // add tasks for collecting results
        final TaskListener getResultTaskListener = new JoinTaskListener(numConsumerTasks, getResultCompletionListener);
        final List<TaskRunnable> getResultTasks = new ArrayList<TaskRunnable>();
        for (RowProcessingConsumer consumer : configurableConsumers) {
            final Task collectResultTask = createCollectResultTask(consumer, resultQueue);
            if (collectResultTask == null) {
                getResultTasks.add(new TaskRunnable(null, getResultTaskListener));
            } else {
                getResultTasks.add(new TaskRunnable(collectResultTask, getResultTaskListener));
            }
        }

        final TaskListener runCompletionListener = new ForkTaskListener("run row processing", taskRunner,
                getResultTasks);

        final RowProcessingMetrics rowProcessingMetrics = getRowProcessingMetrics();
        final RunRowProcessingPublisherTask runTask = new RunRowProcessingPublisherTask(this, rowProcessingMetrics);

        final TaskListener referenceDataInitFinishedListener = new ForkTaskListener("Initialize row consumers",
                taskRunner, Arrays.asList(new TaskRunnable(runTask, runCompletionListener)));

        final RunNextTaskTaskListener initializeFinishedListener = new RunNextTaskTaskListener(taskRunner,
                new InitializeReferenceDataTask(lifeCycleHelper), referenceDataInitFinishedListener);

        // kick off the initialization
        initializeConsumers(initializeFinishedListener);
    }

    /**
     * Initializes consumers of this {@link RowProcessingPublisher}. Once
     * consumers are initialized, row processing can begin, expected rows can be
     * calculated and more.
     * 
     * @param finishedListener
     */
    public void initializeConsumers(TaskListener finishedListener) {
        final List<RowProcessingConsumer> configurableConsumers = getConfigurableConsumers();
        final int numConfigurableConsumers = configurableConsumers.size();
        final TaskListener initFinishedListener = new JoinTaskListener(numConfigurableConsumers, finishedListener);
        final TaskRunner taskRunner = _publishers.getTaskRunner();
        for (RowProcessingConsumer consumer : configurableConsumers) {
            TaskRunnable task = createInitTask(consumer, initFinishedListener);
            taskRunner.run(task);
        }
    }

    /**
     * Closes consumers of this {@link RowProcessingPublisher}. Usually this
     * will be done automatically when
     * {@link #runRowProcessing(Queue, TaskListener)} is invoked.
     */
    public void closeConsumers() {
        final List<RowProcessingConsumer> configurableConsumers = getConfigurableConsumers();
        final TaskRunner taskRunner = _publishers.getTaskRunner();
        for (RowProcessingConsumer consumer : configurableConsumers) {
            TaskRunnable task = createCloseTask(consumer, null);
            taskRunner.run(task);
        }
    }

    private Task createCollectResultTask(RowProcessingConsumer consumer, Queue<JobAndResult> resultQueue) {
        if (consumer instanceof TransformerConsumer || consumer instanceof FilterConsumer) {
            return null;
        } else if (consumer instanceof AnalyzerConsumer) {
            final AnalyzerConsumer analyzerConsumer = (AnalyzerConsumer) consumer;
            final Analyzer<?> analyzer = analyzerConsumer.getComponent();
            final AnalysisJob analysisJob = _publishers.getAnalysisJob();
            final AnalysisListener analysisListener = _publishers.getAnalysisListener();
            return new CollectResultsTask(analyzer, analysisJob, consumer.getComponentJob(), resultQueue,
                    analysisListener);
        } else {
            throw new IllegalStateException("Unknown consumer type: " + consumer);
        }
    }

    private TaskRunnable createCloseTask(RowProcessingConsumer consumer, TaskListener closeTaskListener) {
        final LifeCycleHelper lifeCycleHelper = _publishers.getLifeCycleHelper();
        final ComponentDescriptor<?> descriptor = consumer.getComponentJob().getDescriptor();
        final Object component = consumer.getComponent();
        return new TaskRunnable(null, new CloseTaskListener(lifeCycleHelper, descriptor, component, _successful,
                closeTaskListener));
    }

    private TaskRunnable createInitTask(RowProcessingConsumer consumer, TaskListener listener) {
        final ComponentJob componentJob = consumer.getComponentJob();
        final Object component = consumer.getComponent();
        final BeanConfiguration configuration = ((ConfigurableBeanJob<?>) componentJob).getConfiguration();
        final ComponentDescriptor<?> descriptor = componentJob.getDescriptor();

        // make a component-context specific injection manager
        final LifeCycleHelper lifeCycleHelper;
        {
            final LifeCycleHelper outerLifeCycleHelper = _publishers.getLifeCycleHelper();
            final boolean includeNonDistributedTasks = outerLifeCycleHelper.isIncludeNonDistributedTasks();
            final AnalysisJob analysisJob = _publishers.getAnalysisJob();
            final InjectionManager outerInjectionManager = outerLifeCycleHelper.getInjectionManager();
            final ReferenceDataActivationManager referenceDataActivationManager = outerLifeCycleHelper
                    .getReferenceDataActivationManager();
            final ContextAwareInjectionManager injectionManager = new ContextAwareInjectionManager(
                    outerInjectionManager, analysisJob, componentJob, _publishers.getAnalysisListener());

            lifeCycleHelper = new LifeCycleHelper(injectionManager, referenceDataActivationManager,
                    includeNonDistributedTasks);
        }

        InitializeTask task = new InitializeTask(lifeCycleHelper, descriptor, component, configuration);
        return new TaskRunnable(task, listener);
    }

    @Override
    public String toString() {
        return "RowProcessingPublisher[table=" + _table.getQualifiedLabel() + ", consumers=" + _consumers.size() + "]";
    }

    public AnalyzerJob[] getAnalyzerJobs() {
        List<AnalyzerJob> analyzerJobs = new ArrayList<AnalyzerJob>();
        for (RowProcessingConsumer consumer : _consumers) {
            if (consumer instanceof AnalyzerConsumer) {
                AnalyzerJob analyzerJob = ((AnalyzerConsumer) consumer).getComponentJob();
                analyzerJobs.add(analyzerJob);
            }
        }
        return analyzerJobs.toArray(new AnalyzerJob[analyzerJobs.size()]);
    }
}
