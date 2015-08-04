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
package org.datacleaner.connection.datahub.dummy;

import org.apache.metamodel.schema.AbstractTable;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.ImmutableColumn;
import org.apache.metamodel.schema.Relationship;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.TableType;

/**
 * Dummy implementation of Datahub table, final version must be implemented in metamodel
 * @author hetty
 *
 */
public class DatahubDummyTable extends AbstractTable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String _name;

    public DatahubDummyTable(String name) {
        _name = name;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public Column[] getColumns() {
        Column[] columns = new Column[3];
        columns[0] = new ImmutableColumn("id", ColumnType.INTEGER, this, 1, 10, "integer", false, "remarks", true, null, true);
        columns[1] = new ImmutableColumn("name", ColumnType.VARCHAR, this, 1, 50, "string", false, "remarks", false, null, false);
        columns[2] = new ImmutableColumn("age", ColumnType.INTEGER, this, 1, 10, "integer", false, "remarks", false, null, false);
        return columns;
    }

    @Override
    public Schema getSchema() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TableType getType() {
        return TableType.TABLE;
    }

    @Override
    public Relationship[] getRelationships() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getRemarks() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getQuote() {
        // TODO Auto-generated method stub
        return null;
    }


}
