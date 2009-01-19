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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import dk.eobjects.metamodel.schema.Table;

/**
 * A progress observer for benchmarking and bug-finding purposes. The progress
 * observer records all events and provides some utility functionality to
 * support investigation
 */
public class BenchmarkProgressObserver implements IProgressObserver {

	private long _initTime;
	private Map<Table, Long> _beginTime = new HashMap<Table, Long>();
	private Map<Table, Long> _beginCount = new HashMap<Table, Long>();
	private Map<Table, Long> _successTime = new HashMap<Table, Long>();
	private Map<Table, Long> _successCount = new HashMap<Table, Long>();
	private Map<Table, Long> _failureTime = new HashMap<Table, Long>();
	private Map<Table, Long> _failureCount = new HashMap<Table, Long>();
	private Map<Table, Throwable> _failureThrowables = new HashMap<Table, Throwable>();
	private Map<Table, List<Long>> _progressTimes = new HashMap<Table, List<Long>>();
	private Map<Table, List<Long>> _progressCounts = new HashMap<Table, List<Long>>();
	private Table[] _tables;

	public void init(Table[] tables) {
		_tables = tables;
		_initTime = System.currentTimeMillis();
	}

	public synchronized void notifyBeginning(Table table, long numRows) {
		if (_beginTime.containsKey(table)) {
			throw new IllegalStateException(
					"notifyBeginning has already been invoked for the table: "
							+ table);
		}
		_beginTime.put(table, System.currentTimeMillis());
		_beginCount.put(table, numRows);
	}

	public synchronized void notifySuccess(Table table, long numRowsProcessed) {
		if (_successTime.containsKey(table)) {
			throw new IllegalStateException(
					"notifySuccess has already been invoked for the table: "
							+ table);
		}
		_successTime.put(table, System.currentTimeMillis());
		_successCount.put(table, numRowsProcessed);
	}

	public synchronized void notifyFailure(Table table, Throwable throwable,
			Long lastRow) {
		if (_failureTime.containsKey(table)) {
			throw new IllegalStateException(
					"notifyFailure has already been invoked for the table: "
							+ table);
		}
		_failureTime.put(table, System.currentTimeMillis());
		_failureCount.put(table, lastRow);
		_failureThrowables.put(table, throwable);
	}

	public synchronized void notifyProgress(Table table, long numRows) {
		List<Long> progressTimes = _progressTimes.get(table);
		if (progressTimes == null) {
			progressTimes = new ArrayList<Long>();
			_progressTimes.put(table, progressTimes);
		}
		progressTimes.add(System.currentTimeMillis());
		List<Long> progressCounts = _progressCounts.get(table);
		if (progressCounts == null) {
			progressCounts = new ArrayList<Long>();
			_progressCounts.put(table, progressCounts);
		}
		progressCounts.add(numRows);
	}

	public void validateSimultaniousExecution() {
		long firstBegin = -1;
		long lastBegin = -1;
		long firstSuccess = -1;
		long lastSuccess = -1;
		for (Entry<Table, Long> entry : _beginTime.entrySet()) {
			if (firstBegin == -1) {
				firstBegin = entry.getValue();
				lastBegin = entry.getValue();
			} else {
				if (firstBegin > entry.getValue()) {
					firstBegin = entry.getValue();
				}
				if (lastBegin < entry.getValue()) {
					lastBegin = entry.getValue();
				}
			}
		}
		for (Entry<Table, Long> entry : _successTime.entrySet()) {
			if (firstSuccess == -1) {
				firstSuccess = entry.getValue();
				lastSuccess = entry.getValue();
			} else {
				if (firstSuccess > entry.getValue()) {
					firstSuccess = entry.getValue();
				}
				if (lastSuccess < entry.getValue()) {
					lastSuccess = entry.getValue();
				}
			}
		}

		if (firstSuccess < lastBegin) {
			throw new IllegalStateException(
					"cannot validate simultanious execution: firstSuccess < lastBegin");
		}
	}

	public void validateRowCount() {
		for (Table table : _tables) {
			Long beginCount = _beginCount.get(table);
			Long successCount = _successCount.get(table);
			if (beginCount == successCount) {
				throw new IllegalStateException(
						"cannot validate row count: beginCount == successCount");
			}

			long progressCount = 0;
			List<Long> progressCounts = _progressCounts.get(table);
			if (progressCounts != null) {
				for (Long count : progressCounts) {
					progressCount += count;
				}
				if (progressCount > successCount) {
					throw new IllegalStateException(
							"cannot validate row count: " + progressCount
									+ " > " + successCount);
				}
			}
		}
	}

	public long getInitTime() {
		return _initTime;
	}

	public Map<Table, Long> getBeginTime() {
		return _beginTime;
	}

	public Map<Table, Long> getBeginCount() {
		return _beginCount;
	}

	public Map<Table, Long> getSuccessTime() {
		return _successTime;
	}

	public Map<Table, Long> getSuccessCount() {
		return _successCount;
	}

	public Map<Table, Long> getFailureTime() {
		return _failureTime;
	}

	public Map<Table, Long> getFailureCount() {
		return _failureCount;
	}

	public Map<Table, Throwable> getFailureThrowables() {
		return _failureThrowables;
	}

	public Map<Table, List<Long>> getProgressTimes() {
		return _progressTimes;
	}
	
	public Map<Table, List<Long>> getProgressCounts() {
		return _progressCounts;
	}

	public Table[] getTables() {
		return _tables;
	}
}