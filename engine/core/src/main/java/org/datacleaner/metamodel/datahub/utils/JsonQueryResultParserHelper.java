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
import java.util.ArrayList;
import java.util.List;

import org.apache.metamodel.schema.Column;
import org.datacleaner.metamodel.datahub.DatahubDataSet;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public class JsonQueryResultParserHelper {


    private boolean _parsingRows;
    private boolean _parsingRow;
    private ArrayList<Object> _currentRow;
    private String _currentFieldName;

    //String result = "{\"table\":{\"header\":[\"CUSTOMERNUMBER\",\"CUSTOMERNAME\",\"CONTACTLASTNAME\",\"CONTACTFIRSTNAME\",\"PHONE\",\"ADDRESSLINE1\",\"ADDRESSLINE2\",\"CITY\",\"STATE\",\"POSTALCODE\",\"COUNTRY\",\"SALESREPEMPLOYEENUMBER\",\"CREDITLIMIT\"],\"rows\":[]}}";

    public DatahubDataSet parseQueryResult(String result, Column[] columns) throws JsonParseException, IOException {
        _parsingRows = false;
        List<Object[]> queryResult = new ArrayList<Object[]>();
        JsonFactory factory = new JsonFactory();
        JsonParser parser = factory.createParser(result);
        JsonToken token = parser.nextToken();
        while (token != null) {
            switch (parser.getCurrentToken()) {
            case FIELD_NAME:
                _currentFieldName = parser.getText();
                break;    
            case START_ARRAY:
                if (_parsingRows) {
                    _parsingRow = true;
                    _currentRow = new ArrayList<Object>();
                } else if ("rows".equals(_currentFieldName)) {
                    _parsingRows = true;
                } 
                break;
            case END_ARRAY:
                if (_parsingRow) {
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
           default:
                break;
            }
            token = parser.nextToken();
        }
       
        return new DatahubDataSet(queryResult, columns);
        
    }

}
