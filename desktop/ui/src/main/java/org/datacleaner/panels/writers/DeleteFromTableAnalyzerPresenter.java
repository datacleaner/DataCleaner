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
package org.datacleaner.panels.writers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.metamodel.schema.Table;
import org.datacleaner.api.InputColumn;
import org.datacleaner.beans.writers.DeleteFromTableAnalyzer;
import org.datacleaner.beans.writers.UpdateTableAnalyzer;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.connection.Datastore;
import org.datacleaner.descriptors.AnalyzerDescriptor;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.guice.DCModule;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.panels.AnalyzerComponentBuilderPanel;
import org.datacleaner.panels.ConfiguredPropertyTaskPane;
import org.datacleaner.panels.TransformerComponentBuilderPresenter;
import org.datacleaner.util.IconUtils;
import org.datacleaner.widgets.DCComboBox.Listener;
import org.datacleaner.widgets.properties.MultipleMappedColumnsPropertyWidget;
import org.datacleaner.widgets.properties.MultipleMappedPrefixedColumnsPropertyWidget;
import org.datacleaner.widgets.properties.PropertyWidget;
import org.datacleaner.widgets.properties.PropertyWidgetFactory;
import org.datacleaner.widgets.properties.SchemaNamePropertyWidget;
import org.datacleaner.widgets.properties.SingleDatastorePropertyWidget;
import org.datacleaner.widgets.properties.SingleTableNamePropertyWidget;

/**
 * Specialized {@link TransformerComponentBuilderPresenter} for the
 * {@link DeleteFromTableAnalyzer}.
 */
class DeleteFromTableAnalyzerPresenter extends AnalyzerComponentBuilderPanel {

    private static final long serialVersionUID = 1L;

    private final Map<ConfiguredPropertyDescriptor, PropertyWidget<?>> _overriddenPropertyWidgets;

    private final ConfiguredPropertyDescriptor _schemaNameProperty;
    private final ConfiguredPropertyDescriptor _tableNameProperty;
    private final ConfiguredPropertyDescriptor _datastoreProperty;
    private final ConfiguredPropertyDescriptor _errorHandlingProperty;
    private final ConfiguredPropertyDescriptor _errorFileLocationProperty;
    private final ConfiguredPropertyDescriptor _additionalErrorLogValuesProperty;
    private final ConfiguredPropertyDescriptor _bufferSizeProperty;
    private final ConfiguredPropertyDescriptor _conditionInputColumnsProperty;
    private final ConfiguredPropertyDescriptor _conditionColumnNamesProperty;
    private final MultipleMappedColumnsPropertyWidget[] _inputColumnPropertyWidgets;

