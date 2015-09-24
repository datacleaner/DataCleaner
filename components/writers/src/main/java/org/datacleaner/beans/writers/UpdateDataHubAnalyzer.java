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

import org.apache.commons.lang.ArrayUtils;
import org.apache.metamodel.BatchUpdateScript;
import org.apache.metamodel.UpdateCallback;
import org.apache.metamodel.UpdateScript;
import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.create.TableCreationBuilder;
import org.apache.metamodel.csv.CsvDataContext;
import org.apache.metamodel.insert.RowInsertionBuilder;
import org.apache.metamodel.query.FilterItem;
import org.apache.metamodel.query.OperatorType;
import org.apache.metamodel.query.SelectItem;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.update.RowUpdationBuilder;
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

@Named("Update DataHub")
@Description("Update records in a table in a registered datastore. This component allows you to map the values available in the flow with the columns of the target table, in order to update the values of these columns in the datastore."
        + "\nTo understand the configuration of the Update table component, consider a typical SQL update statement:"
        + "\n<blockquote>UPDATE table SET name = 'John Doe' WHERE id = 42</blockquote>"
        + "\nHere we see that there is a condition (WHERE id=42) and a value to update (name should become 'John Doe'). This is what the two inputs are referring to. But obviously you are not dealing with constant values like 'John Doe' or '42'. You have a field in your DC job that you want to map to fields in your database."
        + "\nUsually the 'condition value' would be a mapping of the key that you have in your job towards the key that is in the database. The 'values to update' property would include the columns that you wish to update based on the values you have in your job.")
