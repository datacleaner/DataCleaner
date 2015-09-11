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
package org.datacleaner.monitor.server.controllers;

import java.util.HashMap;
import java.util.Map;

/**
 * POJO that Captures the columns to update and their values, the conditions in
 * the where clause and the table name of a parsed UPDATE statement.
 *
 */
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
