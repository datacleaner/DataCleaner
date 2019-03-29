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

import org.datacleaner.components.machinelearning.api.MLClassificationMetadata;
import org.datacleaner.components.machinelearning.api.MLClassificationRecord;
import org.datacleaner.components.machinelearning.api.MLClassificationTrainer;
import org.datacleaner.components.machinelearning.api.MLTrainerCallback;
import org.datacleaner.components.machinelearning.api.MLTrainingOptions;
import org.datacleaner.components.machinelearning.api.MLClassifier;
import org.datacleaner.components.machinelearning.api.MLFeatureModifier;

import smile.classification.RandomForest;

public class RandomForestClassificationTrainer implements MLClassificationTrainer {

    private final MLTrainingOptions trainingOptions;
    private final int numTrees;

    public RandomForestClassificationTrainer(MLTrainingOptions trainingOptions, int numTrees) {
        this.trainingOptions = trainingOptions;
        this.numTrees = numTrees;
    }

    @Override
    public MLClassifier train(Iterable<MLClassificationRecord> data, List<MLFeatureModifier> featureModifiers,
            MLTrainerCallback callback) {
        final List<Object> classifications = MLFeatureUtils.toClassifications(data);
        final double[][] x = MLFeatureUtils.toFeatureVector(data, featureModifiers);
        final int[] y = MLFeatureUtils.toClassificationVector(data);

        final RandomForest randomForest = new RandomForest(x, y, numTrees);
        final MLClassificationMetadata classificationMetadata =
                new MLClassificationMetadata(trainingOptions.getClassificationType(), classifications,
                        trainingOptions.getColumnNames(), featureModifiers);
        return new SmileClassifier(randomForest, classificationMetadata);
    }

}
