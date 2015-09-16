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

import java.util.Arrays;
import java.util.List;

import org.apache.metamodel.util.CollectionUtils;
import org.apache.metamodel.util.HasNameMapper;
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
    private transient InputColumn<?>[] _inputColumns;

    public CoalesceUnit(List<InputColumn<?>> inputColumns) {
        this(inputColumns.toArray(new InputColumn[inputColumns.size()]));
    }

    public CoalesceUnit(String... columnNames) {
        _inputColumnNames = columnNames;
    }

    public CoalesceUnit(InputColumn<?>... inputColumns) {
        if (inputColumns == null || inputColumns.length == 0) {
            throw new IllegalArgumentException("InputColumns cannot be null or empty");
        }
        _inputColumns = inputColumns;
        _inputColumnNames = new String[inputColumns.length];
        for (int i = 0; i < _inputColumnNames.length; i++) {
            _inputColumnNames[i] = inputColumns[i].getName();
        }
    }

    public String[] getInputColumnNames() {
        return _inputColumnNames;
    }

    public InputColumn<?>[] getInputColumns(InputColumn<?>[] allInputColumns) {
        if (_inputColumns == null) {
            _inputColumns = new InputColumn[_inputColumnNames.length];
            for (int i = 0; i < _inputColumnNames.length; i++) {
                boolean found = false;

                final String name = _inputColumnNames[i];

                // first do an exact match round
                for (int j = 0; j < allInputColumns.length; j++) {
                    InputColumn<?> inputColumn = allInputColumns[j];
                    if (name.equals(inputColumn.getName())) {
                        _inputColumns[i] = inputColumn;
                        found = true;
                    }
                }
                
                if (!found) {
                    // try with trimming and case-insensitive matching
                    for (int j = 0; j < allInputColumns.length; j++) {
                        InputColumn<?> inputColumn = allInputColumns[j];
                        if (name.trim().equalsIgnoreCase(inputColumn.getName().trim())) {
                            _inputColumns[i] = inputColumn;
                            found = true;
                        }
                    }
                }

                if (!found) {
                    List<String> names = CollectionUtils.map(allInputColumns, new HasNameMapper());
                    throw new IllegalStateException("Column '" + name + "' not found. Available columns: " + names);
                }
            }
        }
        return _inputColumns;
    }

    public Class<?> getOutputDataType(InputColumn<?>[] allInputColumns) {
        Class<?> candidate = null;
        for (final InputColumn<?> inputColumn : getInputColumns(allInputColumns)) {
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
        result = prime * result + Arrays.hashCode(_inputColumnNames);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CoalesceUnit other = (CoalesceUnit) obj;
        if (!Arrays.equals(_inputColumnNames, other._inputColumnNames))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "CoalesceUnit[inputColumnNames=" + Arrays.toString(_inputColumnNames) + "]";
    }

    public String getSuggestedOutputColumnName() {
        return _inputColumnNames[0];
    }
}
