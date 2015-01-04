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
package org.eobjects.analyzer.beans.stringpattern;

import org.eobjects.analyzer.beans.api.ParameterizableMetric;
import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.result.Crosstab;

import junit.framework.TestCase;

public class PatternFinderResultTest extends TestCase {

    public void testGetMatchCountMetric() throws Exception {
        String dimPattern = PatternFinderAnalyzer.DIMENSION_NAME_PATTERN;
        String dimMeasure = PatternFinderAnalyzer.DIMENSION_NAME_MEASURES;
        Crosstab<Number> crosstab = new Crosstab<Number>(Number.class, dimPattern, dimMeasure);
        crosstab.where(dimPattern, "aaaa").where(dimMeasure, PatternFinderAnalyzer.MEASURE_MATCH_COUNT).put(3, true);
        crosstab.where(dimPattern, "Aaaaa").where(dimMeasure, PatternFinderAnalyzer.MEASURE_MATCH_COUNT).put(2, true);
        crosstab.where(dimPattern, "9999").where(dimMeasure, PatternFinderAnalyzer.MEASURE_MATCH_COUNT).put(1, true);

        TokenizerConfiguration tokenizerConfiguration = new TokenizerConfiguration();
        PatternFinderResult patternFinderResult = new PatternFinderResult(new MockInputColumn<String>("foo"), crosstab,
                tokenizerConfiguration);

        ParameterizableMetric matchCount = patternFinderResult.getMatchCount();
        assertEquals("[aaaa, Aaaaa, 9999]", matchCount.getParameterSuggestions().toString());

        // exact matches
        assertEquals(3, matchCount.getValue("aaaa").intValue());
        assertEquals(2, matchCount.getValue("Aaaaa").intValue());
        assertEquals(1, matchCount.getValue("9999").intValue());

        // expanded matches
        assertEquals(3, matchCount.getValue("aaa").intValue());
        assertEquals(2, matchCount.getValue("Aaaaaaaaaa").intValue());
        assertEquals(1, matchCount.getValue("9").intValue());

        // non matches
        assertEquals(0, matchCount.getValue("AAaaaa").intValue());

        // IN expressions
        assertEquals(5, matchCount.getValue("IN [aaa,Aaa]").intValue());
        assertEquals(3, matchCount.getValue("IN [9999,Aaa]").intValue());
        assertEquals(1, matchCount.getValue("NOT IN [aaa,Aaa]").intValue());
    }
}
