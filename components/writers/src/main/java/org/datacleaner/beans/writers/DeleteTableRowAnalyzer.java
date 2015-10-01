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
package org.datacleaner.beans.writers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.metamodel.BatchUpdateScript;
import org.apache.metamodel.UpdateCallback;
import org.apache.metamodel.UpdateScript;
import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.create.TableCreationBuilder;
import org.apache.metamodel.csv.CsvDataContext;
import org.apache.metamodel.delete.RowDeletionBuilder;
import org.apache.metamodel.insert.RowInsertionBuilder;
import org.apache.metamodel.query.FilterItem;
import org.apache.metamodel.query.OperatorType;
import org.apache.metamodel.query.SelectItem;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.Action;
import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.Resource;
import org.datacleaner.api.Analyzer;
import org.datacleaner.api.Categorized;
import org.datacleaner.api.ColumnProperty;
import org.datacleaner.api.ComponentContext;
import org.datacleaner.api.Concurrent;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.ExecutionLogMessage;
import org.datacleaner.api.FileProperty;
import org.datacleaner.api.FileProperty.FileAccessMode;
import org.datacleaner.api.HasLabelAdvice;
import org.datacleaner.api.Initialize;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.MappedProperty;
import org.datacleaner.api.Provided;
import org.datacleaner.api.SchemaProperty;
import org.datacleaner.api.TableProperty;
import org.datacleaner.api.Validate;
import org.datacleaner.components.categories.WriteSuperCategory;
import org.datacleaner.components.convert.ConvertToBooleanTransformer;
import org.datacleaner.components.convert.ConvertToNumberTransformer;
import org.datacleaner.connection.CsvDatastore;
import org.datacleaner.connection.FileDatastore;
import org.datacleaner.connection.SchemaNavigator;
import org.datacleaner.connection.UpdateableDatastore;
import org.datacleaner.connection.UpdateableDatastoreConnection;
import org.datacleaner.data.MetaModelInputColumn;
import org.datacleaner.descriptors.FilterDescriptor;
import org.datacleaner.descriptors.TransformerDescriptor;
import org.datacleaner.desktop.api.PrecedingComponentConsumer;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.util.WriteBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named("Delete from table")
@Description("Delete records in a table in a registered datastore that match the specified condition.")
@Categorized(superCategory = WriteSuperCategory.class)
@Concurrent(true)
public class DeleteTableRowAnalyzer implements Analyzer<WriteDataResult>, Action<Iterable<Object[]>>, HasLabelAdvice,
        PrecedingComponentConsumer {

    private static final String PROPERTY_NAME_CONDITION_VALUES = "Condition values";

    private static final File TEMP_DIR = FileHelper.getTempDir();

    private static final String ERROR_MESSAGE_COLUMN_NAME = "update_table_error_message";

    private static final Logger logger = LoggerFactory.getLogger(DeleteTableRowAnalyzer.class);

    @Inject
    @Configured(value = PROPERTY_NAME_CONDITION_VALUES, order = 1)
    @Description("Values that make up the condition for the rows to delete")
    InputColumn<?>[] conditionValues;

    @Inject
    @Configured(order = 2)
    @Description("Names of columns in the target table, which form the conditions of the delete.")
    @ColumnProperty
    @MappedProperty(PROPERTY_NAME_CONDITION_VALUES)
    String[] conditionColumnNames;

    @Inject
    @Configured(order = 3)
    @Description("Datastore to delete from")
    UpdateableDatastore datastore;

    @Inject
    @Configured(order = 4, required = false)
    @Description("Schema name of target table")
    @SchemaProperty
    String schemaName;

    @Inject
    @Configured(order = 5, required = false)
    @Description("Table to target (delete from)")
    @TableProperty
    String tableName;

    @Inject
    @Configured(order = 6, value = "Buffer size")
    @Description("How much data to buffer before committing batches of data. Large batches often perform better, but require more memory.")
    WriteBufferSizeOption bufferSizeOption = WriteBufferSizeOption.MEDIUM;

    @Inject
    @Configured(value = "How to handle deletion errors?", order = 7)
    ErrorHandlingOption errorHandlingOption = ErrorHandlingOption.STOP_JOB;

    @Inject
    @Configured(value = "Error log file location", required = false, order = 8)
    @Description("Directory or file path for saving erroneous records")
    @FileProperty(accessMode = FileAccessMode.SAVE, extension = ".csv")
    File errorLogFile = TEMP_DIR;

    @Inject
    @Configured(required = false, order = 9)
    @Description("Additional values to write to error log")
    InputColumn<?>[] additionalErrorLogValues;

    @Inject
    @Provided
    ComponentContext _componentContext;

    private Column[] _targetConditionColumns;
    private WriteBuffer _writeBuffer;
    private AtomicInteger _updatedRowCount;
    private AtomicInteger _errorRowCount;
    private CsvDataContext _errorDataContext;

    @Validate
    public void validate() {
        if (conditionValues.length != conditionColumnNames.length) {
            throw new IllegalStateException("Condition values and condition column names should have equal length");
        }
    }

    @Initialize
    public void init() throws IllegalArgumentException {
        _errorRowCount = new AtomicInteger();
        _updatedRowCount = new AtomicInteger();
        if (errorHandlingOption == ErrorHandlingOption.SAVE_TO_FILE) {
            _errorDataContext = createErrorDataContext();
        }

        int bufferSize = bufferSizeOption.calculateBufferSize(0); //TODO what buffer size? needed?
        logger.info("Row buffer size set to {}", bufferSize);

        _writeBuffer = new WriteBuffer(bufferSize, this);

        final UpdateableDatastoreConnection con = datastore.openConnection();
        try {
            final SchemaNavigator schemaNavigator = con.getSchemaNavigator();

            final List<String> columnsNotFound = new ArrayList<String>();

            _targetConditionColumns = schemaNavigator.convertToColumns(schemaName, tableName, conditionColumnNames);
            for (int i = 0; i < _targetConditionColumns.length; i++) {
                if (_targetConditionColumns[i] == null) {
                    columnsNotFound.add(conditionColumnNames[i]);
                }
            }

            if (!columnsNotFound.isEmpty()) {
                throw new IllegalArgumentException("Could not find column(s): " + columnsNotFound);
            }
        } finally {
            con.close();
        }
    }

    @Override
    public String getSuggestedLabel() {
        if (datastore == null || tableName == null) {
            return null;
        }
        return datastore.getName() + " - " + tableName;
    }

    private void validateCsvHeaders(CsvDataContext dc) {
        Schema schema = dc.getDefaultSchema();
        if (schema.getTableCount() == 0) {
            // nothing to worry about, we will create the table ourselves
            return;
        }
        Table table = schema.getTables()[0];

        // verify that table names correspond to what we need!

        for (String columnName : conditionColumnNames) {
            Column column = table.getColumnByName(columnName);
            if (column == null) {
                throw new IllegalStateException("Error log file does not have required column header: " + columnName);
            }
        }
        Column column = table.getColumnByName(ERROR_MESSAGE_COLUMN_NAME);
        if (column == null) {
            throw new IllegalStateException("Error log file does not have required column: "
                    + ERROR_MESSAGE_COLUMN_NAME);
        }
    }

    private CsvDataContext createErrorDataContext() {
        final File file;

        if (errorLogFile == null || TEMP_DIR.equals(errorLogFile)) {
            try {
                file = File.createTempFile("updation_error", ".csv");
            } catch (IOException e) {
                throw new IllegalStateException("Could not create new temp file", e);
            }
        } else if (errorLogFile.isDirectory()) {
            file = new File(errorLogFile, "updation_error_log.csv");
        } else {
            file = errorLogFile;
        }

        final CsvDataContext dc = new CsvDataContext(file);

        final Schema schema = dc.getDefaultSchema();

        if (file.exists() && file.length() > 0) {
            validateCsvHeaders(dc);
        } else {
            // create table if no table exists.
            dc.executeUpdate(new UpdateScript() {
                @Override
                public void run(UpdateCallback cb) {
                    TableCreationBuilder tableBuilder = cb.createTable(schema, "error_table");
                    for (String columnName : conditionColumnNames) {
                        tableBuilder = tableBuilder.withColumn(columnName);
                    }

                    tableBuilder = tableBuilder.withColumn(ERROR_MESSAGE_COLUMN_NAME);

                    tableBuilder.execute();
                }
            });
        }

        return dc;
    }

    @Override
    public void run(InputRow row, int distinctCount) {

        final Object[] rowData;
        if (additionalErrorLogValues == null) {
            rowData = new Object[conditionColumnNames.length];
        } else {
            rowData = new Object[conditionColumnNames.length + additionalErrorLogValues.length];
        }
        for (int i = 0; i < conditionValues.length; i++) {
            rowData[i] = row.getValue(conditionValues[i]);
        }

        if (additionalErrorLogValues != null) {
            for (int i = 0; i < additionalErrorLogValues.length; i++) {
                Object value = row.getValue(additionalErrorLogValues[i]);
                rowData[conditionColumnNames.length + i] = value;
            }
        }

        try {
            // perform conversion in a separate loop, since it might crash and
            // the
            // error data will be more complete if first loop finished.
            for (int i = 0; i < conditionValues.length; i++) {
                int index = i;
                rowData[index] = convertType(rowData[index], _targetConditionColumns[i]);

                if (logger.isDebugEnabled()) {
                    logger.debug("Value for {} set to: {}", conditionColumnNames[i], rowData[index]);
                }
            }
        } catch (RuntimeException e) {
            for (int i = 0; i < distinctCount; i++) {
                errorOccurred(rowData, e);
            }
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Adding row data to buffer: {}", Arrays.toString(rowData));
        }

        for (int i = 0; i < distinctCount; i++) {
            _writeBuffer.addToBuffer(rowData);
        }
    }

    private Object convertType(final Object value, Column targetColumn) throws IllegalArgumentException {
        if (value == null) {
            return null;
        }
        Object result = value;
        ColumnType type = targetColumn.getType();
        if (type.isLiteral()) {
            // for strings, only convert some simple cases, since JDBC drivers
            // typically also do a decent job here (with eg. Clob types, char[]
            // types etc.)
            if (value instanceof Number || value instanceof Date) {
                result = value.toString();
            }
        } else if (type.isNumber()) {
            Number numberValue = ConvertToNumberTransformer.transformValue(value);
            if (numberValue == null && !"".equals(value)) {
                throw new IllegalArgumentException("Could not convert " + value + " to number");
            }
            result = numberValue;
        } else if (type == ColumnType.BOOLEAN) {
            Boolean booleanValue = ConvertToBooleanTransformer.transformValue(value);
            if (booleanValue == null && !"".equals(value)) {
                throw new IllegalArgumentException("Could not convert " + value + " to boolean");
            }
            result = booleanValue;
        }
        return result;
    }

    @Override
    public WriteDataResult getResult() {
        _writeBuffer.flushBuffer();

        final int updatedRowCount = _updatedRowCount.get();

        final FileDatastore errorDatastore;
        if (_errorDataContext != null) {
            Resource resource = _errorDataContext.getResource();
            errorDatastore = new CsvDatastore(resource.getName(), resource);
        } else {
            errorDatastore = null;
        }

        return new WriteDataResultImpl(0, updatedRowCount, datastore, schemaName, tableName, _errorRowCount.get(),
                errorDatastore);
    }

    /**
     * Method invoked when flushing the buffer
     */
    @Override
    public void run(final Iterable<Object[]> buffer) throws Exception {

        UpdateableDatastoreConnection con = datastore.openConnection();
        try {
            final Column[] whereColumns = con.getSchemaNavigator().convertToColumns(schemaName, tableName,
                    conditionColumnNames);
            final UpdateableDataContext dc = con.getUpdateableDataContext();
            dc.executeUpdate(new BatchUpdateScript() {
                @Override
                public void run(UpdateCallback callback) {
                    int deleteCount = 0;
                    for (Object[] rowData : buffer) {
                        RowDeletionBuilder deletionBuilder = callback.deleteFrom(tableName);

                        for (int i = 0; i < whereColumns.length; i++) {
                            final Object value = rowData[i];
                            final Column whereColumn = whereColumns[i];
                            final FilterItem filterItem = new FilterItem(new SelectItem(whereColumn),
                                    OperatorType.EQUALS_TO, value);

                            deletionBuilder = deletionBuilder.where(filterItem);
                        }

                        if (logger.isDebugEnabled()) {
                            logger.debug("Updating: {}", Arrays.toString(rowData));
                        }

                        try {
                            deletionBuilder.execute();
                            deleteCount++;
                            _updatedRowCount.incrementAndGet();
                        } catch (final RuntimeException e) {
                            errorOccurred(rowData, e);
                        }
                    }

                    if (deleteCount > 0) {
                        _componentContext.publishMessage(new ExecutionLogMessage(deleteCount + " deletes executed"));
                    }
                }
            });
        } finally {
            con.close();
        }
    }

    protected void errorOccurred(final Object[] rowData, final RuntimeException e) {
        _errorRowCount.incrementAndGet();
        if (errorHandlingOption == ErrorHandlingOption.STOP_JOB) {
            throw e;
        } else {
            logger.warn("Error occurred while deleting record. Writing to error stream", e);
            _errorDataContext.executeUpdate(new UpdateScript() {
                @Override
                public void run(UpdateCallback cb) {
                    RowInsertionBuilder insertBuilder = cb
                            .insertInto(_errorDataContext.getDefaultSchema().getTables()[0]);

                    insertBuilder = insertBuilder.value(ERROR_MESSAGE_COLUMN_NAME, e.getMessage());
                    insertBuilder.execute();
                }
            });
        }
    }

    @Override
    public void configureForTransformedData(AnalysisJobBuilder analysisJobBuilder, TransformerDescriptor<?> descriptor) {
        final List<Table> tables = analysisJobBuilder.getSourceTables();
        if (tables.size() == 1) {
            final List<MetaModelInputColumn> sourceColumns = analysisJobBuilder.getSourceColumnsOfTable(tables.get(0));
            final List<InputColumn<?>> primaryKeys = new ArrayList<InputColumn<?>>();
            for (MetaModelInputColumn inputColumn : sourceColumns) {
                if (inputColumn.getPhysicalColumn().isPrimaryKey()) {
                    primaryKeys.add(inputColumn);
                }
            }

            if (!primaryKeys.isEmpty()) {
                conditionValues = primaryKeys.toArray(new InputColumn[primaryKeys.size()]);
            }
        }
    }

    @Override
    public void configureForFilterOutcome(AnalysisJobBuilder analysisJobBuilder, FilterDescriptor<?, ?> descriptor,
            String categoryName) {
    }
}
