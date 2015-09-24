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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.ws.rs.core.UriBuilder;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.http.client.utils.URIBuilder;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.LookAndFeelManager;
import org.datacleaner.util.NumberDocument;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.jdesktop.swingx.JXFormattedTextField;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.VerticalLayout;


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

    private class ServerChoiceDialog extends JComponent {
        private final JXTextField _hostnameField;
        private final JXFormattedTextField _portField;

        public ServerChoiceDialog(final Dialog dialog, final String host, final int port) {
            _hostnameField = WidgetFactory.createTextField("hostname", 10);
            if (host == null) {
                _hostnameField.setText("localhost");
            } else {
                _hostnameField.setText(host);
            }

            final NumberFormat integerFormat = NumberFormat.getIntegerInstance();
            integerFormat.setMaximumIntegerDigits(5);
            integerFormat.setMinimumIntegerDigits(1);
            _portField = WidgetFactory.createFormattedTextField("port", 4, integerFormat);
            _portField.setDocument(new NumberDocument(false, false));
            _portField.setText("9000");
            if (port == -1) {
                _portField.setText("9000");
            } else {
                _portField.setText(Integer.toString(port));
            }

            JButton okButton = new JButton("OK");
            okButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent aE) {
                    try {
                        final URI uri =
                                new URIBuilder().setScheme(HDFS_SCHEME).setHost(_hostnameField.getText()).setPort(Integer.parseInt(_portField.getText())).setPath("/").build();
                        // Try a read. TODO: Is there something better to verify HDFS connectivity?
                        FileSystem tempFS = getFileSystemFromUri(uri);
                        tempFS.listStatus(new Path(uri));

                        // Let's update the URI
                        _fileSystem = tempFS;
                        setUri(uri);
                        dialog.setVisible(false);

                    } catch (URISyntaxException e) {
                        JOptionPane.showMessageDialog(ServerChoiceDialog.this, "This server address is wrong", "Wrong server address", JOptionPane.ERROR_MESSAGE);
                    } catch (IOException | NumberFormatException e) {
                        // _fileSystem.makeQualified will throw illegal argument is it cannot connect.
                        JOptionPane.showMessageDialog(ServerChoiceDialog.this, "This server address is not available", "Server unavailable", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    dialog.setVisible(false);
                }
            });

            setLayout(new VerticalLayout(2));
            DCPanel propertiesPanel = new DCPanel();
            propertiesPanel.setLayout(new GridBagLayout());
            WidgetUtils.addToGridBag(DCLabel.dark("Hostname: "), propertiesPanel, 0, 0);
            WidgetUtils.addToGridBag(_hostnameField, propertiesPanel, 1, 0, 1, 1, GridBagConstraints.WEST, WidgetUtils.DEFAULT_PADDING, 1, 1, GridBagConstraints.HORIZONTAL);
            WidgetUtils.addToGridBag(DCLabel.dark("Port: "), propertiesPanel, 0, 1);
            WidgetUtils.addToGridBag(_portField, propertiesPanel, 1, 1, 1, 1, GridBagConstraints.WEST, WidgetUtils.DEFAULT_PADDING, 1, 1, GridBagConstraints.HORIZONTAL);

            add(propertiesPanel);
            add(DCPanel.flow(Alignment.RIGHT, okButton, cancelButton));
            setBorder(new EmptyBorder(WidgetUtils.DEFAULT_PADDING, WidgetUtils.DEFAULT_PADDING, WidgetUtils.DEFAULT_PADDING, WidgetUtils.DEFAULT_PADDING));

            // setPreferredSize(new Dimension(400, 300));
        }

    }

    protected class HdfsServerComboBoxModel extends AbstractListModel<Path> implements ComboBoxModel<Path> {
        List<Path> directories = new ArrayList<>();

        int[] depths = null;
        Path selectedDirectory = null;

        public HdfsServerComboBoxModel() {
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
            return (depths != null && i >= 0 && i < depths.length) ? depths[i] : 0;
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

    class IndentIcon implements Icon {
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

        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected,
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

    public class HdfsServerDirectoryModel extends AbstractListModel<FileStatus> implements PropertyChangeListener {
        private FileStatus[] _files;

        public HdfsServerDirectoryModel() {
            updateFileList();
        }

        private void updateFileList() {
            if (_fileSystem == null || getUri() == null) {
                _files = new FileStatus[0];
                return;
            }
            FileStatus[] fileStatuses;
            try {
                fileStatuses = _fileSystem.listStatus(new Path(getUri()));
                System.out.println("updated!");
                // Natural ordering is the URL
                Arrays.sort(fileStatuses);
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

    class HdfsFileListRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected,
                boolean cellHasFocus) {

            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value == null) {
                setText("");
                return this;
            }

            FileStatus fileStatus = (FileStatus) value;
            final String directoryName = fileStatus.getPath().getName();
            setText(directoryName);

            if (fileStatus.isDirectory()) {
                setIcon(DIRECTORY_ICON);
            } else {
                setIcon(FILE_ICON);
            }

            return this;
        }
    }

    public static final Icon DIRECTORY_ICON = UIManager.getIcon("FileView.directoryIcon");
    public static final Icon FILE_ICON = UIManager.getIcon("FileView.fileIcon");
    public static final Icon COMPUTER_ICON = UIManager.getIcon("FileView.computerIcon");

    final static int space = 10;
    public static String HDFS_SCHEME = "hdfs";
    private final OpenType _openType;
    private final JList<FileStatus> _fileList;
    private FileSystem _fileSystem;
    private JDialog _dialog = null;

    private DCComboBox<Path> _pathsCombobox;

    private URI _uri;
    private HdfsServerComboBoxModel _directoryComboBoxModel = new HdfsServerComboBoxModel();

    HdfsUrlChooser(OpenType openType) {
        this(null, openType);
    }

    HdfsUrlChooser(URI uri, OpenType openType) {
        if (uri != null) {
            if (!HDFS_SCHEME.equals(uri.getScheme())) {
                throw new IllegalArgumentException("Only HDFS allowed");
            }

            uri = UriBuilder.fromUri(uri).replaceQuery(null).fragment(null).build();
            getFileSystemFromUri(uri);
            setUri(uri);
        }

        _openType = openType;
        final DCLabel lookInLabel = DCLabel.dark("Look in:");
        _pathsCombobox = new DCComboBox<>(new HdfsServerComboBoxModel());
        _pathsCombobox.setRenderer(new ServerComboBoxRenderer());
        _pathsCombobox.setMinimumSize(new Dimension(300, 40));
        _pathsCombobox.addListener(new DCComboBox.Listener<Path>() {
            @Override
            public void onItemSelected(final Path directory) {
                _fileSystem = getFileSystemFromUri(directory.toUri());
                setUri(directory.toUri());
                ((HdfsServerDirectoryModel) _fileList.getModel()).updateFileList();
            }
        });
        _fileList = new JList<>(new HdfsServerDirectoryModel());
        _fileList.setLayoutOrientation(JList.VERTICAL_WRAP);
        _fileList.setPreferredSize(new Dimension(300, 200));
        _fileList.setCellRenderer(new HdfsFileListRenderer());

        setLayout(new BorderLayout());
        final DCPanel topPanel = new DCPanel();
        topPanel.setLayout(new GridBagLayout());
        topPanel.setPreferredSize(new Dimension(300, 40));
        WidgetUtils.addToGridBag(lookInLabel, topPanel, 0, 0, 1, 1, GridBagConstraints.WEST, 5);
        WidgetUtils.addToGridBag(_pathsCombobox, topPanel, 1, 0, 100, 40, GridBagConstraints.WEST, 5, 1, 1, GridBagConstraints.HORIZONTAL);

        add(topPanel, BorderLayout.NORTH);
        add(_fileList, BorderLayout.CENTER);
    }

    public static URI showDialog(Component parent, URI currentUri, OpenType openType)
            throws HeadlessException {
        final HdfsUrlChooser chooser = new HdfsUrlChooser(currentUri, openType);
        if (chooser._dialog != null) {
            // Prevent to show second instance of _dialog if the previous one still exists
            return null;
        }

        final ComponentListener listener = new ComponentAdapter() {
            @Override
            public void componentShown(final ComponentEvent e) {
                if (chooser.getUri() == null) {
                    chooser.showServerInputDialog();
                }

                if (chooser.getUri() == null) {
                    chooser._dialog.setVisible(false);
                }

                ((HdfsServerDirectoryModel) chooser._fileList.getModel()).updateFileList();
            }
        };

        chooser.createDialog(parent, listener);

        if (chooser.getUri() != null) {
            chooser.rescanServer();
        }

        return chooser.getCurrentDirectory().toUri();
    }

    private static void initDialog(final JDialog dialog, JComponent component, Component parentComponent) {
        Container contentPane = dialog.getContentPane();

        contentPane.setLayout(new BorderLayout());
        contentPane.add(component, BorderLayout.CENTER);
        dialog.setResizable(false);
        if (JDialog.isDefaultLookAndFeelDecorated()) {
            boolean supportsWindowDecorations =
                    UIManager.getLookAndFeel().getSupportsWindowDecorations();
            if (supportsWindowDecorations) {
                dialog.setUndecorated(true);
            }
        }
        dialog.pack();
        dialog.setLocationRelativeTo(parentComponent);
    }

    public static void main(String[] args) {
        LookAndFeelManager.get().init();

        final JFrame frame = new JFrame("test");
        final DCPanel panel = new DCPanel(WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        JButton testDialogButton = new JButton("Test dialog");

        testDialogButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                HdfsUrlChooser.showDialog(frame, null, OpenType.LOAD);
            }
        });
        panel.add(testDialogButton);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
    }

    public Configuration getHadoopConfiguration(URI uri) {
        final Configuration conf = new Configuration();
        conf.set("fs.defaultFS", uri.toString());
        return conf;
    }

    private Path getCurrentDirectory() {
        return getUri() == null ? null : new Path(getUri());
    }

    private Path getRoot() {
        Path p = getCurrentDirectory();
        Path root = p;
        while ((p = p.getParent()) != null) {
            root = p;
        }

        return root;
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

    private boolean isDirectory(final Path file) {
        try {
            return _fileSystem.getFileStatus(file).isDirectory();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private JDialog createSimpleDialog(Component component) {
        while (!(component instanceof Window) && component != null) {
            component = component.getParent();
        }

        if (component instanceof Frame) {
            return new JDialog((Frame) component, _openType.getTitle(), true);
        } else if (component instanceof Dialog) {
            return new JDialog((Dialog) component, _openType.getTitle(), true);
        }

        throw new UnsupportedOperationException("Cannot create dialog for a component without a frame or dialog parent");
    }

    protected void createDialog(Component parent) {
        createDialog(parent, null);
    }

    protected void createDialog(Component parent, ComponentListener listener) {
        _dialog = createSimpleDialog(parent);
        initDialog(_dialog, this, parent);

        if (listener != null) {
            _dialog.addComponentListener(listener);
        }
        _dialog.setVisible(true);
        _dialog.dispose();
        _dialog = null;
    }

    private void showServerInputDialog() {
        final JDialog dialog = createSimpleDialog(this);
        String host = null;
        int port = -1;

        if (getUri() != null) {
            host = getUri().getHost();
            port = getUri().getPort();
        }

        final ServerChoiceDialog serverChoiceDialog = new ServerChoiceDialog(dialog, host, port);

        initDialog(dialog, serverChoiceDialog, this);

        // Modal, will not return until closed, and will update _uri
        dialog.setVisible(true);
        dialog.dispose();
    }

    FileSystem getFileSystemFromUri(URI uri) {
        try {
            URI baseUri = UriBuilder.fromUri(uri).replacePath("/").build();
            return FileSystem.newInstance(getHadoopConfiguration(baseUri));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public URI getUri() {
        return _uri;
    }

    public void setUri(URI uri) {
        _uri = uri;
        _pathsCombobox.setModel(new HdfsServerComboBoxModel());
    }

}
