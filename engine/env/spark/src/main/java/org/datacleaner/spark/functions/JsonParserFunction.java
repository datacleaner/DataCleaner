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

import java.util.List;

import org.apache.metamodel.schema.Column;
import org.apache.spark.api.java.function.Function;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.connection.JsonDatastore;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonParserFunction implements Function<String, Object[]> {

    private static final long serialVersionUID = 1L;
    private final JsonDatastore _jsonDatastore;
    private List<Column> _columns;

    public JsonParserFunction(final JsonDatastore jsonDatastore) {
        _jsonDatastore = jsonDatastore;
    }

    @Override
    public Object[] call(final String line) throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode readTree = mapper.readTree(line);
        return getValues(readTree);
    }

    private Object[] getValues(final JsonNode readTree) {
        final List<Column> columns = getColumns();
        final Object[] list = new Object[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            final JsonNode node = readTree.findValue(columns.get(i).getName());
            final String value = node.asText();
            list[i] = value;
        }
        return list;
    }

    public List<Column> getColumns() {
        if (_columns == null) {
            try (DatastoreConnection openConnection = _jsonDatastore.openConnection()) {
                _columns = openConnection.getDataContext().getDefaultSchema().getTable(0).getColumns();
            }
        }
        return _columns;
    }
}
