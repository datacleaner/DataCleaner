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

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

import javax.inject.Named;

import org.apache.commons.lang.SerializationUtils;
import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.FileProperty;
import org.datacleaner.api.FileProperty.FileAccessMode;
import org.datacleaner.api.Initialize;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.Transformer;
import org.datacleaner.api.Validate;
import org.datacleaner.components.machinelearning.api.MLClassificationRecord;
import org.datacleaner.components.machinelearning.api.MLRegressor;
import org.datacleaner.components.machinelearning.impl.MLClassificationRecordImpl;

import com.google.common.io.Files;

@Named("Apply regression")
@Description("Applies a regression model to incoming records. Note that the regression model must first be trained using one of the analyzers found in the 'Machine Learning' category.")
@Categorized(MachineLearningCategory.class)
public class MLRegressionTransformer implements Transformer {

    @Configured
    InputColumn<?>[] features;

    @Configured
    @FileProperty(accessMode = FileAccessMode.OPEN, extension = ".model.ser")
    File modelFile = new File("regression.model.ser");

    private MLRegressor regressor;

    @Validate
    public void validate() throws IOException {
        if (!modelFile.exists()) {
            throw new IllegalArgumentException("Model file '" + modelFile + "' does not exist.");
        }
        regressor = (MLRegressor) SerializationUtils.deserialize(Files.toByteArray(modelFile));

        MLComponentUtils.validateRegressorMapping(regressor, features);
    }

    @Initialize
    public void init() {
        try {
            final byte[] bytes = Files.toByteArray(modelFile);
            regressor = (MLRegressor) SerializationUtils.deserialize(bytes);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public OutputColumns getOutputColumns() {
        String modelName = modelFile.getName();
        if (modelName.toLowerCase().endsWith(".model.ser")) {
            modelName = modelName.substring(0, modelName.length() - ".model.ser".length());
        }
        final String[] columnNames = new String[] { modelName + " value" };
        final Class<?>[] columnTypes = new Class[] { Double.class };
        return new OutputColumns(columnNames, columnTypes);
    }

    @Override
    public Object[] transform(InputRow inputRow) {
        final MLClassificationRecord record = MLClassificationRecordImpl.forEvaluation(inputRow, features);
        final double prediction = regressor.predict(record);
        return new Object[] { prediction };
    }
}
