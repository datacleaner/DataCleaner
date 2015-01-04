/**
 * AnalyzerBeans
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
package org.eobjects.analyzer.beans.stringpattern;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eobjects.analyzer.beans.api.Distributed;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.reference.SimpleStringPattern;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.Crosstab;
import org.eobjects.analyzer.result.CrosstabDimension;
import org.eobjects.analyzer.result.Metric;
import org.eobjects.analyzer.result.QueryParameterizableMetric;

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

    public PatternFinderResult(InputColumn<String> column, Crosstab<?> crosstab,
            TokenizerConfiguration tokenizerConfiguration) {
        _column = column;
        _groupColumn = null;
        _crosstabs = new HashMap<String, Crosstab<?>>();
        _crosstabs.put(null, crosstab);
        _tokenizerConfiguration = tokenizerConfiguration;
    }

    public PatternFinderResult(InputColumn<String> column, InputColumn<String> groupColumn,
            Map<String, Crosstab<?>> crosstabs, TokenizerConfiguration tokenizerConfiguration) {
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
            protected int getInstanceCount(String instance) {
                return getMatchCount(instance);
            }

            @Override
            protected int getTotalCount() {
                return PatternFinderResult.this.getTotalCount();
            }

            @Override
            public Collection<String> getParameterSuggestions() {
                Crosstab<?> crosstab = getSingleCrosstab();
                CrosstabDimension patternDimension = crosstab
                        .getDimension(PatternFinderAnalyzer.DIMENSION_NAME_PATTERN);
                List<String> categories = patternDimension.getCategories();
                return categories;
            }
        };
    }

    private int getTotalCount() {
        int sum = 0;

        Crosstab<?> crosstab = getSingleCrosstab();
        CrosstabDimension patternDimension = crosstab.getDimension(PatternFinderAnalyzer.DIMENSION_NAME_PATTERN);
        List<String> categories = patternDimension.getCategories();
        for (String category : categories) {
            Object value = crosstab.where(patternDimension, category)
                    .where(PatternFinderAnalyzer.DIMENSION_NAME_MEASURES, PatternFinderAnalyzer.MEASURE_MATCH_COUNT)
                    .get();
            if (value instanceof Number) {
                sum += ((Number) value).intValue();
            }
        }
        return sum;
    }

    public int getMatchCount(String pattern) {
        Crosstab<?> crosstab = getSingleCrosstab();
        CrosstabDimension patternDimension = crosstab.getDimension(PatternFinderAnalyzer.DIMENSION_NAME_PATTERN);
        List<String> categories = patternDimension.getCategories();
        for (String category : categories) {
            SimpleStringPattern stringPattern = new SimpleStringPattern(category, category, _tokenizerConfiguration);
            if (stringPattern.matches(pattern)) {
                Object value = crosstab
                        .where(patternDimension, category)
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
