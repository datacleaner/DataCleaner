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

import smile.classification.SVM;
import smile.math.kernel.GaussianKernel;

public class SvmClasificationTrainer implements MLClassificationTrainer {

    private final MLClassificationTrainingOptions trainingOptions;

    public SvmClasificationTrainer(MLClassificationTrainingOptions trainingOptions) {
        this.trainingOptions = trainingOptions;
    }

    @Override
    public MLClassifier train(Iterable<MLClassificationRecord> data, List<MLFeatureModifier> featureModifiers,
            MLClassificationTrainerCallback callback) {
        final int epochs = trainingOptions.getEpochs();

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

        final int numClasses = classifications.size();
        // TODO: Consider hyper parameters
        final SVM<double[]> svm =
                new SVM<double[]>(new GaussianKernel(8.0), 5.0, numClasses, SVM.Multiclass.ONE_VS_ONE);
        final double[][] x = trainingInstances.toArray(new double[trainingInstances.size()][]);
        final int[] y = responseVariables.stream().mapToInt(i -> i).toArray();

        for (int j = 0; j < epochs; j++) {
            svm.learn(x, y);
            callback.epochDone(j + 1);
        }
        svm.finish();
        svm.trainPlattScaling(x, y);

        final List<String> featureNames = trainingOptions.getColumnNames();
        final MLClassificationMetadata metadata = new MLClassificationMetadata(trainingOptions.getClassificationType(),
                classifications, featureNames, featureModifiers);
        return new SmileClassifier(svm, metadata);
    }

}
