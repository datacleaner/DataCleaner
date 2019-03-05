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
package org.datacleaner.components.machinelearning.impl;

import org.datacleaner.components.convert.ConvertToNumberTransformer;
import org.datacleaner.components.machinelearning.api.MLFeatureModifier;
import org.datacleaner.components.machinelearning.api.MLFeatureModifierBuilder;

public class ScaledMinMaxFeatureModifierBuilder implements MLFeatureModifierBuilder {

    private Double min;
    private Double max;

    @Override
    public void addRecordValue(Object value) {
        final Number n = ConvertToNumberTransformer.transformValue(value);
        if (n != null) {
            synchronized (this) {
                if (min == null) {
                    min = n.doubleValue();
                    max = n.doubleValue();
                } else {
                    if (n.doubleValue() < min.doubleValue()) {
                        min = n.doubleValue();
                    }
                    if (n.doubleValue() > max.doubleValue()) {
                        max = n.doubleValue();
                    }
                }
            }
        }
    }

    @Override
    public MLFeatureModifier build() {
        if (min == null) {
            // we have to set SOME value, so scaling between 0 and 1000
            min = 0d;
            max = 1000d;
        }
        if (min.doubleValue() == max.doubleValue()) {
            // there has to be SOME span of values, so adding/subtracting 100
            min = min - 100d;
            max = max + 100d;
        }
        return new ScaledMinMaxFeatureModifier(min, max);
    }

}
