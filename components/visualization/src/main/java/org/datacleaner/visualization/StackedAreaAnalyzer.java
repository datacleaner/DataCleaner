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

import java.util.ArrayList;

import javax.inject.Named;

import org.datacleaner.api.Analyzer;
import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.Initialize;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.Validate;

@Named("Stacked area plot")
@Description("Plots a number of related measures in a stacked area chart. Useful visualization for showing the relative influence of each measure compared to the sum of measures.")
@Categorized(VisualizationCategory.class)
public class StackedAreaAnalyzer implements Analyzer<StackedAreaAnalyzerResult> {

    @Configured(value = "Measure columns", order = 1)
    InputColumn<Number>[] measureColumns = null;

    @Configured(value = "Category column", order = 2)
    InputColumn<?> categoryColumn = null;

    StackedAreaAnalyzerResult result = null;

    @Validate
    void validate() {
        result = new StackedAreaAnalyzerResult(categoryColumn, measureColumns);
        if (!result.isNumberCategory() && !result.isTimeCategory()) {
            throw new IllegalStateException("Category column must be either a number or time based");
        }
    }

    @Initialize
    void initialize() {
        result = new StackedAreaAnalyzerResult(categoryColumn, measureColumns);
    }

    @Override
    public void run(InputRow row, int distinctCount) {

        final Object category = row.getValue(categoryColumn);
        if (category != null) {
            final ArrayList<Number> measures = new ArrayList<>();
            for (int i = 0; i < measureColumns.length; i++) {
                measures.add(row.getValue(measureColumns[i]));
            }

            for (int i = 1; i < distinctCount; i++) {
                result.addMeasures(category, measures);
            }
        }

    }

    @Override
    public StackedAreaAnalyzerResult getResult() {
        return result;
    }

}
