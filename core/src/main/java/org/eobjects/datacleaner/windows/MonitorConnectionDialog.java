/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.windows;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.bootstrap.DCWindowContext;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.user.MonitorConnection;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.user.UserPreferencesImpl;
import org.eobjects.datacleaner.util.DCDocumentListener;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.LookAndFeelManager;
import org.eobjects.datacleaner.util.NumberDocument;
import org.eobjects.datacleaner.util.SecurityUtils;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.DCCheckBox;
import org.eobjects.datacleaner.widgets.DCCheckBox.Listener;
import org.eobjects.datacleaner.widgets.DCLabel;
import org.eobjects.datacleaner.widgets.DescriptionLabel;
import org.eobjects.metamodel.util.FileHelper;
import org.jdesktop.swingx.JXTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * Dialog for setting up the users connection to the DataCleaner DQ Monitor
 * webapp.
 */
public class MonitorConnectionDialog extends AbstractDialog {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(MonitorConnectionDialog.class);
    private static final ImageManager imageManager = ImageManager.getInstance();

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

    private final HttpClient _httpClient;

    @Inject
    public MonitorConnectionDialog(WindowContext windowContext, UserPreferences userPreferences, HttpClient httpClient) {
        super(windowContext, imageManager.getImage("images/window/banner-dq-monitor.png"));
        _userPreferences = userPreferences;
        _httpClient = httpClient;

        final MonitorConnection monitorConnection = _userPreferences.getMonitorConnection();

        _urlLabel = DCLabel.bright("");
        _urlLabel.setForeground(WidgetUtils.BG_COLOR_LESS_BRIGHT);
        _urlLabel.setBorder(new EmptyBorder(0, 0, 25, 0));

        _httpsCheckBox = new DCCheckBox<Void>("Use HTTPS?", false);
        if (monitorConnection != null && monitorConnection.isHttps()) {
            _httpsCheckBox.setSelected(true);
        }
        _httpsCheckBox.setBorderPainted(false);
        _httpsCheckBox.setOpaque(false);
        _httpsCheckBox.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
        _httpsCheckBox.addListener(new Listener<Void>() {
            @Override
            public void onItemSelected(Void item, boolean selected) {
                updateUrlLabel();
            }
        });

        _hostnameTextField = WidgetFactory.createTextField("Hostname");
        if (monitorConnection != null && monitorConnection.getHostname() != null) {
            _hostnameTextField.setText(monitorConnection.getHostname());
        } else {
            _hostnameTextField.setText("localhost");
        }
        _hostnameTextField.getDocument().addDocumentListener(new DCDocumentListener() {
            @Override
            protected void onChange(DocumentEvent event) {
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
            protected void onChange(DocumentEvent event) {
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
            protected void onChange(DocumentEvent event) {
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
            protected void onChange(DocumentEvent event) {
                updateUrlLabel();
            }
        });

        _usernameTextField = WidgetFactory.createTextField("Username");
        _passwordTextField = WidgetFactory.createPasswordField();

        _authenticationCheckBox = new DCCheckBox<Void>("Use authentication?", true);
        _authenticationCheckBox.setBorderPainted(false);
        _authenticationCheckBox.setOpaque(false);
        _authenticationCheckBox.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
        _authenticationCheckBox.addListener(new Listener<Void>() {
            @Override
            public void onItemSelected(Void item, boolean selected) {
                _usernameTextField.setEnabled(selected);
                _passwordTextField.setEnabled(selected);
            }
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
        } catch (NumberFormatException e) {
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

        return new MonitorConnection(_hostnameTextField.getText(), port, _contextPathTextField.getText(),
                _httpsCheckBox.isSelected(), _tenantTextField.getText(), username, password);
    }

    private void updateUrlLabel() {
        final MonitorConnection monitorConnection = createMonitorConnection();
        _urlLabel.setText("Repository url: " + monitorConnection.getRepositoryUrl());
    }

    @Override
    public String getWindowTitle() {
        return "DataCleaner dq monitor connection";
    }

    @Override
    protected String getBannerTitle() {
        return "DataCleaner dq monitor\nSet up connection";
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

        final JButton testButton = WidgetFactory.createButton("Test connection", "images/actions/refresh.png");
        testButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                final MonitorConnection connection = createMonitorConnection();
                connection.prepareClient(_httpClient);
                final String pingUrl = connection.getRepositoryUrl() + "/ping";
                try {
                    final HttpResponse response = _httpClient.execute(new HttpGet(pingUrl));
                    final StatusLine statusLine = response.getStatusLine();

                    if (statusLine.getStatusCode() == 200) {
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
                        WidgetUtils.showErrorMessage("Server reported error", "Server replied with status "
                                + statusLine.getStatusCode() + ":\n" + reasonPhrase, null);
                    }
                } catch (Exception e) {
                    WidgetUtils
                            .showErrorMessage(
                                    "Connection failed",
                                    "Connecting to DataCleaner dq monitor failed. Did you remember to fill in all the nescesary fields?",
                                    e);
                }
            }
        });

        final JButton saveButton = WidgetFactory.createButton("Save connection", "images/actions/save.png");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final MonitorConnection monitorConnection = createMonitorConnection();
                _userPreferences.setMonitorConnection(monitorConnection);

                MonitorConnectionDialog.this.close();
            }
        });

        final DCPanel buttonPanel = new DCPanel();
        buttonPanel.setBorder(WidgetUtils.BORDER_EMPTY);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        buttonPanel.add(testButton);
        buttonPanel.add(saveButton);

        final DescriptionLabel descriptionLabel = new DescriptionLabel();
        descriptionLabel
                .setText("The DataCleaner dq monitor is a separate web application that is part of the DataCleaner eco-system. "
                        + "In this dialog you can configure your connection to it. "
                        + "With the dq monitor you can create, share, monitor and govern current and historic data quality metrics. "
                        + "You can also set up alerts to react when certain metrics are out of their expected ranges.");

        final DCPanel panel = new DCPanel();
        panel.setLayout(new BorderLayout());
        panel.add(descriptionLabel, BorderLayout.NORTH);
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        panel.setPreferredSize(getDialogWidth(), 400);

        return panel;
    }
    
    @Override
    protected void initialize() {
        super.initialize();
        // set focus to password field if username field is already filled
        if (!StringUtils.isNullOrEmpty(_usernameTextField.getText())) {
            _passwordTextField.setBorder(WidgetUtils.BORDER_EMPHASIZE_FIELD);
            
            boolean focused = _passwordTextField.requestFocusInWindow();
            assert focused;
        }
    }

    public static void main(String[] args) {
        LookAndFeelManager.getInstance().init();
        UserPreferences userPreferences = new UserPreferencesImpl(null);
        WindowContext windowContext = new DCWindowContext(null, userPreferences, null);
        MonitorConnectionDialog dialog = new MonitorConnectionDialog(windowContext, userPreferences,
                new DefaultHttpClient());

        dialog.open();
    }
}
