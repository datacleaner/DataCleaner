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
import java.util.List;

import org.datacleaner.components.machinelearning.api.MLClassificationRecord;
import org.datacleaner.components.machinelearning.api.MLFeatureModifier;

public class MLFeatureUtils {

    /**
     * Ensures that a feature value is in the valid range (from 0 to 1).
     * 
     * @param scaled
     * @return
     */
    public static double ensureFeatureInRange(double value) {
        return Math.max(0d, Math.min(1d, value));
    }

    public static double[] generateFeatureValues(MLClassificationRecord record,
            List<MLFeatureModifier> featureModifiers) {
        final Object[] recordValues = record.getRecordValues();
        assert featureModifiers.size() == recordValues.length;

        final double[] featureValues = new double[getFeatureCount(featureModifiers)];

        int offset = 0;
        for (int i = 0; i < recordValues.length; i++) {
            final Object value = recordValues[i];
            final MLFeatureModifier featureModifier = featureModifiers.get(i);
            final double[] vector = featureModifier.generateFeatureValues(value);
            System.arraycopy(vector, 0, featureValues, offset, vector.length);
            offset += vector.length;
        }
        return featureValues;
    }

    public static int getFeatureCount(Collection<MLFeatureModifier> featureModifiers) {
        return featureModifiers.stream().mapToInt(f -> f.getFeatureCount()).sum();
    }
}
