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
package org.eobjects.datacleaner.phonetic;

import junit.framework.TestCase;

import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;
import org.eobjects.analyzer.result.SimilarityResult;

public class PhoneticSimilarityFinderTest extends TestCase {

	private MockInputColumn<String> col = new MockInputColumn<String>("foo", String.class);

	public void testGetResult() throws Exception {
		PhoneticSimilarityFinder analyzer = new PhoneticSimilarityFinder(col);

		// 4 similar sounding kasper's
		analyzer.run(new MockInputRow().put(col, "kasper"), 1);
		analyzer.run(new MockInputRow().put(col, "gasper"), 1);
		analyzer.run(new MockInputRow().put(col, "qasper"), 1);
		analyzer.run(new MockInputRow().put(col, "kaspar"), 1);

		// 3 similar sounding hello's
		analyzer.run(new MockInputRow().put(col, "hello"), 1);
		analyzer.run(new MockInputRow().put(col, "hallo"), 1);
		analyzer.run(new MockInputRow().put(col, "hellow"), 1);

		// something without similarities
		analyzer.run(new MockInputRow().put(col, "wowsers"), 1);

		SimilarityResult result = analyzer.getResult();

		assertEquals(3, result.getSimilarValues("kasper").size());
		assertEquals("[gasper, kaspar, qasper]", result.getSimilarValues("kasper").toString());
		assertEquals(3, result.getSimilarValues("gasper").size());
		assertEquals(3, result.getSimilarValues("qasper").size());
		assertEquals(3, result.getSimilarValues("kaspar").size());

		assertEquals("[hallo, hellow]", result.getSimilarValues("hello").toString());
		assertEquals(2, result.getSimilarValues("hallo").size());
		assertEquals(2, result.getSimilarValues("hellow").size());
		assertEquals(0, result.getSimilarValues("wowsers").size());

		assertEquals(2, result.getSimilarityGroups().size());
	}

	public void testDiacritics() throws Exception {
		PhoneticSimilarityFinder analyzer = new PhoneticSimilarityFinder(col);

		// 4 similar sounding kasper's
		analyzer.run(new MockInputRow().put(col, "Lekker"), 1);
		analyzer.run(new MockInputRow().put(col, "Lækker"), 1);
		analyzer.run(new MockInputRow().put(col, "Läkker"), 1);

		SimilarityResult result = analyzer.getResult();
		assertEquals(1, result.getSimilarityGroups().size());
	}
}
