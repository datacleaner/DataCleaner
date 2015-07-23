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

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.metamodel.csv.CsvConfiguration;
import org.apache.metamodel.csv.CsvDataContext;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.FileResource;
import org.apache.metamodel.util.Resource;
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
import org.datacleaner.api.Provided;
import org.datacleaner.api.Validate;
import org.datacleaner.beans.writers.WriteDataResult;
import org.datacleaner.beans.writers.WriteDataResultImpl;
import org.datacleaner.components.categories.WriteSuperCategory;
import org.datacleaner.connection.CsvDatastore;
import org.datacleaner.connection.Datastore;
import org.datacleaner.descriptors.FilterDescriptor;
import org.datacleaner.descriptors.TransformerDescriptor;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.output.OutputWriter;
import org.datacleaner.output.csv.CsvOutputWriter;
import org.datacleaner.output.csv.CsvOutputWriterFactory;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.sort.SortMergeWriter;

@Named("Create CSV file")
@Alias("Write to CSV file")
@Description("Write data to a CSV file on your harddrive. CSV file writing is extremely fast and the file format is commonly used in many tools. But CSV files do not preserve data types.")
@Categorized(superCategory = WriteSuperCategory.class)
@Distributed(false)
public class CreateCsvFileAnalyzer extends AbstractOutputWriterAnalyzer implements HasLabelAdvice {

    public static final String PROPERTY_FILE = "File Resource";
    public static final String PROPERTY_OVERWRITE_FILE_IF_EXISTS = "Overwrite file if exists";
    public static final String PROPERTY_COLUMN_TO_BE_SORTED_ON = "Column to be sorted on";

    @Inject
    @Configured(value = PROPERTY_FILE, order = 1)
    @FileProperty(accessMode = FileAccessMode.SAVE, extension = { "csv", "tsv", "txt", "dat" })
    Resource file;

    @Inject
    @Configured(order = 2, required = false)
    char separatorChar = ',';

    @Inject
    @Configured(order = 3, required = false)
    Character quoteChar = '"';

    @Inject
    @Configured(order = 4, required = false)
    Character escapeChar = '\\';

    @Inject
    @Configured(order = 5, required = false)
    boolean includeHeader = true;

    @Inject
    @Configured(order = 6, required = false, value = PROPERTY_COLUMN_TO_BE_SORTED_ON)
    InputColumn<?> columnToBeSortedOn;

    @Inject
    @Configured(value = PROPERTY_OVERWRITE_FILE_IF_EXISTS)
    boolean overwriteFileIfExists;

    @Inject
    @Provided
    UserPreferences userPreferences;

    private Resource _targetFile;
    private int _indexOfColumnToBeSortedOn = -1;
    private boolean _isColumnToBeSortedOnPresentInInput = true;
    private String[] _headers;

    @Initialize
    public void initTempFile() throws Exception {

        if (_targetFile == null) {
            if (columnToBeSortedOn != null) {
                _targetFile = new FileResource(File.createTempFile("csv_file_analyzer", ".csv"));
            } else {
                _targetFile = file;
            }
        }
    }

    @Override
    public String getSuggestedLabel() {
        if (file == null) {
            return null;
        }
        return file.getName();
    }

    @Validate
    public void validate() {
        if (file.isExists() && !overwriteFileIfExists) {
            throw new IllegalStateException(
                    "The file already exists. Please configure the job to overwrite the existing file.");
        }
    }

    @Override
    public void configureForFilterOutcome(AnalysisJobBuilder ajb, FilterDescriptor<?, ?> descriptor, String categoryName) {
        final String dsName = ajb.getDatastore().getName();
        final File saveDatastoreDirectory = userPreferences.getSaveDatastoreDirectory();
        final String displayName = descriptor.getDisplayName();
        final File fileTemp = new File(saveDatastoreDirectory, "output-" + dsName + "-" + displayName + "-"
                + categoryName + ".csv");
        file = new FileResource(fileTemp);
    }

