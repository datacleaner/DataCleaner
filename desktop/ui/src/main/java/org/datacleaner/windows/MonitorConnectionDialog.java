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
import java.io.InputStream;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.metamodel.util.FileHelper;
import org.datacleaner.bootstrap.DCWindowContext;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.user.MonitorConnection;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.user.UserPreferencesImpl;
import org.datacleaner.util.DCDocumentListener;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.LookAndFeelManager;
import org.datacleaner.util.NumberDocument;
import org.datacleaner.util.SecurityUtils;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.util.http.MonitorHttpClient;
import org.datacleaner.widgets.Alignment;
import org.datacleaner.widgets.DCCheckBox;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.widgets.DescriptionLabel;
import org.jdesktop.swingx.JXTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

/**
 * Dialog for setting up the users connection to the DataCleaner monitor webapp.
 */
public class MonitorConnectionDialog extends AbstractDialog {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(MonitorConnectionDialog.class);
    private static final ImageManager imageManager = ImageManager.get();

    private final UserPreferences _userPreferences;
    private final DCCheckBox<Void> _httpsCheckBox;
    private final JXTextField _hostnameTextField;
    private final JXTextField _portTextField;
    private final JXTextField _contextPathTextField;
    private final JXTextField _tenantTextField;

    private final DCCheckBox<Void> _authenticationCheckBox;
    private final JXTextField _usernameTextField;
    private final JPasswordField _passwordTextField;

    private final DCLabel _urlLabel;

    @Inject
    public MonitorConnectionDialog(final WindowContext windowContext, final UserPreferences userPreferences) {
        super(windowContext, imageManager.getImage("images/window/banner-dq-monitor.png"));
        _userPreferences = userPreferences;

        final MonitorConnection monitorConnection = _userPreferences.getMonitorConnection();

        _urlLabel = DCLabel.bright("");
        _urlLabel.setForeground(WidgetUtils.BG_COLOR_LESS_BRIGHT);
        _urlLabel.setBorder(new EmptyBorder(0, 0, 25, 0));

        _httpsCheckBox = new DCCheckBox<>("Use HTTPS?", false);
        if (monitorConnection != null && monitorConnection.isHttps()) {
            _httpsCheckBox.setSelected(true);
        }
        _httpsCheckBox.setBorderPainted(false);
        _httpsCheckBox.setOpaque(false);
        _httpsCheckBox.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
        _httpsCheckBox.addListener((item, selected) -> updateUrlLabel());

        _hostnameTextField = WidgetFactory.createTextField("Hostname");
        if (monitorConnection != null && monitorConnection.getHostname() != null) {
            _hostnameTextField.setText(monitorConnection.getHostname());
        } else {
            _hostnameTextField.setText("localhost");
        }
        _hostnameTextField.getDocument().addDocumentListener(new DCDocumentListener() {
            @Override
            protected void onChange(final DocumentEvent event) {
                updateUrlLabel();
            }
        });

        _portTextField = WidgetFactory.createTextField("Port");
        _portTextField.setDocument(new NumberDocument(false));
        if (monitorConnection != null) {
            _portTextField.setText(monitorConnection.getPort() + "");
        } else {
            _portTextField.setText("8080");
        }
        _portTextField.getDocument().addDocumentListener(new DCDocumentListener() {
            @Override
            protected void onChange(final DocumentEvent event) {
                updateUrlLabel();
            }
        });

        _contextPathTextField = WidgetFactory.createTextField("Context path");
        if (monitorConnection != null) {
            _contextPathTextField.setText(monitorConnection.getContextPath());
        } else {
            _contextPathTextField.setText("DataCleaner-monitor");
        }
        _contextPathTextField.getDocument().addDocumentListener(new DCDocumentListener() {
            @Override
            protected void onChange(final DocumentEvent event) {
                updateUrlLabel();
            }
        });

        _tenantTextField = WidgetFactory.createTextField("Tenant ID");
        if (monitorConnection != null) {
            _tenantTextField.setText(monitorConnection.getTenantId());
        } else {
            _tenantTextField.setText("DC");
        }
        _tenantTextField.getDocument().addDocumentListener(new DCDocumentListener() {
            @Override
            protected void onChange(final DocumentEvent event) {
                updateUrlLabel();
            }
        });

        _usernameTextField = WidgetFactory.createTextField("Username");
        _passwordTextField = WidgetFactory.createPasswordField();

        _authenticationCheckBox = new DCCheckBox<>("Use authentication?", true);
        _authenticationCheckBox.setBorderPainted(false);
        _authenticationCheckBox.setOpaque(false);
        _authenticationCheckBox.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
        _authenticationCheckBox.addListener((item, selected) -> {
            _usernameTextField.setEnabled(selected);
            _passwordTextField.setEnabled(selected);
        });

        if (monitorConnection != null && monitorConnection.isAuthenticationEnabled()) {
            _authenticationCheckBox.setSelected(true);

            final String username = monitorConnection.getUsername();
            _usernameTextField.setText(username);

            final String decodedPassword = SecurityUtils.decodePassword(monitorConnection.getEncodedPassword());
            _passwordTextField.setText(decodedPassword);
        } else {
            _authenticationCheckBox.setSelected(false);
        }

        updateUrlLabel();
    }

