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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dk.eobjects.datacleaner.data.DataContextSelection;
import dk.eobjects.metamodel.query.Query;
import dk.eobjects.metamodel.schema.Table;

public final class QueryThreadManager<P> {

	private static final int SLEEP_MILLIS = 200;

	private static final Log _log = LogFactory.getLog(QueryThreadManager.class);

	private List<QueryThread<P>> _queryThreads = new ArrayList<QueryThread<P>>();
	private ThreadGroup _threadGroup;
	private ExecutionConfiguration _executionConfiguration;
	private DataContextSelection _dataContextSelection;
	private Error _error;

	public QueryThreadManager(DataContextSelection dataContextSelection,
			ExecutionConfiguration executionConfiguration) {
		_dataContextSelection = dataContextSelection;
		_executionConfiguration = executionConfiguration;
		_threadGroup = new ThreadGroup("querythread-group") {

			@SuppressWarnings("unchecked")
			@Override
			public void uncaughtException(Thread thread, Throwable throwable) {
				_log.error("uncaught exception in thread", throwable);

				QueryThread<P> queryThread = (QueryThread) thread;
				queryThread.getProcessorDelegate().handleException(throwable);
				
				if (throwable instanceof Error) {
					// QueryThreads catch most known exceptions so if this is an
					// Error (such as an OutOfMemoryError) we need to shut down
					// all threads.
					stopAllThreads();
					_error = (Error) throwable;
				}
			}
		};
	}
	
	public Error getError() {
		return _error;
	}

	public void addThreadsForQueries(List<Query> queries,
			ProcessorDelegate<?, P> processorDelegate) {
		int i = 1;
		for (Query query : queries) {
			String threadName = new StringBuilder().append("querythread-")
					.append(i).toString();
			QueryThread<P> queryThread = new QueryThread<P>(_threadGroup,
					threadName, query, processorDelegate);
			_queryThreads.add(queryThread);
			i++;
		}
	}

	public void runThreads() {
		List<DataContextProvider<P>> providers = new LinkedList<DataContextProvider<P>>();
		// if there are less queries than number of max connections, only create
		// connections for the corresponding number of queries
		int numConnections = Math.min(_queryThreads.size(),
				_executionConfiguration.getMaxConnections());
		int maxQueriesPerConnection = _executionConfiguration
				.getMaxQueriesPerConnection();
		for (int i = 0; i < numConnections; i++) {
			DataContextSelection dataContextSelection;
			if (i == 0) {
				dataContextSelection = _dataContextSelection;
			} else {
				try {
					dataContextSelection = _dataContextSelection.duplicate();
				} catch (SQLException e) {
					// This shouldn't be possible because the Executor validates
					// and corrects the ExecutorConfiguration if the DataContext
					// is not duplicatable.
					throw new IllegalStateException(
							"Unexpected un-duplicatable DataContext", e);
				}
			}
			providers.add(new DataContextProvider<P>(dataContextSelection,
					maxQueriesPerConnection));
		}

		for (QueryThread<P> queryThread : _queryThreads) {
			boolean provided = false;
			while (!provided) {
				for (DataContextProvider<P> dataContextProvider : providers) {
					if (dataContextProvider
							.provideDataContextAndStart(queryThread)) {
						provided = true;
						break;
					}
				}
				if (!provided) {
					try {
						Thread.sleep(SLEEP_MILLIS);
					} catch (InterruptedException e) {
						_log.debug(e);
					}
				}
			}
		}
	}

	public void waitForThreads() {
		LinkedList<QueryThread<P>> activeThreads = new LinkedList<QueryThread<P>>(
				_queryThreads);
		while (!activeThreads.isEmpty()) {
			for (Iterator<QueryThread<P>> it = activeThreads.iterator(); it
					.hasNext();) {
				QueryThread<P> queryThread = it.next();
				if (!queryThread.isAlive()) {
					it.remove();
				}
			}
			if (!activeThreads.isEmpty()) {
				try {
					Thread.sleep(SLEEP_MILLIS);
				} catch (InterruptedException e) {
					_log.debug(e);
				}
			}
		}
	}

	public void stopAllThreads() {
		for (QueryThread<P> queryThread : _queryThreads) {
			queryThread.safeStop(false);
		}
	}

	public void stopThreadsForTable(Table table) {
		for (QueryThread<P> queryThread : _queryThreads) {
			if (table == queryThread.getProcessorDelegate().getTable()) {
				queryThread.safeStop(false);
			}
		}
	}
}
