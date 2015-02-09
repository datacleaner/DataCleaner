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
package org.datacleaner.widgets;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.TransferHandler;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import org.datacleaner.connection.Datastore;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.user.DatastoreSelectedListener;
import org.datacleaner.user.MutableDatastoreCatalog;
import org.datacleaner.util.DatastoreCreationUtil;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Dropzone extends DCPanel {
    
    private static final Logger logger = LoggerFactory.getLogger(Dropzone.class);
    private static final long serialVersionUID = 1L;
    private MutableDatastoreCatalog _datastoreCatalog;
    private DatastoreSelectedListener _datastoreSelectListener;

    public Dropzone(final MutableDatastoreCatalog datastoreCatalog,
            final DatastoreSelectedListener datastoreSelectListener) {
        super(WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        _datastoreCatalog = datastoreCatalog;
        _datastoreSelectListener = datastoreSelectListener;
        setBorder(new CompoundBorder(BorderFactory.createDashedBorder(WidgetUtils.BG_COLOR_DARK), new EmptyBorder(10,
                10, 10, 10)));
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        DCLabel dropFileLabel = DCLabel.dark("Drop file");
        dropFileLabel.setFont(WidgetUtils.FONT_BANNER);
        dropFileLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(dropFileLabel);

        add(Box.createVerticalStrut(10));

        JButton orClickButton = WidgetFactory.createPrimaryButton("(or click to use dialog)", IconUtils.DATASTORE_TYPE_FILE_BRIGHT);
        orClickButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(orClickButton);
        orClickButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                final DCFileChooser fileChooser = new DCFileChooser();
                final int result = fileChooser.showOpenDialog(Dropzone.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();

                    if (file.exists()) {
                        Datastore datastore = DatastoreCreationUtil.createAndAddUniqueDatastoreFromFile(
                                datastoreCatalog, file);
                        datastoreSelectListener.datastoreSelected(datastore);
                    }
                }
            }
        });
        makeDroppable();
    }

    private void makeDroppable() {
        TransferHandler handler = new TransferHandler() {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean canImport(TransferHandler.TransferSupport info) {
                // we only import FileList
                if (!info.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    return false;
                }
                return true;
            }

            @SuppressWarnings("unchecked")
            @Override
            public boolean importData(TransferHandler.TransferSupport info) {
                if (!info.isDrop()) {
                    return false;
                }

                // Check for FileList flavor
                if (!info.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    logger.warn("List doesn't accept a drop of this type.");
                    return false;
                }

                // Get the fileList that is being dropped.
                Transferable t = info.getTransferable();
                List<File> data;
                try {
                    data = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
                } catch (Exception e) {
                    return false;
                }
                if (data.size() != 1) {
                    logger.warn("Only one file/directory supported.");
                    return false;
                }
                File file = data.get(0);
                if (!file.exists()) {
                    return false;
                }
                
                Datastore datastore = DatastoreCreationUtil
                        .createAndAddUniqueDatastoreFromFile(_datastoreCatalog, file);
                _datastoreSelectListener.datastoreSelected(datastore);
                return true;
            }

        };
        setTransferHandler(handler);
    }

}
