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
package org.datacleaner.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectOutputStream.PutField;
import java.io.Reader;
import java.io.Serializable;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang.SerializationException;
import org.apache.commons.lang.SerializationUtils;
import org.apache.metamodel.data.DataSetHeader;
import org.apache.metamodel.data.DefaultRow;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.data.SimpleDataSetHeader;
import org.apache.metamodel.query.SelectItem;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.util.FileHelper;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A physical {@link InputRow} originating from a MetaModel {@link Row} object.
 */
public final class MetaModelInputRow extends AbstractInputRow {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(MetaModelInputRow.class);

    private final Row _row;
    private final int _rowNumber;

    public MetaModelInputRow(int rowNumber, Row row) {
        _rowNumber = rowNumber;
        _row = row;
    }

    @Override
    public int getId() {
        return _rowNumber;
    }

    public Row getRow() {
        return _row;
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        try {
            // try to serialize first to ensure that row is not carrying
            // non-serializable things
            SerializationUtils.serialize(_row, new NullOutputStream());
            stream.defaultWriteObject();
        } catch (SerializationException e) {
            // "recover" by returning a copy of the row with only serializable
            // values
            final PutField putFields = stream.putFields();
            putFields.put("_rowNumber", _rowNumber);
            final DataSetHeader header = new SimpleDataSetHeader(_row.getSelectItems());
            final Object[] originalValues = _row.getValues();
            final Object[] values = new Object[originalValues.length];
            for (int i = 0; i < originalValues.length; i++) {
                final Object value = originalValues[i];
                if (value instanceof Serializable) {
                    values[i] = value;
                } else {
                    values[i] = NON_SERIALIZABLE_REPLACEMENT_VALUE;
                }
            }
            putFields.put("_row", new DefaultRow(header, values));
            stream.writeFields();
        }
    }

    @Override
    public boolean containsInputColumn(InputColumn<?> inputColumn) {
        if (!inputColumn.isPhysicalColumn()) {
            return false;
        }
        Column physicalColumn = inputColumn.getPhysicalColumn();
        SelectItem[] selectItems = _row.getSelectItems();
        for (SelectItem selectItem : selectItems) {
            if (selectItem.getColumn() != null && selectItem.getAggregateFunction() == null) {
                Column column = selectItem.getColumn();
                if (physicalColumn.equals(column)) {
                    return true;
                }
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> E getValueInternal(InputColumn<E> column) {
        if (!column.isPhysicalColumn()) {
            return null;
        }
        Column physicalColumn = column.getPhysicalColumn();
        Object value = _row.getValue(physicalColumn);

        value = convertValue(value);

        return (E) value;
    }

    private Object convertValue(Object value) {
        if (value instanceof Clob) {
            try {
                Reader reader = ((Clob) value).getCharacterStream();
                try {
                    value = FileHelper.readAsString(reader);
                } finally {
                    FileHelper.safeClose(reader);
                }
            } catch (SQLException e) {
                logger.error("Failed to convert CLOB to String", e);
                value = null;
            }
        } else if (value instanceof Blob) {
            try {
                InputStream inputStream = ((Blob) value).getBinaryStream();
                try {
                    value = FileHelper.readAsBytes(inputStream);
                } finally {
                    FileHelper.safeClose(inputStream);
                }
            } catch (SQLException e) {
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
        List<InputColumn<?>> result = new ArrayList<InputColumn<?>>();
        SelectItem[] selectItems = _row.getSelectItems();
        for (SelectItem selectItem : selectItems) {
            if (selectItem.getColumn() != null && selectItem.getAggregateFunction() == null) {
                result.add(new MetaModelInputColumn(selectItem.getColumn()));
            }
        }
        return result;
    }
}
