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

import javax.inject.Inject;
import javax.swing.JComboBox;

import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.datacleaner.user.DatastoreChangeListener;
import org.eobjects.datacleaner.user.MutableDatastoreCatalog;
import org.eobjects.datacleaner.widgets.SchemaStructureComboBoxListRenderer;

/**
 * {@link PropertyWidget} for single datastore properties. Shown as a combo box.
 * 
 * @author Kasper Sørensen
 */
public class SingleDatastorePropertyWidget extends AbstractPropertyWidget<Datastore> implements DatastoreChangeListener {

	private static final long serialVersionUID = 1L;

	private final DatastoreCatalog _datastoreCatalog;
	private final JComboBox _comboBox;

	@Inject
	public SingleDatastorePropertyWidget(AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
			ConfiguredPropertyDescriptor propertyDescriptor) {
		super(beanJobBuilder, propertyDescriptor);

		_datastoreCatalog = beanJobBuilder.getAnalysisJobBuilder().getConfiguration().getDatastoreCatalog();

		String[] datastoreNames = _datastoreCatalog.getDatastoreNames();
		Object[] items = new Object[datastoreNames.length];
		for (int i = 0; i < items.length; i++) {
			items[i] = _datastoreCatalog.getDatastore(datastoreNames[i]);
		}
		_comboBox = new JComboBox(items);
		_comboBox.setRenderer(new SchemaStructureComboBoxListRenderer());
		_comboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				fireValueChanged();
			}
		});
		add(_comboBox);
	}

	@Override
	public void addNotify() {
		super.addNotify();
		if (_datastoreCatalog instanceof MutableDatastoreCatalog) {
			((MutableDatastoreCatalog) _datastoreCatalog).addListener(this);
		}
	}

	@Override
	public void removeNotify() {
		super.removeNotify();
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
		_comboBox.setSelectedItem(value);
	}

	@Override
	public void onAdd(Datastore datastore) {
		_comboBox.addItem(datastore);
	}

	@Override
	public void onRemove(Datastore datastore) {
		_comboBox.removeItem(datastore);
	}

}
