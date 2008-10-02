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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.MetaModelHelper;
import dk.eobjects.metamodel.data.DataSet;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.FromItem;
import dk.eobjects.metamodel.query.Query;
import dk.eobjects.metamodel.query.SelectItem;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Table;

/**
 * Abstract implementation of the IRunner interface, providing a lot of the
 * "plumbing code" for creating a runner.
 * 
 * @param <G>
 *            the type that is being configured by <E>
 * 
 */
public abstract class AbstractRunner<E extends IRunnableConfiguration, F, G>
		implements IRunner<E, F> {

	protected Log _log = LogFactory.getLog(getClass());
	protected Map<Table, List<F>> _results = new HashMap<Table, List<F>>();
	protected List<E> _configurations = new ArrayList<E>();
	private List<IProgressObserver> _progressObservers = new ArrayList<IProgressObserver>();

	public void addConfiguration(E configuration) {
		_configurations.add(configuration);
	}

	public void execute(DataContext dataContext) {
		try {

			if (_log.isDebugEnabled()) {
				_log.debug("execute() beginning");
				_log.debug("data context: " + dataContext);
				_log.debug("progress observers: "
						+ ArrayUtils.toString(_progressObservers));
			}

			List<Column> columns = getAllColumns();
			Table[] tables = MetaModelHelper.getTables(columns);
			initObservers(tables);
			for (int i = 0; i < tables.length; i++) {
				Table table = tables[i];
				if (_log.isDebugEnabled()) {
					_log.debug("querying table: " + table.getName());
				}
				notifyExecutionBegin(table);
				try {

					Map<E, Column[]> configurations = getConfigurationsForTable(table);
					G[] processors = initConfigurations(configurations);

					Column[] columnsToQuery = getColumnsToQuery(configurations);
					if (_log.isDebugEnabled()) {
						_log.debug("querying columns: "
								+ ArrayUtils.toString(columnsToQuery));
					}

					Query q = new Query();
					q.from(new FromItem(table).setAlias("t"));
					q.select(columnsToQuery);
					SelectItem countAllItem = SelectItem.getCountAllItem();
					q.select(countAllItem);
					q.groupBy(columnsToQuery);
					DataSet data = dataContext.executeQuery(q);

					int rowNumber = 0;
					while (data.next()) {
						Row row = data.getRow();
						Long count;
						Object countValue = row.getValue(countAllItem);
						if (countValue instanceof Long) {
							count = (Long) countValue;
						} else if (countValue instanceof Number) {
							count = ((Number) countValue).longValue();
						} else {
							count = new Long(countValue.toString());
						}
						for (int j = 0; j < processors.length; j++) {
							G processor = processors[j];
							processRow(row, count, processor);
						}
						rowNumber++;
						if (rowNumber % 500 == 0) {
							if (_log.isInfoEnabled()) {
								_log
										.info("Processing row number: "
												+ rowNumber);
							}
						}
					}

					for (int j = 0; j < processors.length; j++) {
						G processor = processors[j];
						F result = getResult(processor);
						addResultForTable(table, result);
					}
				} catch (Throwable t) {
					_log.error(t);
					notifyExecutionFailed(table, t);
				}
				notifyExecutionSuccess(table);
			}

			if (_log.isDebugEnabled()) {
				_log.debug("execute() finished");
			}
		} catch (Throwable t) {
			// This shouldn't be possible, but to comply with the no-exceptions
			// requirement, we add logging.
			_log.fatal(t);
		}
	}

	private void initObservers(Table[] tables) {
		for (IProgressObserver observer : _progressObservers) {
			observer.init(tables);
		}
	}

	private void notifyExecutionSuccess(Table table) {
		for (IProgressObserver observer : _progressObservers) {
			observer.notifyExecutionSuccess(table);
		}
	}

	private void notifyExecutionFailed(Table table, Throwable t) {
		for (IProgressObserver observer : _progressObservers) {
			observer.notifyExecutionFailed(table, t);
		}
	}

	private void notifyExecutionBegin(Table table) {
		for (IProgressObserver observer : _progressObservers) {
			observer.notifyExecutionBegin(table);
		}
	}

	protected abstract G[] initConfigurations(Map<E, Column[]> configurations);

	protected abstract void processRow(Row row, long count, G processor);

	protected abstract F getResult(G processor);

	private Column[] getColumnsToQuery(Map<E, Column[]> configurations) {
		List<Column> result = new ArrayList<Column>();
		for (Entry<E, Column[]> entry : configurations.entrySet()) {
			Column[] columns = entry.getValue();
			for (int i = 0; i < columns.length; i++) {
				if (!result.contains(columns[i])) {
					result.add(columns[i]);
				}
			}
		}
		return result.toArray(new Column[result.size()]);
	}

	private Map<E, Column[]> getConfigurationsForTable(Table table) {
		Map<E, Column[]> result = new HashMap<E, Column[]>();
		for (E configuration : _configurations) {
			Column[] columns = configuration.getColumns();
			Column[] tableColumns = MetaModelHelper.getTableColumns(table,
					columns);
			if (tableColumns.length > 0) {
				result.put(configuration, tableColumns);
			}
		}
		return result;
	}

	public List<F> getResults() {
		List<F> results = new ArrayList<F>();
		Collection<List<F>> values = _results.values();
		for (List<F> list : values) {
			results.addAll(list);
		}
		return results;
	}

	protected List<Column> getAllColumns() {
		List<Column> result = new ArrayList<Column>();
		for (IRunnableConfiguration configuration : _configurations) {
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

	public List<F> getResultsForTable(Table table) {
		return _results.get(table);
	}

	protected void addResultForTable(Table table, F result) {
		List<F> resultsForTable = _results.get(table);
		if (resultsForTable == null) {
			resultsForTable = new ArrayList<F>();
		}
		resultsForTable.add(result);
		_results.put(table, resultsForTable);
	}

	public void addProgressObserver(IProgressObserver observer) {
		_progressObservers.add(observer);
	}

	public void removeProgressObserver(IProgressObserver observer) {
		_progressObservers.remove(observer);
	}
}