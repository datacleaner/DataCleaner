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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Named;

import org.apache.metamodel.query.Query;
import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.util.HasName;
import org.apache.metamodel.util.ObjectComparator;
import org.datacleaner.api.Categorized;
import org.datacleaner.api.Close;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
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
public class GrouperTransformer extends MultiStreamComponent {

    public static final String PROPERTY_GROUP_KEY = "Group key";
    public static final String PROPERTY_AGGREGATED_VALUES = "Aggregated values";
    public static final String PROPERTY_AGGREGATION_TYPES = "AggregationTypes";
    public static final String PROPERTY_VALUE_SORTATION = "Value sortation";

    private static final Object NULL_KEY = new Object();

    public static enum AggregationType implements HasName {
        CONCAT_VALUES("Concatenate values"), FIRST_VALUE("Select first value"), CREATE_LIST("Create list of values");

        private final String _name;

        private AggregationType(String name) {
            _name = name;
        }

        @Override
        public String getName() {
            return _name;
        }
    }

    public static enum SortationType implements HasName {
        NONE("None"), RECORD_ORDER("Record order"), NATURAL_SORT_ASC("Natural sort, ascending"), NATURAL_SORT_DESC(
                "Natural sort, descending");

        private final String _name;

        private SortationType(String name) {
            _name = name;
        }

        @Override
        public String getName() {
            return _name;
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

    private final ConcurrentMap<Object, ConcurrentLinkedDeque<List<Object>>> _values = new ConcurrentHashMap<>();
    private OutputRowCollector _rowCollector;

    @Initialize
    public void init() {
        _values.clear();
    }

    @Override
    public OutputDataStream[] getOutputDataStreams() {
        final OutputDataStreamBuilder outputDataStreamBuilder = OutputDataStreams.pushDataStream("output");
        outputDataStreamBuilder.withColumnLike(groupKey);
        outputDataStreamBuilder.withColumn("row_count", ColumnType.INTEGER);
        for (int i = 0; i < aggregatedValues.length; i++) {
            final InputColumn<?> inputColumn = aggregatedValues[i];
            final AggregationType aggregationType = aggregationTypes[i];
            switch (aggregationType) {
            case FIRST_VALUE:
                outputDataStreamBuilder.withColumnLike(inputColumn);
                break;
            case CONCAT_VALUES:
                outputDataStreamBuilder.withColumn(inputColumn.getName(), ColumnType.STRING);
                break;
            case CREATE_LIST:
                outputDataStreamBuilder.withColumn(inputColumn.getName(), ColumnType.LIST);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported aggregation type: " + aggregationType);
            }
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

        final List<Object> values = row.getValues(aggregatedValues);
        if (valueSortation == SortationType.RECORD_ORDER) {
            // add row ID for the sortation
            values.add(row.getId());
        }

        ConcurrentLinkedDeque<List<Object>> newCollectionOfValues = new ConcurrentLinkedDeque<>();
        ConcurrentLinkedDeque<List<Object>> previousCollectionOfValues = _values
                .putIfAbsent(key, newCollectionOfValues);
        if (previousCollectionOfValues == null) {
            newCollectionOfValues.add(values);
        } else {
            previousCollectionOfValues.add(values);
        }
    }

    @Close
    public void close() {
        final Set<Entry<Object, ConcurrentLinkedDeque<List<Object>>>> entries = _values.entrySet();
        for (Entry<Object, ConcurrentLinkedDeque<List<Object>>> entry : entries) {
            final Object key = entry.getKey();
            final ConcurrentLinkedDeque<List<Object>> collectionOfValues = entry.getValue();

            final Object[] values = new Object[2 + aggregatedValues.length];
            values[0] = key == NULL_KEY ? null : key;
            values[1] = collectionOfValues.size();

            final List<Object> aggregatedValues = aggregate(collectionOfValues);
            for (int i = 0; i < aggregatedValues.size(); i++) {
                values[i + 2] = aggregatedValues.get(i);
            }

            _rowCollector.putValues(values);
        }
    }

    private List<Object> aggregate(Collection<List<Object>> collectionOfValues) {
        final int valueCount = aggregatedValues.length;
        final List<Object> result = new ArrayList<>(valueCount);
        for (int i = 0; i < valueCount; i++) {
            // Do the column-wise aggregation
            final Object aggregatedValue = aggregate(collectionOfValues, i);
            result.add(aggregatedValue);
        }
        return result;
    }

    private Object aggregate(Collection<List<Object>> collectionOfValues, int i) {
        final List<Object> result = new ArrayList<>(collectionOfValues.size());

        switch (valueSortation) {
        case RECORD_ORDER:
            final SortedMap<Number, Object> sortedValueMap = new TreeMap<>();
            for (List<Object> values : collectionOfValues) {
                final Object value = values.get(i);
                if (includeValue(value)) {
                    // row ID will be the last of the values in each object-list
                    final Number rowId = (Number) values.get(values.size() - 1);
                    sortedValueMap.put(rowId, value);
                }
            }
            result.addAll(sortedValueMap.values());
            break;
        default:
            for (List<Object> values : collectionOfValues) {
                final Object value = values.get(i);
                if (includeValue(value)) {
                    result.add(value);
                }
            }
        }

        switch (valueSortation) {
        case NONE:
        case RECORD_ORDER:
            // nothing to do
            break;
        case NATURAL_SORT_ASC:
            Collections.sort(result, ObjectComparator.getComparator());
            break;
        case NATURAL_SORT_DESC:
            Collections.sort(result, Collections.reverseOrder(ObjectComparator.getComparator()));
            break;
        }

        final AggregationType aggregationType = aggregationTypes[i];
        switch (aggregationType) {
        case FIRST_VALUE:
            if (result.isEmpty()) {
                return null;
            }
            return result.iterator().next();
        case CREATE_LIST:
            return result;
        case CONCAT_VALUES:
            final StringBuilder sb = new StringBuilder();
            for (Object value : result) {
                if (sb.length() > 0) {
                    sb.append(concatenationSeparator);
                }
                sb.append(value);
            }
            return sb.toString();
        default:
            throw new UnsupportedOperationException("Unsupported aggregation type: " + aggregationType);
        }
    }

    private boolean includeValue(Object value) {
        return !(skipNullValues && value == null);
    }
}
