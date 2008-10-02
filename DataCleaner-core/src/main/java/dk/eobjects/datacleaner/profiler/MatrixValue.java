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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.data.DataSet;
import dk.eobjects.metamodel.data.IRowFilter;
import dk.eobjects.metamodel.data.Row;
import dk.eobjects.metamodel.data.RowFilterDataSetStrategyWrapper;
import dk.eobjects.metamodel.query.Query;
import dk.eobjects.metamodel.query.SelectItem;

/**
 * Represents a value/measure in a matrix. Each MatrixValue consists of an
 * object (the value) and optionally some details which can be explored to
 * understand the value. Generally speaking all the "interesting" MatrixValues
 * will have associated details for the user to explore and gain insight.
 * 
 * @see IMatrix
 */
public class MatrixValue implements Comparable<MatrixValue> {

	private Object _value;
	private Query _detailQuery;
	private SelectItem[] _detailSelectItems;
	private List<Object[]> _detailRows;
	private List<IRowFilter> _filters;

	public MatrixValue(Object value) {
		setValue(value);
	}

	/**
	 * @return the value that this MatrixValue holds
	 */
	public Object getValue() {
		return _value;
	}

	/**
	 * @return true if details can be extracted to describe the value
	 */
	public boolean isDetailed() {
		return (_detailQuery != null || (_detailRows != null && _detailSelectItems != null));
	}

	/**
	 * Retrieves details about the MatrixValue
	 * 
	 * @param dataContext
	 *            the dataContext to use for retrieving the details
	 * @return a DataSet containing the details, typically some profiled rows
	 *         that explain the value/measure
	 */
	public DataSet getDetails(DataContext dataContext) {
		DataSet dataSet = null;
		if (_detailSelectItems != null && _detailRows != null) {
			dataSet = new DataSet(_detailSelectItems, _detailRows);
		}
		if (_detailQuery != null && dataContext != null) {
			dataSet = dataContext.executeQuery(_detailQuery);
		}
		if (dataSet != null && _filters != null) {
			dataSet = new DataSet(new RowFilterDataSetStrategyWrapper(dataSet,
					_filters.toArray(new IRowFilter[_filters.size()])));
		}
		return dataSet;
	}

	public MatrixValue setValue(Object value) {
		_value = value;
		return this;
	}

	public MatrixValue setDetailSource(Query detailQuery) {
		_detailQuery = detailQuery;
		return this;
	}

	public MatrixValue setDetailSource(DataSet detailDataSet) {
		if (detailDataSet != null) {
			_detailSelectItems = detailDataSet.getSelectItems();
			_detailRows = detailDataSet.toObjectArrays();
		} else {
			_detailSelectItems = null;
			_detailRows = null;
		}
		return this;
	}

	public MatrixValue addDetailRowFilter(IRowFilter filter) {
		if (_filters == null) {
			_filters = new ArrayList<IRowFilter>(1);
		}
		_filters.add(filter);
		return this;
	}

	public IRowFilter[] getDetailRowFilters() {
		if (_filters == null) {
			return new IRowFilter[0];
		}
		return _filters.toArray(new IRowFilter[_filters.size()]);
	}

	public MatrixValue addDetailRow(Row detailRow) {
		if (detailRow != null) {
			if (_detailSelectItems == null) {
				_detailSelectItems = detailRow.getSelectItems();
			}
			if (_detailRows == null) {
				_detailRows = new ArrayList<Object[]>();
			}
			_detailRows.add(detailRow.getValues());
		}
		return this;
	}

	@Override
	public String toString() {
		if (_detailQuery == null && _detailRows == null
				&& _detailSelectItems == null) {
			if (_value == null) {
				return "<null>";
			}
			return _value.toString();
		}
		ToStringBuilder tsb = new ToStringBuilder(this,
				ToStringStyle.SHORT_PREFIX_STYLE).append("value", _value);
		if (_detailQuery != null) {
			tsb.append("detailQuery", _detailQuery);
		}
		if (_detailSelectItems != null && _detailRows != null) {
			tsb.append("detailSelectItems", _detailSelectItems);
			tsb.append("detailRows", _detailRows.size());
		}
		return tsb.toString();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(_value).append(_detailQuery)
				.append(_detailRows).append(_detailSelectItems).toHashCode();
	}

	public int compareTo(MatrixValue that) {
		CompareToBuilder ctb = new CompareToBuilder();
		ctb.append(this._value, that._value);
		return ctb.toComparison();
	}
}