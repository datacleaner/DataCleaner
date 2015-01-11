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
package org.datacleaner.beans.stringpattern;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import junit.framework.TestCase;

public class PredefinedTokenTokenizerTest extends TestCase {

	public void testOverlappingPatterns() throws Exception {
		PredefinedTokenDefinition pt = new PredefinedTokenDefinition("greeting", "hello .*", "hi .*");

		Set<Pattern> patterns = pt.getTokenRegexPatterns();
		assertEquals(2, patterns.size());
		for (Pattern pattern : patterns) {
			// both patterns can find a match here
			assertTrue(pattern.matcher("hello hi there").find());
		}

		List<Token> tokens = new PredefinedTokenTokenizer(pt).tokenize("hello hi there");
		assertEquals(2, tokens.size());
		assertEquals("UndefinedToken['hello ']", tokens.get(0).toString());
		assertEquals("Token['hi there' (PREDEFINED greeting)]", tokens.get(1).toString());
	}

	public void testTitulation() throws Exception {
		PredefinedTokenDefinition pt = new PredefinedTokenDefinition("titulation", "(Mr|Ms)\\.");
		List<Token> tokens;

		tokens = PredefinedTokenTokenizer.tokenizeInternal("Mr. Kasper", pt, pt.getTokenRegexPatterns().iterator().next());
		assertEquals(2, tokens.size());
		assertEquals("Token['Mr.' (PREDEFINED titulation)]", tokens.get(0).toString());
		assertEquals("UndefinedToken[' Kasper']", tokens.get(1).toString());

		tokens = PredefinedTokenTokenizer.tokenizeInternal("Dear Mr. Winfried", pt, pt.getTokenRegexPatterns().iterator()
				.next());
		assertEquals(3, tokens.size());
		assertEquals("UndefinedToken['Dear ']", tokens.get(0).toString());
		assertEquals("Token['Mr.' (PREDEFINED titulation)]", tokens.get(1).toString());
		assertEquals("UndefinedToken[' Winfried']", tokens.get(2).toString());

		tokens = PredefinedTokenTokenizer.tokenizeInternal("Dear Ms. Barbara", pt, pt.getTokenRegexPatterns().iterator()
				.next());
		assertEquals(3, tokens.size());
		assertEquals("UndefinedToken['Dear ']", tokens.get(0).toString());
		assertEquals("Token['Ms.' (PREDEFINED titulation)]", tokens.get(1).toString());
		assertEquals("UndefinedToken[' Barbara']", tokens.get(2).toString());
	}

	public void testTokenizeInternal() throws Exception {
		PredefinedTokenDefinition pt = new PredefinedTokenDefinition("greeting", "hello");
		List<Token> tokens = PredefinedTokenTokenizer.tokenizeInternal("hello there hello world", pt, pt
				.getTokenRegexPatterns().iterator().next());
		assertEquals(4, tokens.size());

		assertEquals("Token['hello' (PREDEFINED greeting)]", tokens.get(0).toString());
		assertEquals("UndefinedToken[' there ']", tokens.get(1).toString());
		assertEquals("Token['hello' (PREDEFINED greeting)]", tokens.get(2).toString());
		assertEquals("UndefinedToken[' world']", tokens.get(3).toString());

		tokens = PredefinedTokenTokenizer.tokenizeInternal("world hello", pt, pt.getTokenRegexPatterns().iterator().next());
		assertEquals(2, tokens.size());
		assertEquals("UndefinedToken['world ']", tokens.get(0).toString());
		assertEquals("Token['hello' (PREDEFINED greeting)]", tokens.get(1).toString());
	}

	public void testSimpleTokenSeparation() throws Exception {
		PredefinedTokenDefinition pt = new PredefinedTokenDefinition("greeting", "hi", "hello", "howdy");

		PredefinedTokenTokenizer tokenizer = new PredefinedTokenTokenizer(pt);

		List<Token> tokens = tokenizer.tokenize("Well hello there world");
		assertEquals(3, tokens.size());

		assertEquals("UndefinedToken['Well ']", tokens.get(0).toString());
		assertEquals("Token['hello' (PREDEFINED greeting)]", tokens.get(1).toString());
		assertEquals("UndefinedToken[' there world']", tokens.get(2).toString());

		tokens = tokenizer.tokenize("howdy Well hello there hi world hi");
		assertEquals(7, tokens.size());
		assertEquals("Token['howdy' (PREDEFINED greeting)]", tokens.get(0).toString());
		assertEquals("Token['hello' (PREDEFINED greeting)]", tokens.get(2).toString());
		assertEquals("Token['hi' (PREDEFINED greeting)]", tokens.get(4).toString());
		assertEquals("UndefinedToken[' world ']", tokens.get(5).toString());
		assertEquals("Token['hi' (PREDEFINED greeting)]", tokens.get(6).toString());
	}
}
