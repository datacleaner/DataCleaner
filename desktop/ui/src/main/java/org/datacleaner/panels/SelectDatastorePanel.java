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

import java.awt.BorderLayout;

import javax.swing.Box;

import org.datacleaner.database.DatabaseDriverCatalog;
import org.datacleaner.guice.InjectorBuilder;
import org.datacleaner.user.DatastoreSelectedListener;
import org.datacleaner.user.MutableDatastoreCatalog;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCLabel;
import org.jdesktop.swingx.VerticalLayout;

public class SelectDatastorePanel extends DCSplashPanel {

    private static final long serialVersionUID = 1L;

    public SelectDatastorePanel(DCGlassPane glassPane, InjectorBuilder injectorBuilder,
            DatabaseDriverCatalog databaseDriverCatalog, MutableDatastoreCatalog datastoreCatalog,
            DatastoreSelectedListener datastoreSelectListener) {
        final DCPanel containerPanel = new DCPanel();
        containerPanel.setLayout(new VerticalLayout());
        
        containerPanel.add(Box.createVerticalStrut(20));

        final DCLabel newDatastoreLabel = DCLabel.bright("Use new datastore");
        newDatastoreLabel.setFont(WidgetUtils.FONT_HEADER2);
        containerPanel.add(newDatastoreLabel);

        containerPanel.add(new AddDatastorePanel(datastoreCatalog, databaseDriverCatalog, injectorBuilder,
                datastoreSelectListener));

        containerPanel.add(Box.createVerticalStrut(20));

        final DCLabel existingDatastoreLabel = DCLabel.bright("Use existing datastore");
        existingDatastoreLabel.setFont(WidgetUtils.FONT_HEADER2);
        containerPanel.add(existingDatastoreLabel);

        final ExistingDatastorePanel existingDatastoresPanel = new ExistingDatastorePanel(datastoreCatalog,
                datastoreSelectListener);
        containerPanel.add(existingDatastoresPanel);
        setLayout(new BorderLayout());

        add(createTitleLabel("Select datastore"), BorderLayout.NORTH);
        add(wrapContentInScrollerWithMaxWidth(containerPanel), BorderLayout.CENTER);
    }
}
