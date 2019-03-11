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

import java.util.Set;

import org.datacleaner.components.machinelearning.api.MLFeatureModifier;
import org.datacleaner.components.machinelearning.api.MLFeatureModifierBuilder;
import org.datacleaner.components.machinelearning.api.MLTrainingOptions;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

public class VectorNGramFeatureModifierBuilder implements MLFeatureModifierBuilder {

    private final int gramLength;
    private final Multiset<String> grams;
    private final MLTrainingOptions options;
    
    public VectorNGramFeatureModifierBuilder(int gramLength) {
        this(new MLTrainingOptions(-1, true), gramLength);
    }

    public VectorNGramFeatureModifierBuilder(MLTrainingOptions options, int gramLength) {
        this.gramLength = gramLength;
        this.options = options;
        this.grams = HashMultiset.create();
    }

    @Override
    public void addRecordValue(Object value) {
        final Iterable<String> parts = VectorNGramFeatureModifier.split(value);
        for (String part : parts) {
            for (int index = 0; index + gramLength <= part.length(); index++) {
                final String gram = part.substring(index, index + gramLength);
                synchronized (this) {
                    grams.add(gram);
                }
            }
        }
    }

    @Override
    public MLFeatureModifier build() {
        return new VectorNGramFeatureModifier(gramLength, getGrams());
    }

    protected Set<String> getGrams() {
        final Set<String> resultSet = MLFeatureUtils.sanitizeFeatureVectorSet(grams, options);
        return resultSet;
    }
}
