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

import java.io.File;
import java.io.IOException;

import javax.inject.Named;

import org.apache.commons.lang.SerializationUtils;
import org.datacleaner.api.Analyzer;
import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.FileProperty;
import org.datacleaner.api.Initialize;
import org.datacleaner.api.FileProperty.FileAccessMode;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.Validate;
import org.datacleaner.components.machinelearning.api.MLClassificationRecord;
import org.datacleaner.components.machinelearning.api.MLClassifier;
import org.datacleaner.components.machinelearning.impl.MLClassificationRecordImpl;
import org.datacleaner.result.Crosstab;

import com.google.common.io.Files;

@Named("Classifier cross-evaluation")
@Categorized(MachineLearningCategory.class)
public class MLEvaluationAnalyzer implements Analyzer<MLAnalyzerResult> {

    @Configured
    InputColumn<?> classification;

    @Configured
    InputColumn<?>[] features;

    @Configured
    @FileProperty(accessMode = FileAccessMode.OPEN, extension = ".model.ser")
    File modelFile = new File("classifier.model.ser");

    private MLClassifier classifier;
    private MLConfusionMatrixBuilder confusionMatrixBuilder;

    @Validate
    public void validate() throws IOException {
        if (!modelFile.exists()) {
            throw new IllegalArgumentException("Model file '" + modelFile + "' does not exist.");
        }
        classifier = (MLClassifier) SerializationUtils.deserialize(Files.toByteArray(modelFile));

        MLComponentUtils.validateClassifierMapping(classifier, features);
    }

    @Initialize
    public void init() throws IOException {
        classifier = (MLClassifier) SerializationUtils.deserialize(Files.toByteArray(modelFile));
        confusionMatrixBuilder = new MLConfusionMatrixBuilder(classifier);
    }

    @Override
    public void run(InputRow row, int distinctCount) {
        final MLClassificationRecord record = MLClassificationRecordImpl.forTraining(row, classification, features);
        if (record == null) {
            return;
        }

        confusionMatrixBuilder.append(record);
    }

    @Override
    public MLAnalyzerResult getResult() {
        final Crosstab<Integer> crosstab = confusionMatrixBuilder.build();
        return new MLAnalyzerResult(null, null, crosstab);
    }
}
