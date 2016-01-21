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
package org.datacleaner.components.group;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Named;

import org.apache.metamodel.query.FunctionType;
import org.apache.metamodel.query.Query;
import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.util.AggregateBuilder;
import org.apache.metamodel.util.HasName;
import org.datacleaner.api.Categorized;
import org.datacleaner.api.Close;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.Distributed;
import org.datacleaner.api.Initialize;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.MappedProperty;
import org.datacleaner.api.MultiStreamComponent;
import org.datacleaner.api.OutputDataStream;
import org.datacleaner.api.OutputRowCollector;
import org.datacleaner.components.categories.CompositionCategory;
import org.datacleaner.job.output.OutputDataStreamBuilder;
import org.datacleaner.job.output.OutputDataStreams;

@Named("Grouper")
@Description("A component that allows grouping and aggregating values with the same key.")
@Categorized(value = CompositionCategory.class)
@Distributed(false)
public class GrouperTransformer extends MultiStreamComponent {

    public static final String PROPERTY_GROUP_KEY = "Group key";
    public static final String PROPERTY_AGGREGATED_VALUES = "Aggregated values";
    public static final String PROPERTY_AGGREGATION_TYPES = "AggregationTypes";
    public static final String PROPERTY_VALUE_SORTATION = "Value sortation";

    private static final Object NULL_KEY = new Object();

    public static enum AggregationType implements HasName {
        CONCAT_VALUES("Concatenate values"), FIRST_VALUE("Select first value"), LAST_VALUE(
                "Select last value"), RANDOM_VALUE("Select random value"), CREATE_LIST("Create list of values"), SUM(
                        "Calculate sum"), AVG("Calculate average");

        private final String _name;

        private AggregationType(String name) {
            _name = name;
        }

        @Override
        public String getName() {
            return _name;
        }

        public AggregateBuilder<?> createAggregateBuilder(SortationType sortationType, boolean skipNulls,
                String concatenationSeparator) {
            switch (this) {
            case CONCAT_VALUES:
                return new ConcatAggregateBuilder(sortationType, skipNulls, concatenationSeparator);
            case CREATE_LIST:
                return new CreateListAggregateBuilder(sortationType, skipNulls);
            case FIRST_VALUE:
                return FunctionType.FIRST.createAggregateBuilder();
            case LAST_VALUE:
                return FunctionType.LAST.createAggregateBuilder();
            case SUM:
                return FunctionType.SUM.createAggregateBuilder();
            case AVG:
                return FunctionType.AVG.createAggregateBuilder();
            case RANDOM_VALUE:
                return FunctionType.RANDOM.createAggregateBuilder();
            default:
                throw new UnsupportedOperationException();
            }
        }

        public void addColumnToOutputStream(OutputDataStreamBuilder outputDataStreamBuilder,
                InputColumn<?> inputColumn) {
            switch (this) {
            case FIRST_VALUE:
            case LAST_VALUE:
            case RANDOM_VALUE:
                outputDataStreamBuilder.withColumnLike(inputColumn);
                break;
            case SUM:
            case AVG:
                outputDataStreamBuilder.withColumn(inputColumn.getName(), ColumnType.NUMBER);
                break;
            case CONCAT_VALUES:
                outputDataStreamBuilder.withColumn(inputColumn.getName(), ColumnType.STRING);
                break;
            case CREATE_LIST:
                outputDataStreamBuilder.withColumn(inputColumn.getName(), ColumnType.LIST);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported aggregation type: " + this);
            }

        }
    }

    @Configured(order = 1, value = PROPERTY_GROUP_KEY)
    InputColumn<?> groupKey;

    @Configured(order = 2, value = PROPERTY_AGGREGATED_VALUES)
    InputColumn<?>[] aggregatedValues;

    @Configured(order = 3, value = PROPERTY_AGGREGATION_TYPES)
    @MappedProperty(PROPERTY_AGGREGATED_VALUES)
    AggregationType[] aggregationTypes;

    @Configured(order = 4, value = PROPERTY_VALUE_SORTATION)
    SortationType valueSortation = SortationType.NONE;

    @Configured
    String concatenationSeparator = ", ";

    @Configured
    boolean skipNullGroupKeys = true;

    @Configured
    boolean skipNullValues = true;

