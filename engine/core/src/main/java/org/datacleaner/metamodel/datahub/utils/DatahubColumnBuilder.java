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
package org.datacleaner.metamodel.datahub.utils;

import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.ImmutableColumn;
import org.apache.metamodel.schema.Table;
import org.datacleaner.metamodel.datahub.DatahubTable;


public class DatahubColumnBuilder {
    String _name;
    ColumnType _type;
    Integer _number;
    Integer _size;
    String _nativeType;
    boolean _nullable;
    String _remarks;
    boolean _indexed;
    String _quote;
    boolean _primaryKey;
    Table _table;
    
    public DatahubColumnBuilder withName(String name) {
        _name = name;
        return this;
    }
    
    public Column build() {
        return new ImmutableColumn(_name,
                _type, _table, _number, _size, _nativeType, _nullable,
                _remarks, _indexed, _quote, _primaryKey);
    }

    public DatahubColumnBuilder withIndexed(boolean indexed) {
        _indexed = indexed;
        return this;
    }

    public DatahubColumnBuilder withQuote(String quote) {
        _quote = quote;
        return this;
        
    }

    public DatahubColumnBuilder withPrimaryKey(boolean primaryKey) {
        _primaryKey = primaryKey;
        return this;
        
    }

    public DatahubColumnBuilder withRemarks(String remarks) {
        _remarks = remarks;
        return this;
        
    }

    public DatahubColumnBuilder withNullable(boolean nullable) {
        _nullable = nullable;
        return this;
        
    }

    public DatahubColumnBuilder withType(String type) {
        _type = toColumnType(type);
        return this;        
    }

    private ColumnType toColumnType(String columnType) {
        if (columnType.equals("INTEGER")) {
            return ColumnType.INTEGER;
        } else if (columnType.equals("LIST")) {
            return ColumnType.LIST;
        } else if (columnType.equals("BIGINT")) {
            return ColumnType.BIGINT;
        } else if (columnType.equals("VARCHAR")) {
            return ColumnType.VARCHAR;
        } else if (columnType.equals("TIMESTAMP")) {
            return ColumnType.TIMESTAMP;
        } else if (columnType.equals("DATE")) {
            return ColumnType.DATE;
        } else if (columnType.equals("BOOLEAN")) {
            return ColumnType.BOOLEAN;
        }
        //TODO throw exception?
        return null;
    }

    public DatahubColumnBuilder withNativeType(String nativeType) {
        _nativeType = nativeType;
        return this;
        
    }

    public DatahubColumnBuilder withSize(Integer size) {
        _size = size;
        return this;
        
    }

    public DatahubColumnBuilder withTable(DatahubTable table) {
        _table = table;
        return this;
    }

    public DatahubColumnBuilder withNumber(Integer number) {
        _number = number;
        return this;
        
    }

}
