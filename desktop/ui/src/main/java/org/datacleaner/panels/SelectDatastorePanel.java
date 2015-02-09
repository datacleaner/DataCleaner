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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;

import org.datacleaner.database.DatabaseDriverCatalog;
import org.datacleaner.guice.InjectorBuilder;
import org.datacleaner.user.DatastoreSelectedListener;
import org.datacleaner.user.MutableDatastoreCatalog;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCLabel;

public class SelectDatastorePanel extends DCSplashPanel {

    private static final long serialVersionUID = 1L;

    public SelectDatastorePanel(DCGlassPane glassPane, InjectorBuilder injectorBuilder,
            DatabaseDriverCatalog databaseDriverCatalog, MutableDatastoreCatalog datastoreCatalog,
            DatastoreSelectedListener datastoreSelectListener) {
        setBorder(new EmptyBorder(10, 10, 10, 10));

        final DCPanel containerPanel = new DCPanel();
        containerPanel.setLayout(new GridBagLayout());

        DCLabel newDatastoreLabel = DCLabel.bright("Use new datastore");
        newDatastoreLabel.setFont(WidgetUtils.FONT_BANNER);
        newDatastoreLabel.setForeground(WidgetUtils.BG_COLOR_BLUE_MEDIUM);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.BASELINE_LEADING;
        containerPanel.add(newDatastoreLabel, c);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 1.0;
        c.anchor = GridBagConstraints.PAGE_START;
        c.fill = GridBagConstraints.HORIZONTAL;
        containerPanel.add(new AddDataStorePanel(datastoreCatalog, databaseDriverCatalog, injectorBuilder,
                datastoreSelectListener), c);

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.gridheight = 2;
        c.fill = GridBagConstraints.VERTICAL;
        containerPanel.add(new JSeparator(JSeparator.VERTICAL), c);

        DCLabel existingDatastoreLabel = DCLabel.bright("Use existing datastore");
        existingDatastoreLabel.setFont(WidgetUtils.FONT_BANNER);
        existingDatastoreLabel.setForeground(WidgetUtils.BG_COLOR_BLUE_MEDIUM);
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 0;
        c.insets = new Insets(0, 10, 0, 0);
        c.anchor = GridBagConstraints.BASELINE_LEADING;
        containerPanel.add(existingDatastoreLabel, c);

        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 1;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.BOTH;
        containerPanel.add(new ExistingDatastorePanel(datastoreCatalog, datastoreSelectListener), c);
        
        setLayout(new BorderLayout());
        add(wrapContentInScrollerWithMaxWidth(containerPanel), BorderLayout.CENTER);
    }
}
