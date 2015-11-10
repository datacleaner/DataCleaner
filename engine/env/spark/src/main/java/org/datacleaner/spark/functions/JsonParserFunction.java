package org.datacleaner.spark.functions;

import java.util.Iterator;

import org.apache.metamodel.json.JsonDataContext;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;
import org.apache.spark.api.java.function.Function;
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
        final Object[] inputRows = getInputRows(readTree);
        return inputRows;
    }

    private Object[] getInputRows(JsonNode readTree) {
        final Column[] columns = getColumns();
        final Object[] list = new Object[columns.length];
        final Iterator<JsonNode> mainIterator = readTree.iterator();
        int i = 0;
        while (mainIterator.hasNext()) {
            final JsonNode node = mainIterator.next();
            final String value = node.asText();
            list[i] = value;
            i++;
        }
        return list;
    }

    public Column[] getColumns() {
        if (_columns == null) {
            final JsonDataContext jsonContext = new JsonDataContext(_jsonDatastore.getResource());
            final Table table = jsonContext.getDefaultSchema().getTable(0);
            _columns = table.getColumns();
        }
        return _columns;
    }
}