    @Override
    public void configureForTransformedData(AnalysisJobBuilder ajb, TransformerDescriptor<?> descriptor) {
        final String dsName = ajb.getDatastore().getName();
        final File saveDatastoreDirectory = userPreferences.getSaveDatastoreDirectory();
        final String displayName = descriptor.getDisplayName();
        final File fileTemp = new File(saveDatastoreDirectory, "output-" + dsName + "-" + displayName + ".csv");
        file = new FileResource(fileTemp);
    }

    @Override
    public OutputWriter createOutputWriter() {

        List<String> headers = new ArrayList<String>();
        for (int i = 0; i < columns.length; i++) {
            String columnName = getColumnHeader(i);
            headers.add(columnName);
            if (columnToBeSortedOn != null) {
                if (columns[i].equals(columnToBeSortedOn)) {
                    _indexOfColumnToBeSortedOn = i;
                }
            }
        }

        if (columnToBeSortedOn != null) {
            if (_indexOfColumnToBeSortedOn == -1) {
                _isColumnToBeSortedOnPresentInInput = false;
                _indexOfColumnToBeSortedOn = columns.length;
                headers.add(columnToBeSortedOn.getName());
                InputColumn<?>[] newColumns = new InputColumn<?>[columns.length + 1];
                for (int i = 0; i < columns.length; i++) {
                    newColumns[i] = columns[i];
                }
                newColumns[columns.length] = columnToBeSortedOn;
                columns = newColumns;
            }
        }

        if (_targetFile == null) {
            try {
                initTempFile();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        _headers = headers.toArray(new String[0]);
        return CsvOutputWriterFactory.getWriter(_targetFile, _headers, separatorChar, getSafeQuoteChar(),
                getSafeEscapeChar(), includeHeader, columns);
    }

    private char getSafeQuoteChar() {
        if (quoteChar == null) {
            return CsvConfiguration.NOT_A_CHAR;
        }
        return quoteChar;
    }

    private char getSafeEscapeChar() {
        if (escapeChar == null) {
            return CsvConfiguration.NOT_A_CHAR;
        }
        return escapeChar;
    }

    private String getColumnHeader(int i) {
        if (fields == null) {
            return columns[i].getName();
        }
        return fields[i];
    }

    @Override
    protected WriteDataResult getResultInternal(int rowCount) {
        final CsvConfiguration csvConfiguration = new CsvConfiguration(CsvConfiguration.DEFAULT_COLUMN_NAME_LINE,
                FileHelper.DEFAULT_ENCODING, separatorChar, getSafeQuoteChar(), getSafeEscapeChar(), false, true);

        if (columnToBeSortedOn != null) {

            final CsvDataContext tempDataContext = new CsvDataContext(_targetFile, csvConfiguration);
            final Table table = tempDataContext.getDefaultSchema().getTable(0);

            final Comparator<? super Row> comparator = SortHelper.createComparator(columnToBeSortedOn,
                    _indexOfColumnToBeSortedOn);

            final SortMergeWriter<Row, OutputWriter> sortMergeWriter = new SortMergeWriter<Row, OutputWriter>(
                    comparator) {

                @Override
                protected void writeHeader(OutputWriter writer) throws IOException {
                    // The header is already written
                }

                @Override
                protected void writeRow(OutputWriter writer, Row row, int count) throws IOException {
                    for (int i = 0; i < count; i++) {
                        List<Object> valuesList = new ArrayList<Object>(Arrays.asList(row.getValues()));
                        if (!_isColumnToBeSortedOnPresentInInput) {
                            valuesList.remove(_indexOfColumnToBeSortedOn);
                        }
                        final Object[] values = valuesList.toArray(new Object[0]);
                        writer.addToBuffer(values);
                    }
                }

                @Override
                protected OutputWriter createWriter(Resource file) {
                    return CsvOutputWriterFactory.getWriter(file, _headers, separatorChar, getSafeQuoteChar(),
                            getSafeEscapeChar(), includeHeader, columns);
                }

                @Override
                protected CsvOutputWriter createWriter(File file) {
                    return null;
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

        final Datastore datastore = new CsvDatastore(file.getName(), file, csvConfiguration);
        return new WriteDataResultImpl(rowCount, datastore, null, null);
    }

    public void setFile(Resource file) {
        this.file = file;
    }
}
