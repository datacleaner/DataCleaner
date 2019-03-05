/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Free Software Foundation, Inc.
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
package org.datacleaner.components.machinelearning.impl;

import java.util.Collection;

import org.datacleaner.components.machinelearning.api.MLFeatureModifier;
import org.datacleaner.components.machinelearning.api.MLFeatureModifierType;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;

public class VectorNGramFeatureModifier implements MLFeatureModifier {

    private static final long serialVersionUID = 1L;

    public static Iterable<String> split(Object value) {
        final String str;
        if (value == null) {
            str = "";
        } else {
            str = value.toString().toLowerCase().chars().map(c -> {
                if (Character.isLetter(c)) {
                    return c;
                }
                // replace punctuation and such, leaving only letters and whitespace
                return ' ';
            }).collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
        }
        return Splitter.on(CharMatcher.whitespace()).omitEmptyStrings().split(str);
    }

    private final String[] grams;
    private final int n;

    public VectorNGramFeatureModifier(int n, Collection<String> grams) {
        this.n = n;
        this.grams = grams.toArray(new String[grams.size()]);
    }

    @Override
    public double[] generateFeatureValues(Object value) {
        final double[] result = new double[getFeatureCount()];
        final Iterable<String> parts = split(value);
        for (String part : parts) {
            if (part.length() >= n) {
                for (int i = 0; i < grams.length; i++) {
                    final String gram = grams[i];
                    if (part.contains(gram)) {
                        result[i] = 1;
                    }
                }
            }
        }
        return result;
    }

    @Override
    public int getFeatureCount() {
        return grams.length;
    }

    @Override
    public MLFeatureModifierType getType() {
        return MLFeatureModifierType.getNGramType(n);
    }
}
