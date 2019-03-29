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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang.SerializationUtils;
import org.apache.metamodel.util.CollectionUtils;
import org.apache.metamodel.util.HasNameMapper;
import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.Initialize;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.NumberProperty;
import org.datacleaner.components.machinelearning.api.MLFeatureModifier;
import org.datacleaner.components.machinelearning.api.MLFeatureModifierBuilder;
import org.datacleaner.components.machinelearning.api.MLFeatureModifierBuilderFactory;
import org.datacleaner.components.machinelearning.api.MLFeatureModifierType;
import org.datacleaner.components.machinelearning.api.MLRegressionRecord;
import org.datacleaner.components.machinelearning.api.MLRegressor;
import org.datacleaner.components.machinelearning.api.MLRegressorTrainer;
import org.datacleaner.components.machinelearning.api.MLTrainerCallback;
import org.datacleaner.components.machinelearning.api.MLTrainingConstraints;
import org.datacleaner.components.machinelearning.api.MLTrainingOptions;
import org.datacleaner.components.machinelearning.impl.MLFeatureModifierBuilderFactoryImpl;
import org.datacleaner.components.machinelearning.impl.MLFeatureUtils;
import org.datacleaner.components.machinelearning.impl.MLRegressionRecordImpl;
import org.datacleaner.util.Percentage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

@Categorized(MachineLearningCategory.class)
public abstract class MLRegressionTrainingAnalyzer extends MLTrainingAnalyzer<MLRegressionAnalyzerResult> {

    private static final Logger logger = LoggerFactory.getLogger(MLRegressionTrainingAnalyzer.class);
    private static final MLFeatureModifierBuilderFactory featureModifierBuilderFactory =
            new MLFeatureModifierBuilderFactoryImpl();

    @Configured
    InputColumn<Number> regressionOutput;

    @Configured
    @Description("Determine how much (if any) of the records should be used for cross-validation.")
    @NumberProperty(negative = false)
    Percentage crossValidationSampleRate = new Percentage(10);

    private AtomicInteger recordCounter;
    private Collection<MLRegressionRecord> trainingRecords;
    private Collection<MLRegressionRecord> crossValidationRecords;
    private List<MLFeatureModifierBuilder> featureModifierBuilders;

    @Initialize
    public void init() {
        recordCounter = new AtomicInteger();
        trainingRecords = new ConcurrentLinkedQueue<>();
        crossValidationRecords = new ConcurrentLinkedQueue<>();
        featureModifierBuilders = new ArrayList<>(featureModifierTypes.length);

        final int maxFeatures = maxFeaturesGeneratedPerColumn == null ? -1 : maxFeaturesGeneratedPerColumn;
        final MLTrainingConstraints constraints = new MLTrainingConstraints(maxFeatures, includeUniqueValueFeatures);
        for (MLFeatureModifierType featureModifierType : featureModifierTypes) {
            final MLFeatureModifierBuilder featureModifierBuilder =
                    featureModifierBuilderFactory.create(featureModifierType, constraints);
            featureModifierBuilders.add(featureModifierBuilder);
        }
    }

    @Override
    public void run(InputRow row, int distinctCount) {
        final MLRegressionRecord record = MLRegressionRecordImpl.forTraining(row, regressionOutput, featureColumns);
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
    public MLRegressionAnalyzerResult getResult() {
        final List<MLFeatureModifier> featureModifiers =
                featureModifierBuilders.stream().map(MLFeatureModifierBuilder::build).collect(Collectors.toList());
        final List<String> columnNames = CollectionUtils.map(featureColumns, new HasNameMapper());
        final MLTrainingOptions options = new MLTrainingOptions(Double.class, columnNames, featureModifiers);

        final MLRegressorTrainer trainer = createTrainer(options);
        log("Training model starting. Records=" + trainingRecords.size() + ", Columns=" + columnNames.size()
                + ", Features=" + MLFeatureUtils.getFeatureCount(featureModifiers) + ".");
        final MLRegressor regressor = trainer.train(trainingRecords, featureModifiers, new MLTrainerCallback() {
            @Override
            public void epochDone(int epochNo, int expectedEpochs) {
                if (expectedEpochs > 1) {
                    log("Training progress: Epoch " + epochNo + " of " + expectedEpochs + " done.");
                }
            }
        });

        if (saveModelToFile != null) {
            logger.info("Saving model to file: {}", saveModelToFile);
            try {
                final byte[] bytes = SerializationUtils.serialize(regressor);
                Files.write(bytes, saveModelToFile);
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to save model to file: " + saveModelToFile, e);
            }
        }

        log("Trained model. Creating evaluation matrices.");

        return new MLRegressionAnalyzerResult(regressor);
    }

    protected abstract MLRegressorTrainer createTrainer(MLTrainingOptions options);
}
