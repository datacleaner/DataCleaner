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
package org.datacleaner.api;

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
public class OutputColumns implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String[] columnNames;
    private final Class<?>[] columnTypes;

    /**
     * Constructs an OutputColumns object with a variable amount of anonymous
     * columns.
     * 
     * @param columns
     *            the amount of columns.
     * 
     * @deprecated use {@link #OutputColumns(int, Class)} instead.
     */
    @Deprecated
    public OutputColumns(int columns) {
        this(columns, Object.class);
    }

    /**
     * Constructs an OutputColumns object with a variable amount of anonymous
     * columns of the same type.
     * 
     * @param columns
     *            the amount of columns.
     * @param columnType
     *            the data type of the columns.
     */
    public OutputColumns(int columns, Class<?> columnType) {
        if (columns < 1) {
            throw new IllegalArgumentException("columns must be 1 or higher");
        }
        columnNames = new String[columns];
        columnTypes = new Class[columns];
        for (int i = 0; i < columnTypes.length; i++) {
            columnTypes[i] = columnType;
        }
    }

    /**
     * Constructs an OutputColumns object with named columns.
     * 
     * @param columnNames
     *            the names of the output columns.
     * @deprecated use {@link #OutputColumns(String[], Class[])} instead.
     */
    @Deprecated
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
     * 
     * @deprecated use {@link #OutputColumns(String[], Class[])} instead
     */
    public OutputColumns(String firstColumnName, String... additionalColumnNames) {
        this(Object.class, firstColumnName, additionalColumnNames);
    }

    public OutputColumns(Class<?> columnType, String firstColumnName, String... additionalColumnNames) {
        int length = additionalColumnNames.length + 1;
        columnNames = new String[length];
        columnTypes = new Class[length];

        columnNames[0] = firstColumnName;
        columnTypes[0] = columnType;
        for (int i = 0; i < additionalColumnNames.length; i++) {
            columnNames[i + 1] = additionalColumnNames[i];
            columnTypes[i + 1] = columnType;
        }
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
     * Constructs an OutputColumns object with named columns.
     * 
     * @param columnType
     *            the column type of all columns
     * @param columnNames
     *            the names of the columns
     */
    public OutputColumns(Class<?> columnType, String[] columnNames) {
        if (columnNames == null || columnType == null) {
            throw new IllegalArgumentException("Arguments cannot be null");
        }
        if (columnNames.length < 1) {
            throw new IllegalArgumentException("Column names length must be 1 or greater");
        }
        this.columnNames = columnNames.clone();
        this.columnTypes = new Class[columnNames.length];
        for (int i = 0; i < columnTypes.length; i++) {
            columnTypes[i] = columnType;
        }
    }

    /**
     * Gets the column type (if specified) by index
     * 
     * @param index
     *            the index of the column
     * @return the type (if specified) of the column
     */
    public Class<?> getColumnType(int index) {
        Class<?> cls = columnTypes[index];
        if (cls == null) {
            return Object.class;
        }
        return cls;
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
