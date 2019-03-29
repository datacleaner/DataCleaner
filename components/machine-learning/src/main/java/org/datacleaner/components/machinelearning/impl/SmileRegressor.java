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

import java.util.List;

import org.datacleaner.components.machinelearning.api.MLFeatureModifier;
import org.datacleaner.components.machinelearning.api.MLRecord;
import org.datacleaner.components.machinelearning.api.MLRegressionMetadata;
import org.datacleaner.components.machinelearning.api.MLRegressor;

import smile.regression.Regression;

public class SmileRegressor implements MLRegressor {

    private static final long serialVersionUID = 1L;
    
    private final MLRegressionMetadata metadata;
    private final Regression<double[]> regression;
    
    public SmileRegressor(MLRegressionMetadata metadata, Regression<double[]> regression) {
        this.metadata = metadata;
        this.regression = regression;
    }

    @Override
    public MLRegressionMetadata getMetadata() {
        return metadata;
    }

    @Override
    public double predict(MLRecord record) {
        final List<MLFeatureModifier> featureModifiers = getMetadata().getFeatureModifiers();
        final double[] featureValues = MLFeatureUtils.generateFeatureValues(record, featureModifiers);
        return predict(featureValues);
    }

    @Override
    public double predict(double[] featureValues) {
        return regression.predict(featureValues);
    }

}
