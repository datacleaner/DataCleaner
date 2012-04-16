/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.panels.updatetable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eobjects.analyzer.beans.writers.UpdateTableAnalyzer;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.job.builder.AnalyzerJobBuilder;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.panels.AnalyzerJobBuilderPanel;
import org.eobjects.datacleaner.panels.ConfiguredPropertyTaskPane;
import org.eobjects.datacleaner.panels.TransformerJobBuilderPresenter;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.widgets.DCComboBox.Listener;
import org.eobjects.datacleaner.widgets.properties.MultipleMappedColumnsPropertyWidget;
import org.eobjects.datacleaner.widgets.properties.PropertyWidget;
import org.eobjects.datacleaner.widgets.properties.PropertyWidgetFactory;
import org.eobjects.datacleaner.widgets.properties.SchemaNamePropertyWidget;
import org.eobjects.datacleaner.widgets.properties.SingleDatastorePropertyWidget;
import org.eobjects.datacleaner.widgets.properties.TableNamePropertyWidget;
import org.eobjects.metamodel.schema.Schema;
import org.eobjects.metamodel.schema.Table;

/**
 * Specialized {@link TransformerJobBuilderPresenter} for the
 * {@link UpdateTableAnalyzer}.
 * 
 * @author Kasper Sørensen
 */
class UpdateTableJobBuilderPresenter extends AnalyzerJobBuilderPanel {

    private static final long serialVersionUID = 1L;

    private final Map<ConfiguredPropertyDescriptor, PropertyWidget<?>> _overriddenPropertyWidgets;

    private final ConfiguredPropertyDescriptor _schemaNameProperty;
    private final ConfiguredPropertyDescriptor _tableNameProperty;
    private final ConfiguredPropertyDescriptor _datastoreProperty;
    private final ConfiguredPropertyDescriptor _inputColumnsProperty;
    private final ConfiguredPropertyDescriptor _columnNamesProperty;
    private final ConfiguredPropertyDescriptor _errorHandlingProperty;
    private final ConfiguredPropertyDescriptor _errorFileLocationProperty;
    private final ConfiguredPropertyDescriptor _additionalErrorLogValuesProperty;
    private final ConfiguredPropertyDescriptor _bufferSizeProperty;
    private final ConfiguredPropertyDescriptor _conditionInputColumnsProperty;
    private final ConfiguredPropertyDescriptor _conditionColumnNamesProperty;
    private final MultipleMappedColumnsPropertyWidget[] _inputColumnPropertyWidgets;

    public UpdateTableJobBuilderPresenter(AnalyzerJobBuilder<UpdateTableAnalyzer> analyzerJobBuilder,
            WindowContext windowContext, PropertyWidgetFactory propertyWidgetFactory,
            AnalyzerBeansConfiguration configuration) {
        super(analyzerJobBuilder, propertyWidgetFactory);
        _overriddenPropertyWidgets = new HashMap<ConfiguredPropertyDescriptor, PropertyWidget<?>>();

        final AnalyzerBeanDescriptor<UpdateTableAnalyzer> descriptor = analyzerJobBuilder.getDescriptor();
        assert descriptor.getComponentClass() == UpdateTableAnalyzer.class;

        _datastoreProperty = descriptor.getConfiguredProperty("Datastore");
        _schemaNameProperty = descriptor.getConfiguredProperty("Schema name");
        _tableNameProperty = descriptor.getConfiguredProperty("Table name");
        _inputColumnsProperty = descriptor.getConfiguredProperty("Values");
        _columnNamesProperty = descriptor.getConfiguredProperty("Column names");
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
                analyzerJobBuilder, _datastoreProperty, configuration.getDatastoreCatalog());
        _overriddenPropertyWidgets.put(_datastoreProperty, datastorePropertyWidget);

        // The schema name (String) property
        final SchemaNamePropertyWidget schemaNamePropertyWidget = new SchemaNamePropertyWidget(analyzerJobBuilder,
                _schemaNameProperty);
        _overriddenPropertyWidgets.put(_schemaNameProperty, schemaNamePropertyWidget);

        // The table name (String) property
        final TableNamePropertyWidget tableNamePropertyWidget = new TableNamePropertyWidget(analyzerJobBuilder,
                _tableNameProperty);
        _overriddenPropertyWidgets.put(_tableNameProperty, tableNamePropertyWidget);

        _inputColumnPropertyWidgets = new MultipleMappedColumnsPropertyWidget[2];

