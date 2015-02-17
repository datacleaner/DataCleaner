package org.datacleaner.extension.output;

import java.io.Closeable;
import java.io.File;
import java.util.List;

import org.apache.metamodel.DataContext;
import org.apache.metamodel.UpdateCallback;
import org.apache.metamodel.UpdateScript;
import org.apache.metamodel.create.CreateTable;
import org.apache.metamodel.excel.ExcelDataContext;
import org.apache.metamodel.insert.RowInsertionBuilder;
import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.Action;
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

    public ExcelDataContextWriter(File file, String sheetName) {
        _dataContext = new ExcelDataContext(file);
        _sheetName = sheetName;
        _buffer = new WriteBuffer(2000, new Action<Iterable<Object[]>>() {

            @Override
            public void run(final Iterable<Object[]> records) throws Exception {
                _dataContext.executeUpdate(new UpdateScript() {
                    @Override
                    public void run(UpdateCallback callback) {
                        final Table table = callback.getDataContext().getDefaultSchema().getTableByName(_sheetName);
                        for (Object[] objects : records) {
                            final RowInsertionBuilder insert = callback.insertInto(table);
                            for (int i = 0; i < objects.length; i++) {
                                insert.value(i, objects[i]);
                            }
                            insert.execute();
                        }
                    }
                });
            }
        });
    }

    @Override
    public void close() {
        _buffer.flushBuffer();
    }

    public void createTable(List<String> headers) {
        final CreateTable createTable = new CreateTable(_dataContext.getDefaultSchema(), _sheetName);
        for (String header : headers) {
            createTable.withColumn(header).ofType(ColumnType.STRING);
        }
        _dataContext.executeUpdate(createTable);
    }

    public void insertValues(Object[] values) {
        _buffer.addToBuffer(values);
    }
}
