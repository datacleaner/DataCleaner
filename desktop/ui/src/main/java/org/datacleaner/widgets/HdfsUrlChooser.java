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
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.ws.rs.core.UriBuilder;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.http.client.utils.URIBuilder;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.IconUtils;
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
        private final DCPanel _panel;

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
                                new URIBuilder().setScheme(HDFS_SCHEME).setHost(_hostnameField.getText()).setPort(Integer.parseInt(_portField.getText())).build();
                        // Try a read. TODO: Is there something better to verify HDFS connectivity?
                        _fileSystem.listStatus(new Path(uri));

                        // Let's update the URI
                        _uri = uri;
                        dialog.setVisible(false);

                    } catch (URISyntaxException e) {
                        JOptionPane.showMessageDialog(ServerChoiceDialog.this, "This server address is wrong", "Wrong server address", JOptionPane.ERROR_MESSAGE);
                    } catch (IOException e) {
                        // _fileSystem.makeQualified will throw illegal argument is it cannot connect.
                        JOptionPane.showMessageDialog(ServerChoiceDialog.this, "This server address is not available", "Server unavailable", JOptionPane.ERROR_MESSAGE);
                    } catch (NumberFormatException e){
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

            _panel = new DCPanel();
            _panel.setLayout(new VerticalLayout(2));
            _panel.add(_hostnameField);
            _panel.add(_portField);
            _panel.add(DCPanel.flow(Alignment.RIGHT, okButton, cancelButton));
        }

    }

    protected class HdfsServerComboBoxModel extends AbstractListModel<Object> implements ComboBoxModel<Object> {
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
        public Object getSelectedItem() {
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
        public Object getElementAt(int index) {
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
            setText(directory.getName());
            final int depth = _directoryComboBoxModel.getDepth(index);
            ii._depth = depth;
            if(depth == 0){
                ii._icon = FILE_ICON;
            }
            setIcon(ii);

            return this;
        }
    }

    public class HdfsServerDirectoryModel extends AbstractListModel<Object> implements PropertyChangeListener {
        private FileStatus[] _files;

        public HdfsServerDirectoryModel() {
            updateFileList();
        }

        private void updateFileList() {
            FileStatus[] fileStatuses;
            try {
                fileStatuses = _fileSystem.listStatus(new Path(_uri));
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
        public Object getElementAt(final int index) {
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
    final static int space = 10;
    public static String HDFS_SCHEME = "hdfs";
    private final FileSystem _fileSystem;
    private final OpenType _openType;
    private JDialog _dialog = null;
    private DCPanel _topPanel;
    private DCPanel _filePanel;
    private ServerComboBoxRenderer _serverComboBoxRenderer;
    private URI _uri;
    private HdfsServerComboBoxModel _directoryComboBoxModel = new HdfsServerComboBoxModel();
    private HdfsServerDirectoryModel _hdfsServerDirectoryModel;

    HdfsUrlChooser(OpenType openType) {
        this(null, openType);
    }

    HdfsUrlChooser(URI uri, OpenType openType) {
        if (uri != null) {
            if (!HDFS_SCHEME.equals(uri.getScheme())) {
                throw new IllegalArgumentException("Only HDFS allowed");
            }
            _uri = UriBuilder.fromUri(uri).replacePath(null).fragment(null).build();
        }

        _openType = openType;
        try {
            _fileSystem = FileSystem.newInstance(new Configuration());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Path getCurrentDirectory() {
        return _uri == null ? null : new Path(_uri);
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
        if (_uri != null) {
            try {
                _fileSystem.listStatus(new Path(_uri));
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

    public static URI showDialog(Component parent, URI currentUri, OpenType openType)
            throws HeadlessException {
        HdfsUrlChooser chooser = new HdfsUrlChooser(currentUri, openType);
        if (chooser._dialog != null) {
            // Prevent to show second instance of _dialog if the previous one still exists
            return null;
        }

        chooser.createDialog(parent);

        if (chooser._uri != null) {
            chooser.rescanServer();
        }

        return chooser.getCurrentDirectory().toUri();
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

    protected void createDialog(Component parent) throws HeadlessException {
        _dialog = createSimpleDialog(parent);
        initDialog(_dialog, this, parent);

        _dialog.setVisible(true);
        _dialog.dispose();
        _dialog = null;
    }

    private void createModel() {
        _hdfsServerDirectoryModel = new HdfsServerDirectoryModel();
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

    private void showServerInputDialog(){
        JDialog dialog = createSimpleDialog(this);
        ServerChoiceDialog serverChoiceDialog = new ServerChoiceDialog(dialog, _uri.getHost(), _uri.getPort());

        initDialog(dialog, serverChoiceDialog, this);

        // Modal, will not return until closed, and will update _uri
        dialog.setVisible(true);
        dialog.dispose();
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
}
