/* DataCleaner (community edition)
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
import java.util.List;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public class JsonQueryDatasetResponseParser {

    private boolean _parsingRows;
    private boolean _parsingRow;
    private ArrayList<Object> _currentRow;
    private String _currentFieldName;
    private int _arrayCount;

    public List<Object[]> parseQueryResult(final InputStream inputStream) throws JsonParseException, IOException {
        _parsingRows = false;
        _parsingRow = false;
        _arrayCount = 0;
        final List<Object[]> queryResult = new ArrayList<>();
        final JsonFactory factory = new JsonFactory();
        final JsonParser parser = factory.createParser(inputStream);
        JsonToken token = parser.nextToken();
        while (token != null) {
            switch (parser.getCurrentToken()) {
            case FIELD_NAME:
                _currentFieldName = parser.getText();
                break;
            case START_ARRAY:
                if (_parsingRows) {
                    _parsingRow = true;
                    _currentRow = new ArrayList<>();
                } else if ("rows".equals(_currentFieldName)) {
                    _parsingRows = true;
                } else if (_parsingRows) {
                    _arrayCount++;
                }
                break;
            case END_ARRAY:
                if (_arrayCount > 0) {
                    _arrayCount--;
                } else if (_parsingRow) {
                    _parsingRow = false;
                    queryResult.add(_currentRow.toArray(new Object[_currentRow.size()]));
                } else if (_parsingRows) {
                    _parsingRows = false;
                }
                break;
            case VALUE_STRING:
                if (_parsingRow) {
                    _currentRow.add(parser.getText());
                }
                break;
            case VALUE_NULL:
                if (_parsingRow) {
                    _currentRow.add(null);
                }
                break;
            default:
                break;
            }
            token = parser.nextToken();
        }

        return queryResult;
    }

}
