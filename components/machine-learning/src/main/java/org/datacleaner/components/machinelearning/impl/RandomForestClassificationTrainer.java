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

import java.util.ArrayList;
import java.util.List;

import org.datacleaner.components.machinelearning.api.MLClassificationMetadata;
import org.datacleaner.components.machinelearning.api.MLClassificationTrainer;
import org.datacleaner.components.machinelearning.api.MLClassificationTrainerCallback;
import org.datacleaner.components.machinelearning.api.MLClassificationRecord;
import org.datacleaner.components.machinelearning.api.MLClassificationTrainingOptions;
import org.datacleaner.components.machinelearning.api.MLClassifier;
import org.datacleaner.components.machinelearning.api.MLFeatureModifier;

import smile.classification.RandomForest;

public class RandomForestClassificationTrainer implements MLClassificationTrainer {

    private final MLClassificationTrainingOptions trainingOptions;

    public RandomForestClassificationTrainer(MLClassificationTrainingOptions trainingOptions) {
        this.trainingOptions = trainingOptions;
    }

    @Override
    public MLClassifier train(Iterable<MLClassificationRecord> data, List<MLFeatureModifier> featureModifiers,
            MLClassificationTrainerCallback callback) {
        final int epochs = trainingOptions.getEpochs();
        final int numTrees = trainingOptions.getLayerSize();

        final List<double[]> trainingInstances = new ArrayList<>();
        final List<Integer> responseVariables = new ArrayList<>();

        final List<Object> classifications = new ArrayList<>();

        for (MLClassificationRecord record : data) {
            final Object classification = record.getClassification();

            int classificationIndex = classifications.indexOf(classification);
            if (classificationIndex == -1) {
                classifications.add(classification);
                classificationIndex = classifications.size() - 1;
            }

            final double[] features = MLFeatureUtils.generateFeatureValues(record, featureModifiers);
            trainingInstances.add(features);
            responseVariables.add(classificationIndex);
        }

        // multiply the results for each epoch
        final double[][] x = new double[trainingInstances.size() * epochs][];
        final int[] y = new int[responseVariables.size() * epochs];
        for (int epoch = 0; epoch < epochs; epoch++) {
            final int offsetX = epoch * trainingInstances.size();
            final int offsetY = epoch * responseVariables.size();
            for (int i = 0; i < trainingInstances.size(); i++) {
                x[i + offsetX] = trainingInstances.get(i);
            }
            for (int i = 0; i < responseVariables.size(); i++) {
                y[i + offsetY] = responseVariables.get(i);
            }
        }

        final RandomForest randomForest = new RandomForest(x, y, numTrees);
        final MLClassificationMetadata classificationMetadata =
                new MLClassificationMetadata(trainingOptions.getClassificationType(), classifications,
                        trainingOptions.getColumnNames(), featureModifiers);
        return new SmileClassifier(randomForest, classificationMetadata);
    }

}
