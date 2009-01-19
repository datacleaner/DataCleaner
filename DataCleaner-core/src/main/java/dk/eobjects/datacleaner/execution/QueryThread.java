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

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.data.DataSet;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.Query;
import dk.eobjects.metamodel.query.SelectItem;

class QueryThread<P> extends StoppableThread {

	private final SelectItem _countAllItem = SelectItem.getCountAllItem();

	private ProcessorDelegate<?, P> _processorDelegate;
	private Query _query;
	private DataContext _dataContext;

	public QueryThread(ThreadGroup group, String name, Query query,
			ProcessorDelegate<?, P> processorDelegate) {
		super(group, name);
		_query = query;
		_processorDelegate = processorDelegate;
	}

	public void setDataContext(DataContext dataContext) {
		_dataContext = dataContext;
	}

	public ProcessorDelegate<?, P> getProcessorDelegate() {
		return _processorDelegate;
	}

	public Query getQuery() {
		return _query;
	}

	public DataContext getDataContext() {
		return _dataContext;
	}

	@Override
	public void run() {
		DataSet data = null;
		try {
			if (keepRunning()) {
				data = _dataContext.executeQuery(_query);
				while (keepRunning() && data.next()) {
					Row row = data.getRow();
					Long count;
					Object countValue = row.getValue(_countAllItem);
					if (countValue == null) {
						count = 1l;
					} else if (countValue instanceof Long) {
						count = (Long) countValue;
					} else if (countValue instanceof Number) {
						count = ((Number) countValue).longValue();
					} else {
						count = new Long(countValue.toString());
					}

					_processorDelegate.processRow(row, count);
				}
			}
			_processorDelegate.threadFinished();
		} finally {
			if (data != null) {
				data.close();
			}
		}
	}
}
