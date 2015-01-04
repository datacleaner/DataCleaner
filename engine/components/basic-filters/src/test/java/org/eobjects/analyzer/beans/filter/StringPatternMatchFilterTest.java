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
package org.eobjects.analyzer.beans.filter;

import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;
import org.eobjects.analyzer.reference.RegexStringPattern;
import org.eobjects.analyzer.reference.StringPattern;

import junit.framework.TestCase;

public class StringPatternMatchFilterTest extends TestCase {

	public void testFilterSinglePattern() throws Exception {
		StringPattern stringPattern = new RegexStringPattern("very simple email pattern", ".+@.+", true);
		MockInputColumn<String> column = new MockInputColumn<String>("my col", String.class);
		StringPatternFilter filter = new StringPatternFilter(column, new StringPattern[] { stringPattern },
				MatchFilterCriteria.ANY);

		assertEquals(ValidationCategory.VALID, filter.categorize(new MockInputRow().put(column, "kasper@eobjects.org")));
		assertEquals(ValidationCategory.INVALID, filter.categorize(new MockInputRow().put(column, "kasper@")));

		// it shouldn't matter if ANY or ALL criteria is being used
		filter = new StringPatternFilter(column, new StringPattern[] { stringPattern }, MatchFilterCriteria.ALL);

		assertEquals(ValidationCategory.VALID, filter.categorize(new MockInputRow().put(column, "kasper@eobjects.org")));
		assertEquals(ValidationCategory.INVALID, filter.categorize(new MockInputRow().put(column, "kasper@")));
	}

	public void testFilterMultiplePatterns() throws Exception {
		StringPattern stringPattern1 = new RegexStringPattern("very simple email pattern", ".+@.+", true);
		StringPattern stringPattern2 = new RegexStringPattern("something with 'kas'", ".*kas.*", true);
		MockInputColumn<String> column = new MockInputColumn<String>("my col", String.class);
		StringPatternFilter filter = new StringPatternFilter(column, new StringPattern[] { stringPattern1,
				stringPattern2 }, MatchFilterCriteria.ANY);

		assertEquals(ValidationCategory.VALID, filter.categorize(new MockInputRow().put(column, "kasper@eobjects.org")));
		assertEquals(ValidationCategory.VALID, filter.categorize(new MockInputRow().put(column, "kasper@")));
		assertEquals(ValidationCategory.INVALID, filter.categorize(new MockInputRow().put(column, "ankit@")));

		filter = new StringPatternFilter(column, new StringPattern[] { stringPattern1, stringPattern2 },
				MatchFilterCriteria.ALL);
		assertEquals(ValidationCategory.VALID, filter.categorize(new MockInputRow().put(column, "kasper@eobjects.org")));
		assertEquals(ValidationCategory.INVALID, filter.categorize(new MockInputRow().put(column, "kasper@")));
		assertEquals(ValidationCategory.INVALID, filter.categorize(new MockInputRow().put(column, "ankit@")));
	}
}
