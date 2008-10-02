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

import java.util.StringTokenizer;

import org.apache.commons.lang.ArrayUtils;

/**
 * Default implementation of the IMatrix interface. Please note that this
 * implementation is immutable.
 * 
 * @see MatrixBuilder
 */
class SimpleMatrix implements IMatrix {

	private String[] _columns;
	private String[] _rows;
	private MatrixValue[][] _values;

	/**
	 * Default constructor for matrices.
	 * 
	 * @param columns
	 *            the names of the columns
	 * @param rows
	 *            the names of the rows
	 * @param values
	 *            the values of the matrix
	 */
	public SimpleMatrix(String[] columns, String[] rows, MatrixValue[][] values) {
		_columns = columns;
		_rows = rows;
		_values = values;
	}

	/**
	 * Alternative constructor, using comma separated strings for columns and
	 * rows.
	 * 
	 * @param columns
	 *            a comma separated string containing the column names
	 * @param rows
	 *            a comma separated string containing the row names
	 * @param numbers
	 *            the values of the matrix
	 */
	public SimpleMatrix(String columns, String rows, MatrixValue[][] values) {
		_columns = tokenize(columns);
		_rows = tokenize(rows);
		_values = values;
	}

	private String[] tokenize(String commaSeparatedString) {
		StringTokenizer tokenizer = new StringTokenizer(commaSeparatedString,
				",");
		String[] result = new String[tokenizer.countTokens()];
		for (int i = 0; tokenizer.hasMoreTokens(); i++) {
			String token = tokenizer.nextToken();
			result[i] = token.trim();
		}
		return result;
	}

	public String[] getColumnNames() {
		return _columns;
	}

	public String[] getRowNames() {
		return _rows;
	}

	public MatrixValue getValue(int rowNumber, int columnNumber) {
		return _values[rowNumber][columnNumber];
	}

	public MatrixValue getValue(String rowName, String columnName) {
		int rowIndex = ArrayUtils.indexOf(_rows, rowName);
		int columnIndex = ArrayUtils.indexOf(_columns, columnName);
		return getValue(rowIndex, columnIndex);
	}

	public MatrixValue[][] getValues() {
		return _values;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Matrix[columnNames="
				+ ArrayUtils.toString(_columns));
		for (int i = 0; i < _rows.length; i++) {
			sb.append(',' + _rows[i] + "=" + ArrayUtils.toString(_values[i]));
		}
		sb.append(']');
		return sb.toString();
	}
}