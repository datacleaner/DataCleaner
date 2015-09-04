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

import org.apache.metamodel.MetaModelException;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.data.Style;
import org.apache.metamodel.jdbc.dialects.IQueryRewriter;
import org.apache.metamodel.query.FilterItem;
import org.apache.metamodel.query.builder.FilterBuilder;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.update.RowUpdationBuilder;

public class DataHubUpdateBuilder implements RowUpdationBuilder {

    public DataHubUpdateBuilder(DataHubUpdateCallback dataHubUpdateCallback, Table table, IQueryRewriter queryRewriter) {
        // TODO 
    }

    @Override
    public RowUpdationBuilder value(int columnIndex, Object value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RowUpdationBuilder value(int columnIndex, Object value, Style style) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RowUpdationBuilder value(Column column, Object value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RowUpdationBuilder value(Column column, Object value, Style style) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RowUpdationBuilder value(String columnName, Object value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RowUpdationBuilder value(String columnName, Object value, Style style) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Row toRow() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isSet(Column column) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public FilterBuilder<RowUpdationBuilder> where(Column column) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FilterBuilder<RowUpdationBuilder> where(String columnName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RowUpdationBuilder where(FilterItem... filterItems) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RowUpdationBuilder where(Iterable<FilterItem> filterItems) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Table getTable() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String toSql() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void execute() throws MetaModelException {
        // TODO Auto-generated method stub

    }

}
