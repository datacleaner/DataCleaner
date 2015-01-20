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
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.Distributed;
import org.datacleaner.api.FileProperty;
import org.datacleaner.api.FileProperty.FileAccessMode;
import org.datacleaner.api.HasLabelAdvice;
import org.datacleaner.api.Initialize;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.Provided;
import org.datacleaner.beans.writers.WriteDataResult;
import org.datacleaner.beans.writers.WriteDataResultImpl;
import org.datacleaner.components.categories.WriteDataCategory;
import org.datacleaner.connection.CsvDatastore;
import org.datacleaner.connection.Datastore;
import org.datacleaner.descriptors.FilterBeanDescriptor;
import org.datacleaner.descriptors.TransformerBeanDescriptor;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.output.OutputWriter;
import org.datacleaner.output.csv.CsvOutputWriterFactory;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.CompareUtils;
import org.datacleaner.util.sort.SortMergeWriter;

@Named("Create CSV file")
@Alias("Write to CSV file")
@Description("Write data to a CSV file on your harddrive. CSV file writing is extremely fast and the file format is commonly used in many tools. But CSV files do not preserve data types.")
@Categorized(WriteDataCategory.class)
@Distributed(false)
public class CreateCsvFileAnalyzer extends AbstractOutputWriterAnalyzer implements HasLabelAdvice {

    @Configured(order = 1)
    @FileProperty(accessMode = FileAccessMode.SAVE, extension = { "csv", "tsv", "txt", "dat" })
    File file;

    @Configured(order = 2, required = false)
    Character separatorChar = ',';

    @Configured(order = 3, required = false)
    Character quoteChar = '"';

    @Configured(order = 4, required = false)
    Character escapeChar = '\\';

    @Configured(order = 5, required = false)
    boolean includeHeader = true;

    @Configured(order = 6, required = false)
    InputColumn<?> columnToBeSortedOn ;

    @Inject
    @Provided
    UserPreferences userPreferences;

    private File _targetFile;
    
    private int indexOfColumnToBeSortedOn = -1 ;

    private boolean isColumnToBeSortedOnPresentInInput = true ; 


    @Initialize
    public void initTempFile() throws Exception {
        if(columnToBeSortedOn != null) {
            _targetFile = File.createTempFile("csv_file_analyzer", ".csv");
        } else {
            _targetFile = file ;
        }
    }

    @Override
    public void configureForFilterOutcome(AnalysisJobBuilder ajb, FilterBeanDescriptor<?, ?> descriptor, String categoryName) {
        final String dsName = ajb.getDatastore().getName();
        final File saveDatastoreDirectory = userPreferences.getSaveDatastoreDirectory();
        final String displayName = descriptor.getDisplayName();
        file = new File(saveDatastoreDirectory, "output-" + dsName + "-" + displayName + "-" + categoryName + ".csv");
    }

    @Override
    public void configureForTransformedData(AnalysisJobBuilder ajb, TransformerBeanDescriptor<?> descriptor) {
        final String dsName = ajb.getDatastore().getName();
        final File saveDatastoreDirectory = userPreferences.getSaveDatastoreDirectory();
        final String displayName = descriptor.getDisplayName();
        file = new File(saveDatastoreDirectory, "output-" + dsName + "-" + displayName + ".csv");
    }

    @Override
    public String getSuggestedLabel() {
        if (file == null) {
            return null;
        }
        return file.getName();
    }

    @Override
    public OutputWriter createOutputWriter() {
        List<String> headers = new ArrayList<String>() ;
        for (int i = 0; i < columns.length; i++) {
            String columnName = columns[i].getName();
            headers.add(columnName);
            if(columnToBeSortedOn != null) {
                if(columnName.equals(columnToBeSortedOn.getName())){
                    indexOfColumnToBeSortedOn = i ;
                }
            }
        }

        if(columnToBeSortedOn != null) {
            if(indexOfColumnToBeSortedOn == -1) {
                this.isColumnToBeSortedOnPresentInInput  = false ;
                indexOfColumnToBeSortedOn = columns.length ;
                headers.add(columnToBeSortedOn.getName()) ;
                InputColumn<?>[] newColumns = new InputColumn<?>[columns.length + 1] ;
                for(int i = 0; i < columns.length; i++){
                    newColumns[i] = columns[i] ;
                }
                newColumns[columns.length]  = columnToBeSortedOn ;
                columns = newColumns ;
            }
        }
       
        
        return CsvOutputWriterFactory.getWriter(_targetFile.getPath(), headers.toArray(new String[0]), separatorChar, quoteChar, escapeChar, includeHeader, columns);
    }

