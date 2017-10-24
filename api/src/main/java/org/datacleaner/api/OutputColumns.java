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
 */
public class OutputColumns implements Serializable {

    /**
     * Constant {@link OutputColumns} values for components with no/zero output
     * columns.
     */
    public static final OutputColumns NO_OUTPUT_COLUMNS = new OutputColumns();
    private static final long serialVersionUID = 1L;
    private final String[] columnNames;
    private final Class<?>[] columnTypes;

    /**
     * Private constructor used to create an empty output columns objects.
     */
    private OutputColumns() {
        columnNames = new String[0];
        columnTypes = new Class[0];
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
    public OutputColumns(final int columns, final Class<?> columnType) {
        if (columns < 1) {
            throw new IllegalArgumentException("columns must be 1 or higher");
        }
        columnNames = new String[columns];
        columnTypes = new Class[columns];
        for (int i = 0; i < columnTypes.length; i++) {
            columnTypes[i] = columnType;
        }
    }

    public OutputColumns(final Class<?> columnType, final String firstColumnName,
            final String... additionalColumnNames) {
        final int length = additionalColumnNames.length + 1;
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
    public OutputColumns(final String[] columnNames, Class<?>[] columnTypes) {
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
    public OutputColumns(final Class<?> columnType, final String[] columnNames) {
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
    public Class<?> getColumnType(final int index) {
        final Class<?> cls = columnTypes[index];
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
    public void setColumnType(final int index, final Class<?> type) {
        columnTypes[index] = type;
    }

    /**
     * Gets the column name of a column by index
     *
     * @param index
     *            the index of the column
     * @return the name of the column
     */
    public String getColumnName(final int index) {
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
    public void setColumnName(final int index, final String name) {
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
