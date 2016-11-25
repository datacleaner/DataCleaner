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
package org.datacleaner.result;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import junit.framework.TestCase;

public class AbstractValueCountingAnalyzerResultTest extends TestCase {

    private Collection<ValueFrequency> list;

    @Override
    protected void setUp() throws Exception {
        list = new ArrayList<>();
        list.add(new SingleValueFrequency("foo", 10));
        list.add(new SingleValueFrequency("bar", 10));
        list.add(new SingleValueFrequency("baz", 10));
        list.add(new SingleValueFrequency("buh", 20));
        list.add(new SingleValueFrequency("bah", 30));
        list.add(new SingleValueFrequency("hey", 40));
        list.add(new SingleValueFrequency("yo", 40));
    }

    public void testGetReducedValueFrequenciesVanilla() throws Exception {
        final AbstractValueCountingAnalyzerResult analyzerResult = new MockValueCountingAnalyzerResult(list);

        final Collection<ValueFrequency> reduced = analyzerResult.getReducedValueFrequencies(10);
        assertEquals(list.size(), reduced.size());
        assertEquals("[[hey->40], [yo->40], [bah->30], [buh->20], [bar->10], [baz->10], [foo->10]]",
                reduced.toString());
    }

    public void testGetReducedValueFrequenciesExpandUniques() throws Exception {
        final Collection<String> values =
                Arrays.asList("this is a long test split into many individual word tokens".split(" "));
        list.add(new CompositeValueFrequency("<uniques>", values, 1));

        final AbstractValueCountingAnalyzerResult analyzerResult = new MockValueCountingAnalyzerResult(list);

        final Collection<ValueFrequency> reduced = analyzerResult.getReducedValueFrequencies(40);
        assertEquals("[[hey->40], [yo->40], [bah->30], [buh->20], [bar->10], [baz->10], [foo->10], "
                + "[a->1], [individual->1], [into->1], [is->1], [long->1], [many->1], [split->1], "
                + "[test->1], [this->1], [tokens->1], [word->1]]", reduced.toString());
    }

}
