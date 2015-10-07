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
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URI;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.TransferHandler;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import org.apache.metamodel.util.HdfsResource;
import org.datacleaner.connection.CsvDatastore;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.connection.FileDatastore;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.user.DatastoreSelectedListener;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.DatastoreCreationUtil;
import org.datacleaner.util.FileFilters;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.windows.HdfsUrlChooser;
import org.datacleaner.windows.HdfsUrlChooser.OpenType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A dropzone where the user can drop files to register them as datastores.
 */
public class Dropzone extends DCPanel {

    private static final Logger logger = LoggerFactory.getLogger(Dropzone.class);
    private static final long serialVersionUID = 1L;

    private final DatastoreCatalog _datastoreCatalog;
    private final DatastoreSelectedListener _datastoreSelectListener;
    private final UserPreferences _userPreferences;

    public Dropzone(final DatastoreCatalog datastoreCatalog, final DatastoreSelectedListener datastoreSelectListener,
            final UserPreferences userPreferences) {
        super(WidgetUtils.BG_SEMI_TRANSPARENT);
        _datastoreCatalog = datastoreCatalog;
        _datastoreSelectListener = datastoreSelectListener;
        _userPreferences = userPreferences;
        setLayout(new GridBagLayout());

        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(new CompoundBorder(BorderFactory.createDashedBorder(WidgetUtils.BG_COLOR_MEDIUM, 3f, 3.0f, 3.0f,
                false), new EmptyBorder(30, 30, 30, 30)));

        final DCLabel dropFileLabel = DCLabel.dark("<html><b>Drop file</b> here</html>");
        dropFileLabel.setFont(WidgetUtils.FONT_BANNER);
        add(dropFileLabel, new GridBagConstraints(0, 0, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER,
                GridBagConstraints.NONE, new Insets(0, 0, 10, 0), 0, 0));

        // orclick button
        final JButton orClickButton = WidgetFactory.createPrimaryButton("(Click to browse)", IconUtils.FILE_FILE);
        orClickButton.setFont(WidgetUtils.FONT_HEADER2);
        orClickButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(orClickButton, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.EAST,
                GridBagConstraints.NONE, new Insets(0, 0, 10, 0), 0, 0));
        orClickButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showFileChooser();
            }
        });
        // select hadoop file button
        final JButton selectHadoopButton = WidgetFactory.createPrimaryButton("Select Hadoop HDFS file",
                IconUtils.FILE_HDFS);
        selectHadoopButton.setFont(WidgetUtils.FONT_HEADER2);
        selectHadoopButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(selectHadoopButton, new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0, GridBagConstraints.WEST,
                GridBagConstraints.NONE, new Insets(0, 10, 10, 0), 0, 0));

        final Component dropZone = this;

        selectHadoopButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                URI selectedFile = HdfsUrlChooser.showDialog(dropZone, null, OpenType.LOAD);
                logger.info("Selected HDFS file: " + selectedFile);
                
                if (selectedFile != null) {
                    final Datastore datastore = new CsvDatastore(selectedFile.getPath(), new HdfsResource(selectedFile
                            .toString()));
                    _datastoreSelectListener.datastoreSelected(datastore);
                }
            }

        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showFileChooser();
            }
        });

        makeDroppable();
    }

    protected void showFileChooser() {
        final DCFileChooser fileChooser = new DCFileChooser(_userPreferences.getOpenDatastoreDirectory());
        fileChooser.addChoosableFileFilter(FileFilters.ALL);
        fileChooser.setFileFilter(FileFilters.allDataFiles());

        final int result = fileChooser.showOpenDialog(Dropzone.this);
        if (result == JFileChooser.APPROVE_OPTION) {
            final File file = fileChooser.getSelectedFile();

            if (file.exists()) {
                Datastore datastore = null;
                final String filename = file.getName();
                final String filePath = file.getAbsolutePath();
                final String[] datastoreNames = _datastoreCatalog.getDatastoreNames();
                for (int i = 0; i < datastoreNames.length; i++) {
                    final Datastore existingDatastore = _datastoreCatalog.getDatastore(datastoreNames[i]);
                    if (existingDatastore instanceof FileDatastore) {
                        FileDatastore fileDatastore = (FileDatastore) existingDatastore;
                        final String datastoreFilename = fileDatastore.getFilename();
                        if (filename.equals(datastoreFilename) || filePath.equals(datastoreFilename)) {
                            datastore = _datastoreCatalog.getDatastore(filename);
                        }
                    }
                }
                if (datastore == null) {
                    datastore = DatastoreCreationUtil.createAndAddUniqueDatastoreFromFile(_datastoreCatalog, file);
                }
                _datastoreSelectListener.datastoreSelected(datastore);

                final File directory;
                if (file.isFile()) {
                    directory = file.getParentFile();
                } else if (file.isDirectory()) {
                    directory = file;
                } else {
                    directory = _userPreferences.getOpenDatastoreDirectory();
                }
                _userPreferences.setOpenDatastoreDirectory(directory);
            }
        }
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
