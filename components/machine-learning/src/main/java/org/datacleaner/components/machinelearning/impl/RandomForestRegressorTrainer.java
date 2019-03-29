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
import org.datacleaner.components.machinelearning.api.MLRegressionMetadata;
import org.datacleaner.components.machinelearning.api.MLRegressionRecord;
import org.datacleaner.components.machinelearning.api.MLRegressor;
import org.datacleaner.components.machinelearning.api.MLRegressorTrainer;
import org.datacleaner.components.machinelearning.api.MLTrainerCallback;
import org.datacleaner.components.machinelearning.api.MLTrainingOptions;

import smile.regression.RandomForest;

public class RandomForestRegressorTrainer implements MLRegressorTrainer {

    private final int numTrees;
    private final MLTrainingOptions trainingOptions;

    public RandomForestRegressorTrainer(MLTrainingOptions trainingOptions, int numTrees) {
        this.trainingOptions = trainingOptions;
        this.numTrees = numTrees;
    }

    @Override
    public MLRegressor train(Iterable<MLRegressionRecord> data, List<MLFeatureModifier> featureModifiers,
            MLTrainerCallback callback) {
        final double[][] x = MLFeatureUtils.toFeatureVector(data, featureModifiers);
        final double[] y = MLFeatureUtils.toRegressionOutputVector(data);
        final RandomForest regression = new RandomForest(x, y, numTrees);

        final MLRegressionMetadata metadata =
                new MLRegressionMetadata(trainingOptions.getColumnNames(), trainingOptions.getFeatureModifiers());
        return new SmileRegressor(metadata, regression);
    }
}
