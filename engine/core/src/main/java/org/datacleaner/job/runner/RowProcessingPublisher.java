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
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
import org.datacleaner.api.Analyzer;
import org.datacleaner.api.Filter;
import org.datacleaner.api.HasAnalyzerResult;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.Transformer;
import org.datacleaner.configuration.ContextAwareInjectionManager;
import org.datacleaner.configuration.InjectionManager;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.data.MetaModelInputRow;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.AnalyzerJob;
import org.datacleaner.job.ComponentJob;
import org.datacleaner.job.FilterJob;
import org.datacleaner.job.FilterOutcome;
import org.datacleaner.job.HasFilterOutcomes;
import org.datacleaner.job.TransformerJob;
import org.datacleaner.job.concurrent.ForkTaskListener;
import org.datacleaner.job.concurrent.JoinTaskListener;
import org.datacleaner.job.concurrent.RunNextTaskTaskListener;
import org.datacleaner.job.concurrent.SingleThreadedTaskRunner;
import org.datacleaner.job.concurrent.TaskListener;
import org.datacleaner.job.concurrent.TaskRunnable;
import org.datacleaner.job.concurrent.TaskRunner;
import org.datacleaner.job.tasks.CloseTaskListener;
import org.datacleaner.job.tasks.CollectResultsTask;
import org.datacleaner.job.tasks.ConsumeRowTask;
import org.datacleaner.job.tasks.InitializeTask;
import org.datacleaner.job.tasks.RunRowProcessingPublisherTask;
import org.datacleaner.job.tasks.Task;
import org.datacleaner.lifecycle.LifeCycleHelper;
import org.datacleaner.util.SourceColumnFinder;
import org.datacleaner.util.SystemProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RowProcessingPublisher {

    private final static Logger logger = LoggerFactory.getLogger(RowProcessingPublisher.class);

    private final RowProcessingPublisher _parentPublisher;
    private final RowProcessingPublishers _publishers;
    private final TaskRunner _taskRunner;
    private final Table _table;
    private final AnalysisJob _analysisJob;
    private final Set<Column> _physicalColumns = new LinkedHashSet<Column>();
    private final List<RowProcessingConsumer> _consumers = new ArrayList<RowProcessingConsumer>();
    private final LazyRef<RowProcessingQueryOptimizer> _queryOptimizerRef;
    private final AtomicBoolean _successful = new AtomicBoolean(true);
    private final Map<RowProcessingConsumer, ReferenceDataActivationManager> _referenceDataActivationManagers;

    /**
     * Constructor to use for creating a {@link RowProcessingPublisher} which
     * feeds data from a source datastore.
     * 
     * @param publishers
     * @param analysisJob
     * @param table
     * @param taskRunner
     */
    public RowProcessingPublisher(RowProcessingPublishers publishers, AnalysisJob analysisJob, Table table,
            TaskRunner taskRunner) {
        this(publishers, null, analysisJob, table, taskRunner);
    }

    /**
     * Constructor to use for {@link RowProcessingPublisher}s that are parented
     * by another {@link RowProcessingPublisher}. When a parent publisher exists
     * the execution flow is adapted since records will be dispatched by a
     * (component within the) parent instead of sourced by the
     * {@link RowProcessingPublisher} itself.
     * 
     * @param parentPublisher
     * @param analysisJob
     * @param table
     */
    public RowProcessingPublisher(RowProcessingPublisher parentPublisher, AnalysisJob analysisJob, Table table) {
        this(parentPublisher._publishers, parentPublisher, analysisJob, table, new SingleThreadedTaskRunner());
    }

    private RowProcessingPublisher(RowProcessingPublishers publishers, RowProcessingPublisher parentPublisher,
            AnalysisJob analysisJob, Table table, TaskRunner taskRunner) {
        if (publishers == null) {
            throw new IllegalArgumentException("RowProcessingPublishers cannot be null");
        }
        if (analysisJob == null) {
            throw new IllegalArgumentException("AnalysisJob cannot be null");
        }
        if (table == null) {
            throw new IllegalArgumentException("Table cannot be null");
        }
        _analysisJob = analysisJob;
        _parentPublisher = parentPublisher;
        _publishers = publishers;
        _table = table;
        _taskRunner = taskRunner;

        _queryOptimizerRef = createQueryOptimizerRef();
        _referenceDataActivationManagers = new IdentityHashMap<>();

        final boolean aggressiveOptimizeSelectClause = SystemProperties.getBoolean(
                SystemProperties.QUERY_SELECTCLAUSE_OPTIMIZE, false);
        if (!aggressiveOptimizeSelectClause) {
            final Collection<InputColumn<?>> sourceColumns = analysisJob.getSourceColumns();
            final List<Column> columns = new ArrayList<Column>();
            for (InputColumn<?> sourceColumn : sourceColumns) {
                final Column column = sourceColumn.getPhysicalColumn();
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
        final Column[] primaryKeyColumns = _table.getPrimaryKeys();
        if (primaryKeyColumns == null || primaryKeyColumns.length == 0) {
            logger.info("No primary keys defined for table {}, not pre-selecting primary keys", _table.getName());
            return;
        }

        final Collection<InputColumn<?>> sourceInputColumns = _analysisJob.getSourceColumns();
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
                final Datastore datastore = _analysisJob.getDatastore();
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

    public void onAllConsumersRegistered() {
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
     * @see #runRowProcessing(Queue, TaskListener)
     */
    public void processRows(RowProcessingMetrics rowProcessingMetrics) {
        final AnalysisListener analysisListener = _publishers.getAnalysisListener();
        analysisListener.rowProcessingBegin(_analysisJob, rowProcessingMetrics);

        final boolean success;
        if (_parentPublisher == null) {
            success = processRowsFromQuery(analysisListener, rowProcessingMetrics);
        } else {
            success = awaitProcessing(analysisListener);
        }

        if (!success) {
            _successful.set(false);
            return;
        }

        analysisListener.rowProcessingSuccess(_analysisJob, rowProcessingMetrics);
    }

    private boolean awaitProcessing(AnalysisListener listener) {
        final List<RowProcessingConsumer> consumers = _parentPublisher.getConsumers();
        for (RowProcessingConsumer consumer : consumers) {
            final Collection<ActiveOutputDataStream> activeOutputDataStreams = consumer.getActiveOutputDataStreams();
            for (ActiveOutputDataStream activeOutputDataStream : activeOutputDataStreams) {
                try {
                    activeOutputDataStream.await();
                } catch (InterruptedException e) {
                    logger.error("Unexpected error awaiting output data stream", e);
                    listener.errorUknown(_analysisJob, e);
                    return false;
                }
            }
        }
        return true;
    }

    private boolean processRowsFromQuery(AnalysisListener analysisListener, RowProcessingMetrics rowProcessingMetrics) {
        final RowProcessingQueryOptimizer queryOptimizer = getQueryOptimizer();
        final Query finalQuery = queryOptimizer.getOptimizedQuery();

        final RowIdGenerator idGenerator;
        if (finalQuery.getFirstRow() == null) {
            idGenerator = new SimpleRowIdGenerator();
        } else {
            idGenerator = new SimpleRowIdGenerator(finalQuery.getFirstRow());
        }

        final ConsumeRowHandler consumeRowHandler = createConsumeRowHandler();

        final RowConsumerTaskListener taskListener = new RowConsumerTaskListener(_analysisJob, analysisListener,
                _taskRunner);

        final Datastore datastore = _analysisJob.getDatastore();

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
                    _taskRunner.run(task, taskListener);

                }
            }
            taskListener.awaitTasks(numTasks);
        }

        return !taskListener.isErrornous();
    }

    protected ConsumeRowHandler createConsumeRowHandler() {
        final RowProcessingQueryOptimizer queryOptimizer = getQueryOptimizer();
        final Query finalQuery = queryOptimizer.getOptimizedQuery();

        final RowIdGenerator idGenerator;
        if (finalQuery.getFirstRow() == null) {
            idGenerator = new SimpleRowIdGenerator();
        } else {
            idGenerator = new SimpleRowIdGenerator(finalQuery.getFirstRow());
        }

        final AnalysisListener analysisListener = _publishers.getAnalysisListener();

        for (RowProcessingConsumer consumer : _consumers) {
            final ComponentJob componentJob = consumer.getComponentJob();
            final RowProcessingMetrics rowProcessingMetrics = getRowProcessingMetrics();
            final ComponentMetrics metrics = rowProcessingMetrics.getAnalysisJobMetrics().getComponentMetrics(
                    componentJob);
            analysisListener.componentBegin(_analysisJob, componentJob, metrics);

            if (consumer instanceof TransformerConsumer) {
                ((TransformerConsumer) consumer).setRowIdGenerator(idGenerator);
            }
        }
        final List<RowProcessingConsumer> consumers = queryOptimizer.getOptimizedConsumers();
        final Collection<? extends FilterOutcome> availableOutcomes = queryOptimizer.getOptimizedAvailableOutcomes();
        final ConsumeRowHandler consumeRowHandler = new ConsumeRowHandler(consumers, availableOutcomes);
        return consumeRowHandler;
    }

    public void registerAnalyzer(Analyzer<?> analyzer, AnalyzerJob analyzerJob, InputColumn<?>[] inputColumns) {
        registerConsumer(new AnalyzerConsumer(analyzer, analyzerJob, inputColumns, this));
    }

    public void registerTransformer(Transformer transformer, TransformerJob transformerJob,
            InputColumn<?>[] inputColumns) {
        registerConsumer(new TransformerConsumer(transformer, transformerJob, inputColumns, this));
    }

    public void registerFilter(final Filter<?> filter, final FilterJob filterJob, final InputColumn<?>[] inputColumns) {
        registerConsumer(new FilterConsumer(filter, filterJob, inputColumns, this));
    }

    private void registerConsumer(final RowProcessingConsumer consumer) {
        _consumers.add(consumer);
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

    public RowProcessingConsumer getConsumer(ComponentJob componentJob) {
        for (RowProcessingConsumer consumer : _consumers) {
            if (componentJob.equals(consumer.getComponentJob())) {
                return consumer;
            }
        }
        return null;
    }

    public List<RowProcessingConsumer> getConsumers() {
        return Collections.unmodifiableList(_consumers);
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
        final List<RowProcessingConsumer> configurableConsumers = getConsumers();

        final int numConsumers = configurableConsumers.size();

        // add tasks for closing components
        final TaskListener closeTaskListener = new JoinTaskListener(numConsumers, finishedTaskListener);
        final List<TaskRunnable> closeTasks = new ArrayList<TaskRunnable>(numConsumers);
        for (RowProcessingConsumer consumer : configurableConsumers) {
            closeTasks.add(createCloseTask(consumer, closeTaskListener));
        }

        final TaskListener getResultCompletionListener = new ForkTaskListener("collect results", _taskRunner,
                closeTasks);

        // add tasks for collecting results
        final TaskListener getResultTaskListener = new JoinTaskListener(numConsumers, getResultCompletionListener);
        final List<TaskRunnable> getResultTasks = new ArrayList<TaskRunnable>();
        for (RowProcessingConsumer consumer : configurableConsumers) {
            final Task collectResultTask = createCollectResultTask(consumer, resultQueue);
            if (collectResultTask == null) {
                getResultTasks.add(new TaskRunnable(null, getResultTaskListener));
            } else {
                getResultTasks.add(new TaskRunnable(collectResultTask, getResultTaskListener));
            }
        }

        final TaskListener runCompletionListener = new ForkTaskListener("run row processing", _taskRunner,
                getResultTasks);

        final RowProcessingMetrics rowProcessingMetrics = getRowProcessingMetrics();
        final RunRowProcessingPublisherTask runTask = new RunRowProcessingPublisherTask(this, rowProcessingMetrics);

        final TaskListener initFinishedListener = new RunNextTaskTaskListener(_taskRunner, runTask,
                runCompletionListener);

        // kick off the initialization
        initializeConsumers(initFinishedListener);
    }

    /**
     * Initializes consumers of this {@link RowProcessingPublisher}. Once
     * consumers are initialized, row processing can begin, expected rows can be
     * calculated and more.
     * 
     * @param finishedListener
     */
    public void initializeConsumers(TaskListener finishedListener) {
        final List<RowProcessingConsumer> configurableConsumers = getConsumers();
        final int numConfigurableConsumers = configurableConsumers.size();
        final TaskListener initFinishedListener = new JoinTaskListener(numConfigurableConsumers, finishedListener);
        for (RowProcessingConsumer consumer : configurableConsumers) {
            TaskRunnable task = createInitTask(consumer, initFinishedListener);
            _taskRunner.run(task);
        }
    }

    /**
     * Closes consumers of this {@link RowProcessingPublisher}. Usually this
     * will be done automatically when
     * {@link #runRowProcessing(Queue, TaskListener)} is invoked.
     */
    public void closeConsumers() {
        final List<RowProcessingConsumer> configurableConsumers = getConsumers();
        for (RowProcessingConsumer consumer : configurableConsumers) {
            TaskRunnable task = createCloseTask(consumer, null);
            _taskRunner.run(task);
        }
    }

    private Task createCollectResultTask(RowProcessingConsumer consumer, Queue<JobAndResult> resultQueue) {
        final Object component = consumer.getComponent();
        if (component instanceof HasAnalyzerResult) {
            final HasAnalyzerResult<?> hasAnalyzerResult = (HasAnalyzerResult<?>) component;
            final AnalysisListener analysisListener = _publishers.getAnalysisListener();
            return new CollectResultsTask(hasAnalyzerResult, _analysisJob, consumer.getComponentJob(), resultQueue,
                    analysisListener);
        }
        return null;
    }

    private TaskRunnable createCloseTask(RowProcessingConsumer consumer, TaskListener closeTaskListener) {
        final LifeCycleHelper lifeCycleHelper = getConsumerSpecificLifeCycleHelper(consumer);
        return new TaskRunnable(null, new CloseTaskListener(lifeCycleHelper, consumer, _successful, closeTaskListener));
    }

    private TaskRunnable createInitTask(RowProcessingConsumer consumer, TaskListener listener) {
        final LifeCycleHelper lifeCycleHelper = getConsumerSpecificLifeCycleHelper(consumer);
        final InitializeTask task = new InitializeTask(lifeCycleHelper, consumer, this);
        return new TaskRunnable(task, listener);
    }

    private LifeCycleHelper getConsumerSpecificLifeCycleHelper(RowProcessingConsumer consumer) {
        final LifeCycleHelper outerLifeCycleHelper = _publishers.getLifeCycleHelper();
        final boolean includeNonDistributedTasks = outerLifeCycleHelper.isIncludeNonDistributedTasks();
        final InjectionManager outerInjectionManager = outerLifeCycleHelper.getInjectionManager();
        final ReferenceDataActivationManager referenceDataActivationManager = getConsumerSpecificReferenceDataActivationManager(
                consumer, outerLifeCycleHelper);
        final ContextAwareInjectionManager injectionManager = new ContextAwareInjectionManager(outerInjectionManager,
                _analysisJob, consumer.getComponentJob(), _publishers.getAnalysisListener());

        final LifeCycleHelper lifeCycleHelper = new LifeCycleHelper(injectionManager, referenceDataActivationManager,
                includeNonDistributedTasks);
        return lifeCycleHelper;
    }

    private ReferenceDataActivationManager getConsumerSpecificReferenceDataActivationManager(
            RowProcessingConsumer consumer, LifeCycleHelper outerLifeCycleHelper) {
        ReferenceDataActivationManager manager = _referenceDataActivationManagers.get(consumer);
        if (manager == null) {
            manager = new ReferenceDataActivationManager();
            _referenceDataActivationManagers.put(consumer, manager);
        }
        return manager;
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

    public AnalysisJob getAnalysisJob() {
        return _analysisJob;
    }

    public Datastore getDatastore() {
        return _analysisJob.getDatastore();
    }

    public AnalysisListener getAnalysisListener() {
        return _publishers.getAnalysisListener();
    }

    public SourceColumnFinder getSourceColumnFinder() {
        return _publishers.getSourceColumnFinder();
    }
}
