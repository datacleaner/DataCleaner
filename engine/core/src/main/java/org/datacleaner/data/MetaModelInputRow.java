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
import java.io.ObjectInputStream;
import java.io.Reader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.metamodel.data.Row;
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
public final class MetaModelInputRow extends AbstractLegacyAwareInputRow {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(MetaModelInputRow.class);

    private final Row _row;
    private final long _id;

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

    private void readObject(final ObjectInputStream stream) throws IOException, ClassNotFoundException {
        doReadObject(stream);
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
        final SelectItem[] selectItems = _row.getSelectItems();
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
        final SelectItem[] selectItems = _row.getSelectItems();
        for (final SelectItem selectItem : selectItems) {
            if (selectItem.getColumn() != null && selectItem.getAggregateFunction() == null) {
                result.add(new MetaModelInputColumn(selectItem.getColumn()));
            }
        }
        return result;
    }
}
