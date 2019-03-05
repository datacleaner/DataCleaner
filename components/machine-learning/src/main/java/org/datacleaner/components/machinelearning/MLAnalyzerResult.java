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
package org.datacleaner.components.machinelearning;

import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.components.machinelearning.api.MLClassifier;
import org.datacleaner.result.Crosstab;

public class MLAnalyzerResult implements AnalyzerResult {

    private static final long serialVersionUID = 1L;

    private final MLClassifier trainedClassifier;
    private final Crosstab<Integer> trainedRecordsConfusionMatrix;
    private final Crosstab<Integer> crossValidationConfusionMatrix;

    public MLAnalyzerResult(MLClassifier trainedClassifier, Crosstab<Integer> trainedRecordsConfusionMatrix,
            Crosstab<Integer> crossValidationConfusionMatrix) {
        this.trainedClassifier = trainedClassifier;
        this.trainedRecordsConfusionMatrix = trainedRecordsConfusionMatrix;
        this.crossValidationConfusionMatrix = crossValidationConfusionMatrix;
    }

    public MLClassifier getTrainedClassifier() {
        return trainedClassifier;
    }

    public Crosstab<Integer> getCrossValidationConfusionMatrix() {
        return crossValidationConfusionMatrix;
    }

    public Crosstab<Integer> getTrainedRecordsConfusionMatrix() {
        return trainedRecordsConfusionMatrix;
    }
}
