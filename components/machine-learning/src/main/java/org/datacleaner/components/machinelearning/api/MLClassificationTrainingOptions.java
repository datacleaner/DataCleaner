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
package org.datacleaner.components.machinelearning.api;

import java.io.Serializable;
import java.util.List;

public class MLClassificationTrainingOptions implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<String> columnNames;
    private final Class<?> classificationType;
    private final List<MLFeatureModifier> featureModifiers;

    public MLClassificationTrainingOptions(Class<?> classificationType, List<String> columnNames,
            List<MLFeatureModifier> featureModifiers) {
        this.classificationType = classificationType;
        this.columnNames = columnNames;
        this.featureModifiers = featureModifiers;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public List<MLFeatureModifier> getFeatureModifiers() {
        return featureModifiers;
    }

    public Class<?> getClassificationType() {
        return classificationType;
    }
}
