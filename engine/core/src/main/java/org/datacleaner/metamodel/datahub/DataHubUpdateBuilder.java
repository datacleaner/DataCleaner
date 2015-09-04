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

import java.util.List;

import org.apache.metamodel.MetaModelException;
import org.apache.metamodel.query.FilterClause;
import org.apache.metamodel.query.FilterItem;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.update.AbstractRowUpdationBuilder;

public class DataHubUpdateBuilder extends AbstractRowUpdationBuilder {

    private final DataHubUpdateCallback _callback;

    public DataHubUpdateBuilder(DataHubUpdateCallback dataHubUpdateCallback, Table table) {
        super(table);
        _callback = dataHubUpdateCallback;
    }

    @Override
    public void execute() throws MetaModelException {
        String query = createSqlStatement();
        System.out.println("Executing query: " + query);
        _callback.executeUpdate(query);
    }

    private String createSqlStatement() {
        final Object[] values = getValues();
        final Table table = getTable();
        final StringBuilder sb = new StringBuilder();

        sb.append("UPDATE ");
        sb.append(table.getQualifiedLabel());
        sb.append(" SET ");

        Column[] columns = getColumns();
        boolean[] explicitNulls = getExplicitNulls();
        boolean firstValue = true;
        for (int i = 0; i < columns.length; i++) {
            final Object value = values[i];
            if (value != null || explicitNulls[i]) {
                if (firstValue) {
                    firstValue = false;
                } else {
                    sb.append(',');
                }
                String columnName = columns[i].getName();
                sb.append(columnName);

                sb.append('=');
                sb.append('?');

            }
        }

        List<FilterItem> whereItems = getWhereItems();
        String whereClause = new FilterClause(null, " WHERE ").addItems(whereItems).toSql();
        sb.append(whereClause);
        String sql = sb.toString();
        return sql;
    }

}
