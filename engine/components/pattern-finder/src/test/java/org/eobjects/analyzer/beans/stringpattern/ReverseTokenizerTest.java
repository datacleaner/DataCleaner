/**
 * AnalyzerBeans
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
package org.eobjects.analyzer.beans.stringpattern;

import java.util.List;

import junit.framework.TestCase;

public class ReverseTokenizerTest extends TestCase {

    public void testTextTokens() throws Exception {
        ReverseTokenizer tokenizer = new ReverseTokenizer(new TokenizerConfiguration(true));
        List<Token> tokens = tokenizer.tokenize("AAaaaaaA");
        assertEquals(3, tokens.size());
        assertEquals("[Token['AA' (TEXT)], Token['aaaaa' (TEXT)], Token['A' (TEXT)]]", tokens.toString());
    }

    public void testEntirelyMixedTokens() throws Exception {
        ReverseTokenizer tokenizer = new ReverseTokenizer(new TokenizerConfiguration(true));
        List<Token> tokens = tokenizer.tokenize("????");
        assertEquals(1, tokens.size());
        assertEquals("[Token['????' (MIXED)]]", tokens.toString());
    }

    public void testMixedAndDelimTokens() throws Exception {
        ReverseTokenizer tokenizer = new ReverseTokenizer(new TokenizerConfiguration(true));
        List<Token> tokens = tokenizer.tokenize("hello there __????!");
        assertEquals(7, tokens.size());
        assertEquals("[Token['hello' (TEXT)], Token[' ' (WHITESPACE)], Token['there' (TEXT)], "
                + "Token[' ' (WHITESPACE)], Token['__' (DELIM)], Token['????' (MIXED)], Token['!' (DELIM)]]", tokens.toString());
    }
}
