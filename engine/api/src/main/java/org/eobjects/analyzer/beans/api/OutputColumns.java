/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Neopost - Customer Information Management
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.eobjects.analyzer.beans.api;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Represents the output columns yielded by a Transformer given a certain
 * configuration.
 * 
 * By default the output columns of a transformer will have the type specified
 * by the generic argument (E) in {@link Transformer}<E>. The column type can
 * however be overridden using the output columns, by setting specific column
 * types per column index using the {@link #setColumnType(int, Class)} method.
 */
public final class OutputColumns implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Factory/convenience method for retrieving a single output column
	 * instance.
	 * 
	 * @return a single output column instance.
	 */
	public static OutputColumns singleOutputColumn() {
		return new OutputColumns(1);
	}

	private final String[] columnNames;
	private final Class<?>[] columnTypes;

	/**
	 * Constructs an OutputColumns object with a variable amount of anonymous
	 * columns.
	 * 
	 * @param columns
	 *            the amount of columns.
	 */
	public OutputColumns(int columns) {
		if (columns < 1) {
			throw new IllegalArgumentException("columns must be 1 or higher");
		}
		columnNames = new String[columns];
		columnTypes = new Class[columns];
	}

	/**
	 * Constructs an OutputColumns object with named columns.
	 * 
	 * @param columnNames
	 *            the names of the output columns.
	 */
	public OutputColumns(String[] columnNames) {
		this(columnNames, null);
	}

	/**
	 * Constructs an OutputColumns object with named columns.
	 * 
	 * @param firstColumnName
	 *            the first column name
	 * @param additionalColumnNames
	 *            the additional column names (varargs)
	 */
	public OutputColumns(String firstColumnName, String... additionalColumnNames) {
		int length = additionalColumnNames.length + 1;
		columnNames = new String[length];
		columnNames[0] = firstColumnName;
		for (int i = 0; i < additionalColumnNames.length; i++) {
			columnNames[i + 1] = additionalColumnNames[i];
		}
		columnTypes = new Class[length];
	}

	/**
	 * Constructs an OutputColumns object with named columns.
	 * 
	 * @param columnNames
	 *            the names of the output columns.
	 * @param columnTypes
	 *            the types of the output columns.
	 */
	public OutputColumns(String[] columnNames, Class<?>[] columnTypes) {
		if (columnNames == null) {
			throw new IllegalArgumentException("Arguments cannot be null");
		}
		if (columnNames.length < 1) {
			throw new IllegalArgumentException("Column names length must be 1 or greater");
		}
		if (columnTypes == null) {
			columnTypes = new Class[columnNames.length];
		}
		if (columnNames.length != columnTypes.length) {
			throw new IllegalArgumentException("Column names and column types must have equal length");
		}
		this.columnNames = columnNames.clone();
		this.columnTypes = columnTypes.clone();
	}

	/**
	 * Gets the column type (if specified) by index
	 * 
	 * @param index
	 *            the index of the column
	 * @return the type (if specified) of the column
	 */
	public Class<?> getColumnType(int index) {
		return columnTypes[index];
	}

	/**
	 * Sets the type of a column.
	 * 
	 * @param index
	 *            the index of a column
	 * @param type
	 *            the column type
	 */
	public void setColumnType(int index, Class<?> type) {
		columnTypes[index] = type;
	}

	/**
	 * Gets the column name of a column by index
	 * 
	 * @param index
	 *            the index of the column
	 * @return the name of the column
	 */
	public String getColumnName(int index) {
		return columnNames[index];
	}

	/**
	 * Sets the name of a column.
	 * 
	 * @param index
	 *            the index of a column
	 * @param name
	 *            the column name
	 */
	public void setColumnName(int index, String name) {
		columnNames[index] = name;
	}

	/**
	 * Gets the amount of columns in this OutputColumns object
	 * 
	 * @return an integer representing the amount of columns available
	 */
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public String toString() {
		return "OutputColumns" + Arrays.toString(columnNames);
	}
}
