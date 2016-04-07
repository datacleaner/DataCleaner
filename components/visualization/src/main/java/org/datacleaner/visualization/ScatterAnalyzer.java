package org.datacleaner.visualization;

import java.util.HashMap;
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
public class ScatterAnalyzer implements Analyzer<ScatterAnalyzerResult> {

    private static final String DEFAULT_GROUP_NAME = "Observations";
    @Inject
    @Configured(value = "Variable1")
    @Description("The field with the first variable. Will be plotted on the horizontal X-axis.")
    InputColumn<Number> _variable1 = null;

    @Inject
    @Configured(value = "Variable2")
    @Description("The field with the second variable. Will be plotted on the vertical Y-axis.")
    InputColumn<Number> _variable2 = null;

    @Inject
    @Configured(value = "Group column", required = false)
    InputColumn<?> _groupColumn = null;

    @Inject
    @Provided
    RowAnnotationFactory _rowAnnotationFactory = null;

    private final Map<String, ScatterGroup> _groups = new HashMap<>();

    @Override
    public void run(InputRow row, int distinctCount) {

        final Number value1 = row.getValue(_variable1);
        final Number value2 = row.getValue(_variable2);

        if (value1 != null && value2 != null) {
            final String groupNameValue;
            if (_groupColumn == null) {
                groupNameValue = DEFAULT_GROUP_NAME;
            } else {
                groupNameValue = (String) row.getValue(_groupColumn);
            }

            final String groupName = LabelUtils.getValueLabel(groupNameValue);
            final ScatterGroup group = groups(groupName);
            group.register(value1, value2, row, distinctCount);
        }
    }

    private ScatterGroup groups(String groupName) {
        final ScatterGroup group = new ScatterGroup(groupName, _rowAnnotationFactory);
        _groups.put(groupName, group);
        return group;
    }

    public Map<String, ScatterGroup> getGroups() {
        return _groups;
    }

    @Override
    public ScatterAnalyzerResult getResult() {
        final List<ScatterGroup> groupsList = _groups.values().stream().collect(Collectors.toList());
        return new ScatterAnalyzerResult(groupsList, _variable1, _variable2, _groupColumn);
    }

}