    private final ConcurrentMap<Object, List<AggregateBuilder<?>>> _aggregateBuilders = new ConcurrentHashMap<>();
    private OutputRowCollector _rowCollector;

    @Initialize
    public void init() {
        _aggregateBuilders.clear();
    }

    @Override
    public OutputDataStream[] getOutputDataStreams() {
        final OutputDataStreamBuilder outputDataStreamBuilder = OutputDataStreams.pushDataStream("output");
        outputDataStreamBuilder.withColumnLike(groupKey);
        outputDataStreamBuilder.withColumn("row_count", ColumnType.INTEGER);
        for (int i = 0; i < aggregatedValues.length; i++) {
            final InputColumn<?> inputColumn = aggregatedValues[i];
            final AggregationType aggregationType = (aggregationTypes.length <= i ? AggregationType.CREATE_LIST
                    : aggregationTypes[i]);
            aggregationType.addColumnToOutputStream(outputDataStreamBuilder, inputColumn);
        }

        final OutputDataStream stream = outputDataStreamBuilder.toOutputDataStream();
        return new OutputDataStream[] { stream };
    }

    @Override
    public void initializeOutputDataStream(OutputDataStream stream, Query q, OutputRowCollector collector) {
        _rowCollector = collector;
    }

    @Override
    protected void run(InputRow row) {
        if (_rowCollector == null) {
            // nothing to do
            return;
        }

        Object key = row.getValue(groupKey);
        if (key == null) {
            if (skipNullGroupKeys) {
                // skip it
                return;
            } else {
                key = NULL_KEY;
            }
        }


        final List<AggregateBuilder<?>> aggregateBuilders = getAggregateBuilders(key);
        final long rowId = row.getId();
        
        // send rowId to COUNT function
        aggregateBuilders.get(0).add(rowId);
        
        for (int i = 0; i < aggregatedValues.length; i++) {
            final Object value = row.getValue(aggregatedValues[i]);
            final AggregateBuilder<?> aggregateBuilder = aggregateBuilders.get(i+1);
            synchronized (aggregateBuilder) {
                if (aggregateBuilder instanceof AbstractRowNumberAwareAggregateBuilder) {
                    ((AbstractRowNumberAwareAggregateBuilder<?>) aggregateBuilder).add(value, rowId);
                } else {
                    aggregateBuilder.add(value);
                }
            }
        }
    }

    private List<AggregateBuilder<?>> getAggregateBuilders(Object key) {
        List<AggregateBuilder<?>> collectionOfAggregateBuilders = _aggregateBuilders.get(key);
        if (collectionOfAggregateBuilders == null) {
            final List<AggregateBuilder<?>> newCollectionOfValues = new ArrayList<>(aggregationTypes.length);

            // add COUNT aggregation as first
            newCollectionOfValues.add(FunctionType.COUNT.createAggregateBuilder());

            for (AggregationType aggregationType : aggregationTypes) {
                final AggregateBuilder<?> aggregateBuilder = aggregationType.createAggregateBuilder(valueSortation,
                        skipNullValues, concatenationSeparator);
                newCollectionOfValues.add(aggregateBuilder);
            }

            final List<AggregateBuilder<?>> previousCollectionOfValues = _aggregateBuilders.putIfAbsent(key,
                    newCollectionOfValues);
            if (previousCollectionOfValues == null) {
                collectionOfAggregateBuilders = newCollectionOfValues;
            } else {
                collectionOfAggregateBuilders = previousCollectionOfValues;
            }
        }
        return collectionOfAggregateBuilders;
    }

    @Close
    public void close() {
        final Set<Entry<Object, List<AggregateBuilder<?>>>> entrySet = _aggregateBuilders.entrySet();
        for (Entry<Object, List<AggregateBuilder<?>>> entry : entrySet) {
            final Object key = entry.getKey();
            final List<AggregateBuilder<?>> aggregateBuilders = entry.getValue();

            final Object[] values = new Object[2 + aggregatedValues.length];
            values[0] = key == NULL_KEY ? null : key;
            values[1] = aggregateBuilders.get(0).getAggregate();

            for (int i = 1; i < aggregateBuilders.size(); i++) {
                final AggregateBuilder<?> aggregateBuilder = aggregateBuilders.get(i);
                values[i + 1] = aggregateBuilder.getAggregate();
            }

            _rowCollector.putValues(values);
        }
    }
}
