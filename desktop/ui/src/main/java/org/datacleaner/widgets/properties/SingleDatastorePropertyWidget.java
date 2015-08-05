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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.datacleaner.connection.AccessDatastore;
import org.datacleaner.connection.CassandraDatastore;
import org.datacleaner.connection.CouchDbDatastore;
import org.datacleaner.connection.CsvDatastore;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.connection.DbaseDatastore;
import org.datacleaner.connection.ElasticSearchDatastore;
import org.datacleaner.connection.ExcelDatastore;
import org.datacleaner.connection.FixedWidthDatastore;
import org.datacleaner.connection.HBaseDatastore;
import org.datacleaner.connection.JdbcDatastore;
import org.datacleaner.connection.JsonDatastore;
import org.datacleaner.connection.MongoDbDatastore;
import org.datacleaner.connection.OdbDatastore;
import org.datacleaner.connection.SalesforceDatastore;
import org.datacleaner.connection.SasDatastore;
import org.datacleaner.connection.SugarCrmDatastore;
import org.datacleaner.connection.XmlDatastore;
import org.datacleaner.database.DatabaseDriverCatalog;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.guice.DCModule;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.user.DatastoreChangeListener;
import org.datacleaner.user.MutableDatastoreCatalog;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ReflectionUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCComboBox;
import org.datacleaner.widgets.DCComboBox.Listener;
import org.datacleaner.widgets.PopupButton;
import org.datacleaner.widgets.SchemaStructureComboBoxListRenderer;
import org.datacleaner.windows.AbstractDialog;
import org.datacleaner.windows.AccessDatastoreDialog;
import org.datacleaner.windows.CassandraDatastoreDialog;
import org.datacleaner.windows.CouchDbDatastoreDialog;
import org.datacleaner.windows.CsvDatastoreDialog;
import org.datacleaner.windows.DbaseDatastoreDialog;
import org.datacleaner.windows.ElasticSearchDatastoreDialog;
import org.datacleaner.windows.ExcelDatastoreDialog;
import org.datacleaner.windows.FixedWidthDatastoreDialog;
import org.datacleaner.windows.HBaseDatastoreDialog;
import org.datacleaner.windows.JdbcDatastoreDialog;
import org.datacleaner.windows.JsonDatastoreDialog;
import org.datacleaner.windows.MongoDbDatastoreDialog;
import org.datacleaner.windows.OdbDatastoreDialog;
import org.datacleaner.windows.SalesforceDatastoreDialog;
import org.datacleaner.windows.SasDatastoreDialog;
import org.datacleaner.windows.SugarCrmDatastoreDialog;
import org.datacleaner.windows.XmlDatastoreDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

/**
 * {@link PropertyWidget} for single datastore properties. Shown as a combo box.
 * 
 * @author Kasper Sørensen
 */
public class SingleDatastorePropertyWidget extends AbstractPropertyWidget<Datastore> implements DatastoreChangeListener {

    private static final Logger logger = LoggerFactory.getLogger(SingleDatastorePropertyWidget.class);

    private final DatastoreCatalog _datastoreCatalog;
    private final DCComboBox<Datastore> _comboBox;
    private final DCPanel _panelAroundButton;
    private final Class<?> _datastoreClass;
    private volatile DatastoreConnection _connection;
    
    private final DCModule _dcModule;

    @Inject
    public SingleDatastorePropertyWidget(ComponentBuilder componentBuilder,
            ConfiguredPropertyDescriptor propertyDescriptor, DatastoreCatalog datastoreCatalog, DCModule dcModule) {
        super(componentBuilder, propertyDescriptor);
        _datastoreCatalog = datastoreCatalog;
        _dcModule = dcModule;
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

        final PopupButton createDatastoreButton = WidgetFactory.createDefaultPopupButton("", IconUtils.ACTION_CREATE_TABLE);
        createDatastoreButton.setToolTipText("Create datastore");
        JPopupMenu createDatastoreMenu = createDatastoreButton.getMenu();
        populateCreateDatastoreMenu(createDatastoreMenu);

        _panelAroundButton = DCPanel.around(createDatastoreButton);
        _panelAroundButton.setBorder(WidgetUtils.BORDER_EMPTY);
        _panelAroundButton.setVisible(true);

        final DCPanel panel = new DCPanel();
        panel.setLayout(new BorderLayout());
        panel.add(_comboBox, BorderLayout.CENTER);
        panel.add(_panelAroundButton, BorderLayout.EAST);

        add(panel);
    }

