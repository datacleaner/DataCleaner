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
package org.datacleaner.output.datastore;

import java.io.File;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.metamodel.DataContextFactory;
import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.create.CreateTable;
import org.apache.metamodel.drop.DropTable;
import org.apache.metamodel.schema.Schema;
import org.datacleaner.api.InputColumn;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.JdbcDatastore;
import org.datacleaner.output.OutputRow;
import org.datacleaner.output.OutputWriter;
import org.datacleaner.util.ReflectionUtils;

final class DatastoreOutputWriter implements OutputWriter {

    private static final String DRIVER_CLASS_NAME = "org.h2.Driver";

    private final String _datastoreName;
    private final String _jdbcUrl;
    private final Connection _connection;
    private final String _tableName;
    private final InputColumn<?>[] _columns;
    private final PreparedStatement _insertStatement;
    private final DatastoreCreationDelegate _datastoreCreationDelegate;

    public DatastoreOutputWriter(String datastoreName, String tableName, File directory, InputColumn<?>[] columns,
            DatastoreCreationDelegate datastoreCreationDelegate) {
        this(datastoreName, tableName, directory, columns, datastoreCreationDelegate, true);
    }

    public DatastoreOutputWriter(String datastoreName, String tableName, File directory, InputColumn<?>[] columns,
            DatastoreCreationDelegate datastoreCreationDelegate, boolean truncateExisting) {
        _datastoreName = datastoreName;
        _jdbcUrl = DatastoreOutputUtils.getJdbcUrl(directory, _datastoreName);
        _columns = columns;
        _datastoreCreationDelegate = datastoreCreationDelegate;

        try {
            Class.forName(DRIVER_CLASS_NAME);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }

        try {
            _connection = DriverManager.getConnection(_jdbcUrl, "SA", "");
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }

        // make table name safe
        tableName = DatastoreOutputUtils.safeName(tableName);

        synchronized (DatastoreOutputWriter.class) {
            final UpdateableDataContext dc = DataContextFactory.createJdbcDataContext(_connection);
            dc.refreshSchemas();
            final Schema schema = dc.getDefaultSchema();
            final String[] tableNames = schema.getTableNames();

            if (truncateExisting) {
                _tableName = tableName;

                for (String existingTableName : tableNames) {
                    if (_tableName.equalsIgnoreCase(existingTableName)) {
                        dc.executeUpdate(new DropTable(schema, existingTableName));
                    }
                }
            } else {
                int tableNumber = 0;
                boolean accepted = false;
                String proposalName = null;
                while (!accepted) {
                    tableNumber++;
                    proposalName = tableName + '_' + tableNumber;
                    accepted = true;
                    for (String existingTableName : tableNames) {
                        if (existingTableName.equalsIgnoreCase(proposalName)) {
                            accepted = false;
                            break;
                        }
                    }
                }
                _tableName = proposalName;
            }

            // create a CREATE TABLE statement and execute it
            CreateTable createTable = new CreateTable(schema, _tableName);

            for (int i = 0; i < columns.length; i++) {
                final InputColumn<?> column = columns[i];
                final String columnName = DatastoreOutputUtils.safeName(column.getName());

                if (!isDirectlyInsertableType(column)) {
                    createTable.withColumn(columnName).ofNativeType(getSqlType(String.class));
                } else {
                    final Class<?> dataType = column.getDataType();
                    createTable.withColumn(columnName).ofNativeType(getSqlType(dataType));
                }
            }

            dc.executeUpdate(createTable);
        }

        // create a reusable INSERT statement
        final StringBuilder insertStatementBuilder = new StringBuilder();
        insertStatementBuilder.append("INSERT INTO ");
        insertStatementBuilder.append(_tableName);
        insertStatementBuilder.append(" VALUES (");
        for (int i = 0; i < _columns.length; i++) {
            if (i != 0) {
                insertStatementBuilder.append(',');
            }
            insertStatementBuilder.append('?');
        }
        insertStatementBuilder.append(')');

        try {
            _insertStatement = _connection.prepareStatement(insertStatementBuilder.toString());
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String getSqlType(Class<?> valueType) {
        if (String.class == valueType) {
            return "VARCHAR";
        }
        if (Number.class == valueType) {
            return "DOUBLE";
        }
        if (Integer.class == valueType) {
            return "INTEGER";
        }
        if (Long.class == valueType) {
            return "BIGINT";
        }
        if (Double.class == valueType) {
            return "DOUBLE";
        }
        if (Short.class == valueType) {
            return "SMALLINT";
        }
        if (Float.class == valueType) {
            return "FLOAT";
        }
        if (BigInteger.class == valueType) {
            return "BIGINT";
        }
        if (Character.class == valueType) {
            return "CHAR";
        }
        if (Boolean.class == valueType) {
            return "BOOLEAN";
        }
        if (Byte.class == valueType) {
            return "BINARY";
        }
        if (ReflectionUtils.isDate(valueType)) {
            return "TIMESTAMP";
        }
        if (ReflectionUtils.isByteArray(valueType)) {
            return "BLOB";
        }
        throw new UnsupportedOperationException("Unsupported value type: " + valueType);
    }

    @Override
    public OutputRow createRow() {

        return new DatastoreOutputRow(_insertStatement, _columns);
    }

    public String getTableName() {
        return _tableName;
    }

    @Override
    public void close() {
        try {
            _insertStatement.close();
        } catch (Exception e) {
            // do nothing
        }

        DatastoreOutputWriterFactory.release(this);

        Datastore datastore = new JdbcDatastore(_datastoreName, _jdbcUrl, DRIVER_CLASS_NAME, "SA", "", true);
        _datastoreCreationDelegate.createDatastore(datastore);
    }

    public String getJdbcUrl() {
        return _jdbcUrl;
    }

    public Connection getConnection() {
        return _connection;
    }

    public static boolean isDirectlyInsertableType(InputColumn<?> column) {
        final Class<?> dataType = column.getDataType();
        return ReflectionUtils.isNumber(dataType) || ReflectionUtils.isDate(dataType)
                || ReflectionUtils.isBoolean(dataType);
    }

    @Override
    public void addToBuffer(Object[] rowData) {
        //Do nothing
    }
}
