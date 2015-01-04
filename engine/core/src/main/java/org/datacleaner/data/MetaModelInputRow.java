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

import java.io.InputStream;
import java.io.Reader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.metamodel.data.Row;
import org.apache.metamodel.query.SelectItem;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.util.FileHelper;
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

    @Override
    public boolean containsInputColumn(InputColumn<?> inputColumn) {
        if (!inputColumn.isPhysicalColumn()) {
            return false;
        }
        Column physicalColumn = inputColumn.getPhysicalColumn();
        SelectItem[] selectItems = _row.getSelectItems();
        for (SelectItem selectItem : selectItems) {
            if (selectItem.getColumn() != null && selectItem.getFunction() == null) {
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
            if (selectItem.getColumn() != null && selectItem.getFunction() == null) {
                result.add(new MetaModelInputColumn(selectItem.getColumn()));
            }
        }
        return result;
    }
}
