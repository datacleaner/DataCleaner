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
import java.awt.GridBagLayout;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.event.DocumentEvent;

import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.configuration.ServerInformation;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.server.DirectConnectionHadoopClusterInformation;
import org.datacleaner.user.MutableServerInformationCatalog;
import org.datacleaner.util.DCDocumentListener;
import org.datacleaner.util.ErrorUtils;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.Alignment;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.widgets.DescriptionLabel;
import org.jdesktop.swingx.JXStatusBar;
import org.jdesktop.swingx.JXTextField;

/**
 * Dialog for creating and editing a direct connection to Hadoop namenode
 */
public class DirectConnectionHadoopClusterDialog extends AbstractDialog {

    private final DirectConnectionHadoopClusterInformation _directConnection;
    private final JXTextField _nameTextField;
    private final JXTextField _fileSystemURITextField;
    private final JXTextField _descriptionTextField;
    private final JLabel _statusLabel;
    private final JButton _saveButton;
    private final JButton _cancelButton;
    private final MutableServerInformationCatalog _mutableServerInformationCatalog;
    private ServerInformation _savedServer = null;
    private static final ImageManager imageManager = ImageManager.get();

    public DirectConnectionHadoopClusterDialog(WindowContext windowContext,
            DirectConnectionHadoopClusterInformation directConnection,
            MutableServerInformationCatalog serverinformationCatalog) {
        super(windowContext, ImageManager.get().getImage(IconUtils.FILE_HDFS));

        _directConnection = directConnection;
        _nameTextField = WidgetFactory.createTextField("My cluster");
        _fileSystemURITextField = WidgetFactory.createTextField("hdfs://<hostname>:<port>/");
        _descriptionTextField = WidgetFactory.createTextField("description");
        _mutableServerInformationCatalog = serverinformationCatalog;
        _statusLabel = DCLabel.bright("");

        final String saveButtonText = directConnection == null ? "Register cluster" : "Save cluster";
        _saveButton = WidgetFactory.createPrimaryButton(saveButtonText, IconUtils.ACTION_SAVE_BRIGHT);
        _saveButton.addActionListener(e -> {
            try {
                final URI nameNodeUri = new URI(_fileSystemURITextField.getText().trim());
                final DirectConnectionHadoopClusterInformation newServer = new DirectConnectionHadoopClusterInformation(
                        _nameTextField.getText(), _descriptionTextField.getText(), nameNodeUri);
                _savedServer = newServer;
                if (_directConnection != null) {
                    _mutableServerInformationCatalog.removeServer(_directConnection);
                }
                _mutableServerInformationCatalog.addServerInformation(newServer);
                close();
            } catch (URISyntaxException e1) {
                invalidateForm(e1);
                return;
            } catch (Exception exception) {
                invalidateForm(exception);
                return;
            }
        });

        _cancelButton = WidgetFactory.createDefaultButton("Cancel", IconUtils.ACTION_CANCEL);
        _cancelButton.addActionListener(e -> {
            DirectConnectionHadoopClusterDialog.this.close();
        });

        if (directConnection != null) {
            _nameTextField.setText(directConnection.getName());
            _nameTextField.setEnabled(false);
            _fileSystemURITextField.setText(directConnection.getNameNodeUri().toString());
            final String description = directConnection.getDescription();
            if (description != null) {
                _descriptionTextField.setText(description);
            }
        }

        final DCDocumentListener documentListener = new DCDocumentListener() {
            @Override
            protected void onChange(DocumentEvent event) {
                validateAndUpdate();
            }
        };

        _nameTextField.getDocument().addDocumentListener(documentListener);
        _fileSystemURITextField.getDocument().addDocumentListener(documentListener);
        _descriptionTextField.getDocument().addDocumentListener(documentListener);
    }

    private static final long serialVersionUID = 1L;

    @Override
    public String getWindowTitle() {
        return "Hadoop cluster - Direct namenode connection";
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

        int row = 0;
        WidgetUtils.addToGridBag(DCLabel.bright("Cluster name:"), formPanel, 0, row);
        WidgetUtils.addToGridBag(_nameTextField, formPanel, 1, row);

        row++;
        WidgetUtils.addToGridBag(DCLabel.bright("Description:"), formPanel, 0, row);
        WidgetUtils.addToGridBag(_descriptionTextField, formPanel, 1, row);

        row++;
        WidgetUtils.addToGridBag(DCLabel.bright("File system URI:"), formPanel, 0, row);
        WidgetUtils.addToGridBag(_fileSystemURITextField, formPanel, 1, row);

        final DCPanel buttonPanel = DCPanel.flow(Alignment.CENTER, _saveButton, _cancelButton);
        final DescriptionLabel descriptionLabel = new DescriptionLabel(
                "Fill out the connection information needed for DataCleaner to connect directly to the Apache Hadoop namenode and HDFS.");

        final DCPanel centerPanel = new DCPanel();
        centerPanel.setLayout(new BorderLayout());
        centerPanel.add(descriptionLabel, BorderLayout.NORTH);
        centerPanel.add(formPanel, BorderLayout.CENTER);
        centerPanel.add(buttonPanel, BorderLayout.SOUTH);

        final JXStatusBar statusBar = WidgetFactory.createStatusBar(_statusLabel);

        final DCPanel outerPanel = new DCPanel();
        outerPanel.setLayout(new BorderLayout());
        outerPanel.add(centerPanel, BorderLayout.CENTER);
        outerPanel.add(statusBar, BorderLayout.SOUTH);

        validateAndUpdate();

        outerPanel.setPreferredSize(getDialogWidth(), 300);

        return outerPanel;
    }

    public ServerInformation getSavedServer() {
        return _savedServer;
    }

    private void invalidateForm(Exception exception) {
        setStatusError(exception);
        setSaveButtonEnabled(false);
    }

    private void validateAndUpdate() {
        boolean valid = validateForm();
        setSaveButtonEnabled(valid);
    }

    private boolean validateForm() {
        final String connectionName = _nameTextField.getText();
        if (StringUtils.isNullOrEmpty(connectionName)) {
            setStatusError("Please enter a cluster name");
            return false;
        }

        final String connectionURI = _fileSystemURITextField.getText().trim();
        if (StringUtils.isNullOrEmpty(connectionURI)) {
            return false;
        }

        try {
            final URI uri = new URI(connectionURI);
            if (StringUtils.isNullOrEmpty(uri.getHost())) {
                setStatusError("Please enter a hostname");
                return false;
            }
            final int port = uri.getPort();
            if (port <= 0) {
                setStatusError("The port has to have minimum 4 digits");
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        setStatusValid();
        return true;
    }

    private void setSaveButtonEnabled(boolean enabled) {
        _saveButton.setEnabled(enabled);
    }

    @Override
    protected boolean isWindowResizable() {
        return true;
    }

    protected void setStatusError(Throwable error) {
        error = ErrorUtils.unwrapForPresentation(error);
        setStatusError(error.getMessage());
    }

    protected void setStatusError(String text) {
        _statusLabel.setText(text);
        _statusLabel.setIcon(imageManager.getImageIcon(IconUtils.STATUS_ERROR, IconUtils.ICON_SIZE_SMALL));
    }

    protected void setStatusValid() {
        _statusLabel.setText("Hadoop cluster ready");
        _statusLabel.setIcon(imageManager.getImageIcon(IconUtils.STATUS_VALID, IconUtils.ICON_SIZE_SMALL));
    }
}
