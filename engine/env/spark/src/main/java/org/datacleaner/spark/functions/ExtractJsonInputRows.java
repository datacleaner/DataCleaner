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
package org.datacleaner.spark.functions;

import java.util.ArrayList;
import java.util.List;

import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.json.JsonDataContext;
import org.apache.metamodel.query.Query;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;
import org.datacleaner.connection.JsonDatastore;

/**
 * Reads and extracts data from a Json datastore
 *
 */
public class ExtractJsonInputRows {

    final JsonDatastore _jsonDatastore;

    public ExtractJsonInputRows(JsonDatastore jsonDatastore) {
        _jsonDatastore = jsonDatastore;
    }

    public List<Object[]> getInputRows() {
        final List<Object[]> list = new ArrayList<>();
        final JsonDataContext jsonContext = new JsonDataContext(_jsonDatastore.getResource());
        final Table table = jsonContext.getDefaultSchema().getTable(0);
        final Column[] columns = table.getColumns();
        final Query query = new Query().select(columns).from(table);
        final DataSet executeQuery = jsonContext.executeQuery(query);
        while (executeQuery.next()) {
            final Row row = executeQuery.getRow();
            final Object[] values = row.getValues();
            list.add(values);
        }
        return list;
    }
}
