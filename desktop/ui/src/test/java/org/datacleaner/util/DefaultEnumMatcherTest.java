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
package org.datacleaner.util;

import junit.framework.TestCase;

import org.apache.metamodel.util.HasName;
import org.datacleaner.descriptors.EnumerationValue;
import org.datacleaner.metadata.ColumnMeaning;

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
                return new String[] { "fu", "fee2", "ber2", "ber3", "data cleaner" };
            }
            return null;
        }
    }

    private final DefaultEnumMatcher matcher = new DefaultEnumMatcher(EnumerationValue.providerFromEnumClass(TestEnum.class));

    public void testSuggestByAlias() throws Exception {
        assertEquals(TestEnum.FOO, matcher.suggestMatch("fu").asJavaEnum());
        assertEquals(TestEnum.FOO, matcher.suggestMatch(" FU*").asJavaEnum());

        assertEquals(TestEnum.BAR, matcher.suggestMatch("Barbara").asJavaEnum());
    }

    public void testSuggestByName() throws Exception {
        assertEquals(TestEnum.FOO, matcher.suggestMatch("nameFoo").asJavaEnum());
        assertEquals(TestEnum.BAR, matcher.suggestMatch("nameBAR").asJavaEnum());
    }

    public void testSuggestByConstantName() throws Exception {
        assertEquals(TestEnum.FOO, matcher.suggestMatch("foo").asJavaEnum());
        assertEquals(TestEnum.BAR, matcher.suggestMatch("BAR").asJavaEnum());
        assertEquals(TestEnum.BAZ, matcher.suggestMatch("Baz").asJavaEnum());
    }

    public void testSuggestIgnoringNumbers() throws Exception {
        assertEquals(TestEnum.FOO, matcher.suggestMatch("fee").asJavaEnum());
        assertEquals(TestEnum.FOO, matcher.suggestMatch("ber").asJavaEnum());
        assertEquals(TestEnum.FOO, matcher.suggestMatch("ber3").asJavaEnum());
        assertEquals(null, matcher.suggestMatch("bor"));
    }

    public void testSuggestBasedOnSecondaryMatch() throws Exception {
        assertEquals(TestEnum.FOO, matcher.suggestMatch("data_cleaner").asJavaEnum());
        assertEquals(TestEnum.FOO, matcher.suggestMatch("data1234cleaner").asJavaEnum());
    }

    public void testDontSuggest() throws Exception {
        assertEquals(null, matcher.suggestMatch("fubiebar"));
    }

    public void testDontMatchTooEager() throws Exception {
        final DefaultEnumMatcher matcher = new DefaultEnumMatcher(EnumerationValue.providerFromEnumClass(ColumnMeaning.class));

        assertEquals(ColumnMeaning.PHONE_PHONENUMBER, matcher.suggestMatch("Phone no").asJavaEnum());
        assertEquals(ColumnMeaning.PHONE_PHONENUMBER, matcher.suggestMatch("Phone ID").asJavaEnum());

        assertEquals(null, matcher.suggestMatch("Customer"));

        assertEquals(ColumnMeaning.KEY_PRIMARY, matcher.suggestMatch("Order ID").asJavaEnum());
        assertEquals(ColumnMeaning.PRODUCT_CODE, matcher.suggestMatch("Product ID").asJavaEnum());
        assertEquals(ColumnMeaning.KEY_PRIMARY, matcher.suggestMatch("Person ID").asJavaEnum());
        assertEquals(ColumnMeaning.KEY_PRIMARY, matcher.suggestMatch("Customer ID").asJavaEnum());
    }
}