    private void populateCreateDatastoreMenu(JPopupMenu createDatastoreMenu) {
        final JMenuItem csvMenuItem = new JMenuItem("CSV file");
        csvMenuItem.addActionListener(createActionListener(CsvDatastore.class, CsvDatastoreDialog.class));
        createDatastoreMenu.add(csvMenuItem);
        
        final JMenuItem excelMenuItem = new JMenuItem("Excel spreadsheet");
        excelMenuItem.addActionListener(createActionListener(ExcelDatastore.class, ExcelDatastoreDialog.class));
        createDatastoreMenu.add(excelMenuItem);
        
        final JMenuItem accessMenuItem = new JMenuItem("Access database");
        accessMenuItem.addActionListener(createActionListener(AccessDatastore.class, AccessDatastoreDialog.class));
        createDatastoreMenu.add(accessMenuItem);
        
        final JMenuItem sasMenuItem = new JMenuItem("SAS library");
        sasMenuItem.addActionListener(createActionListener(SasDatastore.class, SasDatastoreDialog.class));
        createDatastoreMenu.add(sasMenuItem);
        
        final JMenuItem dbaseMenuItem = new JMenuItem("DBase database");
        dbaseMenuItem.addActionListener(createActionListener(DbaseDatastore.class, DbaseDatastoreDialog.class));
        createDatastoreMenu.add(dbaseMenuItem);
        
        final JMenuItem fixedWidthMenuItem = new JMenuItem("Fixed width file");
        fixedWidthMenuItem.addActionListener(createActionListener(FixedWidthDatastore.class, FixedWidthDatastoreDialog.class));
        createDatastoreMenu.add(fixedWidthMenuItem);
        
        final JMenuItem xmlMenuItem = new JMenuItem("XML file");
        xmlMenuItem.addActionListener(createActionListener(XmlDatastore.class, XmlDatastoreDialog.class));
        createDatastoreMenu.add(xmlMenuItem);
        
        final JMenuItem jsonMenuItem = new JMenuItem("JSON file");
        jsonMenuItem.addActionListener(createActionListener(JsonDatastore.class, JsonDatastoreDialog.class));
        createDatastoreMenu.add(jsonMenuItem);
        
        final JMenuItem odbMenuItem = new JMenuItem("OpenOffice.org Base database");
        odbMenuItem.addActionListener(createActionListener(OdbDatastore.class, OdbDatastoreDialog.class));
        createDatastoreMenu.add(odbMenuItem);
        
        createDatastoreMenu.addSeparator();
        
        final JMenuItem salesforceMenuItem = new JMenuItem("Salesforce.com");
        salesforceMenuItem.addActionListener(createActionListener(SalesforceDatastore.class, SalesforceDatastoreDialog.class));
        createDatastoreMenu.add(salesforceMenuItem);
        
        final JMenuItem sugarCrmMenuItem = new JMenuItem("SugarCRM");
        sugarCrmMenuItem.addActionListener(createActionListener(SugarCrmDatastore.class, SugarCrmDatastoreDialog.class));
        createDatastoreMenu.add(sugarCrmMenuItem);
        
        createDatastoreMenu.addSeparator();
        
        final JMenuItem mongoDbMenuItem = new JMenuItem("MongoDB database");
        mongoDbMenuItem.addActionListener(createActionListener(MongoDbDatastore.class, MongoDbDatastoreDialog.class));
        createDatastoreMenu.add(mongoDbMenuItem);
        
        final JMenuItem couchDbMenuItem = new JMenuItem("CouchDB database");
        couchDbMenuItem.addActionListener(createActionListener(CouchDbDatastore.class, CouchDbDatastoreDialog.class));
        createDatastoreMenu.add(couchDbMenuItem);
        
        final JMenuItem elasticSearchMenuItem = new JMenuItem("ElasticSearch index");
        elasticSearchMenuItem.addActionListener(createActionListener(ElasticSearchDatastore.class, ElasticSearchDatastoreDialog.class));
        createDatastoreMenu.add(elasticSearchMenuItem);
        
        final JMenuItem cassandraMenuItem = new JMenuItem("Cassandra database");
        cassandraMenuItem.addActionListener(createActionListener(CassandraDatastore.class, CassandraDatastoreDialog.class));
        createDatastoreMenu.add(cassandraMenuItem);
        
        final JMenuItem hbaseMenuItem = new JMenuItem("HBase database");
        hbaseMenuItem.addActionListener(createActionListener(HBaseDatastore.class, HBaseDatastoreDialog.class));
        createDatastoreMenu.add(hbaseMenuItem);
        
        DatabaseDriverCatalog databaseDriverCatalog = _dcModule.createInjectorBuilder().getInstance(DatabaseDriverCatalog.class);
        
        if (databaseDriverCatalog.isInstalled(DatabaseDriverCatalog.DATABASE_NAME_HIVE)) {
            final JMenuItem hiveMenuItem = new JMenuItem("Apache Hive");
            hiveMenuItem.addActionListener(createJdbcActionListener(DatabaseDriverCatalog.DATABASE_NAME_HIVE));
            createDatastoreMenu.add(hiveMenuItem);
        }
        
        if (databaseDriverCatalog.isInstalled(DatabaseDriverCatalog.DATABASE_NAME_MYSQL)) {
            final JMenuItem mysqlMenuItem = new JMenuItem("MySQL connection");
            mysqlMenuItem.addActionListener(createJdbcActionListener(DatabaseDriverCatalog.DATABASE_NAME_MYSQL));
            createDatastoreMenu.add(mysqlMenuItem);
        }
        
        if (databaseDriverCatalog.isInstalled(DatabaseDriverCatalog.DATABASE_NAME_POSTGRESQL)) {
            final JMenuItem postgresqlMenuItem = new JMenuItem("PostgreSQL connection");
            postgresqlMenuItem.addActionListener(createJdbcActionListener(DatabaseDriverCatalog.DATABASE_NAME_POSTGRESQL));
            createDatastoreMenu.add(postgresqlMenuItem);
        }
        
        if (databaseDriverCatalog.isInstalled(DatabaseDriverCatalog.DATABASE_NAME_MICROSOFT_SQL_SERVER_JTDS)) {
            final JMenuItem sqlServerMenuItem = new JMenuItem("Microsoft SQL Server connection");
            sqlServerMenuItem.addActionListener(createJdbcActionListener(DatabaseDriverCatalog.DATABASE_NAME_MICROSOFT_SQL_SERVER_JTDS));
            createDatastoreMenu.add(sqlServerMenuItem);
        } 
    }
    
    private ActionListener createActionListener(final Class<? extends Datastore> datastoreClass, final Class<? extends AbstractDialog> datastoreDialogClass) {
        return new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                final Injector injectorWithNullDatastore = _dcModule.createInjectorBuilder().with(datastoreClass, null).createInjector();
                final AbstractDialog dialog = injectorWithNullDatastore.getInstance(datastoreDialogClass);
                dialog.setVisible(true);
            }
            
        };
    }
    
    private ActionListener createJdbcActionListener(final String databaseName) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                Injector injectorWithDatastore = _dcModule.createInjectorBuilder().with(JdbcDatastore.class, null).createInjector();
                JdbcDatastoreDialog dialog = injectorWithDatastore.getInstance(JdbcDatastoreDialog.class);
                dialog.setSelectedDatabase(databaseName);
                dialog.setVisible(true);
            }
        };
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
