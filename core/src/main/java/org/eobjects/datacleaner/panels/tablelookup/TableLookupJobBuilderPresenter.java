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
import java.util.HashMap;
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
import org.eobjects.datacleaner.panels.TransformerJobBuilderPanel;
import org.eobjects.datacleaner.panels.TransformerJobBuilderPresenter;
import org.eobjects.datacleaner.widgets.properties.PropertyWidget;
import org.eobjects.datacleaner.widgets.properties.PropertyWidgetFactory;
import org.eobjects.datacleaner.widgets.properties.SchemaNamePropertyWidget;
import org.eobjects.datacleaner.widgets.properties.SingleDatastorePropertyWidget;
import org.eobjects.datacleaner.widgets.properties.TableNamePropertyWidget;

/**
 * Specialized {@link TransformerJobBuilderPresenter} for the
 * {@link TableLookupTransformer}.
 * 
 * @author Kasper Sørensen
 */
public class TableLookupJobBuilderPresenter extends TransformerJobBuilderPanel {

	private static final long serialVersionUID = 1L;

	private final Map<ConfiguredPropertyDescriptor, PropertyWidget<?>> _overriddenPropertyWidgets;

	public TableLookupJobBuilderPresenter(TransformerJobBuilder<TableLookupTransformer> transformerJobBuilder,
			WindowContext windowContext, PropertyWidgetFactory propertyWidgetFactory,
			AnalyzerBeansConfiguration configuration) {
		super(transformerJobBuilder, windowContext, propertyWidgetFactory, configuration);
		_overriddenPropertyWidgets = new HashMap<ConfiguredPropertyDescriptor, PropertyWidget<?>>();

		final TransformerBeanDescriptor<?> descriptor = transformerJobBuilder.getDescriptor();
		assert descriptor.getComponentClass() == TableLookupTransformer.class;

		final ConfiguredPropertyDescriptor schemaNameProperty = descriptor.getConfiguredProperty("Schema name");
		final ConfiguredPropertyDescriptor tableNameProperty = descriptor.getConfiguredProperty("Table name");
		final ConfiguredPropertyDescriptor datastoreProperty = descriptor.getConfiguredProperty("Datastore");
		final ConfiguredPropertyDescriptor inputColumnArrayProperty = descriptor.getConfiguredProperty("Condition values");
		final ConfiguredPropertyDescriptor columnNameArrayProperty = descriptor.getConfiguredProperty("Condition columns");

		// The schema name (String) property
		final SchemaNamePropertyWidget schemaNamePropertyWidget = new SchemaNamePropertyWidget(transformerJobBuilder,
				schemaNameProperty);
		_overriddenPropertyWidgets.put(schemaNameProperty, schemaNamePropertyWidget);

		// The table name (String) property
		final TableNamePropertyWidget tableNamePropertyWidget = new TableNamePropertyWidget(transformerJobBuilder,
				tableNameProperty);
		_overriddenPropertyWidgets.put(tableNameProperty, tableNamePropertyWidget);

		// the Datastore property
		assert datastoreProperty != null;
		assert datastoreProperty.getType() == Datastore.class;
		final SingleDatastorePropertyWidget datastorePropertyWidget = new SingleDatastorePropertyWidget(
				transformerJobBuilder, datastoreProperty, configuration.getDatastoreCatalog());
		_overriddenPropertyWidgets.put(datastoreProperty, datastorePropertyWidget);

		// the InputColumn<?>[] property
		assert inputColumnArrayProperty != null;
		assert inputColumnArrayProperty.getType() == InputColumn[].class;
		final TableLookupInputColumnsPropertyWidget inputColumnsPropertyWidget = new TableLookupInputColumnsPropertyWidget(
				transformerJobBuilder, inputColumnArrayProperty, columnNameArrayProperty);
		_overriddenPropertyWidgets.put(inputColumnArrayProperty, inputColumnsPropertyWidget);

		// the String[] property
		assert columnNameArrayProperty != null;
		assert columnNameArrayProperty.getType() == String[].class;
		_overriddenPropertyWidgets.put(columnNameArrayProperty,
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
				inputColumnsPropertyWidget.setTable(tableNamePropertyWidget.getTable());
			}
		});
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
