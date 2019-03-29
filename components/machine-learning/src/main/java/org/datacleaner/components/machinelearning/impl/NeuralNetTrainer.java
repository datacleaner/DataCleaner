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

import smile.classification.NeuralNetwork;
import smile.classification.NeuralNetwork.ActivationFunction;
import smile.classification.NeuralNetwork.ErrorFunction;

public class NeuralNetTrainer implements MLClassificationTrainer {

    private final MLTrainingOptions trainingOptions;
    private final int epochs;
    private final ErrorFunction errorFunction;
    private final ActivationFunction activationFunction;
    private final int[] hiddenNeuronPerLayer;
    private final double learningRate;
    private final double momentum;

    public NeuralNetTrainer(MLTrainingOptions trainingOptions, int epochs, ErrorFunction errorFunction,
            ActivationFunction activationFunction, int[] hiddenNeuronPerLayer, double learningRate, double momentum) {
        this.trainingOptions = trainingOptions;
        this.epochs = epochs;
        this.errorFunction = errorFunction;
        this.activationFunction = activationFunction;
        this.hiddenNeuronPerLayer = hiddenNeuronPerLayer;
        this.learningRate = learningRate;
        this.momentum = momentum;
    }

    @Override
    public MLClassifier train(Iterable<MLClassificationRecord> data, List<MLFeatureModifier> featureModifiers,
            MLTrainerCallback callback) {
        final List<Object> classifications = MLFeatureUtils.toClassifications(data);
        final double[][] x = MLFeatureUtils.toFeatureVector(data, featureModifiers);
        final int[] y = MLFeatureUtils.toClassificationVector(data);

        final int[] unitsPerLayer = new int[hiddenNeuronPerLayer.length + 2];
        // input layer = feature values
        unitsPerLayer[0] = MLFeatureUtils.getFeatureCount(featureModifiers);
        // hidden layers
        for (int i = 0; i < unitsPerLayer.length - 2; i++) {
            unitsPerLayer[i + 1] = hiddenNeuronPerLayer[i];
        }
        // output layer = classifications
        unitsPerLayer[unitsPerLayer.length - 1] = classifications.size();

        NeuralNetwork net = new NeuralNetwork(errorFunction, activationFunction, unitsPerLayer);
        net.setLearningRate(learningRate);
        net.setMomentum(momentum);

        for (int i = 0; i < epochs; i++) {
            net.learn(x, y);
            callback.epochDone(i + 1, epochs);
        }

        final MLClassificationMetadata classificationMetadata =
                new MLClassificationMetadata(trainingOptions.getClassificationType(), classifications,
                        trainingOptions.getColumnNames(), featureModifiers);
        return new SmileClassifier(net, classificationMetadata);
    }

}
