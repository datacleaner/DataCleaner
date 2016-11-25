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
package org.datacleaner.beans.uniqueness;

import java.io.File;
import java.util.Map;

import org.apache.metamodel.util.FileHelper;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;
import org.junit.Test;

import com.google.common.base.Splitter;

import junit.framework.TestCase;

public class UniqueKeyCheckAnalyzerTest extends TestCase {

    @Test
    public void testSimpleScenario() throws Exception {
        final int bufferSizeInTest = 20;

        final MockInputColumn<String> col = new MockInputColumn<>("foo");
        final UniqueKeyCheckAnalyzer analyzer = new UniqueKeyCheckAnalyzer(bufferSizeInTest);
        analyzer.column = col;

        analyzer.init();

        analyzer.run(new MockInputRow().put(col, "foo"), 1);
        analyzer.run(new MockInputRow().put(col, "bar"), 1);

        final Splitter splitter = Splitter.on(' ').omitEmptyStrings();
        final Iterable<String> it =
                splitter.split(FileHelper.readFileAsString(new File("src/test/resources/loremipsum.txt")));
        for (final String str : it) {
            analyzer.run(new MockInputRow().put(col, str), 1);
        }

        analyzer.run(new MockInputRow().put(col, "foo"), 1);
        analyzer.run(new MockInputRow().put(col, "bar"), 1);

        final UniqueKeyCheckAnalyzerResult result = analyzer.getResult();
        assertEquals(73, result.getRowCount());
        assertEquals(60, result.getUniqueCount());
        assertEquals(0, result.getNullCount());
        assertEquals(13, result.getNonUniqueCount());

        final Map<String, Integer> samples = result.getNonUniqueSamples();
        assertEquals("{bar=2, dolor=2, dolore=2, foo=2, in=3, ut=2}", samples.toString());

        assertEquals(
                "Unique key check result:\n" + " - Row count: 73\n" + " - Null count: 0\n" + " - Unique count: 60\n"
                        + " - Non-unique count: 13", result.toString());
    }
}
