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
package org.datacleaner.beans.transform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.datacleaner.beans.api.Alias;
import org.datacleaner.beans.api.Close;
import org.datacleaner.beans.api.ColumnProperty;
import org.datacleaner.beans.api.Concurrent;
import org.datacleaner.beans.api.Configured;
import org.datacleaner.beans.api.Description;
import org.datacleaner.beans.api.Initialize;
import org.datacleaner.beans.api.MappedProperty;
import org.datacleaner.beans.api.OutputColumns;
import org.datacleaner.beans.api.OutputRowCollector;
import org.datacleaner.beans.api.Provided;
import org.datacleaner.beans.api.SchemaProperty;
import org.datacleaner.beans.api.TableProperty;
import org.datacleaner.beans.api.Transformer;
import org.datacleaner.beans.api.TransformerBean;
import org.datacleaner.beans.api.Validate;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.data.InputColumn;
import org.datacleaner.data.InputRow;
import org.datacleaner.util.CollectionUtils2;
import org.datacleaner.util.HasLabelAdvice;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.query.CompiledQuery;
import org.apache.metamodel.query.OperatorType;
import org.apache.metamodel.query.Query;
import org.apache.metamodel.query.QueryParameter;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.HasName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;

/**
 * A transformer that can do a lookup (like a left join) based on a set of
 * columns in any datastore.
 */
@TransformerBean("Table lookup")
@Alias("Datastore lookup")
@Description("Perform a lookup based on a table in any of your registered datastore (like a LEFT join).")
@Concurrent(true)
public class TableLookupTransformer implements Transformer<Object>, HasLabelAdvice {

    private static final Logger logger = LoggerFactory.getLogger(TableLookupTransformer.class);

    private static final String PROPERTY_NAME_DATASTORE = "Datastore";
    private static final String PROPERTY_NAME_SCHEMA_NAME = "Schema name";
    private static final String PROPERTY_NAME_TABLE_NAME = "Table name";

    public static enum JoinSemantic implements HasName {
        @Alias("LEFT")
        LEFT_JOIN_MAX_ONE("Left join (max 1 record)"),

        @Alias("INNER")
        INNER_JOIN("Inner join"),

        @Alias("INNER_MIN_ONE")
        LEFT_JOIN("Left join");

        private final String _name;

        private JoinSemantic(String name) {
            _name = name;
        }

        @Override
        public String getName() {
            return _name;
        }

        public boolean isCacheable() {
            // inner joined result sets are not cached since their size exceeds
            // the cache capabilities.
            return this == LEFT_JOIN_MAX_ONE;
        }
    }

    @Inject
    @Configured(value = PROPERTY_NAME_DATASTORE)
    Datastore datastore;

    @Inject
    @Configured(required = false)
    InputColumn<?>[] conditionValues;

    @Inject
    @Configured(required = false)
    @ColumnProperty
    @MappedProperty(PROPERTY_NAME_TABLE_NAME)
    String[] conditionColumns;

    @Inject
    @Configured
    @ColumnProperty
    @MappedProperty(PROPERTY_NAME_TABLE_NAME)
    String[] outputColumns;

    @Inject
    @Configured(value = PROPERTY_NAME_SCHEMA_NAME)
    @Alias("Schema")
    @SchemaProperty
    @MappedProperty(PROPERTY_NAME_DATASTORE)
    String schemaName;

    @Inject
    @Configured(value = PROPERTY_NAME_TABLE_NAME)
    @Alias("Table")
    @TableProperty
    @MappedProperty(PROPERTY_NAME_SCHEMA_NAME)
    String tableName;

    @Inject
    @Configured
    @Description("Use a client-side cache to avoid looking up multiple times with same inputs.")
    boolean cacheLookups = true;

    @Inject
    @Configured
    @Description("Which kind of semantic to apply to the lookup, compared to a SQL JOIN.")
    JoinSemantic joinSemantic = JoinSemantic.LEFT_JOIN_MAX_ONE;

    @Inject
    @Provided
    OutputRowCollector outputRowCollector;

