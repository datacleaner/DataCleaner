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
package org.datacleaner.beans.stringpattern;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.Distributed;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.Metric;
import org.datacleaner.reference.SimpleStringPattern;
import org.datacleaner.result.Crosstab;
import org.datacleaner.result.CrosstabDimension;
import org.datacleaner.result.QueryParameterizableMetric;

/**
 * Represents the result of the {@link PatternFinderAnalyzer}.
 *
 * A pattern finder result has two basic forms: Grouped or ungrouped. To find
 * out which type a particular instance has, use the
 * {@link #isGroupingEnabled()} method.
 *
 * Ungrouped results only contain a single/global crosstab. A grouped result
 * contain multiple crosstabs, based on groups.
 *
 *
 */
@Distributed(reducer = PatternFinderResultReducer.class)
public class PatternFinderResult implements AnalyzerResult {

    private static final long serialVersionUID = 1L;

    private final InputColumn<String> _column;
    private final InputColumn<String> _groupColumn;
    private final Map<String, Crosstab<?>> _crosstabs;
    private final TokenizerConfiguration _tokenizerConfiguration;

    public PatternFinderResult(final InputColumn<String> column, final Crosstab<?> crosstab,
            final TokenizerConfiguration tokenizerConfiguration) {
        _column = column;
        _groupColumn = null;
        _crosstabs = new HashMap<>();
        _crosstabs.put(null, crosstab);
        _tokenizerConfiguration = tokenizerConfiguration;
    }

    public PatternFinderResult(final InputColumn<String> column, final InputColumn<String> groupColumn,
            final Map<String, Crosstab<?>> crosstabs, final TokenizerConfiguration tokenizerConfiguration) {
        _column = column;
        _groupColumn = groupColumn;
        _crosstabs = crosstabs;
        _tokenizerConfiguration = tokenizerConfiguration;
    }

    public TokenizerConfiguration getTokenizerConfiguration() {
        return _tokenizerConfiguration;
    }

    public InputColumn<String> getColumn() {
        return _column;
    }

    public InputColumn<String> getGroupColumn() {
        return _groupColumn;
    }

    public Map<String, Crosstab<?>> getGroupedCrosstabs() {
        if (!isGroupingEnabled()) {
            throw new IllegalStateException("This result is not a grouped crosstab based Pattern Finder result");
        }
        return _crosstabs;
    }

    public Crosstab<?> getSingleCrosstab() {
        if (isGroupingEnabled()) {
            throw new IllegalStateException("This result is not a single crosstab based Pattern Finder result");
        }
        return _crosstabs.get(null);
    }

    public boolean isGroupingEnabled() {
        return _groupColumn != null;
    }

    @Metric(value = "Match count", supportsInClause = true)
    public QueryParameterizableMetric getMatchCount() {
        return new QueryParameterizableMetric() {

            @Override
            protected int getInstanceCount(final String instance) {
                return getMatchCount(instance);
            }

            @Override
            protected int getTotalCount() {
                return PatternFinderResult.this.getTotalCount();
            }

            @Override
            public Collection<String> getParameterSuggestions() {
                final Crosstab<?> crosstab = getSingleCrosstab();
                final CrosstabDimension patternDimension =
                        crosstab.getDimension(PatternFinderAnalyzer.DIMENSION_NAME_PATTERN);
                return patternDimension.getCategories();
            }
        };
    }

    private int getTotalCount() {
        int sum = 0;

        final Crosstab<?> crosstab = getSingleCrosstab();
        final CrosstabDimension patternDimension = crosstab.getDimension(PatternFinderAnalyzer.DIMENSION_NAME_PATTERN);
        final List<String> categories = patternDimension.getCategories();
        for (final String category : categories) {
            final Object value = crosstab.where(patternDimension, category)
                    .where(PatternFinderAnalyzer.DIMENSION_NAME_MEASURES, PatternFinderAnalyzer.MEASURE_MATCH_COUNT)
                    .get();
            if (value instanceof Number) {
                sum += ((Number) value).intValue();
            }
        }
        return sum;
    }

    public int getMatchCount(final String pattern) {
        final Crosstab<?> crosstab = getSingleCrosstab();
        final CrosstabDimension patternDimension = crosstab.getDimension(PatternFinderAnalyzer.DIMENSION_NAME_PATTERN);
        final List<String> categories = patternDimension.getCategories();
        for (final String category : categories) {
            final SimpleStringPattern stringPattern =
                    new SimpleStringPattern(category, category, _tokenizerConfiguration);
            if (stringPattern.matches(pattern)) {
                final Object value = crosstab.where(patternDimension, category)
                        .where(PatternFinderAnalyzer.DIMENSION_NAME_MEASURES, PatternFinderAnalyzer.MEASURE_MATCH_COUNT)
                        .get();
                if (value instanceof Number) {
                    return ((Number) value).intValue();
                }
            }
        }
        return 0;
    }

    @Metric("Pattern count")
    public int getPatternCount() {
        return getSingleCrosstab().getDimension(PatternFinderAnalyzer.DIMENSION_NAME_PATTERN).getCategoryCount();
    }
}
