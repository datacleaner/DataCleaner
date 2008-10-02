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
import junit.framework.TestCase;

public class TokenTest extends TestCase {

	public void testMixed() throws Exception {
		Token token = new Token("hejsa123".toCharArray());
		assertFalse(token.isWord());
		assertFalse(token.isNumber());
		assertFalse(token.isDelimitor());
		assertTrue(token.isMixed());
		assertEquals(Token.TYPE_MIXED, token.getType());
	}

	public void testNumber() throws Exception {
		Token token = new Token("523453".toCharArray());
		assertFalse(token.isWord());
		assertTrue(token.isNumber());
		assertFalse(token.isDelimitor());
		assertFalse(token.isMixed());
		assertEquals(Token.TYPE_NUMBER, token.getType());
	}

	public void testWord() throws Exception {
		Token token = new Token("blabla".toCharArray());
		assertTrue(token.isWord());
		assertFalse(token.isNumber());
		assertFalse(token.isDelimitor());
		assertFalse(token.isMixed());
		assertEquals(Token.TYPE_WORD, token.getType());
	}

	public void testDelimitor() throws Exception {
		Token token = new Token(".-!".toCharArray());
		assertFalse(token.isWord());
		assertFalse(token.isNumber());
		assertTrue(token.isDelimitor());
		assertFalse(token.isMixed());
		assertEquals(Token.TYPE_DELIM, token.getType());
	}

	public void testSingleCharacter() throws Exception {
		Token token = new Token("b".toCharArray());
		assertEquals("b", token.toString());
		assertFalse(token.isDelimitor());
	}

	public void testToRegex() throws Exception {
		assertEquals("[a-zA-Z]{1,8}", new Token("whatever".toCharArray())
				.toRegex());
		assertEquals("[a-zA-Z0-9]{1,9}", new Token("windows95".toCharArray())
				.toRegex());
		assertEquals("[0-9]{1,3}", new Token("123".toCharArray()).toRegex());
		assertEquals("\\Q,-\\E", new Token(",-".toCharArray()).toRegex());
	}
}