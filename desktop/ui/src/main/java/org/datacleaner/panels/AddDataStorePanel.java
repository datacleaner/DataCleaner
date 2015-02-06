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
package org.datacleaner.panels;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JMenuItem;
import javax.swing.border.EmptyBorder;

import org.datacleaner.connection.CassandraDatastore;
import org.datacleaner.connection.CouchDbDatastore;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.ElasticSearchDatastore;
import org.datacleaner.connection.HBaseDatastore;
import org.datacleaner.connection.JdbcDatastore;
import org.datacleaner.connection.MongoDbDatastore;
import org.datacleaner.connection.SalesforceDatastore;
import org.datacleaner.connection.SugarCrmDatastore;
import org.datacleaner.database.DatabaseDriverCatalog;
import org.datacleaner.guice.InjectorBuilder;
import org.datacleaner.user.DatastoreSelectedListener;
import org.datacleaner.user.MutableDatastoreCatalog;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.Dropzone;
import org.datacleaner.widgets.PopupButton;
import org.datacleaner.windows.AbstractDatastoreDialog;
import org.datacleaner.windows.CassandraDatastoreDialog;
import org.datacleaner.windows.CouchDbDatastoreDialog;
import org.datacleaner.windows.ElasticSearchDatastoreDialog;
import org.datacleaner.windows.HBaseDatastoreDialog;
import org.datacleaner.windows.JdbcDatastoreDialog;
import org.datacleaner.windows.MongoDbDatastoreDialog;
import org.datacleaner.windows.SalesforceDatastoreDialog;
import org.datacleaner.windows.SugarCrmDatastoreDialog;

import com.google.inject.Injector;

public class AddDataStorePanel extends DCPanel {
    private static final long serialVersionUID = 1L;

    private final InjectorBuilder _injectorBuilder;
    private final DatastoreSelectedListener _datastoreSelectedListener;

    private final Dropzone _dropzone;

