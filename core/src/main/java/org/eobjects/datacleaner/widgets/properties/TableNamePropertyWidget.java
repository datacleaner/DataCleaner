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

import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.datacleaner.widgets.SchemaStructureComboBoxListRenderer;
import org.eobjects.metamodel.schema.MutableTable;
import org.eobjects.metamodel.schema.Schema;
import org.eobjects.metamodel.schema.Table;
import org.eobjects.metamodel.util.MutableRef;

/**
 * Alternative String property widget, specifically built for components that
 * need a {@link Table} name drop down.
 * 
 * @author Kasper Sørensen
 */
public class TableNamePropertyWidget extends AbstractPropertyWidget<String> {

	private final JComboBox _comboBox;
	private final MutableRef<Schema> _schemaRef;

	public TableNamePropertyWidget(AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
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
		_schemaRef = new MutableRef<Schema>();
	}
	
	public void addComboItemListener(ItemListener itemListener) {
		_comboBox.addItemListener(itemListener);
	}

	public void setSchema(Schema schema) {
		_schemaRef.set(schema);
		if (schema == null) {
			_comboBox.setModel(new DefaultComboBoxModel(new Object[1]));
		} else {
			Table[] tables = schema.getTables();
			_comboBox.setModel(new DefaultComboBoxModel(tables));
		}
	}

	@Override
	public String getValue() {
		Table table = getTable();
		if (table == null) {
			return null;
		}
		return table.getName();
	}
	

	public Table getTable() {
		return (Table) _comboBox.getSelectedItem();
	}

	@Override
	protected void setValue(String value) {
		if (getValue() == value) {
			return;
		}
		
		final Schema schema = _schemaRef.get();
		final Table table;
		if (value == null) {
			table = null;
		} else if (schema == null) {
			table = new MutableTable(value);
		} else {
			table = schema.getTableByName(value);
		}

		_comboBox.setEditable(true);
		_comboBox.setSelectedItem(table);
		_comboBox.setEditable(false);
	}

}