        // values
        {
            // the InputColumn<?>[] property
            assert _inputColumnsProperty != null;
            assert _inputColumnsProperty.getType() == InputColumn[].class;
            final MultipleMappedColumnsPropertyWidget inputColumnsPropertyWidget = new MultipleMappedColumnsPropertyWidget(
                    analyzerJobBuilder, _inputColumnsProperty, _columnNamesProperty);
            _overriddenPropertyWidgets.put(_inputColumnsProperty, inputColumnsPropertyWidget);

            // the String[] property
            assert _columnNamesProperty != null;
            assert _columnNamesProperty.getType() == String[].class;
            _overriddenPropertyWidgets.put(_columnNamesProperty,
                    inputColumnsPropertyWidget.getMappedColumnNamesPropertyWidget());

            _inputColumnPropertyWidgets[0] = inputColumnsPropertyWidget;
        }

        // condition values
        {
            // the InputColumn<?>[] property
            assert _conditionInputColumnsProperty != null;
            assert _conditionInputColumnsProperty.getType() == InputColumn[].class;
            final MultipleMappedColumnsPropertyWidget inputColumnsPropertyWidget = new MultipleMappedColumnsPropertyWidget(
                    analyzerJobBuilder, _conditionInputColumnsProperty, _conditionColumnNamesProperty);
            _overriddenPropertyWidgets.put(_conditionInputColumnsProperty, inputColumnsPropertyWidget);

            // the String[] property
            assert _conditionColumnNamesProperty != null;
            assert _conditionColumnNamesProperty.getType() == String[].class;
            _overriddenPropertyWidgets.put(_conditionColumnNamesProperty,
                    inputColumnsPropertyWidget.getMappedColumnNamesPropertyWidget());

            _inputColumnPropertyWidgets[1] = inputColumnsPropertyWidget;
        }

        // chain combo boxes
        datastorePropertyWidget.addComboListener(new Listener<Datastore>() {
            @Override
            public void onItemSelected(Datastore item) {
                schemaNamePropertyWidget.setDatastore(item);
            }
        });
        schemaNamePropertyWidget.addComboListener(new Listener<Schema>() {
            @Override
            public void onItemSelected(Schema item) {
                // update the table name when schema is selected
                tableNamePropertyWidget.setSchema(item);
            }
        });
        tableNamePropertyWidget.addComboListener(new Listener<Table>() {
            @Override
            public void onItemSelected(Table item) {
                // update the column combo boxes when the table is selected
                for (int i = 0; i < _inputColumnPropertyWidgets.length; i++) {
                    MultipleMappedColumnsPropertyWidget inputColumnsPropertyWidget = _inputColumnPropertyWidgets[i];
                    inputColumnsPropertyWidget.setTable(item);
                }
            }
        });

        // initialize
        schemaNamePropertyWidget.setDatastore(datastorePropertyWidget.getValue());
        tableNamePropertyWidget.setSchema(schemaNamePropertyWidget.getSchema());

        for (int i = 0; i < _inputColumnPropertyWidgets.length; i++) {
            MultipleMappedColumnsPropertyWidget inputColumnsPropertyWidget = _inputColumnPropertyWidgets[i];
            inputColumnsPropertyWidget.setTable(tableNamePropertyWidget.getTable());
        }
    }

    @Override
    protected List<ConfiguredPropertyTaskPane> createPropertyTaskPanes() {
        final ConfiguredPropertyTaskPane taskPane = new ConfiguredPropertyTaskPane("Update mapping",
                "images/model/column.png", Arrays.asList(_datastoreProperty, _schemaNameProperty, _tableNameProperty,
                        _inputColumnsProperty, _conditionInputColumnsProperty, _bufferSizeProperty));

        final ConfiguredPropertyTaskPane errorHandlingPane = new ConfiguredPropertyTaskPane("Error handling",
                IconUtils.STATUS_WARNING, Arrays.asList(_errorHandlingProperty, _errorFileLocationProperty,
                        _additionalErrorLogValuesProperty), false);

        final List<ConfiguredPropertyTaskPane> propertyTaskPanes = new ArrayList<ConfiguredPropertyTaskPane>();
        propertyTaskPanes.add(taskPane);
        propertyTaskPanes.add(errorHandlingPane);
        return propertyTaskPanes;
    }

    @Override
    protected PropertyWidget<?> createPropertyWidget(AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
            ConfiguredPropertyDescriptor propertyDescriptor) {
        if (_overriddenPropertyWidgets.containsKey(propertyDescriptor)) {
            return _overriddenPropertyWidgets.get(propertyDescriptor);
        }
        return super.createPropertyWidget(beanJobBuilder, propertyDescriptor);
    }
}
