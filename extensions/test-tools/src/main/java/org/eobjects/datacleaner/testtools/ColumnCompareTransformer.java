package org.eobjects.datacleaner.testtools;

import javax.inject.Inject;

import org.eobjects.analyzer.beans.api.Categorized;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.OutputRowCollector;
import org.eobjects.analyzer.beans.api.Provided;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

@TransformerBean("Compare Columns")
@Description("checks if content of columns is valid by comparing it to the content of reference columns.")
@Categorized(TestToolsCategory.class)
public class ColumnCompareTransformer implements Transformer<Object> {

    @Inject
    @Configured
    @Description("Select the columns that should be checkt and provide the reference column")
    InputColumn<String>[] testColumns;

    @Inject
    @Configured(required = false)
    InputColumn<String>[] referenceColumns;

    @Inject
    @Provided
    OutputRowCollector outputRowCollector;

    public ColumnCompareTransformer() {
    }

    @Override
    public OutputColumns getOutputColumns() {
        String[] columnNames = { "Result", "Invalid Columns"};
        Class<?>[] types = { String.class, String.class };
        return new OutputColumns(columnNames, types);
    }

    @Override
    public Object[] transform(InputRow inputRow) {

        String result = "VALID";
        String message = "";
        for (int i = 0; i < testColumns.length; i++) {
            InputColumn<String> inputColumn = testColumns[i];
            String value = inputRow.getValue(inputColumn);
            InputColumn<String> refColumn = referenceColumns[i];
            String refValue = inputRow.getValue(refColumn);
            if (!value.equals(refValue)) {
                if (!message.isEmpty()) {
                    message +=  ", ";
                }
                result = "INVALID";
                message += inputColumn.getName();                
            }

        }
        
        Object[] results = { result, message };
        return results;
    }
}
