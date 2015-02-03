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
