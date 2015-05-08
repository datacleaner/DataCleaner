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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.inject.Named;

import org.apache.commons.io.FilenameUtils;
import org.apache.metamodel.DataContext;
import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.csv.CsvConfiguration;
import org.apache.metamodel.csv.CsvDataContext;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.drop.DropTable;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.FileResource;
import org.apache.metamodel.util.NumberComparator;
import org.datacleaner.api.Alias;
import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.Distributed;
import org.datacleaner.api.FileProperty;
import org.datacleaner.api.FileProperty.FileAccessMode;
import org.datacleaner.api.HasLabelAdvice;
import org.datacleaner.api.Initialize;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.Validate;
import org.datacleaner.beans.writers.WriteDataResult;
import org.datacleaner.beans.writers.WriteDataResultImpl;
import org.datacleaner.components.categories.WriteSuperCategory;
import org.datacleaner.components.convert.ConvertToDateTransformer;
import org.datacleaner.components.convert.ConvertToNumberTransformer;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.connection.ExcelDatastore;
import org.datacleaner.connection.UpdateableDatastoreConnection;
import org.datacleaner.descriptors.FilterDescriptor;
import org.datacleaner.descriptors.TransformerDescriptor;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.output.OutputWriter;
import org.datacleaner.output.csv.CsvOutputWriterFactory;
import org.datacleaner.output.excel.ExcelOutputWriterFactory;
import org.datacleaner.util.CompareUtils;
import org.datacleaner.util.ReflectionUtils;
import org.datacleaner.util.sort.SortMergeWriter;

@Named("Create Excel spreadsheet")
@Alias("Write to Excel spreadsheet")
@Description("Write data to an Excel spreadsheet, useful for manually editing and inspecting the data in Microsoft Excel.")
@Categorized(superCategory = WriteSuperCategory.class)
@Distributed(false)
public class CreateExcelSpreadsheetAnalyzer extends AbstractOutputWriterAnalyzer implements HasLabelAdvice {

    public static final String PROPERTY_FILE = "File";
    public static final String PROPERTY_SHEET_NAME = "Sheet name";
    public static final String PROPERTY_OVERWRITE_SHEET_IF_EXISTS = "Overwrite sheet if exists";
    private static final String[] excelExtension  = {"xlsx", "xls"}; 
    
    private static final char[] ILLEGAL_SHEET_CHARS = new char[] { '.', ':' };

    @Configured(PROPERTY_FILE)
    @FileProperty(accessMode = FileAccessMode.SAVE, extension = { "xls", "xlsx" })
    File file = new File("DataCleaner-staging.xlsx");

    @Configured(PROPERTY_SHEET_NAME)
    String sheetName;

    @Configured(PROPERTY_OVERWRITE_SHEET_IF_EXISTS)
    boolean overwriteSheetIfExists;

    @Configured(order = 1, required = false)
    InputColumn<?> columnToBeSortedOn;

    private final Character separatorChar = ',';
    private final Character quoteChar = '"';
    private final Character escapeChar = '\\';
    private final boolean includeHeader = true;
    private File _targetFile;
    private int indexOfColumnToBeSortedOn = -1;
    private boolean isColumnToBeSortedOnPresentInInput = true;

    @Initialize
    public void initTempFile() throws Exception {
        if (_targetFile == null) {
            if (columnToBeSortedOn != null) {
                _targetFile = File.createTempFile("csv_file_analyzer", ".csv");
            } else {
                _targetFile = file;
            }
        }
    }

    @Override
    public String getSuggestedLabel() {
        if (file == null || sheetName == null) {
            return null;
        }
        return file.getName() + " - " + sheetName;
    }

