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
package org.datacleaner.beans.filter;

import org.datacleaner.api.InputColumn;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;
import org.datacleaner.reference.Dictionary;
import org.datacleaner.reference.SimpleDictionary;

import junit.framework.TestCase;

public class DictionaryLookupFilterTest extends TestCase {

	public void testSimpleLookups() throws Exception {
		InputColumn<String> column = new MockInputColumn<String>("col", String.class);
		Dictionary dictionary = new SimpleDictionary("my dictionary", "foo", "bar", "baz");
		
		DictionaryFilter filter = new DictionaryFilter(column, dictionary);
		assertEquals(DictionaryFilter.Category.VALID, filter.categorize(new MockInputRow().put(column, "foo")));
		assertEquals(DictionaryFilter.Category.INVALID, filter.categorize(new MockInputRow().put(column, "foo ")));
		assertEquals(DictionaryFilter.Category.INVALID, filter.categorize(new MockInputRow().put(column, "foo bar")));
		assertEquals(DictionaryFilter.Category.INVALID, filter.categorize(new MockInputRow().put(column, "foobar")));
		assertEquals(DictionaryFilter.Category.VALID, filter.categorize(new MockInputRow().put(column, "bar")));
		assertEquals(DictionaryFilter.Category.VALID, filter.categorize(new MockInputRow().put(column, "baz")));
		assertEquals(DictionaryFilter.Category.INVALID, filter.categorize(new MockInputRow().put(column, null)));
		assertEquals(DictionaryFilter.Category.INVALID, filter.categorize(new MockInputRow().put(column, "")));
	}
}