    private final Cache<List<Object>, Object[]> cache = CollectionUtils2.<List<Object>, Object[]> createCache(10000,
            5 * 60);
    private Column[] queryOutputColumns;
    private Column[] queryConditionColumns;
    private DatastoreConnection datastoreConnection;
    private CompiledQuery lookupQuery;

    /**
     * Default constructor
     */
    public TableLookupTransformer() {
    }

    /**
     * Constructor for direct usage within e.g. other components where we always
     * expect to do LEFT JOIN (max one record) semantic lookups.
     * 
     * @param datastore
     * @param schemaName
     * @param tableName
     * @param conditionColumns
     * @param conditionValues
     * @param outputColumns
     * @param joinSemantic
     * @param cacheLookups
     */
    public TableLookupTransformer(Datastore datastore, String schemaName, String tableName, String[] conditionColumns,
            InputColumn<?>[] conditionValues, String[] outputColumns, boolean cacheLookups) {
        this.datastore = datastore;
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.conditionColumns = conditionColumns;
        this.conditionValues = conditionValues;
        this.cacheLookups = cacheLookups;
        this.outputColumns = outputColumns;
        this.joinSemantic = JoinSemantic.LEFT_JOIN_MAX_ONE;
    }
    
    @Override
    public String getSuggestedLabel() {
        if (tableName == null) {
            return null;
        }
        return "Lookup: " + tableName;
    }

    private void resetCachedColumns() {
        queryOutputColumns = null;
        queryConditionColumns = null;
    }

    private Column[] getQueryConditionColumns() {
        if (queryConditionColumns == null) {
            if (isCarthesianProductMode()) {
                queryConditionColumns = new Column[0];
            } else {
                try (final DatastoreConnection con = datastore.openConnection()) {
                    queryConditionColumns = con.getSchemaNavigator().convertToColumns(schemaName, tableName,
                            conditionColumns);
                }
            }
        }
        return queryConditionColumns;
    }

    /**
     * Gets the output columns of the lookup query
     * 
     * @param checkNames
     *            whether to check/validate/adjust the names of these columns
     * @return
     */
    private Column[] getQueryOutputColumns(boolean checkNames) {
        if (queryOutputColumns == null) {
            try (final DatastoreConnection con = datastore.openConnection()) {
                queryOutputColumns = con.getSchemaNavigator().convertToColumns(schemaName, tableName, outputColumns);
            }
        } else if (checkNames) {
            if (!isQueryOutputColumnsUpdated()) {
                queryOutputColumns = null;
                return getQueryOutputColumns(false);
            }
        }
        return queryOutputColumns;
    }

    /**
     * Checks the validity of the current (cached) output columns array.
     * 
     * @return true if the current columns are valid
     */
    private boolean isQueryOutputColumnsUpdated() {
        if (queryOutputColumns.length != outputColumns.length) {
            return false;
        }
        for (int i = 0; i < queryOutputColumns.length; i++) {
            Column outputColumn = queryOutputColumns[i];
            String expectedName = outputColumns[i];
            if (!expectedName.equals(outputColumn.getName())) {
                return false;
            }
            if (tableName != null && !tableName.equals(outputColumn.getTable().getName())) {
                return false;
            }
        }

        return true;
    }

    @Initialize
    public void init() {
        datastoreConnection = datastore.openConnection();
        resetCachedColumns();
        cache.invalidateAll();
        compileLookupQuery();
    }

    private void compileLookupQuery() {
        try {
            final Column[] queryOutputColumns = getQueryOutputColumns(false);
            final Column queryOutputColumn = queryOutputColumns[0];
            final Table table = queryOutputColumn.getTable();

            Query query = new Query().from(table).select(queryOutputColumns);

            if (!isCarthesianProductMode()) {
                final Column[] queryConditionColumns = getQueryConditionColumns();
                for (int i = 0; i < queryConditionColumns.length; i++) {
                    query = query.where(queryConditionColumns[i], OperatorType.EQUALS_TO, new QueryParameter());
                }
            }

            if (joinSemantic == JoinSemantic.LEFT_JOIN_MAX_ONE) {
                query = query.setMaxRows(1);
            }

            lookupQuery = datastoreConnection.getDataContext().compileQuery(query);

        } catch (RuntimeException e) {
            logger.error("Error occurred while compiling lookup query", e);
            throw e;
        }
    }

