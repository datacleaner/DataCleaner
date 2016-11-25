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
package org.datacleaner.components.fuse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.datacleaner.api.Convertable;
import org.datacleaner.api.InputColumn;
import org.datacleaner.util.ReflectionUtils;

/**
 * Represents a set of columns to be coalesced.
 */
@Convertable(CoalesceUnitConverter.class)
public class CoalesceUnit {

    private final String[] _inputColumnNames;

    // transient cached view of columns
    private final InputColumn<?>[] _inputColumns;

    public CoalesceUnit(final List<? extends InputColumn<?>> inputColumns) {
        this(inputColumns.toArray(new InputColumn[inputColumns.size()]));
    }

    /**
     * Create an uninitialized CoalesceUnit... The makes it into a pure factory.
     */
    public CoalesceUnit(final String... columnNames) {
        _inputColumnNames = columnNames;
        _inputColumns = null;
    }

    public CoalesceUnit(final InputColumn<?>... inputColumns) {
        if (inputColumns == null || inputColumns.length == 0) {
            throw new IllegalArgumentException("InputColumns cannot be null or empty");
        }
        _inputColumns = inputColumns;
        _inputColumnNames = getInputColumnNames(inputColumns);
    }

    private static String getSimpleName(final String name) {
        final int dotIndex = name.lastIndexOf('.');
        if (dotIndex == -1) {
            return name;
        }
        return name.substring(dotIndex + 1);
    }

    private String[] getInputColumnNames(final InputColumn<?>[] inputColumns) {
        final String[] result = new String[inputColumns.length];
        for (int i = 0; i < inputColumns.length; i++) {
            final InputColumn<?> inputColumn = inputColumns[i];
            if (inputColumn.isPhysicalColumn()) {
                result[i] = inputColumn.getPhysicalColumn().getQualifiedLabel();
            } else {
                result[i] = inputColumn.getName();
            }
        }
        return result;
    }

    public String[] getInputColumnNames() {
        if (_inputColumns != null) {
            // use updated column names if possible - they may have changed
            return getInputColumnNames(_inputColumns);
        }
        return _inputColumnNames;
    }

    /**
     * Creates new {@link CoalesceUnit} if {@link InputColumn}s has changed
     *
     * @param newInputColumns List of new {@link InputColumn}s
     * @return New {@link CoalesceUnit} if {@link InputColumn}s has truly changed, otherwise this.
     */
    public CoalesceUnit getUpdatedCoalesceUnit(final InputColumn<?>[] newInputColumns) {
        if (Arrays.equals(_inputColumns, newInputColumns)) {
            return this;
        } else {
            return new CoalesceUnit(newInputColumns);
        }
    }

    /**
     * Refreshes the current transient setup of {@link InputColumn}s in the
     * {@link CoalesceUnit}. This is necessary to do before any job execution to
     * ensure that the {@link InputColumn} references are intact and don't point
     * to e.g. a copy of the input columns from a cloned job.
     *
     * Not doing this will result in issues such as
     * <a href="https://github.com/datacleaner/DataCleaner/issues/923">issue #923</a>
     *
     * @return A new CoalesceUnit containing the updated columns.
     */
    public CoalesceUnit updateInputColumns(final InputColumn<?>[] allInputColumns) {
        return getUpdatedCoalesceUnit(getUpdatedInputColumns(allInputColumns, true));
    }

    public InputColumn<?>[] getUpdatedInputColumns(final InputColumn<?>[] allInputColumns,
            final boolean exceptionOnMissing) {
        final String[] newInputColumnNames = getInputColumnNames();
        final List<InputColumn<?>> newInputColumns = new ArrayList<>(newInputColumnNames.length);

        for (final String newInputColumnName : newInputColumnNames) {
            final InputColumn<?> updatedInputColumn = findInputColumn(allInputColumns, newInputColumnName);
            if (updatedInputColumn == null) {
                if (exceptionOnMissing) {
                    final List<String> names =
                            Arrays.stream(allInputColumns).map(InputColumn::getName).collect(Collectors.toList());
                    throw new CoalesceUnitMissingColumnException(this, newInputColumnName,
                            "Column '" + newInputColumnName + "' not found. Available columns: " + names);
                }
            } else {
                newInputColumns.add(updatedInputColumn);
            }
        }
        return newInputColumns.toArray(new InputColumn[newInputColumns.size()]);
    }

    private InputColumn<?> findInputColumn(final InputColumn<?>[] allInputColumns, final String inputColumnName) {
        // Exact match round on path.
        for (final InputColumn<?> inputColumn : allInputColumns) {
            if (inputColumnName.contains(".") && inputColumn.isPhysicalColumn() && inputColumnName
                    .equals(inputColumn.getPhysicalColumn().getQualifiedLabel())) {
                return inputColumn;
            }
        }

        // Trimmed and case-insensitive path match round.
        for (final InputColumn<?> inputColumn : allInputColumns) {
            if (inputColumnName.contains(".") && inputColumn.isPhysicalColumn() && inputColumnName.trim()
                    .equalsIgnoreCase(inputColumn.getPhysicalColumn().getQualifiedLabel())) {
                return inputColumn;
            }
        }

        // Legacy: Exact name match round
        for (final InputColumn<?> inputColumn : allInputColumns) {
            if (inputColumnName.equals(inputColumn.getName())) {
                return inputColumn;
            }
        }

        // Legacy: Trimmed and case-insensitive name match round.
        for (final InputColumn<?> inputColumn : allInputColumns) {
            if (inputColumnName.trim().equalsIgnoreCase(inputColumn.getName().trim())) {
                return inputColumn;
            }
        }

        return null;
    }

    public InputColumn<?>[] getInputColumns() {
        return _inputColumns;
    }

    public Class<?> getOutputDataType() {
        Class<?> candidate = null;
        for (final InputColumn<?> inputColumn : _inputColumns) {
            final Class<?> dataType = inputColumn.getDataType();
            if (candidate == null) {
                candidate = dataType;
            } else if (candidate == Object.class) {
                return candidate;
            } else {
                if (candidate != dataType) {
                    if (ReflectionUtils.is(dataType, candidate)) {
                        // keep the current candidate
                    } else if (ReflectionUtils.is(candidate, dataType)) {
                        candidate = dataType;
                    } else {
                        return Object.class;
                    }
                }
            }
        }

        if (candidate == null) {
            return Object.class;
        }

        return candidate;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(getInputColumnNames());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CoalesceUnit other = (CoalesceUnit) obj;
        if (!Arrays.equals(getInputColumnNames(), other.getInputColumnNames())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "CoalesceUnit[inputColumnNames=" + Arrays.toString(getInputColumnNames()) + "]";
    }

    public String getSuggestedOutputColumnName() {
        return getSimpleName(getInputColumnNames()[0]);
    }
}
