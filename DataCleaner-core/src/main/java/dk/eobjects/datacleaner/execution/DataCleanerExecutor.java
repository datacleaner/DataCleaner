/**
 *  This file is part of DataCleaner.
 *
 *  DataCleaner is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DataCleaner is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DataCleaner.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.eobjects.datacleaner.execution;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dk.eobjects.datacleaner.data.DataContextSelection;
import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.MetaModelHelper;
import dk.eobjects.metamodel.QuerySplitter;
import dk.eobjects.metamodel.query.FromItem;
import dk.eobjects.metamodel.query.Query;
import dk.eobjects.metamodel.query.SelectItem;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Table;

/**
 * The DataCleanerExecutor is the central executing entity of DataCleaner. It
 * manages the job configurations, the executor callbacks, threading and the
 * datastore at the same time.
 * 
 * @see IExecutorCallback
 * @see ExecutionConfiguration
 * @see IJobConfiguration
 * 
 * @param <C extends IJobConfiguration> the job configuration object type
 * @param <R>
 *            the result object type
 * @param <P>
 *            the processor object type
 */
public final class DataCleanerExecutor<C extends IJobConfiguration, R, P> {

	private static final Log _log = LogFactory
			.getLog(DataCleanerExecutor.class);
	protected Map<Table, List<R>> _results;
	protected List<C> _configurations;
	protected ExecutionConfiguration _executionConfiguration;
	private ProgressObserverDelegate _progressObserver;
	private IExecutorCallback<C, R, P> _callback;

	public DataCleanerExecutor(IExecutorCallback<C, R, P> callback) {
		if (callback == null) {
			throw new IllegalArgumentException("Runner callback cannot be null");
		}
		_callback = callback;
		_results = new HashMap<Table, List<R>>();
		_configurations = new ArrayList<C>();
		_progressObserver = new ProgressObserverDelegate();
	}

	public void addJobConfiguration(C configuration) {
		_configurations.add(configuration);
	}

	public void setExecutionConfiguration(
			ExecutionConfiguration executionConfiguration) {
		_executionConfiguration = executionConfiguration;
	}

	public void execute(DataContextSelection dataContextSelection) throws Error {
		execute(dataContextSelection, true);
	}

	public void execute(DataContextSelection dataContextSelection,
			boolean synchronize) throws Error {
		if (_log.isDebugEnabled()) {
			_log.debug("execute() beginning");
			_log.debug("data context selection: " + dataContextSelection);
		}
		if (_executionConfiguration == null) {
			_executionConfiguration = new ExecutionConfiguration();
		}

		DataContext dataContext = dataContextSelection.getDataContext();

		List<Column> columns = getAllColumns();
		Table[] tables = MetaModelHelper.getTables(columns);
		_progressObserver.init(tables);

		QueryThreadManager<P> queryThreadManager = new QueryThreadManager<P>(
				dataContextSelection, _executionConfiguration);

		Map<Table, ProcessorDelegate<R, P>> processorDelegates = new HashMap<Table, ProcessorDelegate<R, P>>();
		for (int i = 0; i < tables.length; i++) {
			Table table = tables[i];

			Map<C, Column[]> configurations = getConfigurationsForTable(table);
			List<P> processors = _callback.initProcessors(configurations,
					_executionConfiguration);

			Column[] columnsToQuery = getColumnsToQuery(configurations);
			if (_log.isInfoEnabled()) {
				_log.info("Querying...");
				_log.info("\nTable:" + table);
				_log.info("\nColumns: " + ArrayUtils.toString(columnsToQuery));
			}

			Query q = new Query();
			q.from(new FromItem(table).setAlias("t"));
			q.select(columnsToQuery);

			QuerySplitter qs = new QuerySplitter(dataContext, q);
			_progressObserver.notifyBeginning(table, qs.getRowCount());

			// Group by optimization is only turned on for JDBC
			// based datastores
			if (dataContextSelection.isJdbcSource()
					&& _executionConfiguration.isGroupByOptimizationEnabled()) {
				_log.info("Using group by optimization.");
				q.select(SelectItem.getCountAllItem());
				q.groupBy(columnsToQuery);
			}
			List<Query> queries;
			if (_executionConfiguration.isQuerySplitterEnabled()) {
				_log.info("Using split query optimization.");

				// TODO: Change to long as soon as new MetaModel is
				// released
				qs.setMaxRows((int) _executionConfiguration
						.getQuerySplitterSize());

				queries = qs.splitQuery();
			} else {
				qs = null;

				queries = new LinkedList<Query>();
				queries.add(q);
			}

			ProcessorDelegate<R, P> processorDelegate = new ProcessorDelegate<R, P>(
					queryThreadManager, table, _progressObserver, _callback,
					processors, _results, queries.size());
			processorDelegates.put(table, processorDelegate);

			queryThreadManager.addThreadsForQueries(queries, processorDelegate);
		}

		queryThreadManager.runThreads();
		
		if (synchronize) {
			queryThreadManager.waitForThreads();
			if (queryThreadManager.getError() != null) {
				throw queryThreadManager.getError();
			}
		}
	}

	private Column[] getColumnsToQuery(Map<C, Column[]> configurations) {
		List<Column> result = new ArrayList<Column>();
		for (Entry<C, Column[]> entry : configurations.entrySet()) {
			Column[] columns = entry.getValue();
			for (int i = 0; i < columns.length; i++) {
				if (!result.contains(columns[i])) {
					result.add(columns[i]);
				}
			}
		}
		return result.toArray(new Column[result.size()]);
	}

	private Map<C, Column[]> getConfigurationsForTable(Table table) {
		Map<C, Column[]> result = new HashMap<C, Column[]>();
		for (C configuration : _configurations) {
			Column[] columns = configuration.getColumns();
			Column[] tableColumns = MetaModelHelper.getTableColumns(table,
					columns);
			if (tableColumns.length > 0) {
				result.put(configuration, tableColumns);
			}
		}
		return result;
	}

	public List<R> getResults() {
		List<R> results = new ArrayList<R>();
		Collection<List<R>> values = _results.values();
		for (List<R> list : values) {
			results.addAll(list);
		}
		return results;
	}

	protected List<Column> getAllColumns() {
		List<Column> result = new ArrayList<Column>();
		for (IJobConfiguration configuration : _configurations) {
			Column[] columns = configuration.getColumns();
			for (int i = 0; i < columns.length; i++) {
				Column column = columns[i];
				if (!result.contains(column)) {
					result.add(column);
				}
			}
		}
		return result;
	}

	public Table[] getResultTables() {
		Set<Table> tableSet = _results.keySet();
		return tableSet.toArray(new Table[tableSet.size()]);
	}

	public List<R> getResultsForTable(Table table) {
		return _results.get(table);
	}

	public void addProgressObserver(IProgressObserver observer) {
		_progressObserver.addProgressObserver(observer);
	}

	public void removeProgressObserver(IProgressObserver observer) {
		_progressObserver.removeProgressObserver(observer);
	}
}