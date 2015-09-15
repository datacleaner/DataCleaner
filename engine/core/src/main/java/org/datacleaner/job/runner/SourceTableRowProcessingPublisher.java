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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
import org.datacleaner.api.InputColumn;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.data.MetaModelInputRow;
import org.datacleaner.job.concurrent.ForkTaskListener;
import org.datacleaner.job.concurrent.RunNextTaskTaskListener;
import org.datacleaner.job.concurrent.TaskListener;
import org.datacleaner.job.concurrent.TaskRunnable;
import org.datacleaner.job.tasks.ConsumeRowTask;
import org.datacleaner.job.tasks.RunRowProcessingPublisherTask;
import org.datacleaner.util.SystemProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link RowProcessingPublisher} implementation for source {@link Table}s.
 */
public final class SourceTableRowProcessingPublisher extends AbstractRowProcessingPublisher {

    private final static Logger logger = LoggerFactory.getLogger(SourceTableRowProcessingPublisher.class);

    private final Set<Column> _physicalColumns = new LinkedHashSet<Column>();
    private final LazyRef<RowProcessingQueryOptimizer> _queryOptimizerRef;

    /**
     * Constructor to use for creating a
     * {@link SourceTableRowProcessingPublisher} which feeds data from a source
     * datastore.
     * 
     * @param publishers
     * @param stream
     */
    public SourceTableRowProcessingPublisher(RowProcessingPublishers publishers, RowProcessingStream stream) {
        super(publishers, stream);

        _queryOptimizerRef = createQueryOptimizerRef();

        final boolean aggressiveOptimizeSelectClause = SystemProperties.getBoolean(
                SystemProperties.QUERY_SELECTCLAUSE_OPTIMIZE, false);
        if (!aggressiveOptimizeSelectClause) {
            final Collection<InputColumn<?>> sourceColumns = stream.getAnalysisJob().getSourceColumns();
            final List<Column> columns = new ArrayList<Column>();
            for (InputColumn<?> sourceColumn : sourceColumns) {
                final Column column = sourceColumn.getPhysicalColumn();
                if (column != null && getTable().equals(column.getTable())) {
                    columns.add(column);
                }
            }

            addPhysicalColumns(columns.toArray(new Column[columns.size()]));
        }
    }

    private Table getTable() {
        return getStream().getTable();
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
        final Column[] primaryKeyColumns = getTable().getPrimaryKeys();
        if (primaryKeyColumns == null || primaryKeyColumns.length == 0) {
            logger.info("No primary keys defined for table {}, not pre-selecting primary keys", getTable().getName());
            return;
        }

        final Collection<InputColumn<?>> sourceInputColumns = getAnalysisJob().getSourceColumns();
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
                final Datastore datastore = getAnalysisJob().getDatastore();
                try (final DatastoreConnection con = datastore.openConnection()) {
                    final DataContext dataContext = con.getDataContext();

                    final Column[] columnArray = _physicalColumns.toArray(new Column[_physicalColumns.size()]);
                    final Query baseQuery = dataContext.query().from(getTable()).select(columnArray).toQuery();

                    logger.debug("Base query for row processing: {}", baseQuery);

                    final RowProcessingConsumerSorter sorter = new RowProcessingConsumerSorter(getConsumers());
                    final List<RowProcessingConsumer> sortedConsumers = sorter.createProcessOrderedConsumerList();

                    // try to optimize
                    final RowProcessingQueryOptimizer optimizer = new RowProcessingQueryOptimizerImpl(datastore,
                            sortedConsumers, baseQuery);

                    return optimizer;
                } catch (RuntimeException e) {
                    logger.error("Failed to build query optimizer! {}", e.getMessage(), e);
                    throw e;
                }
            }
        };
    }

    @Override
    public void onAllConsumersRegistered() {
        // can safely load query optimizer in separate thread here
        _queryOptimizerRef.requestLoad();
    }

    public void addPhysicalColumns(Column... columns) {
        for (Column column : columns) {
            if (!getTable().equals(column.getTable())) {
                throw new IllegalArgumentException("Column does not pertain to the correct table. Expected table: "
                        + getTable() + ", actual table: " + column.getTable());
            }
            _physicalColumns.add(column);
        }
    }

    @Override
    protected RowProcessingQueryOptimizer getQueryOptimizer() {
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

    @Override
    protected boolean processRowsInternal(AnalysisListener analysisListener, RowProcessingMetrics rowProcessingMetrics) {
        final RowProcessingQueryOptimizer queryOptimizer = getQueryOptimizer();
        final Query finalQuery = queryOptimizer.getOptimizedQuery();

        final RowIdGenerator idGenerator;
        if (finalQuery.getFirstRow() == null) {
            idGenerator = new SimpleRowIdGenerator();
        } else {
            idGenerator = new SimpleRowIdGenerator(finalQuery.getFirstRow());
        }

        final ConsumeRowHandler consumeRowHandler = createConsumeRowHandler();

        final RowConsumerTaskListener taskListener = new RowConsumerTaskListener(getAnalysisJob(), analysisListener,
                getTaskRunner());

        final Datastore datastore = getAnalysisJob().getDatastore();

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
                    getTaskRunner().run(task, taskListener);

                }
            }
            taskListener.awaitTasks(numTasks);
        }

        return !taskListener.isErrornous();
    }

    @Override
    protected void runRowProcessingInternal(List<TaskRunnable> postProcessingTasks) {
        final TaskListener runCompletionListener = new ForkTaskListener("run row processing (" + getStream() + ")",
                getTaskRunner(), postProcessingTasks);

        final RowProcessingMetrics rowProcessingMetrics = getRowProcessingMetrics();
        final RunRowProcessingPublisherTask runTask = new RunRowProcessingPublisherTask(this, rowProcessingMetrics);

        final TaskListener initFinishedListener = new RunNextTaskTaskListener(getTaskRunner(), runTask,
                runCompletionListener);

        final TaskListener consumerInitFinishedListener = new RunNextTaskTaskListener(getTaskRunner(),
                new FireRowProcessingBeginTask(this, rowProcessingMetrics), initFinishedListener);

        // kick off the initialization
        initializeConsumers(consumerInitFinishedListener);
    }
}
