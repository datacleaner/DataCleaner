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
import java.awt.Image;
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
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.ws.rs.core.UriBuilder;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.HdfsUtils;
import org.datacleaner.util.LookAndFeelManager;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCComboBox;
import org.datacleaner.widgets.DCLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HdfsUrlChooser extends JComponent {

    protected static final Logger logger = LoggerFactory.getLogger(HdfsUrlChooser.class);
    
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

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {

            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value == null) {
                setText("");
                return this;
            }

            FileStatus fileStatus = (FileStatus) value;
            

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

        public void paintIcon(Component c, Graphics g, int x, int y) {

            if (c.getComponentOrientation().isLeftToRight()) {
                _icon.paintIcon(c, g, x + _depth * space, y);
            } else {
                _icon.paintIcon(c, g, x, y);
            }
        }

        public int getIconWidth() {
            return _icon.getIconWidth() + _depth * space;
        }

        public int getIconHeight() {
            return _icon.getIconHeight();
        }
    }

    class ServerComboBoxRenderer extends DefaultListCellRenderer {
        IndentIcon ii = new IndentIcon();

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {

            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value == null) {
                setText("");
                return this;
            }

            Path directory = (Path) value;
            final String directoryName = directory.getName();
            if (directoryName.isEmpty()) {
                setText(directory.toUri().toString());
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

        LinkedList<Path> directories = new LinkedList<>();
        int[] depths = null;

        Path selectedDirectory = null;

        public HdfsComboBoxModel() {
            updateDirectories();
        }

        public void updateDirectories() {
            Path dir = getCurrentDirectory();
            if (dir != null) {
                addItem(dir);
            }
        }

        private void addItem(Path directory) {
            if (directory == null) {
                return;
            }

            directories.clear();
            directories.add(getRoot());

            Path p = directory;
            List<Path> paths = new ArrayList<>(10);
            do {
                paths.add(p);
            } while ((p = p.getParent()) != null);

            int pathCount = paths.size();

            for (int i = 0; i < pathCount; i++) {
                p = paths.get(i);
                if (directories.contains(p)) {
                    int topIndex = directories.indexOf(p);
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
                Path dir = directories.get(i);
                Path parent = dir.getParent();
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

        public int getDepth(int i) {
            if (depths == null || i < 0 || i > depths.length) {
                return 0;
            }

            return depths[i];
        }

        @Override
        public Path getSelectedItem() {
            return selectedDirectory;
        }

        @Override
        public void setSelectedItem(Object selectedDirectory) {
            this.selectedDirectory = (Path) selectedDirectory;
            fireContentsChanged(this, -1, -1);
        }

        @Override
        public int getSize() {
            return directories.size();
        }

        @Override
        public Path getElementAt(int index) {
            return directories.get(index);
        }

    }

    public class HdfsDirectoryModel extends AbstractListModel<FileStatus> implements PropertyChangeListener {
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
                    FileStatus[] newFileStatuses = new FileStatus[fileStatuses.length + 1];
                    final FileStatus levelUp = new FileStatus();
                    levelUp.setSymlink(_currentDirectory.getParent());
                    newFileStatuses[0] = levelUp;
                    
                    for (int i = 0; i < fileStatuses.length; i++) {
                        newFileStatuses[i + 1] = fileStatuses[i];
                    }
                    fileStatuses = newFileStatuses;
                }
            } catch (IOException e) {
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

    final static int space = 10;
    public static String HDFS_SCHEME = "hdfs";
    private final OpenType _openType;
    private final JList<FileStatus> _fileList;
    private FileSystem _fileSystem;
    private JDialog _dialog = null;
    private Path _currentDirectory;
    private Path _selectedFile;

    

    private HdfsComboBoxModel _directoryComboBoxModel = new HdfsComboBoxModel();
    private DCComboBox<Path> _pathsComboBox;

    HdfsUrlChooser(URI uri, OpenType openType) {
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
        _pathsComboBox = new DCComboBox<>(_directoryComboBoxModel);
        _pathsComboBox.setRenderer(new ServerComboBoxRenderer());
        _pathsComboBox.setMinimumSize(new Dimension(300, 40));
        _pathsComboBox.addListener(new DCComboBox.Listener<Path>() {
            @Override
            public void onItemSelected(final Path directory) {
                _fileSystem = HdfsUtils.getFileSystemFromUri(directory.toUri());
                _currentDirectory = directory;
                ((HdfsDirectoryModel) _fileList.getModel()).updateFileList();
            }
        });
        _fileList = new JList<>(new HdfsDirectoryModel());
        _fileList.setLayoutOrientation(JList.VERTICAL_WRAP);
        _fileList.setPreferredSize(new Dimension(300, 200));
        _fileList.setCellRenderer(new HdfsFileListRenderer());
        _fileList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    // Double-click detected
                    final FileStatus element = _fileList.getModel().getElementAt(_fileList.getSelectedIndex());
                    if (element.isSymlink()) {
                        try {
                            _currentDirectory = element.getSymlink();
                        } catch (IOException e) {
                            logger.warn("Could not get the symlink value for element {}", element, e);
                        }
                    } else if (element.isDirectory()) {
                        _currentDirectory = element.getPath();
                    } else if (element.isFile()) {
                        _selectedFile = element.getPath();
                        logger.info("Selected: " + _selectedFile);
                        _dialog.dispose();
                    }
                    ((HdfsDirectoryModel) _fileList.getModel()).updateFileList();
                }
            }
        });
        setLayout(new BorderLayout());
        final DCPanel topPanel = new DCPanel();
        topPanel.setLayout(new GridBagLayout());
        topPanel.setPreferredSize(new Dimension(300, 40));
        WidgetUtils.addToGridBag(lookInLabel, topPanel, 0, 0, 1, 1, GridBagConstraints.WEST, 5);
        WidgetUtils.addToGridBag(_pathsComboBox, topPanel, 1, 0, 100, 40, GridBagConstraints.WEST, 5, 1, 1,
                GridBagConstraints.HORIZONTAL);

        add(topPanel, BorderLayout.NORTH);
        add(_fileList, BorderLayout.CENTER);
    }

    public static URI showDialog(Component parent, URI currentUri, OpenType openType) throws HeadlessException {
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
                    final URI uri = ServerAddressDialog.showHdfsNameNodeDialog(chooser, chooser.getUri());
                    if (uri != null) {
                        chooser.updateCurrentDirectory(new Path(uri));
                    } else {
                        chooser._dialog.setVisible(false);
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
            try {
                return new URI(selectedFile.toString());
            } catch (URISyntaxException e1) {
                throw new IllegalStateException(e1);
            }
        } 
        return null;
    }

    private void updateCurrentDirectory(final Path directory) {
        _currentDirectory = directory;
        _fileSystem = HdfsUtils.getFileSystemFromUri(directory.toUri());
        ((HdfsDirectoryModel) _fileList.getModel()).updateFileList();
        _directoryComboBoxModel.updateDirectories();
    }

    private Path getCurrentDirectory() {
        return _currentDirectory;
    }

    private Path getRoot() {
        Path p = getCurrentDirectory();
        while (!p.isRoot()) {
            p = p.getParent();
        }

        return p;
    }

    private void rescanServer() {
        if (getUri() != null) {
            try {
                _fileSystem.listStatus(new Path(getUri()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected void createDialog(Component chooser, Component parent, ComponentListener listener) {
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
    public static void main(String[] args) {
        LookAndFeelManager.get().init();
        final JFrame frame = new JFrame("test");

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.pack();
        frame.setVisible(true);

        try {
            URI selectedFile = HdfsUrlChooser.showDialog(frame, null, OpenType.LOAD);
            System.out.println("Normal exit, selected file: " + selectedFile);
            System.exit(0);
        } catch (Exception e) {
            System.err.println("Abnormal exit");
            e.printStackTrace(System.err);
            System.exit(1);
        }

    }
}
