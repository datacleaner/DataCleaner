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
package org.datacleaner.monitor.jobwizard.quickanalysis;

import java.util.ArrayList;
import java.util.List;

import org.datacleaner.api.Analyzer;
import org.datacleaner.api.InputColumn;
import org.datacleaner.beans.BooleanAnalyzer;
import org.datacleaner.beans.DateAndTimeAnalyzer;
import org.datacleaner.beans.NumberAnalyzer;
import org.datacleaner.beans.StringAnalyzer;
import org.datacleaner.beans.stringpattern.PatternFinderAnalyzer;
import org.datacleaner.beans.valuedist.ValueDistributionAnalyzer;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerJobBuilder;
import org.datacleaner.util.ReflectionUtils;

/**
 * Builds a 'Quick Analysis' aspect on an {@link AnalysisJobBuilder}.
 */
public class QuickAnalysisBuilder {

    private final int columnsPerAnalyzer;
    private final boolean includeValueDistribution;
    private final boolean includePatternFinder;

    public QuickAnalysisBuilder(int columnsPerAnalyzer, boolean includeValueDistribution, boolean includePatternFinder) {
        this.columnsPerAnalyzer = columnsPerAnalyzer;
        this.includeValueDistribution = includeValueDistribution;
        this.includePatternFinder = includePatternFinder;
    }

    public void configureAnalysisJobBuilder(AnalysisJobBuilder ajb) {
        final List<InputColumn<?>> booleanColumns = new ArrayList<InputColumn<?>>();
        final List<InputColumn<?>> stringColumns = new ArrayList<InputColumn<?>>();
        final List<InputColumn<?>> numberColumns = new ArrayList<InputColumn<?>>();
        final List<InputColumn<?>> dateTimeColumns = new ArrayList<InputColumn<?>>();

        for (InputColumn<?> inputColumn : ajb.getSourceColumns()) {
            final Class<?> dataType = inputColumn.getDataType();
            if (ReflectionUtils.isBoolean(dataType)) {
                booleanColumns.add(inputColumn);
            } else if (ReflectionUtils.isNumber(dataType)) {
                numberColumns.add(inputColumn);
            } else if (ReflectionUtils.isDate(dataType)) {
                dateTimeColumns.add(inputColumn);
            } else if (ReflectionUtils.isString(dataType)) {
                stringColumns.add(inputColumn);
            }
        }

        if (!booleanColumns.isEmpty()) {
            // boolean analyzer contains combination matrices, so all columns
            // are added to a single analyzer job.
            ajb.addAnalyzer(BooleanAnalyzer.class).addInputColumns(booleanColumns);
        }
        if (!numberColumns.isEmpty()) {
            createAnalyzers(ajb, NumberAnalyzer.class, numberColumns);
        }
        if (!dateTimeColumns.isEmpty()) {
            createAnalyzers(ajb, DateAndTimeAnalyzer.class, dateTimeColumns);
        }
        if (!stringColumns.isEmpty()) {
            createAnalyzers(ajb, StringAnalyzer.class, stringColumns);
        }
    }

    /**
     * Registers analyzers and up to 4 columns per analyzer. This restriction is
     * to ensure that results will be nicely readable. A table might contain
     * hundreds of columns.
     * 
     * @param ajb
     * @param analyzerClass
     * @param columns
     */
    private void createAnalyzers(AnalysisJobBuilder ajb, Class<? extends Analyzer<?>> analyzerClass,
            List<InputColumn<?>> columns) {
        AnalyzerJobBuilder<?> analyzerJobBuilder = ajb.addAnalyzer(analyzerClass);
        int columnCount = 0;
        for (InputColumn<?> inputColumn : columns) {
            if (columnCount == columnsPerAnalyzer) {
                analyzerJobBuilder = ajb.addAnalyzer(analyzerClass);
                columnCount = 0;
            }
            analyzerJobBuilder.addInputColumn(inputColumn);

            if (includeValueDistribution) {
                ajb.addAnalyzer(ValueDistributionAnalyzer.class).addInputColumn(inputColumn);
            }
            if (inputColumn.getDataType() == String.class && includePatternFinder) {
                ajb.addAnalyzer(PatternFinderAnalyzer.class).addInputColumn(inputColumn);
            }
            columnCount++;
        }
    }
}
