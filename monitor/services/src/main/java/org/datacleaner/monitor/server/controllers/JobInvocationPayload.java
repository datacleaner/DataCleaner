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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Bean which represents the payload of invoking a DC job using the
 * {@link JobInvocationController}.
 */
public class JobInvocationPayload implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<JobInvocationRowData> rows;

    private List<String> columns;

    public void setRows(List<JobInvocationRowData> rows) {
        this.rows = rows;
    }

    public List<JobInvocationRowData> getRows() {
        if (rows == null) {
            rows = new ArrayList<JobInvocationRowData>();
        }
        return rows;
    }

    public void addRow(Object[] values) {
        getRows().add(new JobInvocationRowData(values));
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public List<String> getColumns() {
        return columns;
    }

    @Override
    public String toString() {
        return "JobInvocationPayload[columns=" + columns + ",rows=" + rows + "]";
    }
}
