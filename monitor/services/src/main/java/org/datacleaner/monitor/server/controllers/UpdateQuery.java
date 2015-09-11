package org.datacleaner.monitor.server.controllers;

import java.util.HashMap;
import java.util.Map;

public class UpdateQuery {

    private String _tableName;
    private Map<String, Object> _updateColumns;
    private Map<String, Object> _whereConditions;
    
    public UpdateQuery() {
        _updateColumns = new HashMap<String, Object>();
        _whereConditions = new HashMap<String, Object>();
    }
    
    public void setUpdatedTable(String updatedTableName) {
        this._tableName = updatedTableName;
    }

    public void addUpdateColumn(String columnName, String value) {
        _updateColumns.put(columnName, value);
    }
    
    public void setWhere(String columnName, String value) {
        _whereConditions.put(columnName, value);
    }

    public String getTable() {
        return _tableName;
    }

    /**
     * @return the columns to update with their new values
     */
    public Map<String, Object> getUpdateColumns() {
        return _updateColumns;
    }

    /**
     * @return the conditions
     */
    public Map<String, Object> getWhereConditions() {
        return _whereConditions;
    }
    
}
