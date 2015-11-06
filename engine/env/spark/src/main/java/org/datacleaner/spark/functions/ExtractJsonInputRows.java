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
import org.apache.metamodel.schema.Table;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.connection.JsonDatastore;
import org.datacleaner.data.MockInputRow;
import org.datacleaner.spark.SparkJobContext;

public class ExtractJsonInputRows  {
    
    final SparkJobContext _sparkJobContext; 
    final JsonDatastore   _jsonDatastore; 
    
    
    public ExtractJsonInputRows(SparkJobContext sparkContext, JsonDatastore jsonDatastore) {
        _sparkJobContext = sparkContext; 
        _jsonDatastore = jsonDatastore; 
    }
    
    
    public List<InputRow> getInputRows(){
    
        final List<InputRow> list = new ArrayList<>();
        final JsonDataContext jsonContext = new JsonDataContext(_jsonDatastore.getResource()); 
        final List<InputColumn<?>> sourceColumns = _sparkJobContext.getAnalysisJob().getSourceColumns();
        final String[] columnNames = new String[sourceColumns.size()];
        for (int i=0;i<sourceColumns.size();i++){
            columnNames[i]= sourceColumns.get(i).getName(); 
        }
        final Table table = jsonContext.getDefaultSchema().getTable(0);
        final Query query = new Query().from(table).where(columnNames);
        final DataSet executeQuery = jsonContext.executeQuery(query); 
        
        while (executeQuery.next()){
            final Row row = executeQuery.getRow();
            final Object[] values = row.getValues();
            list.add(getInputRow(sourceColumns, values)); 
        }
        return list;
    }
    
    private MockInputRow getInputRow(final List<InputColumn<?>> sourceColumns, Object[] values){
    
    final MockInputRow inputRow = new MockInputRow();
    for (InputColumn<?> sourceColumn : sourceColumns) {
        assert sourceColumn.isPhysicalColumn();
        final int columnIndex = sourceColumn.getPhysicalColumn().getColumnNumber();
        final Object value = values[columnIndex];
        inputRow.put(sourceColumn, value);
    }
    return inputRow;
}
}
