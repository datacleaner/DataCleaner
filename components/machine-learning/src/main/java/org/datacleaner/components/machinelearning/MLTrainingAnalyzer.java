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

import javax.inject.Inject;

import org.datacleaner.api.Analyzer;
import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.Categorized;
import org.datacleaner.api.ComponentContext;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.ExecutionLogMessage;
import org.datacleaner.api.FileProperty;
import org.datacleaner.api.FileProperty.FileAccessMode;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.MappedProperty;
import org.datacleaner.api.NumberProperty;
import org.datacleaner.api.Provided;
import org.datacleaner.api.Validate;
import org.datacleaner.components.machinelearning.api.MLFeatureModifierType;

@Categorized(MachineLearningCategory.class)
public abstract class MLTrainingAnalyzer<R extends AnalyzerResult> implements Analyzer<R> {

    public static final String PROPERTY_FEATURE_COLUMNS = "Features";
    public static final String PROPERTY_FEATURE_MODIFIERS = "Feature modifier types";

    @Configured(PROPERTY_FEATURE_COLUMNS)
    InputColumn<?>[] featureColumns;

    @Configured(PROPERTY_FEATURE_MODIFIERS)
    @MappedProperty(PROPERTY_FEATURE_COLUMNS)
    MLFeatureModifierType[] featureModifierTypes;

    @Configured
    @Description("Defines the maximum number of features to generate per column. "
            + "Applies to feature vectors such as 'One-Hot Encoding' or n-grams.")
    @NumberProperty(negative = false, zero = false)
    Integer maxFeaturesGeneratedPerColumn = 25;

    @Configured
    @Description("Include generated features that are only triggered once in the training data set.")
    boolean includeUniqueValueFeatures = false;

    @Configured(required = false)
    @FileProperty(accessMode = FileAccessMode.SAVE, extension = "model.ser")
    File saveModelToFile;

    @Inject
    @Provided
    ComponentContext componentContext;

    @Validate
    public void validate() {
        MLComponentUtils.validateTrainingMapping(featureColumns, featureModifierTypes);
    }

    protected void log(String string) {
        if (componentContext != null) {
            componentContext.publishMessage(new ExecutionLogMessage(string));
        }
    }
}
