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
package org.datacleaner.metamodel.datahub;

import java.util.ArrayList;
import java.util.List;

import org.apache.metamodel.schema.AbstractTable;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Relationship;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.TableType;

/**
 * Dummy implementation of Datahub table, final version must be implemented in
 * metamodel
 * 
 * @author hetty
 *
 */
public class DatahubTable extends AbstractTable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String _name;
    private List<Column> _columns;
    private DatahubSchema _schema;
    private String _dataSourceName;

    public DatahubTable() {
        _name = "";
        _columns = new ArrayList<Column>();
        _dataSourceName = "";
    }


    @Override
    public String getName() {
        return _name;
    }

    @Override
    public Column[] getColumns() {
        return _columns.toArray(new Column[_columns.size()]);
    }

    @Override
    public Schema getSchema() {
        return _schema;
    }

    @Override
    public TableType getType() {
        return TableType.TABLE;
    }

    @Override
    public Relationship[] getRelationships() {
        return new Relationship[0];
    }

    @Override
    public String getRemarks() {
        return null;
    }

    @Override
    public String getQuote() {
        return null;
    }

    public void setName(String name) {
        _name = name;        
    }

    public void add(Column column) {
        _columns.add(column);
    }


    public void setSchema(DatahubSchema schema) {
        _schema = schema;
        
    }

}
