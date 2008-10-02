/**
 *  This file is part of DataCleaner.
 *
 *  DataCleaner is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DataCleaner is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DataCleaner.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.eobjects.datacleaner.profiler.pattern;

import dk.eobjects.datacleaner.profiler.pattern.Token;
import dk.eobjects.datacleaner.profiler.pattern.WordAndNumberTokenizer;
import dk.eobjects.datacleaner.testware.DataCleanerTestCase;

public class WordAndNumberTokenizerTest extends DataCleanerTestCase {

	public void testTokenizeSingleChar() throws Exception {
		WordAndNumberTokenizer t = new WordAndNumberTokenizer();
		Token[] tokens = t.tokenize("b");
		assertEquals(1, tokens.length);
		assertEquals("b", tokens[0].toString());
	}

	public void testTokenize() throws Exception {
		WordAndNumberTokenizer t = new WordAndNumberTokenizer();
		Token[] tokens = t
				.tokenize("Noerrebrogade 2, 2. th, DK2200 Koebenhavn N");

		assertEquals("Noerrebrogade", tokens[0].toString());
		assertEquals(" ", tokens[1].toString());
		assertEquals("2", tokens[2].toString());
		assertEquals(", ", tokens[3].toString());
		assertEquals("2", tokens[4].toString());
		assertEquals(". ", tokens[5].toString());
		assertEquals("th", tokens[6].toString());
		assertEquals(", ", tokens[7].toString());
		assertEquals("DK2200", tokens[8].toString());
		assertEquals(" ", tokens[9].toString());
		assertEquals("Koebenhavn", tokens[10].toString());
		assertEquals(" ", tokens[11].toString());
		assertEquals("N", tokens[12].toString());

		assertTrue("'" + tokens[0] + "' was not evaluated to be a word!",
				tokens[0].isWord());
		assertTrue("'" + tokens[1] + "' was not evaluated to be a delimitor!",
				tokens[1].isDelimitor());
		assertTrue("'" + tokens[2] + "' was not evaluated to be a number!",
				tokens[2].isNumber());
		assertTrue("'" + tokens[3] + "' was not evaluated to be a delimitor!",
				tokens[3].isDelimitor());
		assertTrue(
				"'" + tokens[8] + "' was not evaluated to be a mixed token!",
				tokens[8].isMixed());
	}
}