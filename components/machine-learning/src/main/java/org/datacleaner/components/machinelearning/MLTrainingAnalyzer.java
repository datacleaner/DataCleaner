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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.SerializationUtils;
import org.apache.metamodel.util.CollectionUtils;
import org.apache.metamodel.util.HasNameMapper;
import org.datacleaner.api.Analyzer;
import org.datacleaner.api.Categorized;
import org.datacleaner.api.ComponentContext;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.ExecutionLogMessage;
import org.datacleaner.api.FileProperty;
import org.datacleaner.api.FileProperty.FileAccessMode;
import org.datacleaner.api.Initialize;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.MappedProperty;
import org.datacleaner.api.NumberProperty;
import org.datacleaner.api.Provided;
import org.datacleaner.api.Validate;
import org.datacleaner.components.machinelearning.api.MLClassificationRecord;
import org.datacleaner.components.machinelearning.api.MLClassificationTrainer;
import org.datacleaner.components.machinelearning.api.MLClassificationTrainerCallback;
import org.datacleaner.components.machinelearning.api.MLClassificationTrainingOptions;
import org.datacleaner.components.machinelearning.api.MLClassifier;
import org.datacleaner.components.machinelearning.api.MLFeatureModifier;
import org.datacleaner.components.machinelearning.api.MLFeatureModifierBuilder;
import org.datacleaner.components.machinelearning.api.MLFeatureModifierBuilderFactory;
import org.datacleaner.components.machinelearning.api.MLFeatureModifierType;
import org.datacleaner.components.machinelearning.impl.MLClassificationRecordImpl;
import org.datacleaner.components.machinelearning.impl.MLFeatureModifierBuilderFactoryImpl;
import org.datacleaner.components.machinelearning.impl.MLFeatureUtils;
import org.datacleaner.result.Crosstab;
import org.datacleaner.util.Percentage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

@Named("Classifier training")
@Categorized(MachineLearningCategory.class)
public class MLTrainingAnalyzer implements Analyzer<MLAnalyzerResult> {

    public static final String PROPERTY_FEATURE_COLUMNS = "Features";
    public static final String PROPERTY_FEATURE_MODIFIERS = "Feature modifier types";
    
    private static final Logger logger = LoggerFactory.getLogger(MLTrainingAnalyzer.class);
    private static final MLFeatureModifierBuilderFactory featureModifierBuilderFactory =
            new MLFeatureModifierBuilderFactoryImpl();

    @Configured
    InputColumn<?> classification;

    @Configured(PROPERTY_FEATURE_COLUMNS)
    InputColumn<?>[] featureColumns;

    @Configured(PROPERTY_FEATURE_MODIFIERS)
    @MappedProperty(PROPERTY_FEATURE_COLUMNS)
    MLFeatureModifierType[] featureModifierTypes;

    @Configured
    @Description("Determine how much (if any) of the records should be used for cross-validation.")
    @NumberProperty(negative = false)
    Percentage crossValidationSampleRate = new Percentage(10);

    @Configured
    @NumberProperty(negative = false, zero = false)
    int epochs = 10;

    @Configured
    @NumberProperty(negative = false, zero = false)
    int layerSize = 64;

    @Configured
    MLAlgorithm algorithm = MLAlgorithm.RANDOM_FOREST;

    @Configured(required = false)
    @FileProperty(accessMode = FileAccessMode.SAVE, extension = ".model.ser")
    File saveModelToFile = new File("classifier.model.ser");

    @Inject
    @Provided
    ComponentContext componentContext;

    private AtomicInteger recordCounter;
    private Collection<MLClassificationRecord> trainingRecords;
    private Collection<MLClassificationRecord> crossValidationRecords;
    private List<MLFeatureModifierBuilder> featureModifierBuilders;

    @Validate
    public void validate() {
        MLComponentUtils.validateTrainingMapping(featureColumns, featureModifierTypes);
    }

    @Initialize
    public void init() {
        recordCounter = new AtomicInteger();
        trainingRecords = new ConcurrentLinkedQueue<>();
        crossValidationRecords = new ConcurrentLinkedQueue<>();
        featureModifierBuilders = new ArrayList<>(featureModifierTypes.length);

        for (MLFeatureModifierType featureModifierType : featureModifierTypes) {
            final MLFeatureModifierBuilder featureModifierBuilder =
                    featureModifierBuilderFactory.create(featureModifierType);
            featureModifierBuilders.add(featureModifierBuilder);
        }
    }

    @Override
    public void run(InputRow row, int distinctCount) {
        final MLClassificationRecord record =
                MLClassificationRecordImpl.forTraining(row, classification, featureColumns);
        if (record == null) {
            return;
        }

        final Object[] recordValues = record.getRecordValues();
        for (int i = 0; i < recordValues.length; i++) {
            final MLFeatureModifierBuilder featureModifierBuilder = featureModifierBuilders.get(i);
            featureModifierBuilder.addRecordValue(recordValues[i]);
        }

        final int recordNumber = recordCounter.incrementAndGet();
        if (recordNumber % 100 > crossValidationSampleRate.getNominator()) {
            trainingRecords.add(record);
        } else {
            crossValidationRecords.add(record);
        }
    }

    @Override
    public MLAnalyzerResult getResult() {
        final List<MLFeatureModifier> featureModifiers =
                featureModifierBuilders.stream().map(MLFeatureModifierBuilder::build).collect(Collectors.toList());

        final List<String> columnNames = CollectionUtils.map(featureColumns, new HasNameMapper());
        final MLClassificationTrainingOptions options = new MLClassificationTrainingOptions(
                classification.getDataType(), columnNames, featureModifiers, epochs, layerSize);
        final MLClassificationTrainer trainer = algorithm.createTrainer(options);
        final int epochs = options.getEpochs();
        log("Training " + algorithm.getName() + " model starting. Records=" + trainingRecords.size() + ", Columns="
                + columnNames.size() + ", Features=" + MLFeatureUtils.getFeatureCount(featureModifiers) + ", Epochs="
                + epochs + ".");
        final MLClassifier classifier =
                trainer.train(trainingRecords, featureModifiers, new MLClassificationTrainerCallback() {
                    @Override
                    public void epochDone(int epoch) {
                        log("Training " + algorithm.getName() + " progress: Epoch " + epoch + " of " + epochs
                                + " done.");
                    }
                });

        if (saveModelToFile != null) {
            logger.info("Saving model to file: {}", saveModelToFile);
            try {
                final byte[] bytes = SerializationUtils.serialize(classifier);
                Files.write(bytes, saveModelToFile);
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to save model to file: " + saveModelToFile, e);
            }
        }

        log("Trained " + algorithm.getName() + " model. Creating evaluation matrices.");

        final Crosstab<Integer> trainedRecordsConfusionMatrix =
                createConfusionMatrixCrosstab(classifier, trainingRecords);
        final Crosstab<Integer> crossValidationConfusionMatrix =
                createConfusionMatrixCrosstab(classifier, crossValidationRecords);

        return new MLAnalyzerResult(classifier, trainedRecordsConfusionMatrix, crossValidationConfusionMatrix);
    }

    private void log(String string) {
        if (componentContext != null) {
            componentContext.publishMessage(new ExecutionLogMessage(string));
        }
    }

    private static Crosstab<Integer> createConfusionMatrixCrosstab(MLClassifier classifier,
            Collection<MLClassificationRecord> records) {
        final MLConfusionMatrixBuilder builder = new MLConfusionMatrixBuilder(classifier);
        for (MLClassificationRecord record : records) {
            builder.append(record);
        }
        return builder.build();
    }
}
