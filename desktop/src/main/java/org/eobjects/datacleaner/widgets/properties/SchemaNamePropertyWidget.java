/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.widgets.properties;

import javax.swing.DefaultComboBoxModel;

import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreConnection;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.datacleaner.widgets.DCComboBox;
import org.eobjects.datacleaner.widgets.DCComboBox.Listener;
import org.eobjects.datacleaner.widgets.SchemaStructureComboBoxListRenderer;
import org.eobjects.metamodel.schema.MutableSchema;
import org.eobjects.metamodel.schema.Schema;
import org.eobjects.metamodel.util.CollectionUtils;
import org.eobjects.metamodel.util.MutableRef;

/**
 * Alternative String property widget, specifically built for components that
 * need a {@link Schema} name drop down.
 * 
 * @author Kasper Sørensen
 */
public class SchemaNamePropertyWidget extends AbstractPropertyWidget<String> {

	private final DCComboBox<Schema> _comboBox;
	private final MutableRef<Datastore> _datastoreRef;

	public SchemaNamePropertyWidget(AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
			ConfiguredPropertyDescriptor propertyDescriptor) {
		super(beanJobBuilder, propertyDescriptor);
		_comboBox = new DCComboBox<Schema>();
		_comboBox.setRenderer(new SchemaStructureComboBoxListRenderer(false));
		_comboBox.setEditable(false);
		addComboListener(new Listener<Schema>() {
			@Override
			public void onItemSelected(Schema item) {
				fireValueChanged();
			}
		});
		add(_comboBox);
		_datastoreRef = new MutableRef<Datastore>();

		setValue(getCurrentValue());
	}

	public void addComboListener(Listener<Schema> listener) {
		_comboBox.addListener(listener);
	}

	public void setDatastore(Datastore datastore) {
		String previousValue = getValue();
		_datastoreRef.set(datastore);
		if (datastore == null) {
			_comboBox.setModel(new DefaultComboBoxModel(new Object[1]));
		} else {
			final DatastoreConnection con = datastore.openConnection();
			try {
				Schema[] schemas = con.getSchemaNavigator().getSchemas();
				schemas = CollectionUtils.array(new Schema[1], schemas);
				_comboBox.setModel(new DefaultComboBoxModel(schemas));
				Schema newValue = null;
				if (previousValue != null) {
					newValue = con.getSchemaNavigator().getSchemaByName(previousValue);
				}
				if (newValue == null) {
					newValue = con.getSchemaNavigator().getDefaultSchema();
				}
				_comboBox.setSelectedItem(newValue);
			} finally {
				con.close();
			}
		}
	}

	@Override
	public String getValue() {
		Schema schema = getSchema();
		if (schema == null) {
			return null;
		}
		return schema.getName();
	}

	public Schema getSchema() {
		Schema schema = _comboBox.getSelectedItem();
		return schema;
	}

	@Override
	protected void setValue(String value) {
		final Datastore datastore = _datastoreRef.get();
		if (value == null && datastore != null) {
			DatastoreConnection con = datastore.openConnection();
			try {
				value = con.getSchemaNavigator().getDefaultSchema().getName();
			} finally {
				con.close();
			}
		}

		if (getValue() == value) {
			return;
		}

		final Schema schema;
		if (value == null) {
			schema = null;
		} else if (datastore == null) {
			schema = new MutableSchema(value);
		} else {
			DatastoreConnection con = datastore.openConnection();
			try {
				schema = con.getSchemaNavigator().getSchemaByName(value);
			} finally {
				con.close();
			}
		}

		_comboBox.setEditable(true);
		_comboBox.setSelectedItem(schema);
		_comboBox.setEditable(false);
	}

}
