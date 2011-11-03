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
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.swing.JComboBox;

import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.util.ReflectionUtils;
import org.eobjects.datacleaner.user.DatastoreChangeListener;
import org.eobjects.datacleaner.user.MutableDatastoreCatalog;
import org.eobjects.datacleaner.widgets.SchemaStructureComboBoxListRenderer;
import org.jdesktop.swingx.combobox.ListComboBoxModel;

/**
 * {@link PropertyWidget} for single datastore properties. Shown as a combo box.
 * 
 * @author Kasper Sørensen
 */
public class SingleDatastorePropertyWidget extends AbstractPropertyWidget<Datastore> implements DatastoreChangeListener {

	private static final long serialVersionUID = 1L;

	private final DatastoreCatalog _datastoreCatalog;
	private final JComboBox _comboBox;
	private final Class<?> _datastoreClass;

	@Inject
	public SingleDatastorePropertyWidget(AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
			ConfiguredPropertyDescriptor propertyDescriptor, DatastoreCatalog datastoreCatalog) {
		super(beanJobBuilder, propertyDescriptor);
		_datastoreCatalog = datastoreCatalog;
		_datastoreClass = propertyDescriptor.getBaseType();

		String[] datastoreNames = _datastoreCatalog.getDatastoreNames();
		List<Datastore> list = new ArrayList<Datastore>();
		
		if (!propertyDescriptor.isRequired()) {
			list.add(null);
		}
		
		for (int i = 0; i < datastoreNames.length; i++) {
			Datastore datastore = _datastoreCatalog.getDatastore(datastoreNames[i]);
			if (ReflectionUtils.is(datastore.getClass(), _datastoreClass)) {
				// only include correct subtypes of datastore, it may be eg. a
				// CsvDatastore property.
				list.add(datastore);
			}
		}
		_comboBox = new JComboBox(new ListComboBoxModel<Datastore>(list));
		
		Datastore currentValue = (Datastore) beanJobBuilder.getConfiguredProperty(propertyDescriptor);
		_comboBox.setSelectedItem(currentValue);
		
		_comboBox.setRenderer(new SchemaStructureComboBoxListRenderer());
		_comboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				fireValueChanged();
			}
		});
		add(_comboBox);
	}
	
	public void addComboItemListener(ItemListener listener) {
		_comboBox.addItemListener(listener);
	}
	
	public void removeComboItemListener(ItemListener listener) {
		_comboBox.removeItemListener(listener);
	}

	@Override
	public void onPanelAdd() {
		super.onPanelAdd();
		if (_datastoreCatalog instanceof MutableDatastoreCatalog) {
			((MutableDatastoreCatalog) _datastoreCatalog).addListener(this);
		}
	}

	@Override
	public void onPanelRemove() {
		super.onPanelRemove();
		if (_datastoreCatalog instanceof MutableDatastoreCatalog) {
			((MutableDatastoreCatalog) _datastoreCatalog).removeListener(this);
		}
	}

	@Override
	public Datastore getValue() {
		return (Datastore) _comboBox.getSelectedItem();
	}

	@Override
	protected void setValue(Datastore value) {
		_comboBox.setEditable(true);
		_comboBox.setSelectedItem(value);
		_comboBox.setEditable(false);
	}

	@Override
	public void onAdd(Datastore datastore) {
		if (ReflectionUtils.is(datastore.getClass(), _datastoreClass)) {
			_comboBox.addItem(datastore);
		}
	}

	@Override
	public void onRemove(Datastore datastore) {
		_comboBox.removeItem(datastore);
	}

}
