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
package org.eobjects.datacleaner.widgets.properties;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreConnection;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.datacleaner.widgets.SchemaStructureComboBoxListRenderer;
import org.eobjects.metamodel.schema.MutableSchema;
import org.eobjects.metamodel.schema.Schema;
import org.eobjects.metamodel.util.MutableRef;

/**
 * Alternative String property widget, specifically built for components that
 * need a {@link Schema} name drop down.
 * 
 * @author Kasper Sørensen
 */
public class SchemaNamePropertyWidget extends AbstractPropertyWidget<String> {

	private final JComboBox _comboBox;
	private final MutableRef<Datastore> _datastoreRef;

	public SchemaNamePropertyWidget(AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
			ConfiguredPropertyDescriptor propertyDescriptor) {
		super(beanJobBuilder, propertyDescriptor);
		_comboBox = new JComboBox();
		_comboBox.setRenderer(new SchemaStructureComboBoxListRenderer(false));
		_comboBox.setEditable(false);
		_comboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				fireValueChanged();
			}
		});
		add(_comboBox);
		_datastoreRef = new MutableRef<Datastore>();
	}

	public void setDatastore(Datastore datastore) {
		_datastoreRef.set(datastore);
		if (datastore == null) {
			_comboBox.setModel(new DefaultComboBoxModel(new Object[1]));
		} else {
			final DatastoreConnection con = datastore.openConnection();
			try {
				Schema[] schemas = con.getSchemaNavigator().getSchemas();
				_comboBox.setModel(new DefaultComboBoxModel(schemas));
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
		Schema schema = (Schema) _comboBox.getSelectedItem();
		return schema;
	}

	@Override
	protected void setValue(String value) {
		if (getValue() == value) {
			return;
		}
		
		final Schema schema;
		Datastore datastore = _datastoreRef.get();
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

	public void addComboItemListener(ItemListener itemListener) {
		_comboBox.addItemListener(itemListener);
	}

}