    public AddDataStorePanel(final MutableDatastoreCatalog datastoreCatalog,
            final DatabaseDriverCatalog databaseDriverCatalog, final InjectorBuilder injectorBuilder,
            final DatastoreSelectedListener datastoreSelectedListener) {
        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));
        _injectorBuilder = injectorBuilder;
        _datastoreSelectedListener = datastoreSelectedListener;
        _dropzone = new Dropzone(datastoreCatalog, datastoreSelectedListener);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.weightx = 1.0;
        c.insets = new Insets(0, 0, 10, 0);
        c.fill = GridBagConstraints.BOTH;
        add(_dropzone, c);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 1.0;
        c.insets = new Insets(0, 0, 0, 5);
        c.fill = GridBagConstraints.HORIZONTAL;
        PopupButton databaseButton = new PopupButton("Add database", ImageManager.get().getImageIcon(
                IconUtils.GENERIC_DATASTORE_IMAGEPATH, IconUtils.ICON_SIZE_LARGE));
        WidgetUtils.setWhiteButtonStyle(databaseButton);

        if (databaseDriverCatalog.isInstalled(DatabaseDriverCatalog.DATABASE_NAME_MYSQL)) {
            databaseButton.getMenu().add(
                    createNewJdbcDatastoreButton("MySQL connection", "Connect to a MySQL database",
                            "images/datastore-types/databases/mysql.png", DatabaseDriverCatalog.DATABASE_NAME_MYSQL));
        }
        if (databaseDriverCatalog.isInstalled(DatabaseDriverCatalog.DATABASE_NAME_POSTGRESQL)) {
            databaseButton.getMenu().add(
                    createNewJdbcDatastoreButton("PostgreSQL connection", "Connect to a PostgreSQL database",
                            "images/datastore-types/databases/postgresql.png",
                            DatabaseDriverCatalog.DATABASE_NAME_POSTGRESQL));
        }
        if (databaseDriverCatalog.isInstalled(DatabaseDriverCatalog.DATABASE_NAME_ORACLE)) {
            databaseButton.getMenu().add(
                    createNewJdbcDatastoreButton("Oracle connection", "Connect to a Oracle database",
                            "images/datastore-types/databases/oracle.png", DatabaseDriverCatalog.DATABASE_NAME_ORACLE));
        }
        if (databaseDriverCatalog.isInstalled(DatabaseDriverCatalog.DATABASE_NAME_MICROSOFT_SQL_SERVER_JTDS)) {
            databaseButton.getMenu().add(
                    createNewJdbcDatastoreButton("Microsoft SQL Server connection",
                            "Connect to a Microsoft SQL Server database",
                            "images/datastore-types/databases/microsoft.png",
                            DatabaseDriverCatalog.DATABASE_NAME_MICROSOFT_SQL_SERVER_JTDS));
        }

        databaseButton.getMenu().add(
                createNewDatastoreButton("MongoDB database", "Connect to a MongoDB database",
                        IconUtils.MONGODB_IMAGEPATH, MongoDbDatastore.class, MongoDbDatastoreDialog.class));

        databaseButton.getMenu().add(
                createNewDatastoreButton("CouchDB database", "Connect to an Apache CouchDB database",
                        IconUtils.COUCHDB_IMAGEPATH, CouchDbDatastore.class, CouchDbDatastoreDialog.class));

        databaseButton.getMenu().add(
                createNewDatastoreButton("ElasticSearch index", "Connect to an ElasticSearch index",
                        IconUtils.ELASTICSEARCH_IMAGEPATH, ElasticSearchDatastore.class,
                        ElasticSearchDatastoreDialog.class));

        databaseButton.getMenu().add(
                createNewDatastoreButton("Cassandra database", "Connect to an Apache Cassandra database",
                        IconUtils.CASSANDRA_IMAGEPATH, CassandraDatastore.class, CassandraDatastoreDialog.class));

        databaseButton.getMenu().add(
                createNewDatastoreButton("HBase database", "Connect to an Apache HBase database",
                        IconUtils.HBASE_IMAGEPATH, HBaseDatastore.class, HBaseDatastoreDialog.class));

        add(databaseButton, c);

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 1.0;
        c.insets = new Insets(0, 5, 0, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        PopupButton cloudButton = new PopupButton("Add cloud service", ImageManager.get().getImageIcon(
                IconUtils.DATASTORE_TYPE_CLOUD_DARK, IconUtils.ICON_SIZE_LARGE));
        WidgetUtils.setWhiteButtonStyle(cloudButton);
        add(cloudButton, c);

        cloudButton.getMenu().add(
                createNewDatastoreButton("Salesforce.com", "Connect to a Salesforce.com account",
                        IconUtils.SALESFORCE_IMAGEPATH, SalesforceDatastore.class, SalesforceDatastoreDialog.class));
        cloudButton.getMenu().add(
                createNewDatastoreButton("SugarCRM", "Connect to a SugarCRM system", IconUtils.SUGAR_CRM_IMAGEPATH,
                        SugarCrmDatastore.class, SugarCrmDatastoreDialog.class));
    }

    private JMenuItem createNewJdbcDatastoreButton(final String title, final String description,
            final String imagePath, final String databaseName) {
        final JMenuItem item = WidgetFactory.createMenuItem(title, imagePath);
        item.setToolTipText(description);

        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                Injector injectorWithDatastore = _injectorBuilder.with(JdbcDatastore.class, null).createInjector();
                final JdbcDatastoreDialog dialog = injectorWithDatastore.getInstance(JdbcDatastoreDialog.class);
                dialog.setSelectedDatabase(databaseName);
                dialog.setVisible(true);
                dialog.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        if (dialog.getSavedDatastore() != null) {
                            _datastoreSelectedListener.datastoreSelected(dialog.getSavedDatastore());
                        }
                    }
                });
            }
        });
        return item;
    }

    private <D extends Datastore> JMenuItem createNewDatastoreButton(final String title, final String description,
            final String imagePath, final Class<D> datastoreClass,
            final Class<? extends AbstractDatastoreDialog<D>> dialogClass) {
        final JMenuItem item = WidgetFactory.createMenuItem(title, imagePath);
        item.setToolTipText(description);

        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                Injector injectorWithNullDatastore = _injectorBuilder.with(datastoreClass, null).createInjector();
                final AbstractDatastoreDialog<D> dialog = injectorWithNullDatastore.getInstance(dialogClass);

                dialog.setVisible(true);
                dialog.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        if (dialog.getSavedDatastore() != null) {
                            _datastoreSelectedListener.datastoreSelected(dialog.getSavedDatastore());
                        }
                    }
                });
            }
        });
        return item;
    }

}
