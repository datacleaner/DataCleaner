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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.result.AnalyzerResultReducer;
import org.eobjects.analyzer.result.Crosstab;
import org.eobjects.analyzer.result.CrosstabDimension;
import org.eobjects.analyzer.result.CrosstabNavigator;

/**
 * Result reducer for {@link PatternFinderResult}s
 */
public class PatternFinderResultReducer implements AnalyzerResultReducer<PatternFinderResult> {

    @Override
    public PatternFinderResult reduce(Collection<? extends PatternFinderResult> results) {
        final PatternFinderResult firstResult = results.iterator().next();
        final InputColumn<String> column = firstResult.getColumn();
        final TokenizerConfiguration tokenizerConfiguration = firstResult.getTokenizerConfiguration();
        if (!firstResult.isGroupingEnabled()) {
            // a single list of patterns

            final List<Crosstab<?>> crosstabs = new ArrayList<Crosstab<?>>(results.size());
            for (PatternFinderResult result : results) {
                Crosstab<?> crosstab = result.getSingleCrosstab();
                crosstabs.add(crosstab);
            }

            final Crosstab<?> crosstab = reduce(crosstabs, tokenizerConfiguration);

            return new PatternFinderResult(column, crosstab, tokenizerConfiguration);
        } else {
            // groups of lists of patterns

            final Map<String, List<Crosstab<?>>> groupedCrosstabs = new HashMap<String, List<Crosstab<?>>>();
            for (PatternFinderResult result : results) {
                final Set<Entry<String, Crosstab<?>>> entries = result.getGroupedCrosstabs().entrySet();
                for (Entry<String, Crosstab<?>> entry : entries) {
                    final String group = entry.getKey();
                    List<Crosstab<?>> crosstabsInGroup = groupedCrosstabs.get(group);
                    if (crosstabsInGroup == null) {
                        crosstabsInGroup = new ArrayList<Crosstab<?>>();
                        groupedCrosstabs.put(group, crosstabsInGroup);
                    }
                    crosstabsInGroup.add(entry.getValue());
                }
            }

            final Map<String, Crosstab<?>> crosstabs = new TreeMap<String, Crosstab<?>>();
            final Set<Entry<String, List<Crosstab<?>>>> entries = groupedCrosstabs.entrySet();
            for (Entry<String, List<Crosstab<?>>> entry : entries) {
                final String group = entry.getKey();
                final List<Crosstab<?>> crosstabInGroup = entry.getValue();
                final Crosstab<?> crosstab = reduce(crosstabInGroup, tokenizerConfiguration);
                crosstabs.put(group, crosstab);
            }

            final InputColumn<String> groupColumn = firstResult.getGroupColumn();

            return new PatternFinderResult(column, groupColumn, crosstabs, tokenizerConfiguration);
        }
    }

    private Crosstab<?> reduce(List<Crosstab<?>> crosstabs, TokenizerConfiguration tokenizerConfiguration) {
        if (crosstabs.size() == 1) {
            return crosstabs.get(0);
        }

        final ReversePatternFinder patternFinder = new ReversePatternFinder(tokenizerConfiguration);

        for (Crosstab<?> crosstab : crosstabs) {
            final CrosstabDimension patternDimension = crosstab
                    .getDimension(PatternFinderAnalyzer.DIMENSION_NAME_PATTERN);
            final List<String> patterns = patternDimension.getCategories();

            for (String pattern : patterns) {
                final CrosstabNavigator<?> navigator = crosstab.where(PatternFinderAnalyzer.DIMENSION_NAME_PATTERN,
                        pattern);
                final Number matchCount = (Number) navigator.where(PatternFinderAnalyzer.DIMENSION_NAME_MEASURES,
                        PatternFinderAnalyzer.MEASURE_MATCH_COUNT).get();
                final String sample = (String) navigator.where(PatternFinderAnalyzer.DIMENSION_NAME_MEASURES,
                        PatternFinderAnalyzer.MEASURE_SAMPLE).get();

                patternFinder.run(sample, pattern, matchCount.intValue());
            }
        }

        final Set<Entry<TokenPattern, AtomicInteger>> entries = patternFinder.getPatternCounts().entrySet();
        // sort the entries so that the ones with the highest amount of
        // matches are at the top
        final Set<Entry<TokenPattern, AtomicInteger>> sortedEntrySet = new TreeSet<Entry<TokenPattern, AtomicInteger>>(
                new Comparator<Entry<TokenPattern, AtomicInteger>>() {
                    public int compare(Entry<TokenPattern, AtomicInteger> o1, Entry<TokenPattern, AtomicInteger> o2) {
                        int result = o2.getValue().get() - o1.getValue().get();
                        if (result == 0) {
                            result = o1.getKey().toSymbolicString().compareTo(o2.getKey().toSymbolicString());
                        }
                        return result;
                    }
                });
        sortedEntrySet.addAll(entries);

        final Crosstab<Serializable> crosstab = PatternFinderAnalyzer.createCrosstab();
        for (Entry<TokenPattern, AtomicInteger> entry : sortedEntrySet) {

            final CrosstabNavigator<Serializable> nav = crosstab.navigate();
            final TokenPattern pattern = entry.getKey();
            nav.where(PatternFinderAnalyzer.DIMENSION_NAME_PATTERN, pattern.toSymbolicString());

            nav.where(PatternFinderAnalyzer.DIMENSION_NAME_MEASURES, PatternFinderAnalyzer.MEASURE_MATCH_COUNT);
            final AtomicInteger count = entry.getValue();
            nav.put(count, true);
            
            nav.where(PatternFinderAnalyzer.DIMENSION_NAME_MEASURES, PatternFinderAnalyzer.MEASURE_SAMPLE);
            final String sample = patternFinder.getSample(pattern);
            nav.put(sample, true);
        }

        return crosstab;
    }

}
