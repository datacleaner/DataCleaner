/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Free Software Foundation, Inc.
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
package org.datacleaner.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.lang.reflect.Field;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datacleaner.metamodel.data.DefaultRow;
import org.datacleaner.metamodel.data.Row;
import org.datacleaner.metamodel.data.SimpleDataSetHeader;
import org.datacleaner.metamodel.query.AggregateFunction;
import org.datacleaner.metamodel.query.FunctionType;
import org.datacleaner.metamodel.query.SelectItem;
import org.datacleaner.metamodel.schema.Column;
import org.datacleaner.metamodel.util.FileHelper;
import org.datacleaner.api.InputColumn;
import org.datacleaner.util.ColumnSnapshot;
import org.datacleaner.api.InputRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A physical {@link InputRow} originating from a MetaModel {@link Row} object.
 */
public final class MetaModelInputRow extends AbstractLegacyAwareInputRow {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(MetaModelInputRow.class);

    private static final Map<String, FunctionType> FUNCTION_TYPES = loadFunctionTypes();

    private transient Row _row;
    private long _id;

    public MetaModelInputRow(final long rowNumber, final Row row) {
        _id = rowNumber;
        _row = row;
    }

    @Override
    protected String getFieldNameForNewId() {
        return "_id";
    }

    @Override
    protected String getFieldNameForOldId() {
        return "_rowNumber";
    }

    @Override
    protected Collection<String> getFieldNamesInAdditionToId() {
        return Arrays.asList("_row");
    }

    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.writeLong(_id);

        final List<SelectItem> selectItems = _row.getSelectItems();
        out.writeInt(selectItems.size());
        for (final SelectItem selectItem : selectItems) {
            final Column column = selectItem.getColumn();
            out.writeBoolean(column != null);
            if (column != null) {
                out.writeObject(new ColumnSnapshot(column));
            }
            final AggregateFunction aggregateFunction = selectItem.getAggregateFunction();
            out.writeBoolean(aggregateFunction != null);
            if (aggregateFunction != null) {
                out.writeObject(aggregateFunction.getFunctionName());
            }
            final String expression = selectItem.getExpression();
            out.writeBoolean(expression != null);
            if (expression != null) {
                out.writeObject(expression);
            }
            final String alias = selectItem.getAlias();
            out.writeBoolean(alias != null);
            if (alias != null) {
                out.writeObject(alias);
            }
        }

        out.writeObject(_row.getValues());
    }

    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        _id = in.readLong();

        final int selectItemCount = in.readInt();
        final List<SelectItem> selectItems = new ArrayList<>(selectItemCount);
        for (int i = 0; i < selectItemCount; i++) {
            final Column column;
            if (in.readBoolean()) {
                column = ((ColumnSnapshot) in.readObject()).rebuild();
            } else {
                column = null;
            }
            final String aggregateFunctionName;
            if (in.readBoolean()) {
                aggregateFunctionName = (String) in.readObject();
            } else {
                aggregateFunctionName = null;
            }
            final String expression;
            if (in.readBoolean()) {
                expression = (String) in.readObject();
            } else {
                expression = null;
            }
            final String alias;
            if (in.readBoolean()) {
                alias = (String) in.readObject();
            } else {
                alias = null;
            }

            if (column != null) {
                if (aggregateFunctionName != null) {
                    selectItems.add(new SelectItem(FUNCTION_TYPES.get(aggregateFunctionName), column));
                } else {
                    selectItems.add(new SelectItem(column));
                }
            } else {
                selectItems.add(new SelectItem(expression, alias));
            }
        }

        final Object[] values = (Object[]) in.readObject();
        _row = new DefaultRow(new SimpleDataSetHeader(selectItems), values);
    }

    private static Map<String, FunctionType> loadFunctionTypes() {
        final Map<String, FunctionType> functionTypes = new HashMap<>();
        for (final Field field : FunctionType.class.getFields()) {
            if (FunctionType.class.isAssignableFrom(field.getType())) {
                try {
                    final FunctionType functionType = (FunctionType) field.get(null);
                    functionTypes.put(functionType.getFunctionName(), functionType);
                } catch (final IllegalAccessException e) {
                    logger.warn("Could not access FunctionType field: {}", field.getName());
                }
            }
        }
        return functionTypes;
    }

    @Override
    public long getId() {
        return _id;
    }

    public Row getRow() {
        return _row;
    }

    @Override
    public boolean containsInputColumn(final InputColumn<?> inputColumn) {
        if (!inputColumn.isPhysicalColumn()) {
            return false;
        }
        final Column physicalColumn = inputColumn.getPhysicalColumn();
        final List<SelectItem> selectItems = _row.getSelectItems();
        for (final SelectItem selectItem : selectItems) {
            if (selectItem.getColumn() != null && selectItem.getAggregateFunction() == null) {
                final Column column = selectItem.getColumn();
                if (physicalColumn.equals(column)) {
                    return true;
                }
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> E getValueInternal(final InputColumn<E> column) {
        if (!column.isPhysicalColumn()) {
            return null;
        }
        final Column physicalColumn = column.getPhysicalColumn();
        Object value = _row.getValue(physicalColumn);

        value = convertValue(value);

        return (E) value;
    }

    private Object convertValue(Object value) {
        if (value instanceof Clob) {
            try {
                final Reader reader = ((Clob) value).getCharacterStream();
                try {
                    value = FileHelper.readAsString(reader);
                } finally {
                    FileHelper.safeClose(reader);
                }
            } catch (final SQLException e) {
                logger.error("Failed to convert CLOB to String", e);
                value = null;
            }
        } else if (value instanceof Blob) {
            try {
                final InputStream inputStream = ((Blob) value).getBinaryStream();
                try {
                    value = FileHelper.readAsBytes(inputStream);
                } finally {
                    FileHelper.safeClose(inputStream);
                }
            } catch (final SQLException e) {
                logger.error("Failed to convert BLOB to byte[]", e);
                value = null;
            }
        }
        return value;
    }

    @Override
    public String toString() {
        return "MetaModelInputRow[" + _row + "]";
    }

    @Override
    public List<InputColumn<?>> getInputColumns() {
        final List<InputColumn<?>> result = new ArrayList<>();
        final List<SelectItem> selectItems = _row.getSelectItems();
        for (final SelectItem selectItem : selectItems) {
            if (selectItem.getColumn() != null && selectItem.getAggregateFunction() == null) {
                result.add(new MetaModelInputColumn(selectItem.getColumn()));
            }
        }
        return result;
    }
}
