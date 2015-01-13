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
package org.datacleaner.user;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
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
 * Defines the strategy and rules for doing quick analysis.
 * 
 * @see QuickAnalysisActionListener
 */
public class QuickAnalysisStrategy implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String USER_PREFERENCES_NAMESPACE = "datacleaner.quickanalysis.strategy";

    private final int columnsPerAnalyzer;
    private final boolean includeValueDistribution;
    private final boolean includePatternFinder;

    /**
     * Saves a {@link QuickAnalysisStrategy} to a {@link UserPreferences}
     * object.
     * 
     * @param strategy
     * @param userPreferences
     */
    public static void saveToUserPreferences(QuickAnalysisStrategy strategy, UserPreferences userPreferences) {
        final Map<String, String> properties = userPreferences.getAdditionalProperties();

        properties.put(USER_PREFERENCES_NAMESPACE + ".columnsPerAnalyzer", "" + strategy.columnsPerAnalyzer);
        properties
                .put(USER_PREFERENCES_NAMESPACE + ".includeValueDistribution", "" + strategy.includeValueDistribution);
        properties.put(USER_PREFERENCES_NAMESPACE + ".includePatternFinder", "" + strategy.includePatternFinder);
    }

    /**
     * Loads {@link QuickAnalysisStrategy} from a {@link UserPreferences}
     * object.
     * 
     * @param userPreferences
     * @return
     */
    public static QuickAnalysisStrategy loadFromUserPreferences(UserPreferences userPreferences) {
        final Map<String, String> properties = userPreferences.getAdditionalProperties();

        final int columnsPerAnalyzer = MapUtils.getIntValue(properties, USER_PREFERENCES_NAMESPACE
                + ".columnsPerAnalyzer", 5);
        final boolean includeValueDistribution = MapUtils.getBooleanValue(properties, USER_PREFERENCES_NAMESPACE
                + ".includeValueDistribution", false);
        final boolean includePatternFinder = MapUtils.getBooleanValue(properties, USER_PREFERENCES_NAMESPACE
                + ".includePatternFinder", false);

        return new QuickAnalysisStrategy(columnsPerAnalyzer, includeValueDistribution, includePatternFinder);
    }

    public QuickAnalysisStrategy() {
        this(5, false, false);
    }

    public QuickAnalysisStrategy(int columnsPerAnalyzer, boolean includeValueDistribution, boolean includePatternFinder) {
        this.columnsPerAnalyzer = columnsPerAnalyzer;
        this.includeValueDistribution = includeValueDistribution;
        this.includePatternFinder = includePatternFinder;
    }

    public boolean isIncludePatternFinder() {
        return includePatternFinder;
    }

    public boolean isIncludeValueDistribution() {
        return includeValueDistribution;
    }

    public int getColumnsPerAnalyzer() {
        return columnsPerAnalyzer;
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
        final int columnsPerAnalyzer = getColumnsPerAnalyzer();

        AnalyzerJobBuilder<?> analyzerJobBuilder = ajb.addAnalyzer(analyzerClass);
        int columnCount = 0;
        for (InputColumn<?> inputColumn : columns) {
            if (columnCount == columnsPerAnalyzer) {
                analyzerJobBuilder = ajb.addAnalyzer(analyzerClass);
                columnCount = 0;
            }
            analyzerJobBuilder.addInputColumn(inputColumn);

            if (isIncludeValueDistribution()) {
                ajb.addAnalyzer(ValueDistributionAnalyzer.class).addInputColumn(inputColumn);
            }
            if (inputColumn.getDataType() == String.class && isIncludePatternFinder()) {
                ajb.addAnalyzer(PatternFinderAnalyzer.class).addInputColumn(inputColumn);
            }
            columnCount++;
        }
    }
}
