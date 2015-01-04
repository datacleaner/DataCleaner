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
package org.datacleaner.testtools;

import javax.inject.Inject;

import org.datacleaner.beans.api.Categorized;
import org.datacleaner.beans.api.Configured;
import org.datacleaner.beans.api.Description;
import org.datacleaner.beans.api.OutputColumns;
import org.datacleaner.beans.api.OutputRowCollector;
import org.datacleaner.beans.api.Provided;
import org.datacleaner.beans.api.Transformer;
import org.datacleaner.beans.api.TransformerBean;
import org.datacleaner.data.InputColumn;
import org.datacleaner.data.InputRow;

@TransformerBean("Compare Columns")
@Description("checks if content of columns is valid by comparing it to the content of reference columns.")
@Categorized(TestToolsCategory.class)
public class ColumnCompareTransformer implements Transformer<Object> {

    @Inject
    @Configured
    @Description("Select the columns that should be checked and provide the reference column.")
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
