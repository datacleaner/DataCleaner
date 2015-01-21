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
package org.datacleaner.widgets.properties;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.user.DatastoreChangeListener;
import org.datacleaner.user.MutableDatastoreCatalog;
import org.datacleaner.util.ReflectionUtils;
import org.datacleaner.widgets.DCComboBox;
import org.datacleaner.widgets.DCComboBox.Listener;
import org.datacleaner.widgets.SchemaStructureComboBoxListRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link PropertyWidget} for single datastore properties. Shown as a combo box.
 * 
 * @author Kasper Sørensen
 */
public class SingleDatastorePropertyWidget extends AbstractPropertyWidget<Datastore> implements DatastoreChangeListener {

    private static final Logger logger = LoggerFactory.getLogger(SingleDatastorePropertyWidget.class);

    private final DatastoreCatalog _datastoreCatalog;
    private final DCComboBox<Datastore> _comboBox;
    private final Class<?> _datastoreClass;
    private volatile DatastoreConnection _connection;

    @Inject
    public SingleDatastorePropertyWidget(ComponentBuilder componentBuilder,
            ConfiguredPropertyDescriptor propertyDescriptor, DatastoreCatalog datastoreCatalog) {
        super(componentBuilder, propertyDescriptor);
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
        _comboBox = new DCComboBox<Datastore>(list);
        _comboBox.setRenderer(new SchemaStructureComboBoxListRenderer());

        addComboListener(new Listener<Datastore>() {
            @Override
            public void onItemSelected(Datastore item) {
                openConnection(item);
                fireValueChanged();
            }
        });

        Datastore currentValue = (Datastore) componentBuilder.getConfiguredProperty(propertyDescriptor);
        setValue(currentValue);

        add(_comboBox);
    }

    public void addComboListener(Listener<Datastore> listener) {
        _comboBox.addListener(listener);
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
        openConnection(null);
    }

    @Override
    public Datastore getValue() {
        Object selectedItem = _comboBox.getSelectedItem();
        Datastore datastore = (Datastore) selectedItem;
        openConnection(datastore);
        return datastore;
    }

    @Override
    protected void setValue(Datastore value) {
        if (value == null) {
            _comboBox.setSelectedItem(null);
            return;
        }

        if (getValue() == value) {
            return;
        }

        openConnection(value);

        _comboBox.setEditable(true);
        _comboBox.setSelectedItem(value);
        _comboBox.setEditable(false);
    }

    private void openConnection(Datastore datastore) {
        if (_connection != null && _connection.getDatastore() == datastore) {
            return;
        }
        if (_connection != null) {
            _connection.close();
            _connection = null;
        }
        if (datastore != null) {
            try {
                _connection = datastore.openConnection();
            } catch (Exception e) {
                logger.warn("Could not open connection to datastore: {}", datastore);
            }
        }
    }

    @Override
    public void onAdd(Datastore datastore) {
        if (ReflectionUtils.is(datastore.getClass(), _datastoreClass)) {
            _comboBox.setEditable(true);
            _comboBox.addItem(datastore);
            _comboBox.setEditable(false);
        }
    }

    @Override
    public void onRemove(Datastore datastore) {
        _comboBox.setEditable(true);
        _comboBox.removeItem(datastore);
        _comboBox.setEditable(false);
    }

    public void connectToSchemaNamePropertyWidget(final SchemaNamePropertyWidget schemaNamePropertyWidget) {
        addComboListener(new Listener<Datastore>() {
            @Override
            public void onItemSelected(Datastore item) {
                schemaNamePropertyWidget.setDatastore(item);
            }
        });
    }
}