    @Override
    protected WriteDataResult getResultInternal(int rowCount) {
        final CsvConfiguration csvConfiguration = new CsvConfiguration(CsvConfiguration.DEFAULT_COLUMN_NAME_LINE, FileHelper.DEFAULT_ENCODING,
                separatorChar, quoteChar, escapeChar, false, true);

        if (columnToBeSortedOn != null) {
            
            final CsvDataContext tempDataContext = new CsvDataContext(_targetFile, csvConfiguration);
            final Table table = tempDataContext.getDefaultSchema().getTable(0);

            final Comparator<? super Row> comparator = new Comparator<Row>() {
                @SuppressWarnings("unchecked")
                @Override 
                public int compare(Row row1, Row row2) {
                    Comparable<Object> value1 = (Comparable<Object>) row1.getValue(indexOfColumnToBeSortedOn);
                    Comparable<Object> value2 = (Comparable<Object>) row2.getValue(indexOfColumnToBeSortedOn);
                    int comparableResult = CompareUtils.compare(value1, value2) ;
                    if(comparableResult != 0) {
                        return comparableResult ;
                    } else {
                        // The values of the data at the row, and column to be sorted on are 
                        // exactly the same. Now look at other values of all the columns to 
                        // find if the two rows are same.
                        int numberOfSelectItems = row1.getSelectItems().length ;
                        for(int i = 0; i < numberOfSelectItems; i++) {
                            Comparable<Object> rowValue1 = (Comparable<Object>) row1.getValue(i);
                            Comparable<Object> rowValue2 = (Comparable<Object>) row2.getValue(i);
                            if(CompareUtils.compare(rowValue1, rowValue2) == 0) {
                                continue;
                            } else {
                                return CompareUtils.compare(rowValue1, rowValue2) ;
                            }
                        }
                    }
                
                    return comparableResult;
                }
            };

            final CsvWriter csvWriter = new CsvWriter(csvConfiguration);
            final SortMergeWriter<Row, Writer> sortMergeWriter = new SortMergeWriter<Row, Writer>(comparator) {

                @Override
                protected void writeHeader(Writer writer) throws IOException {
                    List<String> headers = new ArrayList<String>(Arrays.asList(table.getColumnNames()));
                    if(!isColumnToBeSortedOnPresentInInput) {
                        headers.remove(columnToBeSortedOn.getName()) ;
                    }
                        
                    final String[] columnNames = headers.toArray(new String[0]);
                    final String line = csvWriter.buildLine(columnNames);
                    writer.write(line);
                    writer.append('\n');
                }
    
                @Override
                protected void writeRow(Writer writer, Row row, int count) throws IOException {
                    for(int i = 0; i < count; i++) {
                        List<Object> valuesList = new ArrayList<Object>(Arrays.asList(row.getValues())) ;
                        if(!isColumnToBeSortedOnPresentInInput) {
                            valuesList.remove(indexOfColumnToBeSortedOn) ;
                        }
                            
                        final Object[] values = valuesList.toArray(new Object[0]) ;
    
                        final String[] stringValues = new String[values.length];
                        for (int j = 0; j < stringValues.length; j++) {
                            final Object obj = values[j];
                            if (obj != null) {
                                stringValues[j] = obj.toString();
                            }
                        }
                        final String line = csvWriter.buildLine(stringValues);
                        writer.write(line);
                        writer.append('\n');
                    }
                }
    
                @Override
                protected Writer createWriter(File file) {
                    return FileHelper.getWriter(file, FileHelper.DEFAULT_ENCODING);
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
        
        final Resource resource = new FileResource(file);
        final Datastore datastore = new CsvDatastore(file.getName(), resource, csvConfiguration);
        return new WriteDataResultImpl(rowCount, datastore, null, null);
    }

    public void setFile(File file) {
        this.file = file;
    }
}