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
package org.datacleaner.components.tablelookup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.query.CompiledQuery;
import org.apache.metamodel.query.OperatorType;
import org.apache.metamodel.query.Query;
import org.apache.metamodel.query.QueryParameter;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.HasName;
import org.datacleaner.api.Alias;
import org.datacleaner.api.Categorized;
import org.datacleaner.api.Close;
import org.datacleaner.api.ColumnProperty;
import org.datacleaner.api.Concurrent;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.HasAnalyzerResult;
import org.datacleaner.api.HasLabelAdvice;
import org.datacleaner.api.Initialize;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.MappedProperty;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.OutputRowCollector;
import org.datacleaner.api.Provided;
import org.datacleaner.api.SchemaProperty;
import org.datacleaner.api.TableProperty;
import org.datacleaner.api.Transformer;
import org.datacleaner.api.Validate;
import org.datacleaner.components.categories.ImproveSuperCategory;
import org.datacleaner.components.categories.ReferenceDataCategory;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.result.CategorizationResult;
import org.datacleaner.storage.DummyRowAnnotationFactory;
import org.datacleaner.storage.RowAnnotation;
import org.datacleaner.storage.RowAnnotationFactory;
import org.datacleaner.util.CollectionUtils2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;

/**
 * A transformer that can do a lookup (like a left join) based on a set of
 * columns in any datastore.
 */
@Named("Table lookup")
@Alias("Datastore lookup")
@Description("Perform a lookup based on a table in any of your registered datastore (like a LEFT join).")
@Concurrent(true)
@Categorized(superCategory = ImproveSuperCategory.class, value = ReferenceDataCategory.class)
public class TableLookupTransformer implements Transformer, HasLabelAdvice, HasAnalyzerResult<CategorizationResult> {

    public enum JoinSemantic implements HasName {
        @Alias("LEFT")
        LEFT_JOIN_MAX_ONE("Left join (max 1 record)"),

        @Alias("INNER")
        INNER_JOIN("Inner join"),

        @Alias("INNER_MIN_ONE")
        LEFT_JOIN("Left join");

        private final String _name;

