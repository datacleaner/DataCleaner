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
package org.datacleaner.components.machinelearning.api;

import java.util.ArrayList;
import java.util.List;

import org.apache.metamodel.util.HasName;
import org.datacleaner.util.ReflectionUtils;

/**
 * Represents the types of functions can be applied to columns for transforming them into features. The functions are
 * applied to the values of the raw data and transforms it to a set of numeric features.
 */
public enum MLFeatureModifierType implements HasName {

    SCALED_MIN_MAX("Scaled (Min-Max)", Number.class),
    
    DIRECT_NUMERIC("Direct (0.0 to 1.0)", Number.class),

    DIRECT_BOOL("Direct (1 or 0)", Boolean.class),

    VECTOR_ONE_HOT_ENCODING("Vector (One Hot Encoding)", String.class),

    VECTOR_2_GRAM("Vector (2-gram)", String.class),

    VECTOR_3_GRAM("Vector (3-gram)", String.class),

    VECTOR_4_GRAM("Vector (4-gram)", String.class),

    VECTOR_5_GRAM("Vector (5-gram)", String.class),

    ;

    private final Class<?> applicableDataType;
    private final String name;

    private MLFeatureModifierType(final String name, final Class<?> applicableDataType) {
        this.name = name;
        this.applicableDataType = applicableDataType;
    }

    public Class<?> getApplicableDataType() {
        return applicableDataType;
    }

    @Override
    public String getName() {
        return name;
    }

    public static MLFeatureModifierType getNGramType(int n) {
        switch (n) {
        case 2:
            return MLFeatureModifierType.VECTOR_2_GRAM;
        case 3:
            return MLFeatureModifierType.VECTOR_3_GRAM;
        case 4:
            return MLFeatureModifierType.VECTOR_4_GRAM;
        case 5:
            return MLFeatureModifierType.VECTOR_5_GRAM;
        }
        throw new UnsupportedOperationException("No n-gram vector defined for n=" + n);
    }

    public static List<MLFeatureModifierType> getApplicableValues(Class<?> dataType) {
        final List<MLFeatureModifierType> result = new ArrayList<>();
        for (MLFeatureModifierType featureModifierType : values()) {
            if (ReflectionUtils.is(dataType, featureModifierType.getApplicableDataType())) {
                result.add(featureModifierType);
            }
        }
        return result;
    }
}
