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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.border.EmptyBorder;

import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.user.DatastoreSelectedListener;
import org.datacleaner.util.IconUtils;
import org.jdesktop.swingx.VerticalLayout;

public class ExistingDatastorePanel extends DCPanel {

    private static final long serialVersionUID = 1L;

    private final DatastoreCatalog _datastoreCatalog;
    private final DatastoreSelectedListener _datastoreSelectListener;

    public ExistingDatastorePanel(final DatastoreCatalog datastoreCatalog,
            final DatastoreSelectedListener datastoreSelectListener) {
        _datastoreCatalog = datastoreCatalog;
        _datastoreSelectListener = datastoreSelectListener;
        setLayout(new VerticalLayout(4));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        updateDatastores();
    }

    public void updateDatastores() {
        removeAll();
        String[] datastoreNames = _datastoreCatalog.getDatastoreNames();
        for (int i = 0; i < datastoreNames.length; i++) {
            final Datastore datastore = _datastoreCatalog.getDatastore(datastoreNames[i]);
            final DetailedListItemPanel datastorePanel = new DetailedListItemPanel(IconUtils.getDatastoreIcon(
                    datastore, IconUtils.ICON_SIZE_LARGE), "<html><b>" + datastore.getName() + "</b></html>",
                    DatastorePanel.getDescription(datastore));

            datastorePanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    _datastoreSelectListener.datastoreSelected(datastore);
                }
            });
            add(datastorePanel);
        }
    }
}
