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

import java.util.Objects;

import org.datacleaner.components.machinelearning.api.MLFeatureModifierBuilder;
import org.datacleaner.components.machinelearning.api.MLFeatureModifierBuilderFactory;
import org.datacleaner.components.machinelearning.api.MLFeatureModifierType;

public class MLFeatureModifierBuilderFactoryImpl implements MLFeatureModifierBuilderFactory {

    @Override
    public MLFeatureModifierBuilder create(MLFeatureModifierType type) {
        Objects.requireNonNull(type);
        switch (type) {
        case DIRECT_NUMERIC:
            return new DirectNumericFeatureModifierBuilder();
        case DIRECT_BOOL:
            return new DirectBooleanFeatureModifierBuilder();
        case SCALED_MIN_MAX:
            return new ScaledMinMaxFeatureModifierBuilder();
        case VECTOR_ONE_HOT_ENCODING:
            return new VectorOneHotEncodingFeatureModifierBuilder();
        case VECTOR_2_GRAM:
            return new VectorNGramFeatureModifierBuilder(2);
        case VECTOR_3_GRAM:
            return new VectorNGramFeatureModifierBuilder(3);
        case VECTOR_4_GRAM:
            return new VectorNGramFeatureModifierBuilder(4);
        case VECTOR_5_GRAM:
            return new VectorNGramFeatureModifierBuilder(5);
        default:
            throw new UnsupportedOperationException("Unsupported feature modifier type: " + type);
        }
    }

}
