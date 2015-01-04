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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;
import org.eobjects.analyzer.result.renderer.CrosstabTextRenderer;
import org.eobjects.analyzer.storage.InMemoryRowAnnotationFactory;
import org.apache.metamodel.util.Action;

import junit.framework.TestCase;

public class PatternFinderResultReducerTest extends TestCase {

    final PatternFinderResultReducer reducer = new PatternFinderResultReducer();

    public void testBasicScenario() throws Exception {
        List<PatternFinderResult> results = new ArrayList<PatternFinderResult>();
        results.add(createResult1());
        results.add(createResult2());
        results.add(createResult3());

        PatternFinderResult finalResult = reducer.reduce(results);

        assertFalse(finalResult.isGroupingEnabled());

        String text = new CrosstabTextRenderer().render(finalResult.getSingleCrosstab());
        String[] lines = text.split("\n");
        
        assertEquals("                  Match count Sample      ", lines[0]);
        assertEquals("Aaa Aaa                     4 Foo Bar     ", lines[1]);
        assertEquals("Aaaaa aaaaa                 3 Foo bar     ", lines[2]);
        assertEquals("Aaaaaa                      2 Foobar      ", lines[3]);
        assertEquals("Aaa AAA AAA                 1 Foo BAR BAZ ", lines[4]);
        assertEquals("Aaa aaa aaa                 1 Foo bar baz ", lines[5]);
        assertEquals("Aaaaa AAAAA AAAAA           1 Hello THERE WORLD ", lines[6]);

        assertEquals(7,lines.length);

        Collection<String> suggestions = finalResult.getMatchCount().getParameterSuggestions();
        assertEquals("[Aaa Aaa, Aaaaa aaaaa, Aaaaaa, Aaa AAA AAA, Aaa aaa aaa, Aaaaa AAAAA AAAAA]",
                suggestions.toString());
    }

    private PatternFinderResult createResult(MockInputColumn<String> col, Action<PatternFinderAnalyzer> action)
            throws Exception {
        PatternFinderAnalyzer analyzer = new PatternFinderAnalyzer();
        analyzer._rowAnnotationFactory = new InMemoryRowAnnotationFactory();
        analyzer.column = col;
        analyzer.init();

        action.run(analyzer);

        return analyzer.getResult();
    }

    private PatternFinderResult createResult1() throws Exception {
        final MockInputColumn<String> col = new MockInputColumn<String>("column");
        return createResult(col, new Action<PatternFinderAnalyzer>() {

            @Override
            public void run(PatternFinderAnalyzer analyzer) throws Exception {
                analyzer.run(new MockInputRow().put(col, "Foo Bar"), 1);
                analyzer.run(new MockInputRow().put(col, "Foo bar baz"), 1);
                analyzer.run(new MockInputRow().put(col, "Foo bar"), 1);
                analyzer.run(new MockInputRow().put(col, "Foo Baz"), 2);
                analyzer.run(new MockInputRow().put(col, "Foo foo"), 1);
                analyzer.run(new MockInputRow().put(col, "Foo BAR BAZ"), 1);
            }
        });
    }

    private PatternFinderResult createResult2() throws Exception {
        final MockInputColumn<String> col = new MockInputColumn<String>("column");
        return createResult(col, new Action<PatternFinderAnalyzer>() {
            @Override
            public void run(PatternFinderAnalyzer analyzer) throws Exception {
                analyzer.run(new MockInputRow().put(col, "Foobar"), 1);
                analyzer.run(new MockInputRow().put(col, "Foo Bar"), 1);
            }
        });
    }

    private PatternFinderResult createResult3() throws Exception {
        final MockInputColumn<String> col = new MockInputColumn<String>("column");
        return createResult(col, new Action<PatternFinderAnalyzer>() {
            @Override
            public void run(PatternFinderAnalyzer analyzer) throws Exception {
                analyzer.run(new MockInputRow().put(col, "Hello world"), 1);
                analyzer.run(new MockInputRow().put(col, "Hello THERE WORLD"), 1);
                analyzer.run(new MockInputRow().put(col, "Hello"), 1);
            }
        });
    }
}
