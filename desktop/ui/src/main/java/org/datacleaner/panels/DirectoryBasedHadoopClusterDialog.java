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

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;

import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.server.DirectoryBasedHadoopClusterInformation;
import org.datacleaner.user.MutableServerInformationCatalog;
import org.datacleaner.util.DCDocumentListener;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.Alignment;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.widgets.DescriptionLabel;
import org.datacleaner.widgets.FileSelectionListener;
import org.datacleaner.widgets.FilenameTextField;
import org.datacleaner.windows.AbstractDialog;
import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.JXTextField;
import org.jfree.ui.tabbedui.VerticalLayout;

public class DirectoryBasedHadoopClusterDialog extends AbstractDialog {

    public class DirectoryPathPanel extends DCPanel {

        private static final long serialVersionUID = 1L;

        private final FilenameTextField _directoryTextField;
        private final JButton _removeButton;
        private final JPanel _parent;
        private File _directory;

        public DirectoryPathPanel(File directory, JPanel parent) {
            _parent = parent;
            _directory = directory;
            if (directory == null) {
                _directoryTextField = new FilenameTextField(null, true);
            } else {
                _directoryTextField = new FilenameTextField(directory, true);
            }
            _directoryTextField.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            _directoryTextField.addFileSelectionListener(new FileSelectionListener() {

                @Override
                public void onSelected(FilenameTextField filenameTextField, File file) {
                    _directory = file;
                    validateAndUpdate();
                }
            });
            if (_directory != null) {
                _directoryTextField.setFile(_directory);
            }
            _removeButton = WidgetFactory.createSmallButton(IconUtils.ACTION_REMOVE_DARK);

            setLayout(new HorizontalLayout(5));
            add(_directoryTextField);
            add(_removeButton);
            setBorder(WidgetUtils.BORDER_TOP_PADDING);

            _removeButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    _pathPanels.remove(DirectoryPathPanel.this);
                    _parent.remove(DirectoryPathPanel.this);
                    validateAndUpdate();
                }
            });
        }

        public File getDirectory() {
            return _directory;
        }
    }

    private static final long serialVersionUID = 1L;
    private final JXTextField _nameTextField;
    private final JXTextField _descriptionTextField;
    private List<DirectoryPathPanel> _pathPanels;
    private DirectoryBasedHadoopClusterInformation _server;
    private final MutableServerInformationCatalog _serverInformationCatalog;
    private final JButton _saveButton;
    private final JButton _cancelButton;

    public DirectoryBasedHadoopClusterDialog(WindowContext windowContext,
            final DirectoryBasedHadoopClusterInformation server,
            MutableServerInformationCatalog serverInformationCatalog) {
        super(windowContext, ImageManager.get().getImage(IconUtils.FILE_HDFS));
        _server = server;
        _serverInformationCatalog = serverInformationCatalog;
        _nameTextField = WidgetFactory.createTextField("My cluster");
        _descriptionTextField = WidgetFactory.createTextField("Description");

        if (_server != null) {
            final String serverName = _server.getName();
            if (serverName != null) {
                _nameTextField.setText(serverName);
                _nameTextField.setEnabled(false);
            }
            final String description = _server.getDescription();
            if (description != null) {
                _descriptionTextField.setText(description);
            }
        }

        _nameTextField.getDocument().addDocumentListener(new DCDocumentListener() {
            @Override
            protected void onChange(DocumentEvent event) {
                validateAndUpdate();
            }
        });

        final String saveButtonText = server == null ? "Register cluster" : "Save cluster";
        _saveButton = WidgetFactory.createPrimaryButton(saveButtonText, IconUtils.ACTION_SAVE_BRIGHT);
        _cancelButton = WidgetFactory.createDefaultButton("Cancel", IconUtils.ACTION_CANCEL);
        _cancelButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                DirectoryBasedHadoopClusterDialog.this.close();
            }
        });

        _saveButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (_server != null) {
                    _serverInformationCatalog.removeServer(_server);
                }
                try {
                    final List<String> paths = getPathList();

                    final DirectoryBasedHadoopClusterInformation newServer = new DirectoryBasedHadoopClusterInformation(
                            _nameTextField.getText(), _descriptionTextField.getText(), paths.toArray(new String[paths
                                    .size()]));
                    _serverInformationCatalog.addServerInformation(newServer);
                } catch (Exception exception) {
                    WidgetUtils.showErrorMessage("Error", exception);
                    invalidateForm(exception);
                    return;
                }

                close();
            }
        });

    }

    private List<DirectoryPathPanel> getDirectoriesListPanel(JPanel parent) {
        _pathPanels = new ArrayList<>();
        if (_server != null) {
            final String[] directories = _server.getDirectories();
            if (directories != null) {
                for (String directory : directories) {
                    final DirectoryPathPanel directoryPanel = new DirectoryPathPanel(new File(directory),
                            parent);
                    _pathPanels.add(directoryPanel);
                }
            } else {
                _pathPanels.add(new DirectoryPathPanel(null, parent));
            }
        } else {
            _pathPanels.add(new DirectoryPathPanel(null, parent));
        }

        return _pathPanels;
    }

    @Override
    public String getWindowTitle() {
        return "Hadoop cluster - Configuration directory";
    }

    @Override
    protected String getBannerTitle() {
        return getWindowTitle();
    }

    @Override
    protected int getDialogWidth() {
        return 700;
    }

    @Override
    protected JComponent getDialogContent() {

        final DCPanel formPanel = new DCPanel();
        formPanel.setLayout(new GridBagLayout());

        final JPanel listPanel = new DCPanel();
        listPanel.setLayout(new VerticalLayout());
        listPanel.addContainerListener(new ContainerListener() {

            @Override
            public void componentRemoved(ContainerEvent e) {
                formPanel.revalidate();
                formPanel.repaint();
            }

            @Override
            public void componentAdded(ContainerEvent e) {
                formPanel.revalidate();
                formPanel.repaint();
            }
        });

        _pathPanels = getDirectoriesListPanel(listPanel);
        for (int i = 0; i < _pathPanels.size(); i++) {
            listPanel.add(_pathPanels.get(i));
        }

        final JButton addPath = WidgetFactory.createSmallButton(IconUtils.ACTION_ADD_DARK);
        addPath.setToolTipText("Add path to configuration");
        addPath.addActionListener(e -> {
            final DirectoryPathPanel newPathPanel = new DirectoryPathPanel(null, listPanel);
            _pathPanels.add(newPathPanel);
            listPanel.add(newPathPanel);
        });

        WidgetUtils.addToGridBag(DCLabel.bright("Name:"), formPanel, 0, 0);
        WidgetUtils.addToGridBag(_nameTextField, formPanel, 1, 0);
        WidgetUtils.addToGridBag(DCLabel.bright("Description:"), formPanel, 0, 1);
        WidgetUtils.addToGridBag(_descriptionTextField, formPanel, 1, 1);
        WidgetUtils.addToGridBag(DCLabel.bright("Paths:"), formPanel, 0, 2);
        WidgetUtils.addToGridBag(listPanel, formPanel, 1, 2, 2, 1);
        WidgetUtils.addToGridBag(addPath, formPanel, 2, 3, GridBagConstraints.SOUTH);

        final DescriptionLabel descriptionLabel = new DescriptionLabel(
                "Add directories that contain the Apache Hadoop configuration files (such as core-site.xml and yarn-site.xml). DataCleaner will use these configuration files to determine how to connect to Apache Hadoop.");
        final DCPanel buttonsPanel = DCPanel.flow(Alignment.CENTER, _saveButton, _cancelButton);

        final DCPanel centerPanel = new DCPanel();
        centerPanel.setLayout(new BorderLayout());
        centerPanel.add(descriptionLabel, BorderLayout.NORTH);
        centerPanel.add(formPanel, BorderLayout.CENTER);
        centerPanel.add(buttonsPanel, BorderLayout.PAGE_END);

        validateAndUpdate();

        centerPanel.setPreferredSize(getDialogWidth(), 300);

        return centerPanel;

    }

    private void validateAndUpdate() {
        boolean valid = validateForm();
        setSaveButtonEnabled(valid);
    }

    private boolean validateForm() {
        final List<String> paths = getPathList();
        // We do not save a connection if there are no paths
        if (paths.size() == 0) {
            return false;
        }
        final String connectionName = _nameTextField.getText();
        if (StringUtils.isNullOrEmpty(connectionName)) {
            return false;
        }
        return true;
    }
    
    private void invalidateForm(Exception exception) {
        setSaveButtonEnabled(false);
    }

    private void setSaveButtonEnabled(boolean enabled) {
        _saveButton.setEnabled(enabled);
    }

    @Override
    protected boolean isWindowResizable() {
        return true;
    }

    private List<String> getPathList() {
        final List<String> paths = new ArrayList<>();
        for (int i = 0; i < _pathPanels.size(); i++) {
            final DirectoryPathPanel directoryPathPanel = _pathPanels.get(i);
            final File directory = directoryPathPanel.getDirectory();
            if (directory != null && directory.isDirectory()) {
                paths.add(directory.getPath());
            }
        }
        return paths;
    }

}
