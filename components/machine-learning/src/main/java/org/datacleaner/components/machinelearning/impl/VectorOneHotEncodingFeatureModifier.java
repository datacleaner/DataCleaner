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
import java.util.Map;

import org.datacleaner.components.machinelearning.api.MLFeatureModifier;
import org.datacleaner.components.machinelearning.api.MLFeatureModifierType;

import com.google.common.collect.Maps;

public class VectorOneHotEncodingFeatureModifier implements MLFeatureModifier {

    private static final long serialVersionUID = 1L;

    public static String normalize(Object value) {
        if (value == null) {
            return "";
        }
        return value.toString().trim().toLowerCase();
    }

    private final Map<String, Integer> values;

    public VectorOneHotEncodingFeatureModifier(Collection<String> values) {
        this.values = Maps.newHashMapWithExpectedSize(values.size());
        int index = 0;
        for (String value : values) {
            this.values.put(value, index);
            index++;
        }
    }

    @Override
    public double[] generateFeatureValues(Object value) {
        final double[] result = new double[getFeatureCount()];
        final String v = normalize(value);
        final Integer index = values.get(v);
        if (index != null) {
            result[index] = 1;
        }
        return result;
    }

    @Override
    public int getFeatureCount() {
        return values.size();
    }

    @Override
    public MLFeatureModifierType getType() {
        return MLFeatureModifierType.VECTOR_ONE_HOT_ENCODING;
    }
}
