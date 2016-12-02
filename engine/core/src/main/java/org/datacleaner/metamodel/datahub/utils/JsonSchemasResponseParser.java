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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.metamodel.schema.ColumnTypeImpl;
import org.apache.metamodel.schema.MutableColumn;
import org.apache.metamodel.schema.MutableTable;
import org.datacleaner.metamodel.datahub.DataHubSchema;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public class JsonSchemasResponseParser {

    private enum DatastoreObject {
        DATASTORE {
            @Override
            public DatastoreObject previous() {
                return null; // see below for options for this line
            }
        }, SCHEMA, TABLE, COLUMN {
            @Override
            public DatastoreObject next() {
                return null; // see below for options for this line
            }

        };

        public DatastoreObject next() {
            // No bounds checking required here, because the last instance
            // overrides
            return values()[ordinal() + 1];
        }

        public DatastoreObject previous() {
            // No bounds checking required here, because the first instance
            // overrides
            return values()[ordinal() - 1];

        }
    }

    private static final Set<String> datastoreTypes = new HashSet<>(Arrays.asList(
            new String[] { "GoldenRecordDatastore", "SourceRecordSourceFormatDatastore",
                    "SourceRecordGoldenFormatDatastore" }));
    private DatastoreObject _currentObject;
    private String _currentFieldname;
    private DataHubSchema _currentSchema;
    private MutableTable _currentTable;
    private MutableColumn _currentColumn;
    private DataHubSchema _resultSchema;

    private String _currentDataStoreName;
    private List<String> _dataStoreNames = new ArrayList<>();

    public DataHubSchema parseJsonSchema(final InputStream is) throws JsonParseException, IOException {
        _currentObject = DatastoreObject.DATASTORE;
        _currentFieldname = "";
        final JsonFactory factory = new JsonFactory();
        final JsonParser parser = factory.createParser(is);
        JsonToken token = parser.nextToken();
        while (token != null) {
            switch (parser.getCurrentToken()) {
            case START_ARRAY:
                _currentObject = _currentObject.next();
                break;
            case END_ARRAY:
                _currentObject = _currentObject.previous();
                break;
            case START_OBJECT:
                createNewObject();
                break;
            case END_OBJECT:
                addObjectToSchema();
                break;
            case FIELD_NAME:
                _currentFieldname = parser.getText();
                break;
            case VALUE_STRING:
                handleValue(_currentFieldname, parser.getText());
                break;
            case VALUE_FALSE:
                handleBooleanValue(_currentFieldname, false);
                break;
            case VALUE_TRUE:
                handleBooleanValue(_currentFieldname, true);
                break;
            case VALUE_NUMBER_INT:
                handleIntegerValue(_currentFieldname, parser.getIntValue());
                break;
            default:
                break;
            }
            token = parser.nextToken();
        }
        return _resultSchema;
    }

    private void addObjectToSchema() {
        switch (_currentObject) {
        case SCHEMA:
            if (!"INFORMATION_SCHEMA".equals(_currentSchema.getName())) {
                _resultSchema = _currentSchema;
            }
            break;
        case TABLE:
            _currentTable.setSchema(_currentSchema);
            _currentSchema.addTable(_currentTable);
            break;
        case COLUMN:
            _currentColumn.setTable(_currentTable);
            _currentTable.addColumn(_currentColumn);
            break;
        default:
        }
    }

    private void createNewObject() {
        switch (_currentObject) {
        case SCHEMA:
            _currentSchema = new DataHubSchema();

            // TODO: Is this on purpose?
            // fallthru
        case TABLE:
            _currentTable = new MutableTable();
            break;
        case COLUMN:
            _currentColumn = new MutableColumn();
            break;
        default:
        }
    }

    private void handleValue(final String fieldName, final String fieldValue) {
        switch (_currentObject) {
        case SCHEMA:
            handleSchemaField(fieldName, fieldValue);
            break;
        case TABLE:
            handleTableField(fieldName, fieldValue);
            break;
        case COLUMN:
            handleColumnField(fieldName, fieldValue);
            break;
        default:
        }
    }

    private void handleBooleanValue(final String fieldName, final boolean fieldValue) {
        switch (_currentObject) {
        case COLUMN:
            handleBooleanColumnField(fieldName, fieldValue);
            break;
        default:
        }
    }

    private void handleBooleanColumnField(final String fieldName, final boolean fieldValue) {
        if (fieldName.equals("primaryKey")) {
            _currentColumn.setPrimaryKey(fieldValue);
        } else if (fieldName.equals("indexed")) {
            _currentColumn.setIndexed(fieldValue);
        } else if (fieldName.equals("nullable")) {
            _currentColumn.setNullable(fieldValue);
        } else {
            // skip unknown column fields
        }

    }

    private void handleIntegerValue(final String fieldName, final int fieldValue) {
        if (fieldName.equals("number")) {
            _currentColumn.setColumnNumber(fieldValue);
        }
    }

    private void handleColumnField(final String fieldName, final String fieldValue) {
        if (fieldName.equals("name")) {
            _currentColumn.setName(fieldValue);
        } else if (fieldName.equals("quote")) {
            _currentColumn.setQuote(fieldValue);
        } else if (fieldName.equals("remarks")) {
            _currentColumn.setRemarks(fieldValue);
        } else if (fieldName.equals("type")) {
            _currentColumn.setType(ColumnTypeImpl.valueOf(fieldValue));
        } else if (fieldName.equals("nativeType")) {
            _currentColumn.setNativeType(fieldValue);
        } else if (fieldName.equals("size")) {
            _currentColumn.setColumnSize(new Integer(fieldValue));
        } else {
            // skip unknown column fields
        }
    }

    private void handleTableField(final String fieldName, final String fieldValue) {
        if (fieldName.equals("name")) {
            _currentTable.setName(fieldValue);
        }

    }

    private void handleSchemaField(final String fieldName, final String fieldValue) {
        if (fieldName.equals("name")) {
            _currentSchema.setName(fieldValue);
        }
    }

    private void handleDataStoreValue(final String value) {
        if (_currentFieldname.equals("name")) {
            _currentDataStoreName = value;
        } else if (_currentFieldname.equals("type") && datastoreTypes.contains(value)) {
            _dataStoreNames.add(_currentDataStoreName);
        }
    }

    public List<String> parseDataStoreArray(final InputStream inputStream) throws IOException {
        final JsonFactory factory = new JsonFactory();
        final JsonParser parser = factory.createParser(inputStream);
        JsonToken token = parser.nextToken();
        while (token != null) {
            switch (parser.getCurrentToken()) {
            case FIELD_NAME:
                _currentFieldname = parser.getText();
                break;
            case VALUE_STRING:
                handleDataStoreValue(parser.getText());
                break;
            default:
                break;
            }
            token = parser.nextToken();
        }

        return _dataStoreNames;
    }

}
