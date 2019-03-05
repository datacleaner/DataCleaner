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
import java.io.UncheckedIOException;
import java.util.List;

import javax.inject.Named;

import org.apache.commons.lang.SerializationUtils;
import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.FileProperty;
import org.datacleaner.api.FileProperty.FileAccessMode;
import org.datacleaner.api.Initialize;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.Transformer;
import org.datacleaner.api.Validate;
import org.datacleaner.components.machinelearning.api.MLClassification;
import org.datacleaner.components.machinelearning.api.MLClassificationRecord;
import org.datacleaner.components.machinelearning.api.MLClassifier;
import org.datacleaner.components.machinelearning.impl.MLClassificationRecordImpl;

import com.google.common.io.Files;

@Named("Classifier transformer")
@Categorized(MachineLearningCategory.class)
public class MLClassificationTransformer implements Transformer {

    public static enum OutputFormat {
        WINNER_CLASS_AND_CONFIDENCE,
        CONFIDENCE_MATRIX
    }

    @Configured
    InputColumn<?>[] features;

    @Configured
    @FileProperty(accessMode = FileAccessMode.OPEN, extension = ".model.ser")
    File modelFile = new File("classifier.model.ser");

    @Configured
    OutputFormat outputFormat = OutputFormat.WINNER_CLASS_AND_CONFIDENCE;

    private MLClassifier classifier;

    @Validate
    public void validate() throws IOException {
        if (!modelFile.exists()) {
            throw new IllegalArgumentException("Model file '" + modelFile + "' does not exist.");
        }
        classifier = (MLClassifier) SerializationUtils.deserialize(Files.toByteArray(modelFile));

        MLComponentUtils.validateClassifierMapping(classifier, features);
    }

    @Initialize
    public void init() {
        try {
            final byte[] bytes = Files.toByteArray(modelFile);
            classifier = (MLClassifier) SerializationUtils.deserialize(bytes);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public OutputColumns getOutputColumns() {
        if (classifier == null) {
            init();
        }

        if (outputFormat == OutputFormat.WINNER_CLASS_AND_CONFIDENCE) {
            String modelName = modelFile.getName();
            if (modelName.toLowerCase().endsWith(".model.ser")) {
                modelName = modelName.substring(0, modelName.length() - ".model.ser".length());
            }
            final String[] columnNames = new String[] { modelName + " class", modelName + " confidence" };
            final Class<?>[] columnTypes =
                    new Class[] { classifier.getMetadata().getClassificationType(), Double.class };
            return new OutputColumns(columnNames, columnTypes);
        } else {
            final List<Object> classifications = classifier.getMetadata().getClassifications();
            final String[] columnNames = new String[classifications.size()];
            for (int i = 0; i < columnNames.length; i++) {
                columnNames[i] = classifications.toString() + " confidence";
            }
            return new OutputColumns(Double.class, columnNames);
        }
    }

    @Override
    public Object[] transform(InputRow inputRow) {
        final MLClassificationRecord record = MLClassificationRecordImpl.forEvaluation(inputRow, features);
        final MLClassification classification = classifier.classify(record);
        final int bestClassificationIndex = classification.getBestClassificationIndex();
        final double confidence = classification.getConfidence(bestClassificationIndex);
        final Object classificationValue = classifier.getMetadata().getClassification(bestClassificationIndex);
        return new Object[] { classificationValue, confidence };
    }
}
