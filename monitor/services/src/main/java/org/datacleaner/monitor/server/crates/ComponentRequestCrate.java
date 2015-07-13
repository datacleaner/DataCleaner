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
package org.datacleaner.monitor.server.crates;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.metamodel.data.DataSetHeader;
import org.apache.metamodel.data.DefaultRow;
import org.apache.metamodel.data.SimpleDataSetHeader;
import org.apache.metamodel.query.SelectItem;
import org.apache.metamodel.schema.MutableTable;
import org.apache.metamodel.schema.*;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.configuration.jaxb.PojoTableType;
import org.datacleaner.data.MetaModelInputColumn;
import org.datacleaner.data.MetaModelInputRow;
import org.datacleaner.data.MutableInputColumn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author j.horcicka (GMC)
 * @since 10.7.15
 */
public class ComponentRequestCrate {

    private Map<String, String> propertiesMap = new HashMap<>();
    private List<String> columnList = new ArrayList<>();
    private List<List<String>> dataTable = new ArrayList<>();

    @JsonIgnore
    public MutableTable table;
    @JsonIgnore
    public List<InputColumn> inputColumns;
    @JsonIgnore
    public List<InputRow> inputRows;

    public List<List<String>> getDataTable() {
        return dataTable;
    }

    public void setDataTable(List<List<String>> dataTable) {
        this.dataTable = dataTable;
    }

    public List<String> getColumnList() {
        return columnList;
    }

    public void setColumnList(List<String> columnList) {
        this.columnList = columnList;
    }

    public Map<String,String> getPropertiesMap() {
        return propertiesMap;
    }

    public void setPropertiesMap(Map<String, String> propertiesMap) {
        this.propertiesMap = propertiesMap;
    }

    public Table getTable() {
        return table;
    }

    public List<InputColumn> getInputColumns() {
        return inputColumns;
    }

    public List<InputRow> getInputRows() {
        return inputRows;
    }

    public void init() {
        table = new MutableTable("table-name");
        inputColumns = new ArrayList<>();
        int index = 0;
        for (String columnName : columnList) {
            Column column = new MutableColumn(columnName, ColumnType.VARCHAR, table, index, true);
            table.addColumn(index, column);
            InputColumn inCol = new MetaModelInputColumn(column);
            inputColumns.add(inCol);
            index++;
        }

        DataSetHeader header = new SimpleDataSetHeader(table.getColumns());
        inputRows = new ArrayList<>();
        int id = 0;
        for (List<String> row : dataTable) {
            DefaultRow inputRow = new DefaultRow(header, row.toArray());
            inputRows.add(new MetaModelInputRow(id, inputRow));
            id++;
        }
    }
}