    private boolean isCarthesianProductMode() {
        return (conditionColumns == null || conditionColumns.length == 0)
                && (conditionValues == null || conditionValues.length == 0);
    }

    @Validate
    public void validate() {
        if (isCarthesianProductMode()) {
            // carthesian product mode
            return;
        }
        final Column[] queryConditionColumns = getQueryConditionColumns();
        final List<String> columnsNotFound = new ArrayList<String>();
        for (int i = 0; i < queryConditionColumns.length; i++) {
            if (queryConditionColumns[i] == null) {
                columnsNotFound.add(conditionColumns[i]);
            }
        }

        if (!columnsNotFound.isEmpty()) {
            throw new IllegalArgumentException("Could not find column(s): " + columnsNotFound);
        }
    }

    @Override
    public OutputColumns getOutputColumns() {
        Column[] queryOutputColumns = getQueryOutputColumns(true);
        String[] names = new String[queryOutputColumns.length];
        Class<?>[] types = new Class[queryOutputColumns.length];
        for (int i = 0; i < queryOutputColumns.length; i++) {
            Column column = queryOutputColumns[i];
            if (column == null) {
                throw new IllegalArgumentException("Could not find column: " + outputColumns[i]);
            }
            names[i] = column.getName() + " (lookup)";
            types[i] = column.getType().getJavaEquivalentClass();
        }
        return new OutputColumns(names, types);
    }

    @Override
    public Object[] transform(InputRow inputRow) {
        final List<Object> queryInput;

        if (isCarthesianProductMode()) {
            queryInput = Collections.emptyList();
        } else {
            queryInput = new ArrayList<Object>(conditionValues.length);
            for (InputColumn<?> inputColumn : conditionValues) {
                Object value = inputRow.getValue(inputColumn);
                queryInput.add(value);
            }
        }

        logger.info("Looking up based on condition values: {}", queryInput);

        Object[] result;
        if (cacheLookups && joinSemantic.isCacheable()) {
            result = cache.getIfPresent(queryInput);
            if (result == null) {
                result = performQuery(queryInput);
                cache.put(queryInput, result);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Returning cached lookup result: {}", Arrays.toString(result));
                }
            }
        } else {
            result = performQuery(queryInput);
        }

        return result;
    }

    private Object[] performQuery(List<Object> queryInput) {
        try {
            final Column[] queryConditionColumns = getQueryConditionColumns();

            final Object[] parameterValues = new Object[queryConditionColumns.length];
            for (int i = 0; i < queryConditionColumns.length; i++) {
                parameterValues[i] = queryInput.get(i);
            }

            try (final DataSet dataSet = datastoreConnection.getDataContext()
                    .executeQuery(lookupQuery, parameterValues)) {
                return handleDataSet(dataSet);
            }
        } catch (RuntimeException e) {
            logger.error("Error occurred while looking up based on conditions: " + queryInput, e);
            throw e;
        }
    }

    private Object[] handleDataSet(DataSet dataSet) {
        if (!dataSet.next()) {
            logger.info("Result of lookup: None!");
            switch (joinSemantic) {
            case LEFT_JOIN_MAX_ONE:
            case LEFT_JOIN:
                return new Object[outputColumns.length];
            default:
                return null;
            }
        }

        do {
            final Object[] result = dataSet.getRow().getValues();
            if (logger.isInfoEnabled()) {
                logger.info("Result of lookup: " + Arrays.toString(result));
            }
            switch (joinSemantic) {
            case LEFT_JOIN_MAX_ONE:
                return result;
            default:
                outputRowCollector.putValues(result);
            }

        } while (dataSet.next());

        return null;
    }

    @Close
    public void close() {
        if (lookupQuery != null) {
            lookupQuery.close();
            lookupQuery = null;
        }
        if (datastore != null) {
            datastoreConnection.close();
            datastoreConnection = null;
        }
        cache.invalidateAll();
        queryOutputColumns = null;
        queryConditionColumns = null;
    }
}