@Categorized(superCategory = WriteSuperCategory.class)
@Concurrent(true)
public class UpdateDataHubAnalyzer implements Analyzer<WriteDataResult>, Action<Iterable<Object[]>>, HasLabelAdvice,
        PrecedingComponentConsumer {

    private static final String PROPERTY_NAME_VALUES = "Values";
    private static final String PROPERTY_NAME_CONDITION_VALUES = "Condition values";

    private static final File TEMP_DIR = FileHelper.getTempDir();

    private static final String ERROR_MESSAGE_COLUMN_NAME = "update_table_error_message";

    private static final Logger logger = LoggerFactory.getLogger(UpdateDataHubAnalyzer.class);

    @Inject
    @Configured(value = PROPERTY_NAME_VALUES, order = 1)
    @Description("Values to update in the table")
    InputColumn<?>[] values;

    @Inject
    @Configured(order = 2)
    @Description("Names of columns in the target table, on which the values will be updated.")
    @ColumnProperty
    @MappedProperty(PROPERTY_NAME_VALUES)
    String[] columnNames;

    @Inject
    @Configured(value = PROPERTY_NAME_CONDITION_VALUES, order = 3)
    @Description("Values that make up the condition of the table update")
    InputColumn<?>[] conditionValues;

    @Inject
    @Configured(order = 4)
    @Description("Names of columns in the target table, which form the conditions of the update.")
    @ColumnProperty
    @MappedProperty(PROPERTY_NAME_CONDITION_VALUES)
    String[] conditionColumnNames;

    @Inject
    @Configured(order = 5)
    @Description("Datastore to write to")
    UpdateableDatastore datastore;

    @Inject
    @Configured(order = 6, required = false)
    @Description("Schema name of target table")
    @SchemaProperty
    String schemaName;

    @Inject
    @Configured(order = 7, required = false)
    @Description("Table to target (update)")
    @TableProperty
    String tableName;

    @Inject
    @Configured(order = 8, value = "Buffer size")
    @Description("How much data to buffer before committing batches of data. Large batches often perform better, but require more memory.")
    WriteBufferSizeOption bufferSizeOption = WriteBufferSizeOption.MEDIUM;

    @Inject
    @Configured(value = "How to handle updation errors?", order = 9)
    ErrorHandlingOption errorHandlingOption = ErrorHandlingOption.STOP_JOB;

    @Inject
    @Configured(value = "Error log file location", required = false, order = 10)
    @Description("Directory or file path for saving erroneous records")
    @FileProperty(accessMode = FileAccessMode.SAVE, extension = ".csv")
    File errorLogFile = TEMP_DIR;

    @Inject
    @Configured(required = false, order = 11)
    @Description("Additional values to write to error log")
    InputColumn<?>[] additionalErrorLogValues;

    @Inject
    @Provided
    ComponentContext _componentContext;

    private Column[] _targetColumns;
    private Column[] _targetConditionColumns;
    private WriteBuffer _writeBuffer;
    private AtomicInteger _updatedRowCount;
    private AtomicInteger _errorRowCount;
    private CsvDataContext _errorDataContext;

    @Validate
    public void validate() {
        if (values.length != columnNames.length) {
            throw new IllegalStateException("Values and column names should have equal length");
        }

        if (conditionValues.length != conditionColumnNames.length) {
            throw new IllegalStateException("Condition values and condition column names should have equal length");
        }
    }

    @Initialize
    public void init() throws IllegalArgumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("At init() time, InputColumns are: {}", Arrays.toString(values));
        }

        _errorRowCount = new AtomicInteger();
        _updatedRowCount = new AtomicInteger();
        if (errorHandlingOption == ErrorHandlingOption.SAVE_TO_FILE) {
            _errorDataContext = createErrorDataContext();
        }

        int bufferSize = bufferSizeOption.calculateBufferSize(values.length);
        logger.info("Row buffer size set to {}", bufferSize);

        _writeBuffer = new WriteBuffer(bufferSize, this);

        final UpdateableDatastoreConnection con = datastore.openConnection();
        try {
            final SchemaNavigator schemaNavigator = con.getSchemaNavigator();

            final List<String> columnsNotFound = new ArrayList<String>();

            _targetColumns = schemaNavigator.convertToColumns(schemaName, tableName, columnNames);
            for (int i = 0; i < _targetColumns.length; i++) {
                if (_targetColumns[i] == null) {
                    columnsNotFound.add(columnNames[i]);
                }
            }

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

    @Override
    public void run(InputRow row, int distinctCount) {
        if (logger.isDebugEnabled()) {
            logger.debug("At run() time, InputColumns are: {}", Arrays.toString(values));
        }

        final Object[] rowData;
        List<InputColumn<?>> inputColumns = row.getInputColumns();
        List<Object> inputValues = row.getValues();
        int rowSize = inputValues.size();
        if (additionalErrorLogValues == null) {
            rowData = new Object[values.length + conditionColumnNames.length];
        } else {
            rowData = new Object[values.length + conditionColumnNames.length + additionalErrorLogValues.length];
        }
        for (int i = 0; i < values.length; i++) {
            rowData[i] = row.getValue(values[i]);
        }
        for (int i = 0; i < conditionValues.length; i++) {
            rowData[i + values.length] = row.getValue(conditionValues[i]);
        }

        if (additionalErrorLogValues != null) {
            for (int i = 0; i < additionalErrorLogValues.length; i++) {
                Object value = row.getValue(additionalErrorLogValues[i]);
                rowData[values.length + +conditionColumnNames.length + i] = value;
            }
        }

        try {
            // perform conversion in a separate loop, since it might crash and
            // the
            // error data will be more complete if first loop finished.
            for (int i = 0; i < rowSize; i++) {
                rowData[i] = convertType(rowData[i], _targetColumns[i]);

                if (logger.isDebugEnabled()) {
                    logger.debug("Value for {} set to: {}", columnNames[i], rowData[i]);
                }
            }
            for (int i = 0; i < conditionValues.length; i++) {
                int index = i + values.length - 1;
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
            final Column[] updateColumns = con.getSchemaNavigator()
                    .convertToColumns(schemaName, tableName, columnNames);
            final Column[] whereColumns = con.getSchemaNavigator().convertToColumns(schemaName, tableName,
                    conditionColumnNames);

            if (logger.isDebugEnabled()) {
                logger.debug("Updating columns: {}", Arrays.toString(updateColumns));
            }

            final UpdateableDataContext dc = con.getUpdateableDataContext();
            dc.executeUpdate(new BatchUpdateScript() {
                @Override
                public void run(UpdateCallback callback) {
                    int updateCount = 0;
                    for (Object[] rowData : buffer) {
                        RowUpdationBuilder updationBuilder = callback.update(updateColumns[0].getTable());
                        for (int i = 0; i < updateColumns.length; i++) {
                            final Object value = rowData[i];
                            updationBuilder = updationBuilder.value(updateColumns[i], value);
                        }

                        for (int i = 0; i < whereColumns.length; i++) {
                            final Object value = rowData[i + updateColumns.length];
                            final Column whereColumn = whereColumns[i];
                            final FilterItem filterItem = new FilterItem(new SelectItem(whereColumn),
                                    OperatorType.EQUALS_TO, value);

                            updationBuilder = updationBuilder.where(filterItem);
                        }

                        if (logger.isDebugEnabled()) {
                            logger.debug("Updating: {}", Arrays.toString(rowData));
                        }

                        try {
                            updationBuilder.execute();
                            updateCount++;
                            _updatedRowCount.incrementAndGet();
                        } catch (final RuntimeException e) {
                            errorOccurred(rowData, e);
                        }
                    }

                    if (updateCount > 0) {
                        _componentContext.publishMessage(new ExecutionLogMessage(updateCount + " updates executed"));
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
            logger.warn("Error occurred while updating record. Writing to error stream", e);
            _errorDataContext.executeUpdate(new UpdateScript() {
                @Override
                public void run(UpdateCallback cb) {
                    RowInsertionBuilder insertBuilder = cb
                            .insertInto(_errorDataContext.getDefaultSchema().getTables()[0]);
                    for (int i = 0; i < columnNames.length; i++) {
                        insertBuilder = insertBuilder.value(columnNames[i], rowData[i]);
                    }

                    if (additionalErrorLogValues != null) {
                        for (int i = 0; i < additionalErrorLogValues.length; i++) {
                            String columnName = translateAdditionalErrorLogColumnName(additionalErrorLogValues[i]
                                    .getName());
                            Object value = rowData[columnNames.length + i];
                            insertBuilder = insertBuilder.value(columnName, value);
                        }
                    }

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
                    for (String columnName : columnNames) {
                        tableBuilder = tableBuilder.withColumn(columnName);
                    }
                    for (String columnName : conditionColumnNames) {
                        tableBuilder = tableBuilder.withColumn(columnName);
                    }

                    if (additionalErrorLogValues != null) {
                        for (InputColumn<?> inputColumn : additionalErrorLogValues) {
                            String columnName = translateAdditionalErrorLogColumnName(inputColumn.getName());
                            tableBuilder = tableBuilder.withColumn(columnName);
                        }
                    }

                    tableBuilder = tableBuilder.withColumn(ERROR_MESSAGE_COLUMN_NAME);

                    tableBuilder.execute();
                }
            });
        }

        return dc;
    }

    private void validateCsvHeaders(CsvDataContext dc) {
        Schema schema = dc.getDefaultSchema();
        if (schema.getTableCount() == 0) {
            // nothing to worry about, we will create the table ourselves
            return;
        }
        Table table = schema.getTables()[0];

        // verify that table names correspond to what we need!

        for (String columnName : columnNames) {
            Column column = table.getColumnByName(columnName);
            if (column == null) {
                throw new IllegalStateException("Error log file does not have required column header: " + columnName);
            }
        }
        for (String columnName : conditionColumnNames) {
            Column column = table.getColumnByName(columnName);
            if (column == null) {
                throw new IllegalStateException("Error log file does not have required column header: " + columnName);
            }
        }
        if (additionalErrorLogValues != null) {
            for (InputColumn<?> inputColumn : additionalErrorLogValues) {
                String columnName = translateAdditionalErrorLogColumnName(inputColumn.getName());
                Column column = table.getColumnByName(columnName);
                if (column == null) {
                    throw new IllegalStateException("Error log file does not have required column header: "
                            + columnName);
                }
            }
        }

        Column column = table.getColumnByName(ERROR_MESSAGE_COLUMN_NAME);
        if (column == null) {
            throw new IllegalStateException("Error log file does not have required column: "
                    + ERROR_MESSAGE_COLUMN_NAME);
        }
    }

    private String translateAdditionalErrorLogColumnName(String columnName) {
        if (ArrayUtils.contains(columnNames, columnName)) {
            return translateAdditionalErrorLogColumnName(columnName + "_add");
        }
        return columnName;
    }

}
