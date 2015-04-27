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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/***
 * A string pattern finder. This component can consume rows and produce string
 * patterns. It does not contain the actual logic to store/persist the rows, but
 * has callback methods so that it's rather easy to implement this on your own.
 * 
 * 
 * 
 * @param <R>
 *            the type representing the row. Enables the user of the class to
 *            use his own row type, such as InputRow, String[] or even just
 *            Object.
 */
public abstract class PatternFinder<R> {

    private final ConcurrentHashMap<String, Collection<TokenPattern>> _patterns;
    private final TokenizerConfiguration _configuration;
    private final Tokenizer _tokenizer;

    public PatternFinder(Tokenizer tokenizer, TokenizerConfiguration configuration) {
        _configuration = configuration;
        _tokenizer = tokenizer;
        _patterns = new ConcurrentHashMap<>();
    }

    public PatternFinder(TokenizerConfiguration configuration) {
        this(new DefaultTokenizer(configuration), configuration);
    }

    /**
     * This method should be invoked by the user of the PatternFinder. Invoke it
     * for each value in your dataset. Repeated values are handled correctly but
     * if available it is more effecient to handle only the distinct values and
     * their corresponding distinct counts.
     * 
     * @param row
     *            the row containing the value
     * @param value
     *            the string value to be tokenized and matched against other
     *            patterns
     * @param distinctCount
     *            the count of the value
     */
    public void run(R row, String value, int distinctCount) {
        final List<Token> tokens;
        try {
            tokens = _tokenizer.tokenize(value);
        } catch (RuntimeException e) {
            throw new IllegalStateException("Error occurred while tokenizing value: " + value, e);
        }

        final String patternCode = getPatternCode(tokens);
        final Collection<TokenPattern> patterns = getOrCreatePatterns(patternCode);

        boolean match = false;
        for (TokenPattern pattern : patterns) {
            if (pattern.match(tokens)) {
                storeMatch(pattern, row, value, distinctCount);
                match = true;
                break;
            }
        }

        if (!match) {
            final TokenPattern pattern;
            try {
                pattern = new TokenPatternImpl(value, tokens, _configuration);
            } catch (RuntimeException e) {
                throw new IllegalStateException("Error occurred while creating pattern for: " + tokens, e);
            }

            storeNewPattern(pattern, row, value, distinctCount);
            patterns.add(pattern);
        }
    }

    private Collection<TokenPattern> getOrCreatePatterns(String patternCode) {
        final Collection<TokenPattern> newPatterns = new LinkedBlockingQueue<>();
        final Collection<TokenPattern> existingPatterns = _patterns.putIfAbsent(patternCode, newPatterns);
        if (existingPatterns == null) {
            return newPatterns;
        }
        return existingPatterns;
    }

    /**
     * Creates an almost unique String code for a list of tokens. This code is
     * used to improve search time when looking for potential matching patterns.
     * 
     * @param tokens
     * @return
     */
    private String getPatternCode(List<Token> tokens) {
        final StringBuilder sb = new StringBuilder();
        sb.append(tokens.size());
        for (Token token : tokens) {
            sb.append(token.getType().ordinal());
        }
        return sb.toString();
    }

    public Collection<TokenPattern> getPatterns() {
        final Set<TokenPattern> result = new HashSet<TokenPattern>();
        final Collection<Collection<TokenPattern>> values = _patterns.values();
        for (Collection<TokenPattern> set : values) {
            result.addAll(set);
        }
        return result;
    }

    /**
     * This method is invoked every time a new pattern is created (ie. when a
     * match could not be found in the existing patterns).
     * 
     * @param pattern
     *            the newly produced pattern
     * @param row
     *            the row that was handed to the run(...) method
     * @param value
     *            the value that was handed to the run(...) method
     * @param distinctCount
     *            the distinctCount that was handed to the run(...) method
     */
    protected abstract void storeNewPattern(TokenPattern pattern, R row, String value, int distinctCount);

    /**
     * This method is invoked every time a tokenized value matches an existing
     * pattern. All existing patterns will previously have been created using
     * the storeNewPattern(...) method.
     * 
     * @param pattern
     *            the existing pattern
     * @param row
     *            the row that was handed to the run(...) method
     * @param value
     *            the value that was handed to the run(...) method
     * @param distinctCount
     *            the distinctCount that was handed to the run(...) method
     */
    protected abstract void storeMatch(TokenPattern pattern, R row, String value, int distinctCount);
}
