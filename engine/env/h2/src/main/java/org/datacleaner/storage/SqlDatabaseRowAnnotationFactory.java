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
package org.datacleaner.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.data.MockInputRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlDatabaseRowAnnotationFactory implements RowAnnotationFactory {

    private final static Logger logger = LoggerFactory.getLogger(SqlDatabaseRowAnnotationFactory.class);
    private final Map<InputColumn<?>, String> _inputColumnNames = new LinkedHashMap<InputColumn<?>, String>();
    private final Map<RowAnnotation, String> _annotationColumnNames = new HashMap<RowAnnotation, String>();
    private final Connection _connection;
    private final String _tableName;
    private final AtomicInteger _nextColumnIndex = new AtomicInteger(1);

    public SqlDatabaseRowAnnotationFactory(Connection connection, String tableName) {
        _connection = connection;
        _tableName = tableName;
        String intType = SqlDatabaseUtils.getSqlType(Integer.class);
        performUpdate(SqlDatabaseUtils.CREATE_TABLE_PREFIX + tableName + " (id " + intType
                + " PRIMARY KEY, distinct_count " + intType + ")");
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        performUpdate("DROP TABLE " + _tableName);
    }

    private void performUpdate(String sql) {
        SqlDatabaseUtils.performUpdate(_connection, sql);
    }

    @Override
    public RowAnnotation createAnnotation() {
        return new RowAnnotationImpl();
    }

    private boolean containsRow(InputRow row) {
        ResultSet rs = null;
        PreparedStatement st = null;
        try {
            st = _connection.prepareStatement("SELECT COUNT(*) FROM " + _tableName + " WHERE id = ?");
            boolean contains;
            st.setInt(1, row.getId());
            rs = st.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                if (count == 0) {
                    contains = false;
                } else if (count == 1) {
                    contains = true;
                } else {
                    throw new IllegalStateException(count + " rows with id=" + row.getId() + " exists in database!");
                }
            } else {
                contains = false;
            }
            return contains;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        } finally {
            SqlDatabaseUtils.safeClose(rs, st);
        }
    }

    @Override
    public void annotate(InputRow[] rows, RowAnnotation annotation) {
        for (InputRow row : rows) {
            annotate(row, 1, annotation);
        }
    }

    @Override
    public synchronized void annotate(InputRow row, int distinctCount, RowAnnotation annotation) {
        RowAnnotationImpl a = (RowAnnotationImpl) annotation;

        List<InputColumn<?>> inputColumns = row.getInputColumns();
        List<String> columnNames = new ArrayList<String>(inputColumns.size());
        List<Object> values = new ArrayList<Object>(inputColumns.size());
        for (InputColumn<?> inputColumn : inputColumns) {
            String columnName = getColumnName(inputColumn, true);
            columnNames.add(columnName);
            Object value = row.getValue(inputColumn);
            values.add(value);
        }

        String annotationColumnName = getColumnName(annotation, true);

        if (containsRow(row)) {
            PreparedStatement st = null;
            ResultSet rs = null;

            boolean annotated;
            try {
                st = _connection.prepareStatement("SELECT " + annotationColumnName + " FROM " + _tableName
                        + " WHERE id=?");
                st.setInt(1, row.getId());
                rs = st.executeQuery();
                if (rs.next()) {
                    annotated = rs.getBoolean(1);
                } else {
                    logger.error("No rows returned on annotation status for id={}", row.getId());
                    annotated = false;
                }
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            } finally {
                SqlDatabaseUtils.safeClose(rs, st);
            }

            if (!annotated) {
                try {
                    st = _connection.prepareStatement("UPDATE " + _tableName + " SET " + annotationColumnName
                            + "=TRUE WHERE id=?");
                    st.setInt(1, row.getId());
                    st.executeUpdate();
                    a.incrementRowCount(distinctCount);
                } catch (SQLException e) {
                    throw new IllegalStateException(e);
                } finally {
                    SqlDatabaseUtils.safeClose(null, st);
                }
            }

        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("INSERT INTO ");
            sb.append(_tableName);
            sb.append(" (id,distinct_count");
            sb.append(',');
            sb.append(annotationColumnName);
            for (String columnName : columnNames) {
                sb.append(',');
                sb.append(columnName);
            }
            sb.append(") VALUES (?,?,?");
            for (int i = 0; i < values.size(); i++) {
                sb.append(",?");
            }
            sb.append(")");

            PreparedStatement st = null;
            try {
                st = _connection.prepareStatement(sb.toString());
                st.setInt(1, row.getId());
                st.setInt(2, distinctCount);
                st.setBoolean(3, true);
                for (int i = 0; i < values.size(); i++) {
                    st.setObject(i + 4, values.get(i));
                }
                st.executeUpdate();
                a.incrementRowCount(distinctCount);
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            } finally {
                SqlDatabaseUtils.safeClose(null, st);
            }
        }
    }

    private String getColumnName(RowAnnotation annotation, boolean createIfNonExisting) {
        String columnName = _annotationColumnNames.get(annotation);
        if (columnName == null) {
            if (createIfNonExisting) {
                int index = _nextColumnIndex.getAndIncrement();
                columnName = "col" + index;
                performUpdate("ALTER TABLE " + _tableName + " ADD COLUMN " + columnName + " "
                        + SqlDatabaseUtils.getSqlType(Boolean.class) + " DEFAULT FALSE");
                _annotationColumnNames.put(annotation, columnName);
            }
        }
        return columnName;
    }

    private String getColumnName(InputColumn<?> inputColumn, boolean createIfNonExisting) {
        String columnName = _inputColumnNames.get(inputColumn);
        if (columnName == null) {
            if (createIfNonExisting) {
                int index = _nextColumnIndex.getAndIncrement();
                columnName = "col" + index;
                Class<?> javaType = inputColumn.getDataType();

                performUpdate("ALTER TABLE " + _tableName + " ADD COLUMN " + columnName + " "
                        + SqlDatabaseUtils.getSqlType(javaType));
                _inputColumnNames.put(inputColumn, columnName);
            }
        }
        return columnName;
    }

    @Override
    public synchronized void reset(RowAnnotation annotation) {
        String columnName = getColumnName(annotation, false);
        if (columnName != null) {
            performUpdate("UPDATE " + _tableName + " SET " + columnName + " = FALSE");
        }
    }

    @Override
    public InputRow[] getRows(RowAnnotation annotation) {
        String annotationColumnName = getColumnName(annotation, false);
        if (annotationColumnName == null) {
            return new InputRow[0];
        }
        ResultSet rs = null;
        Statement st = null;
        try {
            st = _connection.createStatement();

            StringBuilder sb = new StringBuilder();
            sb.append("SELECT id");
            ArrayList<InputColumn<?>> inputColumns = new ArrayList<InputColumn<?>>(_inputColumnNames.keySet());
            for (InputColumn<?> inputColumn : inputColumns) {
                sb.append(',');
                String columnName = _inputColumnNames.get(inputColumn);
                sb.append(columnName);
            }
            sb.append(" FROM ");
            sb.append(_tableName);
            sb.append(" WHERE ");
            sb.append(annotationColumnName);
            sb.append(" = TRUE");

            rs = st.executeQuery(sb.toString());
            List<InputRow> rows = new ArrayList<InputRow>();
            while (rs.next()) {
                int id = rs.getInt(1);
                MockInputRow row = new MockInputRow(id);
                int colIndex = 2;
                for (InputColumn<?> inputColumn : inputColumns) {
                    Object value = rs.getObject(colIndex);
                    row.put(inputColumn, value);
                    colIndex++;
                }
                rows.add(row);
            }
            return rows.toArray(new InputRow[rows.size()]);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        } finally {
            SqlDatabaseUtils.safeClose(rs, st);
        }
    }

    @Override
    public Map<Object, Integer> getValueCounts(RowAnnotation annotation, InputColumn<?> inputColumn) {
        HashMap<Object, Integer> map = new HashMap<Object, Integer>();

        String inputColumnName = getColumnName(inputColumn, false);
        if (inputColumnName == null) {
            return map;
        }

        String annotationColumnName = getColumnName(annotation, false);
        if (annotationColumnName == null) {
            return map;
        }
        ResultSet rs = null;
        PreparedStatement st = null;
        try {
            st = _connection.prepareStatement("SELECT " + inputColumnName + ", SUM(distinct_count) FROM " + _tableName
                    + " WHERE " + annotationColumnName + " = TRUE GROUP BY " + inputColumnName);
            rs = st.executeQuery();
            while (rs.next()) {
                Object value = rs.getObject(1);
                int count = rs.getInt(2);
                map.put(value, count);
            }
            return map;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        } finally {
            SqlDatabaseUtils.safeClose(rs, st);
        }
    }

    @Override
    public void transferAnnotations(RowAnnotation from, RowAnnotation to) {
        final int increment = from.getRowCount();
        ((RowAnnotationImpl) to).incrementRowCount(increment);
        
        // TODO: Copy records to new annotation also?
    }
}
