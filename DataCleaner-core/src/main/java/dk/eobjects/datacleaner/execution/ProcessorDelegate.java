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
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.schema.Table;

/**
 * Helper class used to delegate processRow() method calls for a table to
 * multiple processors from multiple threads.
 * 
 * @param <R>
 *            the result object type
 * @param <P>
 *            the processor object type
 */
class ProcessorDelegate<R, P> {

	private static final Log _log = LogFactory.getLog(ProcessorDelegate.class);
	private List<P> _processors;
	private IExecutorCallback<?, R, P> _executorCallback;
	private Table _table;
	private IProgressObserver _progressObserver;
	private int _numQueries;
	private int _numSuccesses;
	private long _totalCount;
	private long _tempCount;
	private Map<Table, List<R>> _resultMap;
	private QueryThreadManager<P> _queryThreadManager;
	private boolean _succesfull;

	public ProcessorDelegate(QueryThreadManager<P> queryThreadManager,
			Table table, IProgressObserver progressObserver,
			IExecutorCallback<?, R, P> executorCallback, List<P> processors,
			Map<Table, List<R>> resultMap, int numQueries) {
		super();
		_queryThreadManager = queryThreadManager;
		_table = table;
		_progressObserver = progressObserver;
		_executorCallback = executorCallback;
		_processors = processors;
		_resultMap = resultMap;
		_numQueries = numQueries;

		_numSuccesses = 0;
		_totalCount = 0l;
		_tempCount = 0l;
		_succesfull = true;
	}

	public synchronized void threadFinished() {
		if (_succesfull) {
			_numSuccesses++;
			if (_numSuccesses == _numQueries) {
				_totalCount += _tempCount;

				List<R> results = new ArrayList<R>(_processors.size());
				for (P processor : _processors) {
					results.add(_executorCallback.getResult(processor));
				}
				_resultMap.put(_table, results);

				_progressObserver.notifySuccess(_table, _totalCount);
			}
		}
	}

	public void processRow(Row row, long count) {
		if (_succesfull) {
			synchronized (this) {
				_tempCount += count;
				if (_tempCount > 500) {
					_progressObserver.notifyProgress(_table, _tempCount);
					_totalCount += _tempCount;
					_tempCount = 0;
				}
			}
			for (P processor : _processors) {
				// We put the synchronization on the processor because most
				// IProfile and IValidationRule instances are NOT thread-safe
				synchronized (processor) {
					try {
						_executorCallback.processRow(row, count, processor);
					} catch (Exception e) {
						_log.error("Processor threw exception...");
						_log.error("Processor: " + processor);
						_log.error("Row: " + row);
						handleException(e);
					}
				}
			}
		}
	}

	public void handleException(Throwable t) {
		_succesfull = false;
		_progressObserver.notifyFailure(_table, t, _totalCount + _tempCount);
		_queryThreadManager.stopThreadsForTable(_table);
	}

	public Table getTable() {
		return _table;
	}

	public List<P> getProcessors() {
		return _processors;
	}

	public int getNumQueries() {
		return _numQueries;
	}

	public int getNumSuccesses() {
		return _numSuccesses;
	}

	public long getTotalCount() {
		return _totalCount;
	}
}
