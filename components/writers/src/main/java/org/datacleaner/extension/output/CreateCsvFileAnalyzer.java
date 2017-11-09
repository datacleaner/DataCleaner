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
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.metamodel.csv.CsvConfiguration;
import org.apache.metamodel.csv.CsvDataContext;
import org.apache.metamodel.csv.CsvWriter;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.FileResource;
import org.apache.metamodel.util.Resource;
import org.datacleaner.api.Alias;
import org.datacleaner.api.Categorized;
import org.datacleaner.api.Close;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.FileProperty;
import org.datacleaner.api.FileProperty.FileAccessMode;
import org.datacleaner.api.HasDistributionAdvice;
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
import org.datacleaner.output.csv.CsvOutputWriterFactory;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.sort.SortMergeWriter;

import com.google.common.base.Strings;

@Named("Create CSV file")
@Alias("Write to CSV file")
@Description("Write data to a CSV file. CSV file writing is extremely fast and the file format is commonly "
        + "used in many tools. But CSV files do not preserve data types.")
@Categorized(superCategory = WriteSuperCategory.class)
public class CreateCsvFileAnalyzer extends AbstractOutputWriterAnalyzer
        implements HasLabelAdvice, HasDistributionAdvice {

    public static final String PROPERTY_FILE = "File";
    public static final String PROPERTY_OVERWRITE_FILE_IF_EXISTS = "Overwrite file if exists";
    public static final String PROPERTY_COLUMN_TO_BE_SORTED_ON = "Column to be sorted on";
    public static final String PROPERTY_INCLUDE_HEADER = "Include header";

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
    @Configured(order = 5, required = false, value = PROPERTY_INCLUDE_HEADER)
    boolean includeHeader = true;

    @Inject
    @Description("Character-set encoding to write data with")
    @Configured(order = 6, required = false)
    String encoding = FileHelper.DEFAULT_ENCODING;

    @Inject
    @Description("An optional column to sort all records with. Note that sorting can add substantial "
            + "performance penalties to the overall operation.")
    @Configured(order = 7, required = false, value = PROPERTY_COLUMN_TO_BE_SORTED_ON)
    InputColumn<?> columnToBeSortedOn;

    @Inject
    @Configured(value = PROPERTY_OVERWRITE_FILE_IF_EXISTS)
    boolean overwriteFileIfExists;

    @Inject
    @Provided
    UserPreferences userPreferences;

    private Resource _targetResource;
    private int _indexOfColumnToBeSortedOn = -1;
    private boolean _isColumnToBeSortedOnPresentInInput = true;
    private File tempFile = null;

    @Initialize
    public void initTempFile() throws Exception {
        if (_targetResource == null) {
            if (columnToBeSortedOn != null) {
                 tempFile = File.createTempFile("csv_file_analyzer", ".csv");
                _targetResource = new FileResource(tempFile);
            } else {
                _targetResource = file;
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
        if (!overwriteFileIfExists && file.isExists()) {
            throw new IllegalStateException(
                    "The file already exists. Please configure the job to overwrite the existing file.");
        }
    }

    @Override
    public void configureForFilterOutcome(final AnalysisJobBuilder ajb, final FilterDescriptor<?, ?> descriptor,
            final String categoryName) {
        final String dsName = ajb.getDatastore().getName();
        final File saveDatastoreDirectory = userPreferences.getSaveDatastoreDirectory();
        final String displayName = descriptor.getDisplayName();
        file = new FileResource(
                new File(saveDatastoreDirectory, dsName + "-" + displayName + "-" + categoryName + ".csv"));
    }

    @Override
    public void configureForTransformedData(final AnalysisJobBuilder ajb, final TransformerDescriptor<?> descriptor) {
        final String dsName = ajb.getDatastore().getName();
        final File saveDatastoreDirectory = userPreferences.getSaveDatastoreDirectory();
        final String displayName = descriptor.getDisplayName();
        file = new FileResource(new File(saveDatastoreDirectory, dsName + "-" + displayName + ".csv"));
    }

    @Override
    public OutputWriter createOutputWriter() {
        final List<String> headers = new ArrayList<>();
        for (int i = 0; i < columns.length; i++) {
            final String columnName = getColumnHeader(i);
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
                final InputColumn<?>[] newColumns = new InputColumn<?>[columns.length + 1];
                for (int i = 0; i < columns.length; i++) {
                    newColumns[i] = columns[i];
                }
                newColumns[columns.length] = columnToBeSortedOn;
                columns = newColumns;
            }
        }

        if (_targetResource == null) {
            try {
                initTempFile();
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }

        return CsvOutputWriterFactory
                .getWriter(_targetResource, headers.toArray(new String[0]), getSafeEncoding(), separatorChar,
                        getSafeQuoteChar(), getSafeEscapeChar(), includeHeader, columns);
    }

    private String getSafeEncoding() {
        if (Strings.isNullOrEmpty(encoding)) {
            return FileHelper.DEFAULT_ENCODING;
        }
        return encoding;
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

    private String getColumnHeader(final int index) {
        if (fields == null) {
            return columns[index].getName();
        }
        return fields[index];
    }

    @Override
    protected WriteDataResult getResultInternal(final int rowCount) {
        final CsvConfiguration csvConfiguration =
                new CsvConfiguration(CsvConfiguration.DEFAULT_COLUMN_NAME_LINE, getSafeEncoding(), separatorChar,
                        getSafeQuoteChar(), getSafeEscapeChar(), false, true);

        if (columnToBeSortedOn != null) {

            final CsvDataContext tempDataContext = new CsvDataContext(_targetResource, csvConfiguration);
            final Table table = tempDataContext.getDefaultSchema().getTable(0);

            final Comparator<? super Row> comparator =
                    SortHelper.createComparator(columnToBeSortedOn, _indexOfColumnToBeSortedOn);

            final CsvWriter csvWriter = new CsvWriter(csvConfiguration);
            final SortMergeWriter<Row, Writer> sortMergeWriter = new SortMergeWriter<Row, Writer>(comparator) {

                @Override
                protected void writeHeader(final Writer writer) throws IOException {
                    final List<String> headers = new ArrayList<>(table.getColumnNames());
                    if (!_isColumnToBeSortedOnPresentInInput) {
                        headers.remove(columnToBeSortedOn.getName());
                    }

                    final String[] columnNames = headers.toArray(new String[0]);
                    final String line = csvWriter.buildLine(columnNames);
                    writer.write(line);
                }

                @Override
                protected void writeRow(final Writer writer, final Row row, final int count) throws IOException {
                    for (int i = 0; i < count; i++) {
                        final List<Object> valuesList = new ArrayList<>(Arrays.asList(row.getValues()));
                        if (!_isColumnToBeSortedOnPresentInInput) {
                            valuesList.remove(_indexOfColumnToBeSortedOn);
                        }

                        final Object[] values = valuesList.toArray(new Object[0]);

                        final String[] stringValues = new String[values.length];
                        for (int j = 0; j < stringValues.length; j++) {
                            final Object obj = values[j];
                            if (obj != null) {
                                stringValues[j] = obj.toString();
                            }
                        }
                        final String line = csvWriter.buildLine(stringValues);
                        writer.write(line);
                    }
                }

                @Override
                protected Writer createWriter(final Resource resource) {
                    final OutputStream outputStream = resource.write();
                    return FileHelper.getWriter(outputStream, getSafeEncoding());
                }
            };

            // read from the temp file and sort it into the final file
            try (DataSet dataSet = tempDataContext.query().from(table).selectAll().execute()) {
                while (dataSet.next()) {
                    final Row row = dataSet.getRow();
                    sortMergeWriter.append(row);
                }
            }

            sortMergeWriter.write(file);
        }
        
        final Datastore datastore = new CsvDatastore(file.getName(), file, csvConfiguration);
        return new WriteDataResultImpl(rowCount, datastore, null, null);
    }

    public void setFile(final File file) {
        this.file = new FileResource(file);
    }

    public void setFile(final Resource resource) {
        this.file = resource;
    }

    @Override
    public boolean isDistributable() {
        return columnToBeSortedOn == null;
    }

    @Close
    public void close() {
        // delete the temporary file
        if (tempFile != null) {
            tempFile.delete();
        }
    }
}
