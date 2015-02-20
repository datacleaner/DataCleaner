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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.datacleaner.connection.Datastore;
import org.datacleaner.database.DatabaseDriverCatalog;
import org.datacleaner.guice.InjectorBuilder;
import org.datacleaner.user.DatastoreChangeListener;
import org.datacleaner.user.DatastoreSelectedListener;
import org.datacleaner.user.MutableDatastoreCatalog;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.windows.AnalysisJobBuilderWindow;

/**
 * The usual container panel of {@link SelectDatastorePanel} when selecting to
 * build job from scratch.
 */
public class SelectDatastoreContainerPanel extends DCSplashPanel implements DatastoreSelectedListener,
        DatastoreChangeListener {

    private static final long serialVersionUID = 1L;

    private final SelectDatastorePanel _selectDatastorePanel;
    private final MutableDatastoreCatalog _datastoreCatalog;

    public SelectDatastoreContainerPanel(AnalysisJobBuilderWindow window, InjectorBuilder injectorBuilder,
            DatabaseDriverCatalog databaseDriverCatalog, MutableDatastoreCatalog datastoreCatalog,
            UserPreferences userPreferences) {
        super(window);
        _datastoreCatalog = datastoreCatalog;
        _selectDatastorePanel = new SelectDatastorePanel(injectorBuilder, databaseDriverCatalog, datastoreCatalog,
                userPreferences, this);

        setLayout(new BorderLayout());
        final JScrollPane scroll = wrapContent(_selectDatastorePanel);
        add(createTitleLabel("Select datastore", true), BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentHidden(ComponentEvent e) {
                scroll.getVerticalScrollBar().setValue(0);
            }
        });
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
                _selectDatastorePanel.updateDatastores();
            }
        });
    }

    @Override
    public void onRemove(Datastore datastore) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                _selectDatastorePanel.updateDatastores();
            }
        });
    }

    @Override
    public void datastoreSelected(Datastore datastore) {
        getWindow().setDatastore(datastore);
    }
}
