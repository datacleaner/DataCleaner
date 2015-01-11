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

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Pattern finder to reverse engineer patterns based on a pattern string
 */
public class ReversePatternFinder extends PatternFinder<String> {

    private final HashMap<TokenPattern, AtomicInteger> _patternCounts;
    private final HashMap<TokenPattern, String> _patternSamples;

    public ReversePatternFinder(TokenizerConfiguration configuration) {
        super(new ReverseTokenizer(configuration), configuration);
        _patternCounts = new HashMap<TokenPattern, AtomicInteger>();
        _patternSamples = new HashMap<TokenPattern, String>();
    }

    @Override
    protected void storeNewPattern(TokenPattern pattern, String sample, String originalPattern, int distinctCount) {
        final AtomicInteger counter = new AtomicInteger(distinctCount);
        _patternCounts.put(pattern, counter);
        _patternSamples.put(pattern, sample);
    }

    @Override
    protected void storeMatch(TokenPattern pattern, String sample, String originalPattern, int distinctCount) {
        final AtomicInteger counter = _patternCounts.get(pattern);
        counter.addAndGet(distinctCount);
    }
    
    public HashMap<TokenPattern, AtomicInteger> getPatternCounts() {
        return _patternCounts;
    }
    
    public String getSample(TokenPattern pattern) {
        return _patternSamples.get(pattern);
    }
}
