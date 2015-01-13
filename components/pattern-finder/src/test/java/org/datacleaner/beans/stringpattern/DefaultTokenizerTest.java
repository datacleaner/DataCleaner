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

import junit.framework.TestCase;

public class DefaultTokenizerTest extends TestCase {

	private TokenizerConfiguration conf = new TokenizerConfiguration(false, '.', ',', '-');

	public void testTokenizeEmptyString() throws Exception {
		List<SimpleToken> tokens = DefaultTokenizer.preliminaryTokenize("", conf);
		assertTrue(tokens.isEmpty());
	}

	public void testMinusSignAsDelimOrAsMinus() throws Exception {
		List<SimpleToken> tokens = DefaultTokenizer.preliminaryTokenize("123-456", conf);
		assertEquals(3, tokens.size());
		assertEquals("Token['123' (NUMBER)]", tokens.get(0).toString());
		assertEquals("Token['-' (DELIM)]", tokens.get(1).toString());
		assertEquals("Token['456' (NUMBER)]", tokens.get(2).toString());
	}

	public void testPreliminaryTokenizeAndMixedTokens() throws Exception {
		List<SimpleToken> tokens = DefaultTokenizer.preliminaryTokenize("hi \t123there - yay -10", conf);
		assertEquals(10, tokens.size());
		assertEquals("Token['hi' (TEXT)]", tokens.get(0).toString());
		assertEquals("Token[' \t' (WHITESPACE)]", tokens.get(1).toString());
		assertEquals("Token['123' (NUMBER)]", tokens.get(2).toString());
		assertEquals("Token['there' (TEXT)]", tokens.get(3).toString());
		assertEquals("Token[' ' (WHITESPACE)]", tokens.get(4).toString());
		assertEquals("Token['-' (DELIM)]", tokens.get(5).toString());
		assertEquals("Token[' ' (WHITESPACE)]", tokens.get(6).toString());
		assertEquals("Token['yay' (TEXT)]", tokens.get(7).toString());
		assertEquals("Token[' ' (WHITESPACE)]", tokens.get(8).toString());
		assertEquals("Token['-10' (NUMBER)]", tokens.get(9).toString());

		tokens = DefaultTokenizer.flattenMixedTokens(tokens);
		assertEquals(9, tokens.size());
		assertEquals("Token['123there' (MIXED)]", tokens.get(2).toString());

		tokens = DefaultTokenizer.preliminaryTokenize("w00p", conf);
		assertEquals(3, tokens.size());
		assertEquals("Token['w' (TEXT)]", tokens.get(0).toString());
		assertEquals("Token['00' (NUMBER)]", tokens.get(1).toString());
		assertEquals("Token['p' (TEXT)]", tokens.get(2).toString());

		tokens = DefaultTokenizer.flattenMixedTokens(tokens);
		assertEquals(1, tokens.size());
		assertEquals("Token['w00p' (MIXED)]", tokens.get(0).toString());
	}

	public void testNegativeNumbers() throws Exception {
		List<SimpleToken> tokens = DefaultTokenizer.preliminaryTokenize("10 -4", conf);
		assertEquals(3, tokens.size());
		assertEquals("Token['10' (NUMBER)]", tokens.get(0).toString());
		assertEquals("Token[' ' (WHITESPACE)]", tokens.get(1).toString());
		assertEquals("Token['-4' (NUMBER)]", tokens.get(2).toString());
	}

	public void testDecimals() throws Exception {
		List<SimpleToken> tokens = DefaultTokenizer.preliminaryTokenize("yay 10.1 whut 20,632. hmm", conf);
		assertEquals(10, tokens.size());
		assertEquals("Token['yay' (TEXT)]", tokens.get(0).toString());
		assertEquals("Token[' ' (WHITESPACE)]", tokens.get(1).toString());
		assertEquals("Token['10.1' (NUMBER)]", tokens.get(2).toString());
		assertEquals("Token[' ' (WHITESPACE)]", tokens.get(3).toString());
		assertEquals("Token['whut' (TEXT)]", tokens.get(4).toString());
		assertEquals("Token[' ' (WHITESPACE)]", tokens.get(5).toString());
		assertEquals("Token['20,632' (NUMBER)]", tokens.get(6).toString());
		assertEquals("Token['.' (DELIM)]", tokens.get(7).toString());
		assertEquals("Token[' ' (WHITESPACE)]", tokens.get(8).toString());
		assertEquals("Token['hmm' (TEXT)]", tokens.get(9).toString());

		tokens = DefaultTokenizer.preliminaryTokenize("20,632.20213", conf);
		assertEquals(1, tokens.size());
		assertEquals("Token['20,632.20213' (NUMBER)]", tokens.get(0).toString());

		tokens = DefaultTokenizer.preliminaryTokenize("20,632,123.20213", conf);
		assertEquals(1, tokens.size());
		assertEquals(TokenType.NUMBER, tokens.get(0).getType());

		tokens = DefaultTokenizer.preliminaryTokenize("20,632,.20213", conf);
		assertEquals(3, tokens.size());
		assertEquals("Token['20,632' (NUMBER)]", tokens.get(0).toString());
		assertEquals("Token[',.' (DELIM)]", tokens.get(1).toString());
		assertEquals("Token['20213' (NUMBER)]", tokens.get(2).toString());

		tokens = DefaultTokenizer.preliminaryTokenize(",-20,632.20213", conf);
		assertEquals(2, tokens.size());
		assertEquals("Token[',' (DELIM)]", tokens.get(0).toString());
		assertEquals("Token['-20,632.20213' (NUMBER)]", tokens.get(1).toString());

		tokens = DefaultTokenizer.preliminaryTokenize("20,632.20213,", conf);
		assertEquals(2, tokens.size());
		assertEquals("Token['20,632.20213' (NUMBER)]", tokens.get(0).toString());
		assertEquals("Token[',' (DELIM)]", tokens.get(1).toString());

		tokens = DefaultTokenizer.preliminaryTokenize("20,632-20213,", conf);
		assertEquals(4, tokens.size());
		assertEquals("Token['20,632' (NUMBER)]", tokens.get(0).toString());
		assertEquals("Token['-' (DELIM)]", tokens.get(1).toString());
		assertEquals("Token['20213' (NUMBER)]", tokens.get(2).toString());
		assertEquals("Token[',' (DELIM)]", tokens.get(3).toString());
	}

	public void testNumberParsingWithoutSeparatorChars() throws Exception {
		TokenizerConfiguration c = new TokenizerConfiguration(false, null, null, null);
		List<SimpleToken> tokens = DefaultTokenizer.preliminaryTokenize("20,-632.20213", c);
		assertEquals(5, tokens.size());
		assertEquals("Token['20' (NUMBER)]", tokens.get(0).toString());
		assertEquals("Token[',-' (DELIM)]", tokens.get(1).toString());
		assertEquals("Token['632' (NUMBER)]", tokens.get(2).toString());
		assertEquals("Token['.' (DELIM)]", tokens.get(3).toString());
		assertEquals("Token['20213' (NUMBER)]", tokens.get(4).toString());
	}
}
