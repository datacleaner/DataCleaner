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
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.border.EmptyBorder;

import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.connection.DatastoreDescriptor;
import org.datacleaner.connection.DatastoreDescriptors;
import org.datacleaner.connection.JdbcDatastore;
import org.datacleaner.database.DatabaseDriverCatalog;
import org.datacleaner.database.DatabaseDriverDescriptor;
import org.datacleaner.guice.DCModule;
import org.datacleaner.user.DatastoreSelectedListener;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.Alignment;
import org.datacleaner.widgets.Dropzone;
import org.datacleaner.widgets.PopupButton;
import org.datacleaner.windows.AbstractDatastoreDialog;
import org.datacleaner.windows.JdbcDatastoreDialog;

import com.google.inject.Injector;

public class AddDatastorePanel extends DCPanel {

    private static final long serialVersionUID = 1L;

    private final DCModule _dcModule;
    private final DatastoreSelectedListener _datastoreSelectedListener;
    private final Dropzone _dropzone;
    private final DatastoreCatalog _datastoreCatalog;
    private final DatabaseDriverCatalog _databaseDriverCatalog;

    public AddDatastorePanel(final DatastoreCatalog datastoreCatalog,
            final DatabaseDriverCatalog databaseDriverCatalog, final DCModule dcModule,
            final DatastoreSelectedListener datastoreSelectedListener, UserPreferences userPreferences,
            boolean showExistingDatastoresButton) {
        super();
        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));
        _datastoreCatalog = datastoreCatalog;
        _databaseDriverCatalog = databaseDriverCatalog;
        _dcModule = dcModule;
        _datastoreSelectedListener = datastoreSelectedListener;
        _dropzone = new Dropzone(datastoreCatalog, datastoreSelectedListener, userPreferences);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.weightx = 1.0;
        c.insets = new Insets(0, 0, 10, 0);
        c.fill = GridBagConstraints.BOTH;
        add(_dropzone, c);

        final PopupButton databaseButton = createDatabaseButton();

        final PopupButton cloudButton = createCloudButton();

        final DCPanel buttonPanel;
        if (showExistingDatastoresButton) {
            PopupButton existingDatastoresButton = createExistingDatastoresButton();
            buttonPanel = DCPanel.flow(Alignment.CENTER, databaseButton, cloudButton, existingDatastoresButton);
        } else {
            buttonPanel = DCPanel.flow(Alignment.CENTER, databaseButton, cloudButton);
        }

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 2;
        c.weightx = 1.0;
        c.insets = new Insets(0, 5, 0, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        add(buttonPanel, c);

    }

    private PopupButton createExistingDatastoresButton() {
        final PopupButton existingDatastoresButton = WidgetFactory.createDefaultPopupButton("Existing datastores",
                IconUtils.FILE_FOLDER);
        final JPopupMenu menu = existingDatastoresButton.getMenu();
        final String[] datastoreNames = _datastoreCatalog.getDatastoreNames();
        for (String datastoreName : datastoreNames) {
            final Datastore datastore = _datastoreCatalog.getDatastore(datastoreName);
            final JMenuItem menuItem = WidgetFactory.createMenuItem(datastoreName,
                    IconUtils.getDatastoreIcon(datastore, IconUtils.ICON_SIZE_MENU_ITEM));
            menuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    _datastoreSelectedListener.datastoreSelected(datastore);
                }
            });

            menu.add(menuItem);
        }
        return existingDatastoresButton;
    }

    private PopupButton createCloudButton() {
        final PopupButton cloudButton = WidgetFactory.createDefaultPopupButton("Cloud service",
                IconUtils.CLOUD_IMAGEPATH);
        List<DatastoreDescriptor> cloudDatastores = new DatastoreDescriptors(_databaseDriverCatalog)
                .getAvailableCloudBasedDatastoreDescriptors();

        for (DatastoreDescriptor datastoreDescriptor : cloudDatastores) {
            cloudButton.getMenu().add(
                    createNewDatastoreButton(datastoreDescriptor.getName(), datastoreDescriptor.getDescription(),
                            datastoreDescriptor.getIconPath(), datastoreDescriptor.getDatastoreClass(),
                            datastoreDescriptor.getDatastoreDialogClass()));
        }
        return cloudButton;
    }
    
    private PopupButton createDatabaseButton() {
        final PopupButton databaseButton = WidgetFactory.createDefaultPopupButton("Database",
                IconUtils.GENERIC_DATASTORE_IMAGEPATH);
        databaseButton.setFont(WidgetUtils.FONT_HEADER2);

        List<DatastoreDescriptor> databaseBasedDatastores = new DatastoreDescriptors(_databaseDriverCatalog)
                .getAvailableDatabaseBasedDatastoreDescriptors();

        for (DatastoreDescriptor datastoreDescriptor : databaseBasedDatastores) {
            if (datastoreDescriptor.getDatastoreClass().equals(JdbcDatastore.class)) {
                databaseButton.getMenu().add(createNewJdbcDatastoreButton(datastoreDescriptor.getName()));
            } else {
                databaseButton.getMenu().add(
                        createNewDatastoreButton(datastoreDescriptor.getName(), datastoreDescriptor.getDescription(),
                                datastoreDescriptor.getIconPath(), datastoreDescriptor.getDatastoreClass(),
                                datastoreDescriptor.getDatastoreDialogClass()));
            }
        }

        return databaseButton;
    }

    private JMenuItem createNewJdbcDatastoreButton(final String databaseName) {

        DatabaseDriverDescriptor driverDescriptor = DatabaseDriverCatalog
                .getDatabaseDriverByDriverDatabaseName(databaseName);

        final JMenuItem item = WidgetFactory.createMenuItem(databaseName, driverDescriptor.getIconImagePath());
        item.setToolTipText("Connect to " + databaseName);

        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                Injector injectorWithDatastore = _dcModule.createInjectorBuilder().with(JdbcDatastore.class, null)
                        .createInjector();
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
            final Class<? extends AbstractDatastoreDialog<? extends Datastore>> dialogClass) {
        final JMenuItem item = WidgetFactory.createMenuItem(title, imagePath);
        item.setToolTipText(description);

        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                Injector injectorWithNullDatastore = _dcModule.createInjectorBuilder().with(datastoreClass, null)
                        .createInjector();
                final AbstractDatastoreDialog<? extends Datastore> dialog = injectorWithNullDatastore
                        .getInstance(dialogClass);

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
