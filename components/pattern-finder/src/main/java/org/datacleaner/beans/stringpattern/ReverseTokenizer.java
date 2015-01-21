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

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.StringTokenizer;

/**
 * Tokenizer that can be used to "reverse engineer" a pattern string into a
 * proper list of tokens
 */
public class ReverseTokenizer implements Tokenizer {

    private final TokenizerConfiguration _configuration;
    private final String nullTokenString = NullToken.INSTANCE.getString();
    private final String blankTokenString = BlankToken.INSTANCE.getString();

    public ReverseTokenizer(TokenizerConfiguration configuration) {
        _configuration = configuration;
    }

    @Override
    public List<Token> tokenize(final String pattern) {
        if (pattern == null || nullTokenString.equals(pattern)) {
            return Arrays.asList(NullToken.INSTANCE);
        }
        if (blankTokenString.equals(pattern)) {
            return Arrays.asList(BlankToken.INSTANCE);
        }

        DefaultTokenizer delegate = new DefaultTokenizer(_configuration);
        List<Token> tokens = delegate.tokenize(pattern);

        if (_configuration.isTokenTypeEnabled(TokenType.MIXED)) {
            for (ListIterator<Token> it = tokens.listIterator(); it.hasNext();) {
                Token token = (Token) it.next();
                if (token.getType() == TokenType.DELIM) {
                    final String string = token.getString();
                    if (string.indexOf("??") != -1) {
                        // tokenize the string and split DELIM from MIXED tokens
                        final StringTokenizer tokenizer = new StringTokenizer(string, "?", true);
                        it.remove();

                        final StringBuilder tokenStringBuilder = new StringBuilder();
                        while (tokenizer.hasMoreTokens()) {
                            final String tokenString = tokenizer.nextToken();
                            if (tokenString.startsWith("?")) {
                                tokenStringBuilder.append(tokenString);
                            } else {
                                if (tokenStringBuilder.length() > 0) {
                                    it.add(new SimpleToken(TokenType.MIXED, tokenStringBuilder.toString()));
                                    tokenStringBuilder.setLength(0);
                                }
                                it.add(new SimpleToken(TokenType.DELIM, tokenString));
                            }
                        }
                        if (tokenStringBuilder.length() > 0) {
                            it.add(new SimpleToken(TokenType.MIXED, tokenStringBuilder.toString()));
                            tokenStringBuilder.setLength(0);
                        }
                    }
                }
            }
        }

        return tokens;
    }

}
