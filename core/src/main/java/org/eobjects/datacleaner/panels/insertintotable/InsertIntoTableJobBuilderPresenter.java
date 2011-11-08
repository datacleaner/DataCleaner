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
package org.eobjects.datacleaner.panels.insertintotable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eobjects.analyzer.beans.writers.InsertIntoTableAnalyzer;
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
 * {@link InsertIntoTableAnalyzer}.
 * 
 * @author Kasper Sørensen
 */
class InsertIntoTableJobBuilderPresenter extends AnalyzerJobBuilderPanel {

	private static final long serialVersionUID = 1L;

	private final Map<ConfiguredPropertyDescriptor, PropertyWidget<?>> _overriddenPropertyWidgets;

	private final ConfiguredPropertyDescriptor _schemaNameProperty;
	private final ConfiguredPropertyDescriptor _tableNameProperty;
	private final ConfiguredPropertyDescriptor _datastoreProperty;
	private final ConfiguredPropertyDescriptor _inputColumnsProperty;
	private final ConfiguredPropertyDescriptor _columnNamesProperty;

	public InsertIntoTableJobBuilderPresenter(AnalyzerJobBuilder<InsertIntoTableAnalyzer> analyzerJobBuilder,
			WindowContext windowContext, PropertyWidgetFactory propertyWidgetFactory,
			AnalyzerBeansConfiguration configuration) {
		super(analyzerJobBuilder, propertyWidgetFactory);
		_overriddenPropertyWidgets = new HashMap<ConfiguredPropertyDescriptor, PropertyWidget<?>>();

		final AnalyzerBeanDescriptor<InsertIntoTableAnalyzer> descriptor = analyzerJobBuilder.getDescriptor();
		assert descriptor.getComponentClass() == InsertIntoTableAnalyzer.class;

		_datastoreProperty = descriptor.getConfiguredProperty("Datastore");
		_schemaNameProperty = descriptor.getConfiguredProperty("Schema name");
		_tableNameProperty = descriptor.getConfiguredProperty("Table name");
		_inputColumnsProperty = descriptor.getConfiguredProperty("Values");
		_columnNamesProperty = descriptor.getConfiguredProperty("Column names");

		// the Datastore property
		assert _datastoreProperty != null;
		assert _datastoreProperty.getType() == Datastore.class;
		final SingleDatastorePropertyWidget datastorePropertyWidget = new SingleDatastorePropertyWidget(analyzerJobBuilder,
				_datastoreProperty, configuration.getDatastoreCatalog());
		_overriddenPropertyWidgets.put(_datastoreProperty, datastorePropertyWidget);

		// The schema name (String) property
		final SchemaNamePropertyWidget schemaNamePropertyWidget = new SchemaNamePropertyWidget(analyzerJobBuilder,
				_schemaNameProperty);
		_overriddenPropertyWidgets.put(_schemaNameProperty, schemaNamePropertyWidget);

		// The table name (String) property
		final TableNamePropertyWidget tableNamePropertyWidget = new TableNamePropertyWidget(analyzerJobBuilder,
				_tableNameProperty);
		_overriddenPropertyWidgets.put(_tableNameProperty, tableNamePropertyWidget);

		// the InputColumn<?>[] property
		assert _inputColumnsProperty != null;
		assert _inputColumnsProperty.getType() == InputColumn[].class;
		final MultipleMappedColumnsPropertyWidget inputColumnsPropertyWidget = new MultipleMappedColumnsPropertyWidget(
				analyzerJobBuilder, _inputColumnsProperty, _columnNamesProperty);
		_overriddenPropertyWidgets.put(_inputColumnsProperty, inputColumnsPropertyWidget);

		// the String[] property
		assert _columnNamesProperty != null;
		assert _columnNamesProperty.getType() == String[].class;
		_overriddenPropertyWidgets
				.put(_columnNamesProperty, inputColumnsPropertyWidget.getMappedColumnNamesPropertyWidget());

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
				inputColumnsPropertyWidget.setTable(item);
			}
		});

		// initialize
		schemaNamePropertyWidget.setDatastore(datastorePropertyWidget.getValue());
		tableNamePropertyWidget.setSchema(schemaNamePropertyWidget.getSchema());
		inputColumnsPropertyWidget.setTable(tableNamePropertyWidget.getTable());
	}

	@Override
	protected List<ConfiguredPropertyTaskPane> createPropertyTaskPanes() {
		final List<ConfiguredPropertyTaskPane> propertyTaskPanes = new ArrayList<ConfiguredPropertyTaskPane>();

		final ConfiguredPropertyTaskPane taskPane = new ConfiguredPropertyTaskPane("Insert mapping",
				"images/model/column.png", Arrays.asList(_datastoreProperty, _schemaNameProperty, _tableNameProperty,
						_inputColumnsProperty));
		propertyTaskPanes.add(taskPane);

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
