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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import dk.eobjects.datacleaner.data.DataContextSelection;

/**
 * This class acts as a gatekeeper and pool for a DataContext. It provides the
 * DataContext if there are less threads using the DataContext than the
 * maxQueriesPerConnection property allows.
 * 
 * @author kasper
 * 
 * @param <P>
 */
class DataContextProvider<P> {

	private DataContextSelection _dataContextSelection;
	private int _maxQueriesPerConnection;
	private List<QueryThread<P>> _threads = new LinkedList<QueryThread<P>>();

	public DataContextProvider(DataContextSelection dataContextSelection,
			int maxQueriesPerConnection) {
		if (maxQueriesPerConnection < 1) {
			throw new IllegalArgumentException(
					"maxQueriesPerConnection cannot be less than 1");
		}
		_dataContextSelection = dataContextSelection;
		_maxQueriesPerConnection = maxQueriesPerConnection;
	}

	/**
	 * 
	 * @param requestingThread
	 * @return true, if the thread was instrumented and started, or false, if
	 *         the DataContext is fully utilized already.
	 */
	public synchronized boolean provideDataContextAndStart(
			QueryThread<P> requestingThread) {
		int activeThreads = 0;
		for (Iterator<QueryThread<P>> it = _threads.iterator(); it.hasNext();) {
			QueryThread<P> thread = it.next();
			if (thread.isAlive()) {
				activeThreads++;
				if (activeThreads >= _maxQueriesPerConnection) {
					return false;
				}
			} else {
				// clean up the threads list
				it.remove();
			}
		}
		_threads.add(requestingThread);
		requestingThread.setDataContext(_dataContextSelection.getDataContext());
		requestingThread.start();
		return true;
	}
}
