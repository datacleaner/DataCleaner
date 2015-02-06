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
import org.datacleaner.util.ImageManager;
import org.jdesktop.swingx.VerticalLayout;

public class ExistingDatastorePanel extends DCPanel {
    private static final long serialVersionUID = 1L;

    public ExistingDatastorePanel(final DatastoreCatalog datastoreCatalog,
            final DatastoreSelectedListener datastoreSelectListener) {
        setLayout(new VerticalLayout(4));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        ImageManager imageManager = ImageManager.get();

        String[] datastoreNames = datastoreCatalog.getDatastoreNames();
        for (int i = 0; i < datastoreNames.length; i++) {
            final Datastore datastore = datastoreCatalog.getDatastore(datastoreNames[i]);
            final DetailPanel datastorePanel = new DetailPanel(imageManager.getImageIcon(
                    IconUtils.CSV_IMAGEPATH, IconUtils.ICON_SIZE_LARGE), "<html><b>" + datastore.getName()
                    + "</b></html>", datastore.getDescription());

            datastorePanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    datastoreSelectListener.datastoreSelected(datastore);
                }
            });
            add(datastorePanel);
        }
    }
}
