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
package org.datacleaner.components.machinelearning;

import java.util.List;

import org.datacleaner.api.InputColumn;
import org.datacleaner.components.machinelearning.api.MLClassifier;
import org.datacleaner.components.machinelearning.api.MLFeatureModifier;
import org.datacleaner.components.machinelearning.api.MLFeatureModifierType;
import org.datacleaner.util.ReflectionUtils;

public class MLComponentUtils {

    public static void validateClassifierMapping(MLClassifier classifier, InputColumn<?>[] featureColumns) {
        final int modelFeatures = classifier.getMetadata().getColumnCount();
        if (featureColumns.length > modelFeatures) {
            throw new IllegalArgumentException("Model defines " + modelFeatures + " features, but too few ("
                    + featureColumns.length + ") are configured.");
        }
        if (featureColumns.length < modelFeatures) {
            throw new IllegalArgumentException("Model defines " + modelFeatures + " features, but too many ("
                    + featureColumns.length + ") are configured.");
        }

        final List<MLFeatureModifier> featureModifiers = classifier.getMetadata().getFeatureModifiers();
        for (int i = 0; i < featureColumns.length; i++) {
            final InputColumn<?> column = featureColumns[i];
            final MLFeatureModifier featureModifier = featureModifiers.get(i);
            validateColumnFeatureModifierMapping(i, column, featureModifier);
        }
    }

    public static void validateTrainingMapping(InputColumn<?>[] featureColumns,
            MLFeatureModifierType[] featureModifierTypes) {
        for (int i = 0; i < featureColumns.length; i++) {
            final InputColumn<?> column = featureColumns[i];
            final MLFeatureModifierType featureModifierType = featureModifierTypes[i];
            validateColumnFeatureModifierMapping(i, column, featureModifierType);
            final Class<?> dataType = column.getDataType();
            final Class<?> applicableDataType = featureModifierType.getApplicableDataType();
            if (!ReflectionUtils.is(dataType, applicableDataType)) {
                throw new IllegalArgumentException("Feature '" + featureModifierType.getName() + "' is used with '"
                        + column.getName() + "', but requires a " + applicableDataType.getSimpleName() + " data type.");
            }
        }
    }

    private static void validateColumnFeatureModifierMapping(int index, InputColumn<?> column,
            MLFeatureModifier featureModifier) {
        final MLFeatureModifierType featureModifierType = featureModifier.getType();
        validateColumnFeatureModifierMapping(index, column, featureModifierType);
    }

    private static void validateColumnFeatureModifierMapping(int index, InputColumn<?> column,
            MLFeatureModifierType featureModifierType) {
        final Class<?> applicableDataType = featureModifierType.getApplicableDataType();
        if (!ReflectionUtils.is(column.getDataType(), applicableDataType)) {
            throw new IllegalArgumentException("Column " + (index + 1) + " (" + column.getName() + ") uses '"
                    + featureModifierType + "' which expects data type " + applicableDataType.getSimpleName() + ".");
        }
    }
}