    public MonitorConnection createMonitorConnection() {
        int port = 8080;
        try {
            port = Integer.parseInt(_portTextField.getText());
        } catch (final NumberFormatException e) {
            // do nothing, fall back to 8080.
        }

        final String username;
        final char[] password;
        if (_authenticationCheckBox.isSelected()) {
            username = _usernameTextField.getText();
            password = _passwordTextField.getPassword();
        } else {
            username = null;
            password = null;
        }

        return new MonitorConnection(_userPreferences, _hostnameTextField.getText(), port,
                _contextPathTextField.getText(), _httpsCheckBox.isSelected(), _tenantTextField.getText(), username,
                password);
    }

    private void updateUrlLabel() {
        final MonitorConnection monitorConnection = createMonitorConnection();
        _urlLabel.setText("Repository url: " + monitorConnection.getRepositoryUrl());
    }

    @Override
    public String getWindowTitle() {
        return "DataCleaner monitor connection";
    }

    @Override
    protected String getBannerTitle() {
        return "DataCleaner monitor\nSet up connection";
    }

    @Override
    protected int getDialogWidth() {
        return 500;
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    protected JComponent getDialogContent() {
        final DCPanel formPanel = new DCPanel();

        int row = 0;
        WidgetUtils.addToGridBag(DCLabel.bright("Hostname:"), formPanel, 0, row);
        WidgetUtils.addToGridBag(_hostnameTextField, formPanel, 1, row);

        row++;
        WidgetUtils.addToGridBag(DCLabel.bright("Port:"), formPanel, 0, row);
        WidgetUtils.addToGridBag(_portTextField, formPanel, 1, row);

        row++;
        WidgetUtils.addToGridBag(DCLabel.bright("Context path:"), formPanel, 0, row);
        WidgetUtils.addToGridBag(_contextPathTextField, formPanel, 1, row);

        row++;
        WidgetUtils.addToGridBag(_httpsCheckBox, formPanel, 1, row);

        row++;
        WidgetUtils.addToGridBag(DCLabel.bright("Tenant ID:"), formPanel, 0, row);
        WidgetUtils.addToGridBag(_tenantTextField, formPanel, 1, row);

        row++;
        WidgetUtils.addToGridBag(_urlLabel, formPanel, 0, row, 2, 1);

        row++;
        WidgetUtils.addToGridBag(_authenticationCheckBox, formPanel, 1, row);

        row++;
        WidgetUtils.addToGridBag(DCLabel.bright("Username:"), formPanel, 0, row);
        WidgetUtils.addToGridBag(_usernameTextField, formPanel, 1, row);

        row++;
        WidgetUtils.addToGridBag(DCLabel.bright("Password:"), formPanel, 0, row);
        WidgetUtils.addToGridBag(_passwordTextField, formPanel, 1, row);

        formPanel.setBorder(WidgetUtils.BORDER_EMPTY);

        final JButton testButton = WidgetFactory.createDefaultButton("Test connection", IconUtils.ACTION_REFRESH);
        testButton.addActionListener(event -> {
            final MonitorConnection connection = createMonitorConnection();
            final String pingUrl = connection.getRepositoryUrl() + "/ping";
            final HttpGet request = new HttpGet(pingUrl);
            try (MonitorHttpClient monitorHttpClient = connection.getHttpClient()) {
                final HttpResponse response = monitorHttpClient.execute(request);

                final StatusLine statusLine = response.getStatusLine();

                if (statusLine.getStatusCode() == 200 || statusLine.getStatusCode() == 201) {
                    // read response as JSON.
                    final InputStream content = response.getEntity().getContent();
                    final Map<?, ?> map;
                    try {
                        map = new ObjectMapper().readValue(content, Map.class);
                    } finally {
                        FileHelper.safeClose(content);
                    }
                    logger.info("Ping request responded: {}", map);
                    JOptionPane.showMessageDialog(MonitorConnectionDialog.this, "Connection successful!");
                } else {
                    final String reasonPhrase = statusLine.getReasonPhrase();
                    WidgetUtils.showErrorMessage("Server reported error",
                            "Server replied with status " + statusLine.getStatusCode() + ":\n" + reasonPhrase);
                }
            } catch (final Exception e) {
                // TODO: This dialog is shown behind the modal dialog
                WidgetUtils.showErrorMessage("Connection failed",
                        "Connecting to DataCleaner monitor failed. Did you remember to fill in all the nescesary fields?",
                        e);
            }
        });

        final JButton saveButton = WidgetFactory.createPrimaryButton("Save connection", IconUtils.ACTION_SAVE_BRIGHT);
        saveButton.addActionListener(e -> {
            final MonitorConnection monitorConnection = createMonitorConnection();
            _userPreferences.setMonitorConnection(monitorConnection);

            MonitorConnectionDialog.this.close();
        });

        final DCPanel buttonPanel = DCPanel.flow(Alignment.CENTER, saveButton, testButton);
        buttonPanel.setBorder(WidgetUtils.BORDER_EMPTY);

        final DescriptionLabel descriptionLabel = new DescriptionLabel();
        descriptionLabel.setText(
                "The DataCleaner monitor is a separate web application that is part of the DataCleaner eco-system. "
                        + "In this dialog you can configure your connection to it. "
                        + "With the monitor you can create, share, monitor and govern current and historic data quality metrics. "
                        + "You can also set up alerts to react when certain metrics are out of their expected ranges.");

        final DCPanel panel = new DCPanel();
        panel.setLayout(new BorderLayout());
        panel.add(descriptionLabel, BorderLayout.NORTH);
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        panel.setPreferredSize(getDialogWidth(), 460);

        return panel;
    }

    @Override
    protected void initialize() {
        super.initialize();
        // set focus to password field if username field is already filled
        if (!StringUtils.isNullOrEmpty(_usernameTextField.getText())) {
            _passwordTextField.setBorder(WidgetUtils.BORDER_EMPHASIZE_FIELD);

            final boolean focused = _passwordTextField.requestFocusInWindow();
            assert focused;
        }
    }

    /**
     * Shows a dialog in blocking mode. Only to be used for very
     * important/blocking behaviour.
     *
     * Note that this way of displaying a dialog is not preferred since
     * unexpected exceptions cannot be caught for modal dialogs.
     */
    public void openBlocking() {
        // show modal dialog, this will block until
        // closed.
        setModal(true);
        setAlwaysOnTop(true);
        open();
    }

    public static void main(final String[] args) {
        LookAndFeelManager.get().init();
        final UserPreferences userPreferences = new UserPreferencesImpl(null);
        final WindowContext windowContext = new DCWindowContext(null, userPreferences, null);
        final MonitorConnectionDialog dialog = new MonitorConnectionDialog(windowContext, userPreferences);

        dialog.open();
    }
}