    @Validate
    public void validate() {
        for (char c : ILLEGAL_SHEET_CHARS) {
            if (sheetName.indexOf(c) != -1) {
                throw new IllegalStateException("Sheet name cannot contain '" + c + "'");
            }
        }

        if (file.exists()) {
            Datastore datastore = new ExcelDatastore(file.getName(), new FileResource(file), file.getAbsolutePath());
            try (final DatastoreConnection connection = datastore.openConnection()) {
                final String[] tableNames = connection.getDataContext().getDefaultSchema().getTableNames();
                for (int i = 0; i < tableNames.length; i++) {
                    if (tableNames[i].equals(sheetName)) {
                        if (!overwriteSheetIfExists) {
                            throw new IllegalStateException("The sheet '" + sheetName
                                    + "' already exists. Please select another sheet name.");
                        }
                    }
                }
            }
        }
        
        if (!FilenameUtils.isExtension(file.getName(), excelExtension)){
           throw new IllegalStateException("Please add the '.xlsx'  or '.xls' extension to the filename"); 
        }
       
    }

    @Override
    public void configureForFilterOutcome(AnalysisJobBuilder ajb, FilterDescriptor<?, ?> descriptor, String categoryName) {
        final String dsName = ajb.getDatastore().getName();
        sheetName = fixSheetName("output-" + dsName + "-" + descriptor.getDisplayName() + "-" + categoryName);
    }

    @Override
    public void configureForTransformedData(AnalysisJobBuilder ajb, TransformerDescriptor<?> descriptor) {
        final String dsName = ajb.getDatastore().getName();
        sheetName = fixSheetName("output-" + dsName + "-" + descriptor.getDisplayName());
    }

    private String fixSheetName(String sheet) {
        for (char c : ILLEGAL_SHEET_CHARS) {
            while (sheet.indexOf(c) != -1) {
                sheet = sheet.replace(c, '-');
            }
        }
        return sheet;
    }

    @Override
    public OutputWriter createOutputWriter() {
        if (file.exists()) {
            ExcelDatastore datastore = new ExcelDatastore(file.getName(), new FileResource(file),
                    file.getAbsolutePath());
            try (final UpdateableDatastoreConnection connection = datastore.openConnection()) {
                final DataContext dataContext = connection.getDataContext();
                final String[] tableNames = dataContext.getDefaultSchema().getTableNames();
                for (int i = 0; i < tableNames.length; i++) {
                    if (tableNames[i].equals(sheetName)) {
                        if (overwriteSheetIfExists) {
                            final Table tableSheet = dataContext.getTableByQualifiedLabel(sheetName);
                            final UpdateableDataContext updateableDataContext = connection.getUpdateableDataContext();
                            updateableDataContext.executeUpdate(new DropTable(tableSheet));
                        }
                    }
                }
            }
        }
        // If the user wants the file sorted after a column we create a
        // temporary file and return a CSV writer in order
        // to make a MergeSort on it, otherwise we return a normal Excel writer
        if (columnToBeSortedOn != null) {
            return createTemporaryCsvWriter();
        } else {
            return ExcelOutputWriterFactory.getWriter(file.getPath(), sheetName, columns);
        }
    }

