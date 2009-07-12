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
package dk.eobjects.datacleaner.profiler;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.query.Query;
import dk.eobjects.metamodel.query.SelectItem;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Table;

public abstract class AbstractProfile implements IProfile {

	protected final Log _log = LogFactory.getLog(getClass());
	protected long _totalCount;
	protected Column[] _columns;
	protected Map<String, String> _properties;
	private Exception _error = null;
	private boolean _detailsEnabled = true;

	public void setProperties(Map<String, String> properties) {
		_properties = properties;
	}

	public void initialize(Column... columns) {
		_columns = columns;
	}

	/**
	 * Turns on or off details on matrixvalue details. As a general rule details
	 * should be enabled (and are by default), but for testing purposes and in
	 * non-interactive mode it can be beneficial to turn them off.
	 * 
	 * @param detailsEnabled
	 */
	public void setDetailsEnabled(boolean detailsEnabled) {
		_detailsEnabled = detailsEnabled;
	}

	/**
	 * @return whether or not detail data has been enabled for resulting matrix
	 *         values. As a general rule details are always enabled, but for
	 *         testing purposes and in non-interactive mode it can be beneficial
	 *         to turn them off.
	 */
	public boolean isDetailsEnabled() {
		return _detailsEnabled;
	}

	public void process(Row row, long distinctRowCount) {
		_totalCount += distinctRowCount;
		if (_error == null) {
			try {
				for (int i = 0; i < _columns.length; i++) {
					Column column = _columns[i];
					Object value = row.getValue(column);
					processValue(column, value, distinctRowCount, row);
				}
			} catch (Exception e) {
				_error = e;
			}
		}
	}

	/**
	 * Convenience method to create a base query for matrix values based on the
	 * configured columns (where the other columns are of interest).
	 */
	protected Query getBaseQuery() {
		if (_columns.length > 0) {
			Table table = _columns[0].getTable();
			Query q = new Query().select(table.getColumns()).from(table);
			return q;
		}
		return null;
	}

	/**
	 * Convenience method to create a base query for matrix values based on a
	 * specific column (where only the columns content is of interest).
	 */
	protected Query getBaseQuery(Column column) {
		return new Query().from(column.getTable()).select(
				new SelectItem(column)).selectCount().groupBy(column);
	}

	protected Integer getPropertyInteger(String propertyName) {
		Integer result = null;
		String resultString = _properties.get(propertyName);
		if (resultString != null && !"".equals(resultString)) {
			try {
				result = Integer.parseInt(resultString);
			} catch (NumberFormatException e) {
				_log.warn("Couldn't parse integer for property '"
						+ propertyName + "': " + e.getMessage());
			}
		}
		return result;
	}

	protected abstract void processValue(Column column, Object value,
			long valueCount, Row row);

	public IProfileResult getResult() {
		Class<? extends AbstractProfile> profileClass = this.getClass();
		IProfileDescriptor descriptor = ProfilerManager
				.getProfileDescriptorByProfileClass(profileClass);
		ProfileResult result = new ProfileResult(descriptor, _properties,
				_columns);
		if (_error == null) {
			if (_totalCount > 0) {
				result.setMatrices(getResultMatrices());
			} else {
				result.setError(new Exception(
						"No rows in selected data to profile!"));
			}
		} else {
			result.setError(_error);
		}
		return result;
	}

	protected abstract List<IMatrix> getResultMatrices();

	public void close() {
		//Do nothing
	}
}