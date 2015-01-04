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

public class TokenPatternImplTest extends TestCase {

    private TokenizerConfiguration configuration = new TokenizerConfiguration(true, '.', ',', '-');

    public void testSimpleMatching() throws Exception {
        DefaultTokenizer tokenizer = new DefaultTokenizer(configuration);
        List<Token> tokens;

        tokens = tokenizer.tokenize("hello world");

        TokenPatternImpl tp1 = new TokenPatternImpl("hello world", tokens, configuration);
        assertEquals("aaaaa aaaaa", tp1.toSymbolicString());

        tokens = tokenizer.tokenize("hello pinnochio");
        assertTrue(tp1.match(tokens));
        assertEquals("aaaaa aaaaaaaaa", tp1.toSymbolicString());

        tokens = tokenizer.tokenize("hello you");
        assertTrue(tp1.match(tokens));
        assertEquals("aaaaa aaaaaaaaa", tp1.toSymbolicString());

        tokens = tokenizer.tokenize("hello Mr. FanDango");
        assertFalse(tp1.match(tokens));
        assertEquals("aaaaa aaaaaaaaa", tp1.toSymbolicString());

        configuration.setDiscriminateTextCase(true);
        tokens = tokenizer.tokenize("hello Mr. FanDango");
        TokenPatternImpl tp2 = new TokenPatternImpl("hello Mr. FanDango", tokens, configuration);
        assertEquals("aaaaa Aa. AaaAaaaa", tp2.toSymbolicString());
    }

    public void testBlankPattern() throws Exception {
        DefaultTokenizer tokenizer = new DefaultTokenizer(configuration);
        List<Token> tokens;

        tokens = tokenizer.tokenize("");
        TokenPatternImpl tp1 = new TokenPatternImpl("", tokens, configuration);
        assertEquals("<blank>", tp1.toSymbolicString());
    }

    public void testNullPattern() throws Exception {
        DefaultTokenizer tokenizer = new DefaultTokenizer(configuration);
        List<Token> tokens;

        tokens = tokenizer.tokenize(null);
        TokenPatternImpl tp1 = new TokenPatternImpl(null, tokens, configuration);
        assertEquals("<null>", tp1.toSymbolicString());
    }

    public void testNoneExpandableMatching() throws Exception {

        DefaultTokenizer tokenizer = new DefaultTokenizer(configuration);
        TokenPatternImpl tp;

        // both cases non-expandable
        configuration.setLowerCaseExpandable(false);
        configuration.setUpperCaseExpandable(false);
        tp = new TokenPatternImpl("Hello", tokenizer.tokenize("Hello"), configuration);
        assertTrue(tp.match(tokenizer.tokenize("Wooop")));
        assertFalse(tp.match(tokenizer.tokenize("Greetings")));
        assertFalse(tp.match(tokenizer.tokenize("Hi")));

        // both cases expandable
        configuration.setLowerCaseExpandable(true);
        configuration.setUpperCaseExpandable(true);
        tp = new TokenPatternImpl("Hello", tokenizer.tokenize("Hello"), configuration);
        assertTrue(tp.match(tokenizer.tokenize("Wooop")));
        assertTrue(tp.match(tokenizer.tokenize("Greetings")));
        assertTrue(tp.match(tokenizer.tokenize("Hi")));
        assertTrue(tp.match(tokenizer.tokenize("HHi")));

        // only lower case expandable
        configuration.setLowerCaseExpandable(true);
        configuration.setUpperCaseExpandable(false);
        tp = new TokenPatternImpl("Hello", tokenizer.tokenize("Hello"), configuration);
        assertTrue(tp.match(tokenizer.tokenize("Wooop")));
        assertTrue(tp.match(tokenizer.tokenize("Greetings")));
        assertTrue(tp.match(tokenizer.tokenize("Hi")));
        assertFalse(tp.match(tokenizer.tokenize("HHi")));

        // only upper case expandable
        configuration.setLowerCaseExpandable(false);
        configuration.setUpperCaseExpandable(true);
        tp = new TokenPatternImpl("Hello", tokenizer.tokenize("Hello"), configuration);
        assertTrue(tp.match(tokenizer.tokenize("Wooop")));
        assertFalse(tp.match(tokenizer.tokenize("Greetings")));
        assertFalse(tp.match(tokenizer.tokenize("Hi")));
        assertTrue(tp.match(tokenizer.tokenize("HHiiii")));
    }
}