    public DeleteFromTableAnalyzerPresenter(AnalyzerComponentBuilder<DeleteFromTableAnalyzer> analyzerJobBuilder,
            WindowContext windowContext, PropertyWidgetFactory propertyWidgetFactory,
            DataCleanerConfiguration configuration, DCModule dcModule) {
        super(analyzerJobBuilder, propertyWidgetFactory);
        _overriddenPropertyWidgets = new HashMap<ConfiguredPropertyDescriptor, PropertyWidget<?>>();

        final AnalyzerDescriptor<DeleteFromTableAnalyzer> descriptor = analyzerJobBuilder.getDescriptor();
        assert descriptor.getComponentClass() == DeleteFromTableAnalyzer.class;

        _datastoreProperty = descriptor.getConfiguredProperty("Datastore");
        _schemaNameProperty = descriptor.getConfiguredProperty("Schema name");
        _tableNameProperty = descriptor.getConfiguredProperty("Table name");
        _conditionInputColumnsProperty = descriptor.getConfiguredProperty("Condition values");
        _conditionColumnNamesProperty = descriptor.getConfiguredProperty("Condition column names");
        _bufferSizeProperty = descriptor.getConfiguredProperty("Buffer size");
        _errorHandlingProperty = descriptor.getConfiguredProperty("How to handle updation errors?");
        _errorFileLocationProperty = descriptor.getConfiguredProperty("Error log file location");
        _additionalErrorLogValuesProperty = descriptor.getConfiguredProperty("Additional error log values");

        // the Datastore property
        assert _datastoreProperty != null;
        assert _datastoreProperty.getType() == Datastore.class;
        final SingleDatastorePropertyWidget datastorePropertyWidget = new SingleDatastorePropertyWidget(
                analyzerJobBuilder, _datastoreProperty, configuration.getDatastoreCatalog(), dcModule);
        datastorePropertyWidget.setOnlyUpdatableDatastores(true);
        _overriddenPropertyWidgets.put(_datastoreProperty, datastorePropertyWidget);

        // The schema name (String) property
        final SchemaNamePropertyWidget schemaNamePropertyWidget = new SchemaNamePropertyWidget(analyzerJobBuilder,
                _schemaNameProperty);
        _overriddenPropertyWidgets.put(_schemaNameProperty, schemaNamePropertyWidget);

        // The table name (String) property
        final SingleTableNamePropertyWidget tableNamePropertyWidget = new SingleTableNamePropertyWidget(
                analyzerJobBuilder, _tableNameProperty, windowContext);
        _overriddenPropertyWidgets.put(_tableNameProperty, tableNamePropertyWidget);

        _inputColumnPropertyWidgets = new MultipleMappedColumnsPropertyWidget[2];

        // condition values
        {
            // the InputColumn<?>[] property
            assert _conditionInputColumnsProperty != null;
            assert _conditionInputColumnsProperty.getType() == InputColumn[].class;
            final MultipleMappedColumnsPropertyWidget inputColumnsPropertyWidget = new MultipleMappedPrefixedColumnsPropertyWidget(
                    analyzerJobBuilder, _conditionInputColumnsProperty, _conditionColumnNamesProperty, " = ");

            _overriddenPropertyWidgets.put(_conditionInputColumnsProperty, inputColumnsPropertyWidget);

            // the String[] property
            assert _conditionColumnNamesProperty != null;
            assert _conditionColumnNamesProperty.getType() == String[].class;
            _overriddenPropertyWidgets.put(_conditionColumnNamesProperty,
                    inputColumnsPropertyWidget.getMappedColumnNamesPropertyWidget());

            _inputColumnPropertyWidgets[1] = inputColumnsPropertyWidget;
        }

        // chain combo boxes
        datastorePropertyWidget.connectToSchemaNamePropertyWidget(schemaNamePropertyWidget);
        schemaNamePropertyWidget.connectToTableNamePropertyWidget(tableNamePropertyWidget);

        tableNamePropertyWidget.addComboListener(new Listener<Table>() {
            @Override
            public void onItemSelected(Table item) {
                // update the column combo boxes when the table is selected
                for (int i = 1; i < _inputColumnPropertyWidgets.length; i++) {
                    MultipleMappedColumnsPropertyWidget inputColumnsPropertyWidget = _inputColumnPropertyWidgets[i];
                    inputColumnsPropertyWidget.setTable(item);
                }
            }
        });

        // initialize
        schemaNamePropertyWidget.setDatastore(datastorePropertyWidget.getValue());
        tableNamePropertyWidget.setSchema(datastorePropertyWidget.getValue(), schemaNamePropertyWidget.getSchema());

        for (int i = 1; i < _inputColumnPropertyWidgets.length; i++) {
            MultipleMappedColumnsPropertyWidget inputColumnsPropertyWidget = _inputColumnPropertyWidgets[i];
            inputColumnsPropertyWidget.setTable(tableNamePropertyWidget.getTable());
        }
    }

    @Override
    protected List<ConfiguredPropertyTaskPane> createPropertyTaskPanes() {
        final ConfiguredPropertyTaskPane taskPane1 = new ConfiguredPropertyTaskPane("Table to delete from",
                IconUtils.getImagePathForClass(UpdateTableAnalyzer.class), Arrays.asList(_datastoreProperty,
                        _schemaNameProperty, _tableNameProperty, _bufferSizeProperty));

        final ConfiguredPropertyTaskPane taskPane2 = new ConfiguredPropertyTaskPane("Delete condition",
                "images/model/column.png", Arrays.asList(_conditionInputColumnsProperty));

        final ConfiguredPropertyTaskPane errorHandlingPane = new ConfiguredPropertyTaskPane("Error handling",
                IconUtils.STATUS_WARNING, Arrays.asList(_errorHandlingProperty, _errorFileLocationProperty,
                        _additionalErrorLogValuesProperty), false);

        final List<ConfiguredPropertyTaskPane> propertyTaskPanes = new ArrayList<ConfiguredPropertyTaskPane>();
        propertyTaskPanes.add(taskPane1);
        propertyTaskPanes.add(taskPane2);
        propertyTaskPanes.add(errorHandlingPane);
        return propertyTaskPanes;
    }

    @Override
    protected PropertyWidget<?> createPropertyWidget(ComponentBuilder componentBuilder,
            ConfiguredPropertyDescriptor propertyDescriptor) {
        if (_overriddenPropertyWidgets.containsKey(propertyDescriptor)) {
            return _overriddenPropertyWidgets.get(propertyDescriptor);
        }
        return super.createPropertyWidget(componentBuilder, propertyDescriptor);
    }
}
