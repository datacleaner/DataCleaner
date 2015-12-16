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

import org.apache.metamodel.schema.Column;
import org.apache.spark.api.java.function.Function;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.connection.JsonDatastore;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonParserFunction implements Function<String, Object[]> {

    private static final long serialVersionUID = 1L;
    private final JsonDatastore _jsonDatastore;
    private Column[] _columns;

    public JsonParserFunction(JsonDatastore jsonDatastore) {
        _jsonDatastore = jsonDatastore;
    }

    @Override
    public Object[] call(String line) throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode readTree = mapper.readTree(line);
        final Object[] values = getValues(readTree);
        return values;
    }

    private Object[] getValues(JsonNode readTree) {
        final Column[] columns = getColumns();
        final Object[] list = new Object[columns.length];
        for (int i = 0; i < columns.length; i++) {
            final JsonNode node = readTree.findValue(columns[i].getName());
            final String value = node.asText();
            list[i] = value;
        }
        return list;
    }

    public Column[] getColumns() {
        if (_columns == null) {
            try (final DatastoreConnection openConnection = _jsonDatastore.openConnection()) {
                _columns = openConnection.getDataContext().getDefaultSchema().getTable(0).getColumns();
            }
        }
        return _columns;
    }
}