        JoinSemantic(final String name) {
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

    private static final Logger logger = LoggerFactory.getLogger(TableLookupTransformer.class);
    private static final String PROPERTY_NAME_DATASTORE = "Datastore";
    private static final String PROPERTY_NAME_SCHEMA_NAME = "Schema name";
    private static final String PROPERTY_NAME_TABLE_NAME = "Table name";
    private final Cache<List<Object>, Object[]> cache = CollectionUtils2.createCache(10000, 5 * 60);

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
    @Inject
    @Provided
    RowAnnotationFactory _annotationFactory;
    @Inject
    @Provided
    RowAnnotation _matches;
    @Inject
    @Provided
    RowAnnotation _misses;
    @Inject
    @Provided
    RowAnnotation _cached;
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
    public TableLookupTransformer(final Datastore datastore, final String schemaName, final String tableName,
            final String[] conditionColumns, final InputColumn<?>[] conditionValues, final String[] outputColumns,
            final boolean cacheLookups) {
        this.datastore = datastore;
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.conditionColumns = conditionColumns;
        this.conditionValues = conditionValues;
        this.cacheLookups = cacheLookups;
        this.outputColumns = outputColumns;
        this.joinSemantic = JoinSemantic.LEFT_JOIN_MAX_ONE;
        _annotationFactory = new DummyRowAnnotationFactory();
        _matches = _annotationFactory.createAnnotation();
        _cached = _annotationFactory.createAnnotation();
        _misses = _annotationFactory.createAnnotation();
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
                try (DatastoreConnection con = datastore.openConnection()) {
                    queryConditionColumns =
                            con.getSchemaNavigator().convertToColumns(schemaName, tableName, conditionColumns);
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
    private Column[] getQueryOutputColumns(final boolean checkNames) {
        if (queryOutputColumns == null) {
            try (DatastoreConnection con = datastore.openConnection()) {
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
            final Column outputColumn = queryOutputColumns[i];
            final String expectedName = outputColumns[i];
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

        } catch (final RuntimeException e) {
            logger.error("Error occurred while compiling lookup query", e);
            throw e;
        }
    }

    private boolean isCarthesianProductMode() {
        return (conditionColumns == null || conditionColumns.length == 0) && (conditionValues == null
                || conditionValues.length == 0);
    }

    @Validate
    public void validate() {
        if (isCarthesianProductMode()) {
            // carthesian product mode
            return;
        }
        final Column[] queryConditionColumns = getQueryConditionColumns();
        final List<String> columnsNotFound = new ArrayList<>();
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
        final Column[] queryOutputColumns = getQueryOutputColumns(true);
        final String[] names = new String[queryOutputColumns.length];
        final Class<?>[] types = new Class[queryOutputColumns.length];
        for (int i = 0; i < queryOutputColumns.length; i++) {
            final Column column = queryOutputColumns[i];
            if (column == null) {
                throw new IllegalArgumentException("Could not find column: " + outputColumns[i]);
            }
            names[i] = column.getName() + " (lookup)";
            types[i] = column.getType().getJavaEquivalentClass();
        }
        return new OutputColumns(names, types);
    }

    @Override
    public Object[] transform(final InputRow inputRow) {
        final List<Object> queryInput;

        if (isCarthesianProductMode()) {
            queryInput = Collections.emptyList();
        } else {
            queryInput = new ArrayList<>(conditionValues.length);
            for (final InputColumn<?> inputColumn : conditionValues) {
                final Object value = inputRow.getValue(inputColumn);
                queryInput.add(value);
            }
        }

        logger.info("Looking up based on condition values: {}", queryInput);

        Object[] result;
        if (cacheLookups && joinSemantic.isCacheable()) {
            result = cache.getIfPresent(queryInput);
            if (result == null) {
                result = performQuery(inputRow, queryInput);
                cache.put(queryInput, result);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Returning cached lookup result: {}", Arrays.toString(result));
                }
                // normally performQuery(...) handles row annotation, but this
                // if-else branch does not call performQuery(...) so we manually
                // do it here too.
                _annotationFactory.annotate(inputRow, 1, _cached);
            }
        } else {
            result = performQuery(inputRow, queryInput);
        }

        return result;
    }

    private Object[] performQuery(final InputRow row, final List<Object> queryInput) {
        try {
            final Column[] queryConditionColumns = getQueryConditionColumns();

            final Object[] parameterValues = new Object[queryConditionColumns.length];
            for (int i = 0; i < queryConditionColumns.length; i++) {
                parameterValues[i] = queryInput.get(i);
            }

            try (DataSet dataSet = datastoreConnection.getDataContext().executeQuery(lookupQuery, parameterValues)) {
                return handleDataSet(row, dataSet);
            }
        } catch (final RuntimeException e) {
            logger.error("Error occurred while looking up based on conditions: " + queryInput, e);
            throw e;
        }
    }

    private Object[] handleDataSet(final InputRow row, final DataSet dataSet) {
        if (!dataSet.next()) {

            logger.info("Result of lookup: None!");
            _annotationFactory.annotate(row, 1, _misses);

            switch (joinSemantic) {
            case LEFT_JOIN_MAX_ONE:
            case LEFT_JOIN:
                return new Object[outputColumns.length];
            default:
                return null;
            }
        }

        _annotationFactory.annotate(row, 1, _matches);

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

    @Override
    public CategorizationResult getResult() {
        final Map<String, RowAnnotation> categories = new LinkedHashMap<>();
        categories.put("Match", _matches);
        categories.put("Miss", _misses);
        if (cacheLookups) {
            categories.put("Cached", _cached);
        }
        return new CategorizationResult(_annotationFactory, categories);
    }
}
