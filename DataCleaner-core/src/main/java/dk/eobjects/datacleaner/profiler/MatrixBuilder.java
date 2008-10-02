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
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dk.eobjects.metamodel.util.ObjectComparator;

/**
 * MatrixBuilder is a convenient helper class to build matrices. Think of it
 * like the equivalent to the StringBuilder class, which is used to concatenate,
 * substring etc. Strings
 */
public class MatrixBuilder {

	public static final short ASCENDING = 0;
	public static final short DESCENDING = 1;
	private static Log _log = LogFactory.getLog(MatrixBuilder.class);
	private List<String> _columnNames = new ArrayList<String>();
	private List<String> _rowNames = new ArrayList<String>();
	private List<List<MatrixValue>> _content = new ArrayList<List<MatrixValue>>();

	public MatrixBuilder() {
	}

	public MatrixValue[] addColumn(String columnName, Object... values)
			throws IllegalArgumentException {
		if (values.length != _rowNames.size()) {
			throw new IllegalArgumentException(
					"Invalid size of values, expected: " + _rowNames.size()
							+ ", actual: " + values.length);
		}

		_columnNames.add(columnName);
		MatrixValue[] result = new MatrixValue[_content.size()];
		for (int i = 0; i < _content.size(); i++) {
			List<MatrixValue> row = _content.get(i);
			MatrixValue mv = new MatrixValue(values[i]);
			row.add(mv);
			result[i] = mv;
		}
		return result;
	}

	public MatrixValue[] addRow(String rowName, Object... values)
			throws IllegalArgumentException {
		if (values.length != _columnNames.size()) {
			throw new IllegalArgumentException(
					"Invalid size of values, expected: " + _columnNames.size()
							+ ", actual: " + values.length);
		}

		_rowNames.add(rowName);
		ArrayList<MatrixValue> row = new ArrayList<MatrixValue>();
		for (Object value : values) {
			row.add(new MatrixValue(value));
		}

		_content.add(row);
		return row.toArray(new MatrixValue[row.size()]);
	}

	public IMatrix getMatrix() {
		SimpleMatrix matrix = new SimpleMatrix(_columnNames
				.toArray(new String[_columnNames.size()]), _rowNames
				.toArray(new String[_rowNames.size()]), getContentArray());
		return matrix;
	}

	private MatrixValue[][] getContentArray() {
		MatrixValue[][] contentArray = new MatrixValue[_rowNames.size()][_columnNames
				.size()];
		for (int i = 0; i < _content.size(); i++) {
			List<MatrixValue> row = _content.get(i);
			for (int j = 0; j < row.size(); j++) {
				MatrixValue value = row.get(j);
				contentArray[i][j] = value;
			}
		}
		return contentArray;
	}

	public MatrixValue replaceValue(int rowIndex, int columnIndex, Object value)
			throws IllegalArgumentException {
		try {
			List<MatrixValue> row = _content.get(rowIndex);
			MatrixValue mv = new MatrixValue(value);
			row.set(columnIndex, mv);
			return mv;
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public MatrixValue replaceValue(String columnName, String rowName,
			Object value) throws IllegalArgumentException {
		int columnIndex = ArrayUtils.indexOf(_columnNames
				.toArray(new String[_columnNames.size()]), columnName);
		int rowIndex = ArrayUtils.indexOf(_rowNames
				.toArray(new String[_rowNames.size()]), rowName);
		return replaceValue(rowIndex, columnIndex, value);
	}

	public MatrixValue getMatrixValue(int rowIndex, int columnIndex) {
		return _content.get(rowIndex).get(columnIndex);
	}

	public void sortColumn(int columnIndex, short direction) {
		Comparator<Object> comparator = ObjectComparator.getComparator();

		Object[] unSortedColumn = new Object[_rowNames.size()];
		for (int i = 0; i < _content.size(); i++) {
			List<MatrixValue> row = _content.get(i);
			Object value = row.get(columnIndex);
			unSortedColumn[i] = value;
		}
		_log.debug("Unsorted column: " + ArrayUtils.toString(unSortedColumn));
		Object[] sortedColumn = unSortedColumn.clone();
		Arrays.sort(sortedColumn, comparator);
		if (direction == DESCENDING) {
			ArrayUtils.reverse(sortedColumn);
		}
		_log.debug("Sorted column: " + ArrayUtils.toString(sortedColumn));

		Integer[] newIndexes = new Integer[_rowNames.size()];
		Arrays.fill(newIndexes, -1);
		HashMap<Object, Integer> valueCount = new HashMap<Object, Integer>();
		for (int unsortedIndex = 0; unsortedIndex < unSortedColumn.length; unsortedIndex++) {
			Object value = unSortedColumn[unsortedIndex];
			Integer count = valueCount.get(value);
			if (count == null) {
				count = 0;
			}
			count = count + 1;
			valueCount.put(value, count);
			int sortedIndex = ArrayUtils.indexOf(sortedColumn, value);
			newIndexes[unsortedIndex] = sortedIndex - 1 + count;
		}
		valueCount = null;

		_log.debug("New indexes : " + ArrayUtils.toString(newIndexes));

		MatrixValue[][] origContent = getContentArray();
		for (int i = 0; i < origContent.length; i++) {
			Object[] origRow = origContent[i];
			for (int j = 0; j < origRow.length; j++) {
				MatrixValue oldValue = origContent[i][j];
				_content.get(newIndexes[i]).set(j, oldValue);
			}
		}

		String[] origRowNames = _rowNames.toArray(new String[_rowNames.size()]);
		for (int i = 0; i < origRowNames.length; i++) {
			_rowNames.set(newIndexes[i], origRowNames[i]);
		}
	}

	public void sortColumn(String columnName, short direction) {
		int columnIndex = ArrayUtils.indexOf(_columnNames
				.toArray(new String[_columnNames.size()]), columnName);
		sortColumn(columnIndex, direction);
	}

	public boolean isEmpty() {
		return (_columnNames.size() == 0 || _rowNames.size() == 0);
	}

	public int getRowCount() {
		return _rowNames.size();
	}

	public int getColumnCount() {
		return _columnNames.size();
	}
}