/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.util;

import junit.framework.TestCase;

import org.eobjects.analyzer.util.HasAliases;
import org.eobjects.metamodel.util.HasName;

public class DefaultEnumMatcherTest extends TestCase {

    public static enum TestEnum implements HasName, HasAliases {
        FOO, BAR, BAZ;

        @Override
        public String getName() {
            return "name_" + name();
        }

        @Override
        public String[] getAliases() {
            if (this == BAR) {
                return new String[] { "brrr", "barbara" };
            }
            if (this == FOO) {
                return new String[] { "fu", "fee2", "ber2", "ber3" };
            }
            return null;
        }
    }

    private final EnumMatcher<TestEnum> matcher = new DefaultEnumMatcher<TestEnum>(TestEnum.class);

    public void testSuggestByAlias() throws Exception {
        assertEquals(TestEnum.FOO, matcher.suggestMatch("fu"));
        assertEquals(TestEnum.FOO, matcher.suggestMatch(" FU*"));

        assertEquals(TestEnum.BAR, matcher.suggestMatch("Barbara"));
    }

    public void testSuggestByName() throws Exception {
        assertEquals(TestEnum.FOO, matcher.suggestMatch("nameFoo"));
        assertEquals(TestEnum.BAR, matcher.suggestMatch("nameBAR"));
    }

    public void testSuggestByConstantName() throws Exception {
        assertEquals(TestEnum.FOO, matcher.suggestMatch("foo"));
        assertEquals(TestEnum.BAR, matcher.suggestMatch("BAR"));
        assertEquals(TestEnum.BAZ, matcher.suggestMatch("Baz"));
    }

    public void testSuggestIgnoringNumbers() throws Exception {
        assertEquals(TestEnum.FOO, matcher.suggestMatch("fee"));
        assertEquals(TestEnum.FOO, matcher.suggestMatch("ber"));
        assertEquals(TestEnum.FOO, matcher.suggestMatch("ber3"));
        assertEquals(null, matcher.suggestMatch("bor"));
    }

    public void testDontSuggest() throws Exception {
        assertEquals(null, matcher.suggestMatch("fubiebar"));
    }

}
