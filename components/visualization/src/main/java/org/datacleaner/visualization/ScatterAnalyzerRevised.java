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
package org.datacleaner.visualization;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.datacleaner.api.Analyzer;
import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.Provided;
import org.datacleaner.storage.RowAnnotationFactory;
import org.datacleaner.util.LabelUtils;

@Named("Scatter plot")
@Description("Plots the occurences of two number variables in a scatter plot chart. A useful visualization for identifying outliers in numeric data relationships.")
@Categorized(VisualizationCategory.class)
public class ScatterAnalyzerRevised implements Analyzer<ScatterAnalyzerResultRevised> {

    public static final String PROPERTY_VARIABLE1 = "Variable1";
    public static final String PROPERTY_VARIABLE2 = "Variable2";
    public static final String PROPERTY_GROUP_COLUMN = "Group column";

    private static final String DEFAULT_GROUP_NAME = "Observations";
    @Inject
    @Configured(value = PROPERTY_VARIABLE1)
    @Description("The field with the first variable. Will be plotted on the horizontal X-axis.")
    InputColumn<Number> variable1 = null;

    @Inject
    @Configured(value = PROPERTY_VARIABLE2)
    @Description("The field with the second variable. Will be plotted on the vertical Y-axis.")
    InputColumn<Number> variable2 = null;

    @Inject
    @Configured(value = PROPERTY_GROUP_COLUMN, required = false)
    InputColumn<?> groupColumn = null;

    @Inject
    @Provided
    RowAnnotationFactory rowAnnotationFactory = null;

    private final Map<String, ScatterGroupRevised> _groups = new LinkedHashMap<>();

    @Override
    public void run(InputRow row, int distinctCount) {

        final Number value1 = row.getValue(variable1);
        final Number value2 = row.getValue(variable2);

        if (value1 != null && value2 != null) {
            final Object groupNameValue;
            if (groupColumn == null) {
                groupNameValue = DEFAULT_GROUP_NAME;
            } else {
                groupNameValue = row.getValue(groupColumn);
            }
            final String groupName = LabelUtils.getValueLabel(groupNameValue);
            final ScatterGroupRevised group = groups(groupName);
            group.register(value1, value2, row, distinctCount);
        }
    }

    private ScatterGroupRevised groups(String groupName) {
        if (_groups.containsKey(groupName)) {
            return _groups.get(groupName);
        } else {
            final ScatterGroupRevised group = new ScatterGroupRevised(groupName, rowAnnotationFactory);
            _groups.put(groupName, group);
            return group;
        }
    }

    public Map<String, ScatterGroupRevised> getGroups() {
        return _groups;
    }

    @Override
    public ScatterAnalyzerResultRevised getResult() {
        final List<ScatterGroupRevised> groupsList = _groups.values().stream().collect(Collectors.toList());
        return new ScatterAnalyzerResultRevised(groupsList, variable1, variable2, groupColumn);
    }

}
