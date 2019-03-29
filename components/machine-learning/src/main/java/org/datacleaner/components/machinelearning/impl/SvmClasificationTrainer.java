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

import smile.classification.SVM;
import smile.classification.SVM.Multiclass;
import smile.math.kernel.GaussianKernel;

public class SvmClasificationTrainer implements MLClassificationTrainer {

    private final MLTrainingOptions trainingOptions;
    private final int epochs;
    private final double softMarginPenalty;
    private final Multiclass multiclass;
    private final double gaussianKernelSigma;

    public SvmClasificationTrainer(MLTrainingOptions trainingOptions, int epochs,
            double gaussianKernelSigma, double softMarginPenalty, Multiclass multiclass) {
        this.trainingOptions = trainingOptions;
        this.epochs = epochs;
        this.gaussianKernelSigma = gaussianKernelSigma;
        this.softMarginPenalty = softMarginPenalty;
        this.multiclass = multiclass;
    }

    @Override
    public MLClassifier train(Iterable<MLClassificationRecord> data, List<MLFeatureModifier> featureModifiers,
            MLTrainerCallback callback) {

        final double[][] x = MLFeatureUtils.toFeatureVector(data, featureModifiers);
        final int[] y = MLFeatureUtils.toClassificationVector(data);
        final List<Object> classifications = MLFeatureUtils.toClassifications(data);

        final GaussianKernel kernel = new GaussianKernel(gaussianKernelSigma);
        final int numClasses = classifications.size();

        final SVM<double[]> svm;
        if (numClasses < 3) {
            svm = new SVM<double[]>(kernel, softMarginPenalty);
        } else {
            svm = new SVM<double[]>(kernel, softMarginPenalty, numClasses, multiclass);
        }
        
        for (int j = 0; j < epochs; j++) {
            svm.learn(x, y);
            callback.epochDone(j + 1, epochs);
        }
        svm.finish();
        svm.trainPlattScaling(x, y);

        final List<String> featureNames = trainingOptions.getColumnNames();
        final MLClassificationMetadata metadata = new MLClassificationMetadata(trainingOptions.getClassificationType(),
                classifications, featureNames, featureModifiers);
        return new SmileClassifier(svm, metadata);
    }

}
