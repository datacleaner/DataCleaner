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
package org.datacleaner.output.excel;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.create.TableCreationBuilder;
import org.apache.metamodel.excel.ExcelDataContext;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.MutableRef;
import org.datacleaner.api.InputColumn;
import org.datacleaner.output.OutputWriter;

public final class ExcelOutputWriterFactory {

    private static final Map<String, AtomicInteger> counters = new HashMap<>();
    private static final Map<String, UpdateableDataContext> dataContexts = new HashMap<>();

    public static OutputWriter getWriter(final String filename, final String sheetName, String[] columnNames,
            final InputColumn<?>... columns) {
        final ExcelOutputWriter outputWriter;

        if (columnNames == null || columnNames.length != columns.length) {
            columnNames = new String[columns.length];
            for (int i = 0; i < columns.length; i++) {
                columnNames[i] = columns[i].getName();
            }
        }

        synchronized (dataContexts) {
            UpdateableDataContext dataContext = dataContexts.get(filename);
            if (dataContext == null) {

                final File file = new File(filename);
                dataContext = new ExcelDataContext(file);

                final Table table = getTable(dataContext, sheetName, columnNames);

                dataContexts.put(filename, dataContext);
                counters.put(filename, new AtomicInteger(1));
                outputWriter = new ExcelOutputWriter(dataContext, filename, table, columns);

                // write the headers
            } else {
                // Make sure the schemas are refreshed so the getTable method recreates a sheet if it has been
                // deleted (and that is not yet reflected in the dataContexts map).
                dataContext.refreshSchemas();

                final Table table = getTable(dataContext, sheetName, columnNames);
                outputWriter = new ExcelOutputWriter(dataContext, filename, table, columns);
                counters.get(filename).incrementAndGet();
            }
        }

        return outputWriter;
    }

    private static Table getTable(final UpdateableDataContext dataContext, final String sheetName,
            final String[] columnNames) {
        final Schema schema = dataContext.getDefaultSchema();
        Table table = schema.getTableByName(sheetName);
        if (table == null) {
            final MutableRef<Table> tableRef = new MutableRef<>();
            dataContext.executeUpdate(callback -> {
                final TableCreationBuilder tableBuilder = callback.createTable(schema, sheetName);
                for (final String columnName : columnNames) {
                    tableBuilder.withColumn(columnName);
                }
                tableRef.set(tableBuilder.execute());
            });
            table = tableRef.get();
        }
        return table;
    }

    protected static void release(final String filename) {
        final int count = counters.get(filename).decrementAndGet();
        if (count == 0) {
            synchronized (dataContexts) {
                dataContexts.remove(filename);
            }
        }
    }

}
