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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.datacleaner.metamodel.datahub.DatahubSchema;
import org.datacleaner.metamodel.datahub.DatahubTable;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public class JsonSchemasResponseParser {

    private static final Set<String> datastoreTypes = new HashSet<String>() {
        {
            add("GoldenRecordDatastore");
            add("SourceRecordSourceFormatDatastore");
            add("SourceRecordGoldenFormatDatastore");
        }
    };

    private static enum DatastoreObject {
        DATASTORE {
            @Override
            public DatastoreObject previous() {
                return null; // see below for options for this line
            }
        },
        SCHEMA, TABLE, COLUMN {
            @Override
            public DatastoreObject next() {
                return null; // see below for options for this line
            };
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

    private DatastoreObject _currentObject;
    private String _currentFieldname;
    private DatahubSchema _currentSchema;
    private DatahubTable _currentTable;
    private DatahubColumnBuilder _currentColumnBuilder;
    private DatahubSchema _resultSchema;

    private String _currentDataStoreName;
    private List<String> _dataStoreNames = new ArrayList<String>();

    public DatahubSchema parseJsonSchema(InputStream is)
            throws JsonParseException, IOException {
        _currentObject = DatastoreObject.DATASTORE;
        _currentFieldname = "";
        JsonFactory factory = new JsonFactory();
        JsonParser parser = factory.createParser(is);
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
            _currentColumnBuilder.withTable(_currentTable);
            _currentTable.add(_currentColumnBuilder.build());
            break;
        default:
        }
    }

    private void createNewObject() {
        switch (_currentObject) {
        case SCHEMA:
            _currentSchema = new DatahubSchema();
        case TABLE:
            _currentTable = new DatahubTable();
            break;
        case COLUMN:
            _currentColumnBuilder = new DatahubColumnBuilder();
            break;
        default:
        }
    }

    private void handleValue(String fieldName, String fieldValue) {
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

    private void handleBooleanValue(String fieldName, boolean fieldValue) {
        switch (_currentObject) {
        case COLUMN:
            handleBooleanColumnField(fieldName, fieldValue);
            break;
        default:
        }
    }

    private void handleBooleanColumnField(String fieldName, boolean fieldValue) {
        if (fieldName.equals("primaryKey")) {
            _currentColumnBuilder.withPrimaryKey(fieldValue);
        } else if (fieldName.equals("indexed")) {
            _currentColumnBuilder.withIndexed(fieldValue);
        } else if (fieldName.equals("nullable")) {
            _currentColumnBuilder.withNullable(fieldValue);
        } else {
            // skip unknown column fields
        }

    }

    private void handleIntegerValue(String fieldName, int fieldValue) {
        if (fieldName.equals("number")) {
            _currentColumnBuilder.withNumber(fieldValue);
        }
    }

    private void handleColumnField(String fieldName, String fieldValue) {
        if (fieldName.equals("name")) {
            _currentColumnBuilder.withName(fieldValue);
        } else if (fieldName.equals("quote")) {
            _currentColumnBuilder.withQuote(fieldValue);
        } else if (fieldName.equals("remarks")) {
            _currentColumnBuilder.withRemarks(fieldValue);
        } else if (fieldName.equals("type")) {
            _currentColumnBuilder.withType(fieldValue);
        } else if (fieldName.equals("nativeType")) {
            _currentColumnBuilder.withNativeType(fieldValue);
        } else if (fieldName.equals("size")) {
            _currentColumnBuilder.withSize(new Integer(fieldValue));
        } else {
            // skip unknown column fields
        }
    }

    private void handleTableField(String fieldName, String fieldValue) {
        if (fieldName.equals("name")) {
            _currentTable.setName(fieldValue);
        }

    }

    private void handleSchemaField(String fieldName, String fieldValue) {
        if (fieldName.equals("name")) {
            _currentSchema.setName(fieldValue);
        }
    }


    private void handleDataStoreValue(String value) {
        if (_currentFieldname.equals("name")) {
            _currentDataStoreName = value;
        } else if (_currentFieldname.equals("type")
                && datastoreTypes.contains(value)) {
            _dataStoreNames.add(_currentDataStoreName);
        }
    }

    public List<String> parseDataStoreArray(InputStream inputStream) throws IOException {
        JsonFactory factory = new JsonFactory();
        JsonParser parser = factory.createParser(inputStream);
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
