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
package org.datacleaner.components.machinelearning.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datacleaner.api.InputColumn;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.components.machinelearning.MLTrainingAnalyzer;
import org.datacleaner.components.machinelearning.api.MLFeatureModifierType;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.descriptors.EnumerationValue;
import org.datacleaner.guice.DCModule;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.panels.AnalyzerComponentBuilderPanel;
import org.datacleaner.widgets.properties.MultipleMappedEnumsPropertyWidget;
import org.datacleaner.widgets.properties.PropertyWidget;
import org.datacleaner.widgets.properties.PropertyWidgetFactory;

public class MLTrainingAnalyzerPresenter extends AnalyzerComponentBuilderPanel {

    private static final long serialVersionUID = 1L;

    private final Map<ConfiguredPropertyDescriptor, PropertyWidget<?>> _overriddenPropertyWidgets;

    public MLTrainingAnalyzerPresenter(AnalyzerComponentBuilder<MLTrainingAnalyzer> analyzerJobBuilder,
            WindowContext windowContext, PropertyWidgetFactory propertyWidgetFactory,
            DataCleanerConfiguration configuration, DCModule dcModule) {
        super(analyzerJobBuilder, propertyWidgetFactory);
        _overriddenPropertyWidgets = new HashMap<>();

        final ConfiguredPropertyDescriptor featureColumnsProperty = analyzerJobBuilder.getDescriptor()
                .getConfiguredProperty(MLTrainingAnalyzer.PROPERTY_FEATURE_COLUMNS);
        final ConfiguredPropertyDescriptor featureModifierTypesProperty = analyzerJobBuilder.getDescriptor()
                .getConfiguredProperty(MLTrainingAnalyzer.PROPERTY_FEATURE_MODIFIERS);
        final MultipleMappedEnumsPropertyWidget mappingWidget = new MultipleMappedEnumsPropertyWidget(
                analyzerJobBuilder, featureColumnsProperty, featureModifierTypesProperty) {
            @Override
            protected EnumerationValue[] getEnumConstants(InputColumn<?> inputColumn,
                    ConfiguredPropertyDescriptor mappedEnumsProperty) {
                final List<MLFeatureModifierType> applicableValues = MLFeatureModifierType.getApplicableValues(
                        inputColumn.getDataType());
                return EnumerationValue.fromArray(applicableValues.toArray(new MLFeatureModifierType[applicableValues
                        .size()]));
            }
        };

        _overriddenPropertyWidgets.put(featureColumnsProperty, mappingWidget);
        _overriddenPropertyWidgets.put(featureModifierTypesProperty, mappingWidget.getMappedEnumsPropertyWidget());
    }

    @Override
    protected PropertyWidget<?> createPropertyWidget(final ComponentBuilder componentBuilder,
            final ConfiguredPropertyDescriptor propertyDescriptor) {
        if (_overriddenPropertyWidgets.containsKey(propertyDescriptor)) {
            return _overriddenPropertyWidgets.get(propertyDescriptor);
        }
        return super.createPropertyWidget(componentBuilder, propertyDescriptor);
    }
}
