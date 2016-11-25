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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URI;
import java.util.List;

import javax.inject.Provider;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.TransferHandler;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import org.apache.metamodel.util.FileResource;
import org.apache.metamodel.util.HdfsResource;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.configuration.ServerInformationCatalog;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.connection.FileDatastore;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.server.EnvironmentBasedHadoopClusterInformation;
import org.datacleaner.server.HadoopClusterInformation;
import org.datacleaner.user.DatastoreSelectedListener;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.DatastoreCreationUtil;
import org.datacleaner.util.FileFilters;
import org.datacleaner.util.HadoopResource;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.windows.HdfsUrlChooser;
import org.datacleaner.windows.HdfsUrlChooser.OpenType;
import org.datacleaner.windows.OptionsDialog;
import org.datacleaner.windows.SelectHadoopClusterDialog;
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

    public Dropzone(final DatastoreCatalog datastoreCatalog, final ServerInformationCatalog serverInformationCatalog,
            final DatastoreSelectedListener datastoreSelectListener, final UserPreferences userPreferences,
            final WindowContext windowContext, final Provider<OptionsDialog> optionsDialogProvider) {
        super(WidgetUtils.BG_SEMI_TRANSPARENT);
        _datastoreCatalog = datastoreCatalog;
        _datastoreSelectListener = datastoreSelectListener;
        _userPreferences = userPreferences;
        setLayout(new GridBagLayout());

        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(
                new CompoundBorder(BorderFactory.createDashedBorder(WidgetUtils.BG_COLOR_MEDIUM, 3f, 3.0f, 3.0f, false),
                        new EmptyBorder(30, 30, 30, 30)));

        final DCLabel dropFileLabel = DCLabel.dark("<html><b>Drop file</b> here</html>");
        dropFileLabel.setFont(WidgetUtils.FONT_BANNER);
        add(dropFileLabel,
                new GridBagConstraints(0, 0, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(0, 0, 10, 0), 0, 0));

        // orclick button
        final JButton orClickButton = WidgetFactory.createPrimaryButton("(Click to browse)", IconUtils.FILE_FILE);
        orClickButton.setFont(WidgetUtils.FONT_HEADER2);
        orClickButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(orClickButton,
                new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                        new Insets(0, 0, 10, 0), 0, 0));
        orClickButton.addActionListener(e -> showFileChooser());
        // select hadoop file button
        final JButton selectHadoopButton =
                WidgetFactory.createPrimaryButton("Select Hadoop HDFS file", IconUtils.FILE_HDFS);
        selectHadoopButton.setFont(WidgetUtils.FONT_HEADER2);
        selectHadoopButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(selectHadoopButton,
                new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(0, 10, 10, 0), 0, 0));

        selectHadoopButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {

                String selectedServer = null;
                final String[] serverNames = serverInformationCatalog.getServerNames();
                // If there is only one configuration, it doesn't make sense to
                // show the selection of configuration dialog

                if (serverNames.length == 1) {
                    assert HadoopResource.DEFAULT_CLUSTERREFERENCE.equals(serverNames[0]);

                    // check if YARN_CONF_DIR or HADOOP_CONF_DIR is set
                    if (EnvironmentBasedHadoopClusterInformation.isConfigurationDirectoriesSpecified()) {
                        selectedServer = serverNames[0];
                    }
                }

                if (selectedServer == null) {
                    final SelectHadoopClusterDialog selectHadoopConfigurationDialog =
                            new SelectHadoopClusterDialog(windowContext, serverInformationCatalog,
                                    optionsDialogProvider);
                    selectHadoopConfigurationDialog.setVisible(true);
                    selectedServer = selectHadoopConfigurationDialog.getSelectedConfiguration();
                }

                if (selectedServer != null) {
                    final URI selectedFile = HdfsUrlChooser
                            .showDialog(Dropzone.this, serverInformationCatalog, selectedServer, null, OpenType.LOAD);
                    logger.info("Selected HDFS file: " + selectedFile);

                    if (selectedFile != null) {
                        final HadoopClusterInformation server =
                                (HadoopClusterInformation) serverInformationCatalog.getServer(selectedServer);
                        final HdfsResource resource =
                                new HadoopResource(selectedFile, server.getConfiguration(), selectedServer);
                        final Datastore datastore = DatastoreCreationUtil
                                .createAndAddUniqueDatastoreFromResource(_datastoreCatalog, resource);
                        _datastoreSelectListener.datastoreSelected(datastore);
                    }
                }
            }

        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
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
                        final FileDatastore fileDatastore = (FileDatastore) existingDatastore;
                        final String datastoreFilename = fileDatastore.getFilename();
                        if (filename.equals(datastoreFilename) || filePath.equals(datastoreFilename)) {
                            datastore = _datastoreCatalog.getDatastore(filename);
                        }
                    }
                }
                if (datastore == null) {
                    datastore = DatastoreCreationUtil
                            .createAndAddUniqueDatastoreFromResource(_datastoreCatalog, new FileResource(file));
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
        final TransferHandler handler = new TransferHandler() {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean canImport(final TransferHandler.TransferSupport info) {
                // we only import FileList
                if (!info.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    return false;
                }
                return true;
            }

            @SuppressWarnings("unchecked")
            @Override
            public boolean importData(final TransferHandler.TransferSupport info) {
                if (!info.isDrop()) {
                    return false;
                }

                // Check for FileList flavor
                if (!info.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    logger.warn("List doesn't accept a drop of this type.");
                    return false;
                }

                // Get the fileList that is being dropped.
                final Transferable t = info.getTransferable();
                final List<File> data;
                try {
                    data = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
                } catch (final Exception e) {
                    return false;
                }
                if (data.size() != 1) {
                    logger.warn("Only one file/directory supported.");
                    return false;
                }
                final File file = data.get(0);
                if (!file.exists()) {
                    return false;
                }

                final Datastore datastore = DatastoreCreationUtil
                        .createAndAddUniqueDatastoreFromResource(_datastoreCatalog, new FileResource(file));
                _datastoreSelectListener.datastoreSelected(datastore);
                return true;
            }

        };
        setTransferHandler(handler);
    }

}
