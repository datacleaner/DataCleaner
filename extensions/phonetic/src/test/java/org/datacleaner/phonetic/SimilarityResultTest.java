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
package org.datacleaner.phonetic;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;


import junit.framework.TestCase;

public class SimilarityResultTest extends TestCase {

	public void testGetValues() throws Exception {
		List<SimilarityGroup> similarValues = new ArrayList<SimilarityGroup>();
		similarValues.add(new SimilarityGroup("hello", "world"));
		similarValues.add(new SimilarityGroup("foo", "bar"));
		similarValues.add(new SimilarityGroup("foo", "foobar"));
		similarValues.add(new SimilarityGroup("foo", "world"));
		SimilarityResult result = new SimilarityResult(similarValues);

		assertEquals("[bar, foo, foobar, hello, world]", new TreeSet<String>(result.getValues()).toString());
	}
}
