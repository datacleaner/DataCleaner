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

import javax.swing.Box;
import javax.swing.JComponent;

import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.database.DatabaseDriverCatalog;
import org.datacleaner.guice.DCModule;
import org.datacleaner.user.DatastoreSelectedListener;
import org.datacleaner.user.UserPreferences;
import org.jdesktop.swingx.VerticalLayout;

public class SelectDatastorePanel extends DCPanel {

    private static final long serialVersionUID = 1L;

    private final ExistingDatastorePanel _existingDatastoresPanel;

    public SelectDatastorePanel(DCModule dcModule, DatabaseDriverCatalog databaseDriverCatalog,
            DatastoreCatalog datastoreCatalog, UserPreferences userPreferences,
            DatastoreSelectedListener datastoreSelectListener, boolean showExistingDatastoresAsLongList) {
        super();
        setLayout(new VerticalLayout());

        add(Box.createVerticalStrut(20));

        if (showExistingDatastoresAsLongList) {
            // no need to show this label if existing datastores are shown as
            // popup button
            final JComponent newDatastoreLabel = DCSplashPanel.createSubtitleLabel("Use new datastore");
            add(newDatastoreLabel);
        }

        add(new AddDatastorePanel(datastoreCatalog, databaseDriverCatalog, dcModule, datastoreSelectListener,
                userPreferences, !showExistingDatastoresAsLongList));

        if (showExistingDatastoresAsLongList) {
            final JComponent existingDatastoreLabel = DCSplashPanel.createSubtitleLabel("Use existing datastore");
            _existingDatastoresPanel = new ExistingDatastorePanel(datastoreCatalog, datastoreSelectListener);

            add(Box.createVerticalStrut(20));
            add(existingDatastoreLabel);
            add(_existingDatastoresPanel);
        } else {
            _existingDatastoresPanel = null;
        }

        updateDatastores();
    }

    public void updateDatastores() {
        if (_existingDatastoresPanel != null) {
            _existingDatastoresPanel.updateDatastores();
        }
    }
}
