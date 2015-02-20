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

import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.database.DatabaseDriverCatalog;
import org.datacleaner.guice.InjectorBuilder;
import org.datacleaner.user.DatastoreSelectedListener;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCLabel;
import org.jdesktop.swingx.VerticalLayout;

public class SelectDatastorePanel extends DCPanel {

    private static final long serialVersionUID = 1L;

    private final ExistingDatastorePanel _existingDatastoresPanel;

    public SelectDatastorePanel(InjectorBuilder injectorBuilder, DatabaseDriverCatalog databaseDriverCatalog,
            DatastoreCatalog datastoreCatalog, UserPreferences userPreferences,
            DatastoreSelectedListener datastoreSelectListener) {
        super();
        setLayout(new VerticalLayout());

        add(Box.createVerticalStrut(20));

        final DCLabel newDatastoreLabel = DCLabel.dark("Use new datastore");
        newDatastoreLabel.setFont(WidgetUtils.FONT_HEADER2);
        add(newDatastoreLabel);

        add(new AddDatastorePanel(datastoreCatalog, databaseDriverCatalog, injectorBuilder,
                datastoreSelectListener, userPreferences));

        add(Box.createVerticalStrut(20));

        final DCLabel existingDatastoreLabel = DCLabel.dark("Use existing datastore");
        existingDatastoreLabel.setFont(WidgetUtils.FONT_HEADER2);
        add(existingDatastoreLabel);

        _existingDatastoresPanel = new ExistingDatastorePanel(datastoreCatalog, datastoreSelectListener);
        add(_existingDatastoresPanel);
        
        updateDatastores();
    }

    public void updateDatastores() {
        _existingDatastoresPanel.updateDatastores();
    }
}
