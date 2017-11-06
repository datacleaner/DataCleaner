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
package org.datacleaner.components.fillpattern;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Named;

import org.datacleaner.api.Analyzer;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.MappedProperty;
import org.datacleaner.api.Provided;
import org.datacleaner.storage.RowAnnotationFactory;
import org.datacleaner.util.LabelUtils;

@Named("Fill pattern")
@Description("Determines the patterns of filling specific fields")
public class FillPatternAnalyzer implements Analyzer<FillPatternResult> {

    private static final String GROUP_NAME_SINGLE = "__single_group__";
    private static final int PARALLEL_RESULT_CALCULATION_THRESHOLD = 10;

    public static final String PROPERTY_GROUP_COLUMN = "Group column";
    public static final String PROPERTY_INSPECTED_COLUMNS = "Inspected columns";
    public static final String PROPERTY_INSPECTION_TYPES = "Inspection types";
    public static final String FILLED_LABEL = "<filled>";

    @Inject
    @Configured(order = 1, value = PROPERTY_GROUP_COLUMN, required = false)
    InputColumn<String> groupColumn;

    @Inject
    @Configured(order = 2, value = PROPERTY_INSPECTED_COLUMNS)
    InputColumn<?>[] inspectedColumns;

    @Inject
    @Configured(order = 3, value = PROPERTY_INSPECTION_TYPES)
    @MappedProperty(PROPERTY_INSPECTED_COLUMNS)
    InspectionType[] inspectionTypes;

    @Inject
    @Provided
    RowAnnotationFactory rowAnnotationFactory;

    private final ConcurrentMap<String, FillPatternsBuilder> _buildersByGroup =
            new ConcurrentHashMap<String, FillPatternsBuilder>();

    public void run(InputRow row, int count) {
        final String group;
        if (groupColumn == null) {
            group = GROUP_NAME_SINGLE;
        } else {
            group = row.getValue(groupColumn);
        }

        final FillPatternsBuilder fillPatternsBuilder = getOrCreateFillPatternsBuilder(group);

        final List<Object> inspectedValues = row.getValues(inspectedColumns);
        final List<Object> inspectionOutcomes = new ArrayList<>(inspectionTypes.length);

        for (int i = 0; i < inspectionTypes.length; i++) {
            final Object value = inspectedValues.get(i);
            final InspectionType inspectionType = inspectionTypes[i];
            inspectionOutcomes.add(inspectionType.inspect(value));
        }

        fillPatternsBuilder.addObservation(row, inspectionOutcomes);
    }

    private FillPatternsBuilder getOrCreateFillPatternsBuilder(String group) {
        if (group == null) {
            group = LabelUtils.NULL_LABEL;
        } else if (group.trim().isEmpty()) {
            group = LabelUtils.BLANK_LABEL;
        }

        FillPatternsBuilder fillPatterns = _buildersByGroup.get(group);
        if (fillPatterns == null) {
            // slightly complex code-block to ensure that we have as little as
            // possible blocking work on the map
            final FillPatternsBuilder newFillPatterns = new FillPatternsBuilder(rowAnnotationFactory);
            final FillPatternsBuilder existingFillPatterns = _buildersByGroup.putIfAbsent(group, newFillPatterns);
            if (existingFillPatterns == null) {
                fillPatterns = newFillPatterns;
            } else {
                fillPatterns = existingFillPatterns;
            }
        }
        return fillPatterns;
    }

    public FillPatternResult getResult() {
        final boolean parallel = _buildersByGroup.size() > PARALLEL_RESULT_CALCULATION_THRESHOLD;

        Stream<Entry<String, FillPatternsBuilder>> stream = _buildersByGroup.entrySet().stream();
        if (parallel) {
            stream = stream.parallel();
        }

        final List<FillPatternGroup> fillPatterns = stream.map(e -> {
            final String groupName = groupColumn == null ? null : e.getKey();
            final FillPatternGroup fillPatternsForGroup = e.getValue().build(groupName);
            return fillPatternsForGroup;
        }).sorted().collect(Collectors.toList());
        _buildersByGroup.clear();

        return new FillPatternResult(rowAnnotationFactory, Arrays.asList(inspectedColumns), fillPatterns);
    }

}
