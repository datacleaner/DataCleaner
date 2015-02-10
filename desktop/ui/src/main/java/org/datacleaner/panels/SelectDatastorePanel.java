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
import javax.swing.SwingUtilities;

import org.datacleaner.connection.Datastore;
import org.datacleaner.database.DatabaseDriverCatalog;
import org.datacleaner.guice.InjectorBuilder;
import org.datacleaner.user.DatastoreChangeListener;
import org.datacleaner.user.DatastoreSelectedListener;
import org.datacleaner.user.MutableDatastoreCatalog;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.windows.AnalysisJobBuilderWindow;
import org.jdesktop.swingx.VerticalLayout;

public class SelectDatastorePanel extends DCSplashPanel implements DatastoreChangeListener {

    private static final long serialVersionUID = 1L;

    private final MutableDatastoreCatalog _datastoreCatalog;
    private final ExistingDatastorePanel _existingDatastoresPanel;

    public SelectDatastorePanel(AnalysisJobBuilderWindow window, DCGlassPane glassPane,
            InjectorBuilder injectorBuilder, DatabaseDriverCatalog databaseDriverCatalog,
            MutableDatastoreCatalog datastoreCatalog, UserPreferences userPreferences, DatastoreSelectedListener datastoreSelectListener) {
        super(window);
        _datastoreCatalog = datastoreCatalog;
        final DCPanel containerPanel = new DCPanel();
        containerPanel.setLayout(new VerticalLayout());

        containerPanel.add(Box.createVerticalStrut(20));

        final DCLabel newDatastoreLabel = DCLabel.dark("Use new datastore");
        newDatastoreLabel.setFont(WidgetUtils.FONT_HEADER2);
        containerPanel.add(newDatastoreLabel);

        containerPanel.add(new AddDatastorePanel(datastoreCatalog, databaseDriverCatalog, injectorBuilder,
                datastoreSelectListener, userPreferences));

        containerPanel.add(Box.createVerticalStrut(20));

        final DCLabel existingDatastoreLabel = DCLabel.dark("Use existing datastore");
        existingDatastoreLabel.setFont(WidgetUtils.FONT_HEADER2);
        containerPanel.add(existingDatastoreLabel);

        _existingDatastoresPanel = new ExistingDatastorePanel(datastoreCatalog, datastoreSelectListener);
        containerPanel.add(_existingDatastoresPanel);
        setLayout(new BorderLayout());

        add(createTitleLabel("Select datastore", true), BorderLayout.NORTH);
        add(wrapContentInScrollerWithMaxWidth(containerPanel), BorderLayout.CENTER);
    }

    public void updateDatastores() {
        _existingDatastoresPanel.updateDatastores();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        _datastoreCatalog.addListener(this);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        _datastoreCatalog.removeListener(this);
    }

    @Override
    public void onAdd(Datastore datastore) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                updateDatastores();
            }
        });
    }

    @Override
    public void onRemove(Datastore datastore) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                updateDatastores();
            }
        });
    }
}
