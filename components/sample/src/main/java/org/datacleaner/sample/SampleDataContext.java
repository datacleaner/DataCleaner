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
package org.datacleaner.sample;

import java.util.ArrayList;
import java.util.List;

import org.apache.metamodel.MetaModelException;
import org.apache.metamodel.MetaModelHelper;
import org.apache.metamodel.QueryPostprocessDataContext;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.DefaultRow;
import org.apache.metamodel.data.InMemoryDataSet;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.data.SimpleDataSetHeader;
import org.apache.metamodel.query.SelectItem;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.MutableColumn;
import org.apache.metamodel.schema.MutableSchema;
import org.apache.metamodel.schema.MutableTable;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.schema.TableType;

public class SampleDataContext extends QueryPostprocessDataContext {

    @Override
    protected Schema getMainSchema() throws MetaModelException {
        final MutableSchema schema = new MutableSchema("sample_schema");
        final MutableTable table = new MutableTable("sample_table", TableType.TABLE, schema);
        schema.addTable(table);

        table.addColumn(new MutableColumn("foo", ColumnType.INTEGER, table, 0, true));
        table.addColumn(new MutableColumn("bar", ColumnType.VARCHAR, table, 1, true));
        return schema;
    }

    @Override
    protected String getMainSchemaName() throws MetaModelException {
        return getMainSchema().getName();
    }

    @Override
    protected DataSet materializeMainSchemaTable(final Table table, final List<Column> columns, final int maxRows) {
        final SelectItem[] tableSelectItems = MetaModelHelper.createSelectItems(table.getColumns().toArray(new Column[0]));
        final SelectItem[] selectItems = MetaModelHelper.createSelectItems(columns.toArray(new Column[0]));
        final SimpleDataSetHeader header = new SimpleDataSetHeader(tableSelectItems);

        final List<Row> rows = new ArrayList<>();
        
        final SimpleDataSetHeader subSelectionHeader = new SimpleDataSetHeader(selectItems);

        rows.add(new DefaultRow(header, new Object[] { 1, "hello" }).getSubSelection(subSelectionHeader));
        rows.add(new DefaultRow(header, new Object[] { 2, "there" }).getSubSelection(subSelectionHeader));
        rows.add(new DefaultRow(header, new Object[] { 3, "big" }).getSubSelection(subSelectionHeader));
        rows.add(new DefaultRow(header, new Object[] { 4, "wide" }).getSubSelection(subSelectionHeader));
        rows.add(new DefaultRow(header, new Object[] { 5, "world" }).getSubSelection(subSelectionHeader));

        return new InMemoryDataSet(rows);
    }

}
