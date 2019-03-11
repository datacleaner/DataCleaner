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

public class VectorOneHotEncodingFeatureModifierBuilder implements MLFeatureModifierBuilder {

    private final Multiset<String> values;
    private final MLTrainingOptions options;

    /**
     * Creates as {@link VectorOneHotEncodingFeatureModifierBuilder} with limitless features.
     */
    public VectorOneHotEncodingFeatureModifierBuilder() {
        this(-1, true);
    }

    public VectorOneHotEncodingFeatureModifierBuilder(MLTrainingOptions options) {
        this.values = HashMultiset.create();
        this.options = options;
    }

    /**
     * Creates as {@link VectorOneHotEncodingFeatureModifierBuilder} with optional limits on the features created.
     * 
     * @param maxFeatures the max number of features to generate. Use -1 for no limits.
     * @param includeFeaturesForUniqueValues whether or not to generate features for values that occur just once
     */
    public VectorOneHotEncodingFeatureModifierBuilder(int maxFeatures, boolean includeFeaturesForUniqueValues) {
        this(new MLTrainingOptions(maxFeatures, includeFeaturesForUniqueValues));
    }

    @Override
    public void addRecordValue(Object value) {
        final String v = VectorOneHotEncodingFeatureModifier.normalize(value);
        synchronized (this) {
            values.add(v);
        }
    }

    @Override
    public MLFeatureModifier build() {
        final Set<String> resultSet = MLFeatureUtils.sanitizeFeatureVectorSet(values, options);
        return new VectorOneHotEncodingFeatureModifier(resultSet);
    }

}
