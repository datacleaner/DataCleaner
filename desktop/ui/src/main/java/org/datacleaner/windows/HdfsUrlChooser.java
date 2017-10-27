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
package org.datacleaner.windows;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.ws.rs.core.UriBuilder;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.datacleaner.bootstrap.DCWindowContext;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.configuration.ServerInformation;
import org.datacleaner.configuration.ServerInformationCatalog;
import org.datacleaner.configuration.ServerInformationCatalogImpl;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.server.DirectConnectionHadoopClusterInformation;
import org.datacleaner.server.DirectoryBasedHadoopClusterInformation;
import org.datacleaner.server.EnvironmentBasedHadoopClusterInformation;
import org.datacleaner.server.HadoopClusterInformation;
import org.datacleaner.util.HadoopResource;
import org.datacleaner.util.HdfsUtils;
import org.datacleaner.util.LookAndFeelManager;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCComboBox;
import org.datacleaner.widgets.DCLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HdfsUrlChooser extends JComponent {
    public enum OpenType {
        LOAD("Open"), SAVE("Save");

        private final String _title;

        OpenType(final String title) {
            _title = title;
        }

        public String getTitle() {
            return _title;
        }
    }

    static class HdfsFileListRenderer extends DefaultListCellRenderer {

        private static final long serialVersionUID = 1L;

        public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index,
                final boolean isSelected, final boolean cellHasFocus) {

            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value == null) {
                setText("");
                return this;
            }

            final FileStatus fileStatus = (FileStatus) value;

            final String name;
            if (fileStatus.isDirectory()) {
                setIcon(DIRECTORY_ICON);
                name = fileStatus.getPath().getName();
            } else if (fileStatus.isFile()) {
                setIcon(FILE_ICON);
                name = fileStatus.getPath().getName();
            } else {
                name = "..";

                setIcon(LEVEL_UP_ICON);
            }

            setText(name);

            return this;
        }
    }

    static class IndentIcon implements Icon {
        Icon _icon = DIRECTORY_ICON;
        int _depth = 0;

        public void paintIcon(final Component c, final Graphics graphics, final int x, final int y) {

            if (c.getComponentOrientation().isLeftToRight()) {
                _icon.paintIcon(c, graphics, x + _depth * SPACE, y);
            } else {
                _icon.paintIcon(c, graphics, x, y);
            }
        }

        public int getIconWidth() {
            return _icon.getIconWidth() + _depth * SPACE;
        }

        public int getIconHeight() {
            return _icon.getIconHeight();
        }
    }

    class ServerComboBoxRenderer extends DefaultListCellRenderer {

        private static final long serialVersionUID = 1L;

        IndentIcon ii = new IndentIcon();

        public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index,
                final boolean isSelected, final boolean cellHasFocus) {

            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value == null) {
                setText("");
                return this;
            }

            final Path directory = (Path) value;
            final String directoryName = directory.getName();
            if (directoryName.isEmpty()) {
                final URI uri = directory.toUri();
                setText(uri.toString());
            } else {
                setText(directoryName);
            }

            final int depth = _directoryComboBoxModel.getDepth(index);
            ii._depth = depth;
            if (depth == 0) {
                ii._icon = COMPUTER_ICON;
            }
            setIcon(ii);

            return this;
        }
    }

    class HdfsComboBoxModel extends AbstractListModel<Path> implements ComboBoxModel<Path> {

        private static final long serialVersionUID = 1L;


        LinkedList<Path> directories = new LinkedList<>();
        int[] depths = null;

        Path selectedDirectory = null;

        public HdfsComboBoxModel() {
            updateDirectories();
        }

        public void updateDirectories() {
            final Path dir = getCurrentDirectory();
            if (dir != null) {
                addItem(dir);
            }
        }

        private void addItem(final Path directory) {
            if (directory == null) {
                return;
            }

            directories.clear();
            directories.add(getRoot());

            Path path = directory;
            final List<Path> paths = new ArrayList<>(10);
            do {
                paths.add(path);
            } while ((path = path.getParent()) != null);

            final int pathCount = paths.size();

            for (int i = 0; i < pathCount; i++) {
                path = paths.get(i);
                if (directories.contains(path)) {
                    final int topIndex = directories.indexOf(path);
                    for (int j = i - 1; j >= 0; j--) {
                        directories.add(topIndex + i - j, paths.get(j));
                    }
                    break;
                }
            }
            calculateDepths();
            setSelectedItem(directory);
        }

        private void calculateDepths() {
            depths = new int[directories.size()];
            for (int i = 0; i < depths.length; i++) {
                final Path dir = directories.get(i);
                final Path parent = dir.getParent();
                depths[i] = 0;
                if (parent != null) {
                    for (int j = i - 1; j >= 0; j--) {
                        if (parent.equals(directories.get(j))) {
                            depths[i] = depths[j] + 1;
                            break;
                        }
                    }
                }
            }
        }

        public int getDepth(final int index) {
            if (depths == null || index < 0 || index > depths.length) {
                return 0;
            }

            return depths[index];
        }

        @Override
        public Path getSelectedItem() {
            return selectedDirectory;
        }

        @Override
        public void setSelectedItem(final Object selectedDirectory) {
            this.selectedDirectory = (Path) selectedDirectory;
            fireContentsChanged(this, -1, -1);
        }

        @Override
        public int getSize() {
            return directories.size();
        }

        @Override
        public Path getElementAt(final int index) {
            return directories.get(index);
        }

    }

    public class HdfsDirectoryModel extends AbstractListModel<FileStatus> implements PropertyChangeListener {

        private static final long serialVersionUID = 1L;

        private FileStatus[] _files;

        public HdfsDirectoryModel() {
            updateFileList();
        }

        private void updateFileList() {
            if (_fileSystem == null || _currentDirectory == null) {
                _files = new FileStatus[0];
                return;
            }

            FileStatus[] fileStatuses;
            try {
                fileStatuses = _fileSystem.listStatus(_currentDirectory);
                // Natural ordering is the URL
                Arrays.sort(fileStatuses);

                // Add pointer to the parent directory
                if (!_currentDirectory.isRoot()) {
                    final FileStatus[] newFileStatuses = new FileStatus[fileStatuses.length + 1];
                    final FileStatus levelUp = new FileStatus();
                    levelUp.setSymlink(_currentDirectory.getParent());
                    newFileStatuses[0] = levelUp;

                    System.arraycopy(fileStatuses, 0, newFileStatuses, 1, fileStatuses.length);
                    fileStatuses = newFileStatuses;
                }
            } catch (final IOException e) {
                fileStatuses = new FileStatus[0];
            }

            synchronized (this) {
                _files = fileStatuses;
                fireContentsChanged(this, 0, getSize() - 1);
            }
        }

        @Override
        public int getSize() {
            synchronized (this) {
                return _files.length;
            }
        }

        @Override
        public FileStatus getElementAt(final int index) {
            synchronized (this) {
                return _files[index];
            }
        }

        @Override
        public void propertyChange(final PropertyChangeEvent evt) {
            updateFileList();
        }
    }

    public static final Icon DIRECTORY_ICON = UIManager.getIcon("FileView.directoryIcon");
    public static final Icon FILE_ICON = UIManager.getIcon("FileView.fileIcon");
    public static final Icon COMPUTER_ICON = UIManager.getIcon("FileView.computerIcon");
    public static final Icon LEVEL_UP_ICON = UIManager.getLookAndFeelDefaults().getIcon("FileChooser.upFolderIcon");
    protected static final Logger logger = LoggerFactory.getLogger(HdfsUrlChooser.class);
    private static final long serialVersionUID = 1L;
    private static final int DEFAULT_WIDTH = 600;
    private static final int SPACE = 10;
    public static String HDFS_SCHEME = "hdfs";
    private final OpenType _openType;
    private final JList<FileStatus> _fileList;

    private FileSystem _fileSystem;
    private JDialog _dialog = null;
    private Path _currentDirectory;
    private Path _selectedFile;

    private HdfsComboBoxModel _directoryComboBoxModel = new HdfsComboBoxModel();

    HdfsUrlChooser(URI uri, final OpenType openType) {
        if (uri != null) {
            if (!HDFS_SCHEME.equals(uri.getScheme())) {
                throw new IllegalArgumentException("Only HDFS allowed");
            }

            uri = UriBuilder.fromUri(uri).replaceQuery(null).fragment(null).build();
            HdfsUtils.getFileSystemFromUri(uri);
            _currentDirectory = new Path(uri);
        }

        _openType = openType;
        final DCLabel lookInLabel = DCLabel.dark("Look in:");
        final DCComboBox<Path> pathsComboBox = new DCComboBox<>(_directoryComboBoxModel);
        pathsComboBox.setRenderer(new ServerComboBoxRenderer());
        pathsComboBox.setMinimumSize(new Dimension(DEFAULT_WIDTH, 40));
        pathsComboBox.addListener(new DCComboBox.Listener<Path>() {
            @Override
            public void onItemSelected(final Path directory) {
                if (!directory.isAbsoluteAndSchemeAuthorityNull()) {
                    _fileSystem = HdfsUtils.getFileSystemFromUri(directory.toUri());
                }
                _currentDirectory = directory;
                ((HdfsDirectoryModel) _fileList.getModel()).updateFileList();
            }
        });
        _fileList = new JList<>(new HdfsDirectoryModel());
        _fileList.setLayoutOrientation(JList.VERTICAL_WRAP);
        _fileList.setPreferredSize(new Dimension(DEFAULT_WIDTH, 350));
        _fileList.setBorder(WidgetUtils.BORDER_THIN);
        _fileList.setCellRenderer(new HdfsFileListRenderer());
        _fileList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(final MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    selectOrBrowsePath(false);
                }
            }

            @Override
            public void mousePressed(final MouseEvent e) {
                tryPopup(e);
            }

            @Override
            public void mouseReleased(final MouseEvent e) {
                tryPopup(e);
            }

            private void tryPopup(final MouseEvent e) {
                if (e.isPopupTrigger()) {
                    _fileList.setSelectedIndex(_fileList.locationToIndex(e.getPoint()));
                    final JPopupMenu popupMenu = new JPopupMenu();
                    if (_fileList.getModel().getElementAt(_fileList.getSelectedIndex()).isDirectory()) {
                        final JMenuItem browseMenuItem = new JMenuItem("Browse");
                        browseMenuItem.addActionListener(e1 -> selectOrBrowsePath(false));
                        popupMenu.add(browseMenuItem);
                    }

                    final JMenuItem selectMenuItem = new JMenuItem("Select");
                    selectMenuItem.addActionListener(e1 -> selectOrBrowsePath(true));
                    popupMenu.add(selectMenuItem);
                    popupMenu.show(_fileList, e.getX(), e.getY());
                }
            }
        });
        setLayout(new BorderLayout());
        final DCPanel topPanel = new DCPanel();
        topPanel.setLayout(new GridBagLayout());
        topPanel.setPreferredSize(new Dimension(DEFAULT_WIDTH, 40));
        WidgetUtils.addToGridBag(lookInLabel, topPanel, 0, 0, 1, 1, GridBagConstraints.WEST, 5);
        WidgetUtils.addToGridBag(pathsComboBox, topPanel, 1, 0, 100, 40, GridBagConstraints.WEST, 5, 1, 1,
                GridBagConstraints.HORIZONTAL);

        add(topPanel, BorderLayout.NORTH);

        final DCPanel panel = DCPanel.around(_fileList);
        panel.setBorder(WidgetUtils.BORDER_EMPTY);
        add(panel, BorderLayout.CENTER);
    }

    public static URI showDialog(final Component parent, final ServerInformationCatalog serverInformationCatalog,
            final String selectedServer, final URI currentUri, final OpenType openType) throws HeadlessException {

        final HdfsUrlChooser chooser = new HdfsUrlChooser(currentUri, openType);
        if (chooser._dialog != null) {
            // Prevent to show second instance of _dialog if the previous one
            // still exists
            return null;
        }

        final ComponentListener listener = new ComponentAdapter() {
            @Override
            public void componentShown(final ComponentEvent e) {
                if (chooser._currentDirectory == null) {
                    final boolean configured = chooser.scanHadoopConfigFiles(serverInformationCatalog, selectedServer);
                    if (configured) {
                        chooser.updateDirectories();
                    } else {
                        final URI uri = HdfsServerAddressDialog.showHdfsNameNodeDialog(chooser, chooser.getUri());
                        if (uri != null) {
                            chooser.updateCurrentDirectory(new Path(uri));
                        } else {
                            chooser.updateCurrentDirectory(null);
                            chooser._dialog.setVisible(false);
                        }
                    }
                }
            }
        };

        chooser.createDialog(chooser, parent, listener);

        if (chooser.getUri() != null) {
            chooser.rescanServer();
        }

        final Path selectedFile = chooser.getSelectedFile();
        if (selectedFile != null) {
            return selectedFile.toUri();
        }
        return null;
    }

    private void selectOrBrowsePath(final boolean selectDirectory) {
        // Double-click detected
        final FileStatus element = _fileList.getModel().getElementAt(_fileList.getSelectedIndex());
        if (element.isSymlink()) {
            try {
                _currentDirectory = element.getSymlink();
            } catch (final IOException e) {
                logger.warn("Could not get the symlink value for element {}", element, e);
            }
        } else if (element.isFile() || (element.isDirectory() && selectDirectory)) {
            _selectedFile = element.getPath();
            logger.info("Selected: " + _selectedFile);
            _dialog.dispose();
        } else if (element.isDirectory()) {
            _currentDirectory = element.getPath();
            _directoryComboBoxModel.updateDirectories();
        }
        ((HdfsDirectoryModel) _fileList.getModel()).updateFileList();
    }

    /**
     * This scans Hadoop environment variables for a directory with configuration files
     *
     * @param serverInformationCatalog
     * @return True if a configuration was yielded.
     */
    private boolean scanHadoopConfigFiles(final ServerInformationCatalog serverInformationCatalog,
            final String selectedServer) {

        final HadoopClusterInformation clusterInformation;
        if (selectedServer != null) {
            clusterInformation = (HadoopClusterInformation) serverInformationCatalog.getServer(selectedServer);
        } else {
            clusterInformation = (HadoopClusterInformation) serverInformationCatalog
                    .getServer(HadoopResource.DEFAULT_CLUSTERREFERENCE);
        }

        if (clusterInformation == null) {
            return false;
        }

        final Configuration configuration = HdfsUtils.getHadoopConfigurationWithTimeout(clusterInformation);

        _currentDirectory = new Path("/");

        try {
            _fileSystem = FileSystem.newInstance(configuration);
        } catch (final IOException e) {
            throw new IllegalArgumentException("Illegal URI when showing HDFS chooser", e);
        }
        final HdfsDirectoryModel model = (HdfsDirectoryModel) _fileList.getModel();
        model.updateFileList();
        return model._files.length > 0;
    }

    private void updateDirectories() {
        _directoryComboBoxModel.updateDirectories();
    }


    private void updateCurrentDirectory(final Path directory) {
        _currentDirectory = directory;

        if (directory != null) {
            _fileSystem = HdfsUtils.getFileSystemFromUri(directory.toUri());
            ((HdfsDirectoryModel) _fileList.getModel()).updateFileList();
            updateDirectories();
        }
    }

    private Path getCurrentDirectory() {
        return _currentDirectory;
    }

    private Path getRoot() {
        Path path = getCurrentDirectory();
        while (!path.isRoot()) {
            path = path.getParent();
        }

        return path;
    }

    private void rescanServer() {
        if (_fileSystem != null && getUri() != null) {
            try {
                _fileSystem.listStatus(new Path(getUri()));
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected void createDialog(final Component chooser, final Component parent, final ComponentListener listener) {
        _dialog = WidgetFactory.createModalDialog(chooser, parent, _openType.getTitle(), true);

        if (listener != null) {
            _dialog.addComponentListener(listener);
        }
        _dialog.setVisible(true);
        _dialog.dispose();
        _dialog = null;
    }

    public URI getUri() {
        return _currentDirectory == null ? null : _currentDirectory.toUri();
    }

    private Path getSelectedFile() {
        return _selectedFile;
    }

    // Test
    public static void main(final String[] args) throws URISyntaxException {
        LookAndFeelManager.get().init();

        final List<ServerInformation> servers = new ArrayList<>();
        servers.add(new EnvironmentBasedHadoopClusterInformation("default", "hadoop conf dir"));
        servers.add(new DirectoryBasedHadoopClusterInformation("directory", "directopry set up",
                "C:\\Users\\claudiap\\git\\vagrant-vms\\bigdatavm\\hadoop_conf"));
        servers.add(new DirectConnectionHadoopClusterInformation("namenode", "directconnection",
                new URI("hdfs://192.168.0.255:9000/")));
        final ServerInformationCatalog serverInformationCatalog = new ServerInformationCatalogImpl(servers);

        final JFrame frame = new JFrame("test");

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(DEFAULT_WIDTH, 400);
        frame.pack();
        frame.setVisible(true);

        final WindowContext windowContext = new DCWindowContext(null, null);
        final SelectHadoopClusterDialog selectHadoopConfigurationDialog =
                new SelectHadoopClusterDialog(windowContext, serverInformationCatalog, null);
        selectHadoopConfigurationDialog.setVisible(true);
        final String selectedServer = selectHadoopConfigurationDialog.getSelectedConfiguration();

        try {

            final URI selectedFile =
                    HdfsUrlChooser.showDialog(frame, serverInformationCatalog, selectedServer, null, OpenType.LOAD);
            System.out.println("Normal exit, selected file: " + selectedFile);
            System.exit(0);
        } catch (final Exception e) {
            System.err.println("Abnormal exit");
            e.printStackTrace(System.err);
            System.exit(1);
        }

    }
}
