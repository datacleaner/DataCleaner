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
package org.datacleaner.extension.output;

import java.io.Closeable;
import java.io.File;
import java.util.List;

import org.apache.metamodel.DataContext;
import org.apache.metamodel.create.CreateTable;
import org.apache.metamodel.excel.ExcelDataContext;
import org.apache.metamodel.insert.RowInsertionBuilder;
import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.Table;
import org.datacleaner.util.WriteBuffer;
import org.datacleaner.util.sort.SortMergeWriter;

/**
 * A delegate writer for {@link CreateExcelSpreadsheetAnalyzer} to use together
 * with the {@link SortMergeWriter} when inserting records into an excel
 * spreadsheet.
 *
 * Note: Most of this class is actually fairly generic. It could in the future
 * be applied to work on other {@link DataContext} types as well.
 */
class ExcelDataContextWriter implements Closeable {

    private final ExcelDataContext _dataContext;
    private final String _sheetName;
    private final WriteBuffer _buffer;

    public ExcelDataContextWriter(final File file, final String sheetName) {
        _dataContext = new ExcelDataContext(file);
        _sheetName = sheetName;
        _buffer = new WriteBuffer(2000, records -> _dataContext.executeUpdate(callback -> {
            final Table table = callback.getDataContext().getDefaultSchema().getTableByName(_sheetName);
            for (final Object[] objects : records) {
                final RowInsertionBuilder insert = callback.insertInto(table);
                for (int i = 0; i < objects.length; i++) {
                    insert.value(i, objects[i]);
                }
                insert.execute();
            }
        }));
    }

    @Override
    public void close() {
        _buffer.flushBuffer();
    }

    public void createTable(final List<String> headers) {
        final CreateTable createTable = new CreateTable(_dataContext.getDefaultSchema(), _sheetName);
        for (final String header : headers) {
            createTable.withColumn(header).ofType(ColumnType.STRING);
        }
        _dataContext.executeUpdate(createTable);
    }

    public void insertValues(final Object[] values) {
        _buffer.addToBuffer(values);
    }
}
