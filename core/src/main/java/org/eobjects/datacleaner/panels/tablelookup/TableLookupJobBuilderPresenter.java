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
package org.eobjects.datacleaner.panels.tablelookup;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eobjects.analyzer.beans.transform.TableLookupTransformer;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.descriptors.TransformerBeanDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.panels.ConfiguredPropertyTaskPane;
import org.eobjects.datacleaner.panels.TransformerJobBuilderPanel;
import org.eobjects.datacleaner.panels.TransformerJobBuilderPresenter;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.widgets.properties.PropertyWidget;
import org.eobjects.datacleaner.widgets.properties.PropertyWidgetFactory;
import org.eobjects.datacleaner.widgets.properties.SchemaNamePropertyWidget;
import org.eobjects.datacleaner.widgets.properties.SingleDatastorePropertyWidget;
import org.eobjects.datacleaner.widgets.properties.TableNamePropertyWidget;
import org.eobjects.metamodel.schema.Table;

/**
 * Specialized {@link TransformerJobBuilderPresenter} for the
 * {@link TableLookupTransformer}.
 * 
 * @author Kasper Sørensen
 */
public class TableLookupJobBuilderPresenter extends TransformerJobBuilderPanel {

	private static final long serialVersionUID = 1L;

	private final Map<ConfiguredPropertyDescriptor, PropertyWidget<?>> _overriddenPropertyWidgets;

	private final ConfiguredPropertyDescriptor _schemaNameProperty;
	private final ConfiguredPropertyDescriptor _tableNameProperty;
	private final ConfiguredPropertyDescriptor _datastoreProperty;
	private final ConfiguredPropertyDescriptor _inputColumnArrayProperty;
	private final ConfiguredPropertyDescriptor _columnNameArrayProperty;
	private final ConfiguredPropertyDescriptor _outputColumnsProperty;

	public TableLookupJobBuilderPresenter(TransformerJobBuilder<TableLookupTransformer> transformerJobBuilder,
			WindowContext windowContext, PropertyWidgetFactory propertyWidgetFactory,
			AnalyzerBeansConfiguration configuration) {
		super(transformerJobBuilder, windowContext, propertyWidgetFactory, configuration);
		_overriddenPropertyWidgets = new HashMap<ConfiguredPropertyDescriptor, PropertyWidget<?>>();

		final TransformerBeanDescriptor<?> descriptor = transformerJobBuilder.getDescriptor();
		assert descriptor.getComponentClass() == TableLookupTransformer.class;

		_datastoreProperty = descriptor.getConfiguredProperty("Datastore");
		_schemaNameProperty = descriptor.getConfiguredProperty("Schema name");
		_tableNameProperty = descriptor.getConfiguredProperty("Table name");
		_inputColumnArrayProperty = descriptor.getConfiguredProperty("Condition values");
		_columnNameArrayProperty = descriptor.getConfiguredProperty("Condition columns");
		_outputColumnsProperty = descriptor.getConfiguredProperty("Output columns");

		// the Datastore property
		assert _datastoreProperty != null;
		assert _datastoreProperty.getType() == Datastore.class;
		final SingleDatastorePropertyWidget datastorePropertyWidget = new SingleDatastorePropertyWidget(
				transformerJobBuilder, _datastoreProperty, configuration.getDatastoreCatalog());
		_overriddenPropertyWidgets.put(_datastoreProperty, datastorePropertyWidget);

		// The schema name (String) property
		final SchemaNamePropertyWidget schemaNamePropertyWidget = new SchemaNamePropertyWidget(transformerJobBuilder,
				_schemaNameProperty);
		schemaNamePropertyWidget.setDatastore(datastorePropertyWidget.getValue());
		_overriddenPropertyWidgets.put(_schemaNameProperty, schemaNamePropertyWidget);

		// The table name (String) property
		final TableNamePropertyWidget tableNamePropertyWidget = new TableNamePropertyWidget(transformerJobBuilder,
				_tableNameProperty);
		tableNamePropertyWidget.setSchema(schemaNamePropertyWidget.getSchema());
		_overriddenPropertyWidgets.put(_tableNameProperty, tableNamePropertyWidget);

		// the output columns (String[]) property
		final TableLookupOutputColumnsPropertyWidget outputColumnsPropertyWidget = new TableLookupOutputColumnsPropertyWidget(
				transformerJobBuilder, _outputColumnsProperty);
		outputColumnsPropertyWidget.setTable(tableNamePropertyWidget.getTable());
		_overriddenPropertyWidgets.put(_outputColumnsProperty, outputColumnsPropertyWidget);

		// the InputColumn<?>[] property
		assert _inputColumnArrayProperty != null;
		assert _inputColumnArrayProperty.getType() == InputColumn[].class;
		final TableLookupInputColumnsPropertyWidget inputColumnsPropertyWidget = new TableLookupInputColumnsPropertyWidget(
				transformerJobBuilder, _inputColumnArrayProperty, _columnNameArrayProperty);
		inputColumnsPropertyWidget.setTable(tableNamePropertyWidget.getTable());
		_overriddenPropertyWidgets.put(_inputColumnArrayProperty, inputColumnsPropertyWidget);

		// the String[] property
		assert _columnNameArrayProperty != null;
		assert _columnNameArrayProperty.getType() == String[].class;
		_overriddenPropertyWidgets.put(_columnNameArrayProperty,
				inputColumnsPropertyWidget.getMappedColumnNamesPropertyWidget());

		// chain combo boxes
		datastorePropertyWidget.addComboItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				// update the schema name when datastore is selected
				schemaNamePropertyWidget.setDatastore(datastorePropertyWidget.getValue());
			}
		});
		schemaNamePropertyWidget.addComboItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				// update the table name when schema is selected
				tableNamePropertyWidget.setSchema(schemaNamePropertyWidget.getSchema());
			}
		});
		tableNamePropertyWidget.addComboItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				// update the column combo boxes when the table is selected
				final Table table = tableNamePropertyWidget.getTable();
				inputColumnsPropertyWidget.setTable(table);
				outputColumnsPropertyWidget.setTable(table);
			}
		});
	}

	@Override
	protected List<ConfiguredPropertyTaskPane> createPropertyTaskPanes() {
		final List<ConfiguredPropertyTaskPane> propertyTaskPanes = new ArrayList<ConfiguredPropertyTaskPane>();

		final ConfiguredPropertyTaskPane inputMappingTaskPane = new ConfiguredPropertyTaskPane("Input mapping",
				"images/model/column.png", Arrays.asList(_datastoreProperty, _schemaNameProperty, _tableNameProperty,
						_inputColumnArrayProperty));
		final ConfiguredPropertyTaskPane outputMappingTaskPane = new ConfiguredPropertyTaskPane("Output mapping",
				IconUtils.MENU_OPTIONS, Arrays.asList(_outputColumnsProperty));
		propertyTaskPanes.add(inputMappingTaskPane);
		propertyTaskPanes.add(outputMappingTaskPane);

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
