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

import org.datacleaner.components.convert.ConvertToNumberTransformer;
import org.datacleaner.components.machinelearning.api.MLFeatureModifier;
import org.datacleaner.components.machinelearning.api.MLFeatureModifierType;

public class DirectNumericFeatureModifier implements MLFeatureModifier {

    private static final long serialVersionUID = 1L;

    @Override
    public double[] generateFeatureValues(Object value) {
        final Number n = ConvertToNumberTransformer.transformValue(value);
        if (n == null) {
            return new double[] { 0 };
        }
        final double inRange = MLFeatureUtils.ensureFeatureInRange(n.doubleValue());
        return new double[] { inRange };
    }

    @Override
    public int getFeatureCount() {
        return 1;
    }

    @Override
    public MLFeatureModifierType getType() {
        return MLFeatureModifierType.DIRECT_NUMERIC;
    }
}