    private OutputWriter createTemporaryCsvWriter() {
        final List<String> headers = new ArrayList<String>();
        for (int i = 0; i < columns.length; i++) {
            String columnName = columns[i].getName();
            headers.add(columnName);
            if (columnToBeSortedOn != null) {
                if (columnName.equals(columnToBeSortedOn.getName())) {
                    indexOfColumnToBeSortedOn = i;
                }
            }
        }

        if (indexOfColumnToBeSortedOn == -1) {
            this.isColumnToBeSortedOnPresentInInput = false;
            indexOfColumnToBeSortedOn = columns.length;
            headers.add(columnToBeSortedOn.getName());
            InputColumn<?>[] newColumns = new InputColumn<?>[columns.length + 1];
            for (int i = 0; i < columns.length; i++) {
                newColumns[i] = columns[i];
            }
            newColumns[columns.length] = columnToBeSortedOn;
            columns = newColumns;
        }

        if (_targetFile == null) {
            try {
                initTempFile();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return CsvOutputWriterFactory.getWriter(_targetFile.getPath(), headers.toArray(new String[0]), separatorChar,
                quoteChar, escapeChar, includeHeader, columns);
    }

    @Override
    protected WriteDataResult getResultInternal(int rowCount) {
        if (columnToBeSortedOn != null) {
            // Sorts the file using merge sort in the temporary file and then
            // write into the final file
            mergeSortFile();
        }
        final FileResource resource = new FileResource(file);
        final Datastore datastore = new ExcelDatastore(file.getName(), resource, file.getAbsolutePath());
        final WriteDataResult result = new WriteDataResultImpl(rowCount, datastore, null, sheetName);
        return result;
    }

    private void mergeSortFile() {
        final CsvConfiguration csvConfiguration = new CsvConfiguration(CsvConfiguration.DEFAULT_COLUMN_NAME_LINE,
                FileHelper.DEFAULT_ENCODING, separatorChar, quoteChar, escapeChar, false, true);

        final CsvDataContext tempDataContext = new CsvDataContext(_targetFile, csvConfiguration);
        final Table table = tempDataContext.getDefaultSchema().getTable(0);
        
        final Comparator<? super Row> comparator = createComparator();

        final SortMergeWriter<Row, ExcelDataContextWriter> sortMergeWriter = new SortMergeWriter<Row, ExcelDataContextWriter>(
                comparator) {

            @Override
            protected ExcelDataContextWriter createWriter(File file) {
                return new ExcelDataContextWriter(file, sheetName);
            }

            @Override
            protected void writeHeader(ExcelDataContextWriter writer) throws IOException {
                List<String> headers = new ArrayList<String>(Arrays.asList(table.getColumnNames()));
                if (!isColumnToBeSortedOnPresentInInput) {
                    headers.remove(columnToBeSortedOn.getName());
                }
                writer.createTable(headers);
            }

            @Override
            protected void writeRow(ExcelDataContextWriter writer, Row row, int count) throws IOException {
                for (int i = 0; i < count; i++) {
                    List<Object> valuesList = new ArrayList<Object>(Arrays.asList(row.getValues()));
                    if (!isColumnToBeSortedOnPresentInInput) {
                        valuesList.remove(indexOfColumnToBeSortedOn);
                    }
                    final Object[] values = valuesList.toArray(new Object[0]);
                    writer.insertValues(values);
                }
            }
        };

        // read from the temp file and sort it into the final file
        final DataSet dataSet = tempDataContext.query().from(table).selectAll().execute();
        try {
            while (dataSet.next()) {
                final Row row = dataSet.getRow();
                sortMergeWriter.append(row);
            }
        } finally {
            dataSet.close();
        }
        sortMergeWriter.write(file);
    }
    
    private Comparator<Row> createComparator() {
        final Class<?> dataType = columnToBeSortedOn.getDataType();
        final boolean isNumber = dataType != null && ReflectionUtils.isNumber(dataType);
        final boolean isDate = dataType != null && ReflectionUtils.isDate(dataType);

        return new Comparator<Row>() {
            @Override
            public int compare(Row row1, Row row2) {
                final Comparable<?> value1 = getComparableValue(row1, isNumber, isDate);
                final Comparable<?> value2 = getComparableValue(row2, isNumber, isDate);
                int comparableResult = CompareUtils.compareUnbound(value1, value2);
                if (comparableResult != 0) {
                    return comparableResult;
                } else {
                    // The values of the data at the row, and column to be
                    // sorted on are
                    // exactly the same. Now look at other values of all the
                    // columns to
                    // find if the two rows are same.
                    int numberOfSelectItems = row1.getSelectItems().length;
                    for (int i = 0; i < numberOfSelectItems; i++) {
                        final String rowValue1 = (String) row1.getValue(i);
                        final String rowValue2 = (String) row2.getValue(i);
                        if (CompareUtils.compare(rowValue1, rowValue2) == 0) {
                            continue;
                        } else {
                            return CompareUtils.compare(rowValue1, rowValue2);
                        }
                    }
                }

                return comparableResult;
            }
        };
    }
    
    protected Comparable<?> getComparableValue(Row row, boolean isNumber, boolean isDate) {
        final String value = (String) row.getValue(indexOfColumnToBeSortedOn);
        if (isNumber) {
            final Number result = ConvertToNumberTransformer.transformValue(value);
            if (result instanceof Comparable) {
                return (Comparable<?>) result;
            }
            return NumberComparator.getComparable(result);
        }
        if (isDate) {
            return ConvertToDateTransformer.getInternalInstance().transformValue(value);
        }
        return value;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

}
