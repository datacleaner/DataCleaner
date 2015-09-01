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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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
import org.datacleaner.connection.DatahubDatastore;
import org.datacleaner.guice.Nullable;
import org.datacleaner.metamodel.datahub.DatahubConnection;
import org.datacleaner.user.MutableDatastoreCatalog;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.user.UserPreferencesImpl;
import org.datacleaner.util.DCDocumentListener;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImmutableEntry;
import org.datacleaner.util.LookAndFeelManager;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.util.http.MonitorHttpClient;
import org.datacleaner.widgets.DCLabel;
import org.jdesktop.swingx.JXTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

public class DatahubDatastoreDialog
        extends AbstractDatastoreDialog<DatahubDatastore> {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(DatahubDatastoreDialog.class);

    public static final String CAS_MODE = "cas";
    public static final String DEFAULT_MODE = "default";
    
    private static final long serialVersionUID = 1L;

    private final JXTextField _hostTextField;
    private final JXTextField _portTextField;
    private final JCheckBox _httpsCheckBox;
    private final JXTextField _usernameTextField;
    private final JPasswordField _passwordTextField;
    private final JXTextField _tenantNameTextField;
    private final JCheckBox _acceptUnverifiedSslPeersCheckBox;
    //private final JXTextField _securityModeTextField;
    private final JComboBox<String> _securityModeSelector;
    private final JButton _testButton;
    private final DCLabel _urlLabel;
//    private final JXTextField _contextPathTextField;

    public DatahubConnection createConnection() {
        int port = 8080;
        try {
            port = Integer.parseInt(_portTextField.getText());
        } catch (NumberFormatException e) {
            // do nothing, fall back to 8080.
        }

        final String username;
        final String password;
        if (true) {
            // if (_authenticationCheckBox.isSelected()) {
            username = _usernameTextField.getText();
            password = new String(_passwordTextField.getPassword());
        } else {
            username = null;
            password = null;
        }

        return new DatahubConnection(_hostTextField.getText(), port, username,
                password, _tenantNameTextField.getText(),
                /* _contextPathTextField.getText(), */_httpsCheckBox
                        .isSelected(),
                _acceptUnverifiedSslPeersCheckBox.isSelected(),
                _securityModeSelector.getSelectedItem().toString());
    }

    private void updateUrlLabel() {
        final DatahubConnection connection = createConnection();
        _urlLabel.setText("Repository url: " + connection.getRepositoryUrl());
    }

    @Inject
    public DatahubDatastoreDialog(WindowContext windowContext,
            MutableDatastoreCatalog datastoreCatalog,
            @Nullable DatahubDatastore originalDatastore,
            UserPreferences userPreferences) {
        super(originalDatastore, datastoreCatalog, windowContext, userPreferences);

        _hostTextField = WidgetFactory.createTextField("Hostname");
        _portTextField = WidgetFactory.createTextField("Port");
        _usernameTextField = WidgetFactory.createTextField("Username");
        _passwordTextField = WidgetFactory.createPasswordField();
        _tenantNameTextField = WidgetFactory.createTextField("Tenant id");
//        _contextPathTextField = WidgetFactory.createTextField("Context path");
        _securityModeSelector = new JComboBox<>(new String[] { DEFAULT_MODE, CAS_MODE });
        _securityModeSelector.setEditable(false);
        
        _urlLabel = DCLabel.bright("");
        _urlLabel.setForeground(WidgetUtils.BG_COLOR_LESS_BRIGHT);
        _urlLabel.setBorder(new EmptyBorder(5, 0, 5, 0));

        final DCDocumentListener genericDocumentListener = new DCDocumentListener() {
            @Override
            protected void onChange(DocumentEvent event) {
                validateAndUpdate();
                updateUrlLabel();
            }
        };

        _httpsCheckBox = new JCheckBox("Use HTTPS?", false);
        _httpsCheckBox.setOpaque(false);
        _httpsCheckBox.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
        _httpsCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                updateUrlLabel();
            }
        });

        _acceptUnverifiedSslPeersCheckBox = new JCheckBox("Accept unverified SSL peers?", false);
        _acceptUnverifiedSslPeersCheckBox.setOpaque(false);
        _acceptUnverifiedSslPeersCheckBox.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);

        _hostTextField.getDocument().addDocumentListener(genericDocumentListener);
        _portTextField.getDocument().addDocumentListener(genericDocumentListener);
        _usernameTextField.getDocument().addDocumentListener(genericDocumentListener);
        _passwordTextField.getDocument().addDocumentListener(genericDocumentListener);
        _securityModeSelector.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                validateAndUpdate();
                updateUrlLabel();
            }
        });

        _testButton = WidgetFactory.createDefaultButton("Test connection", IconUtils.ACTION_REFRESH);
        _testButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                final DatahubConnection connection = createConnection();
                final String pingUrl = connection.getRepositoryUrl() + "/ping";
                final HttpGet request = new HttpGet(pingUrl);
                try (final MonitorHttpClient monitorHttpClient = connection.getHttpClient()) {
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
                        LOGGER.info("Ping request responded: {}", map);
                        JOptionPane.showMessageDialog(
                                DatahubDatastoreDialog.this,
                                "Connection successful!");
                    } else {
                        final String reasonPhrase = statusLine
                                .getReasonPhrase();
                        WidgetUtils.showErrorMessage("Server reported error", "Server replied with status "
                                        + statusLine.getStatusCode() + ":\n"
                                        + reasonPhrase);
                    }
                } catch (Exception e) {
                    // TODO: This dialog is shown behind the modal dialog
                    WidgetUtils.showErrorMessage(
                            "Connection failed",
                            "Connecting to DataHub failed. Did you remember to fill in all the necessary fields?",
                            e);
                }
            }

        });

        if (originalDatastore != null) {
            _hostTextField.setText(originalDatastore.getHost());
            _portTextField.setText(originalDatastore.getPort() + "");
            _httpsCheckBox.setSelected(originalDatastore.https());
            _acceptUnverifiedSslPeersCheckBox.setSelected(originalDatastore.acceptUnverifiedSslPeers());
            if(originalDatastore.getSecurityMode().equalsIgnoreCase(CAS_MODE)) {
                _securityModeSelector.setSelectedIndex(1);
            } else {
                _securityModeSelector.setSelectedIndex(0);
            }

            _datastoreNameTextField.setText(originalDatastore.getName());
            _datastoreNameTextField.setEditable(false);

            _usernameTextField.setText(originalDatastore.getUsername());
            _passwordTextField.setText(originalDatastore.getPassword());
            _tenantNameTextField.setText(originalDatastore.getTenantName());
        } else {
            _hostTextField.setText("localhost");
            _portTextField.setText("8080");
        }
        updateUrlLabel();
    }

    @Override
    protected boolean validateForm() {
        final String host = _hostTextField.getText();
        if (StringUtils.isNullOrEmpty(host)) {
            setStatusError("Please enter DataHub host name");
            return false;
        }

        final String port = _portTextField.getText();
        if (StringUtils.isNullOrEmpty(port)) {
            setStatusError("Please enter DataHub port number");
            return false;
        } else {
            try {
                int portInt = Integer.parseInt(port);
                if (portInt <= 0) {
                    setStatusError("Please enter a valid (positive port number)");
                    return false;
                }
            } catch (NumberFormatException e) {
                setStatusError("Please enter a valid port number");
                return false;
            }
        }

        final String datastoreName = _datastoreNameTextField.getText();
        if (StringUtils.isNullOrEmpty(datastoreName)) {
            setStatusError("Please enter a datastore name");
            return false;
        }

        final String username = _usernameTextField.getText();
        if (StringUtils.isNullOrEmpty(username)) {
            setStatusError("Please enter username");
            return false;
        }

        final String tenantName = _tenantNameTextField.getText();
        if (StringUtils.isNullOrEmpty(tenantName)) {
            setStatusError("Please enter tenant name");
            return false;
        }

        setStatusValid();
        return true;
    }

    @Override
    protected DatahubDatastore createDatastore() {
        final String host = _hostTextField.getText();
        final Integer port = Integer.parseInt(_portTextField.getText());
        final String name = _datastoreNameTextField.getText();
        final String username = _usernameTextField.getText();
        final char[] passwordChars = _passwordTextField.getPassword();
        final String password = String.valueOf(passwordChars);
        final String tenantName = _tenantNameTextField.getText();
        final boolean https = _httpsCheckBox.isSelected();
        final boolean acceptUnverifiedSslPeersCheckBox = _acceptUnverifiedSslPeersCheckBox
                .isSelected();
        final String securityMode = _securityModeSelector.getSelectedItem().toString();

        return new DatahubDatastore(name, host, port, username, password,
                tenantName, https, acceptUnverifiedSslPeersCheckBox,
                securityMode);
    }

    @Override
    public String getWindowTitle() {
        return "DataHub datastore";
    }

    @Override
    protected String getBannerTitle() {
        return "DataHub";
    }

    @Override
    protected String getDatastoreIconPath() {
        return IconUtils.DATAHUB_IMAGEPATH;
    }

    @Override
    protected List<Entry<String, JComponent>> getFormElements() {
        List<Entry<String, JComponent>> result = super.getFormElements();
        result.add(new ImmutableEntry<String, JComponent>("DataHub hostname", _hostTextField));
        result.add(new ImmutableEntry<String, JComponent>("DataHub port",  _portTextField));
        result.add(new ImmutableEntry<String, JComponent>("", _httpsCheckBox));
        result.add(new ImmutableEntry<String, JComponent>("", _acceptUnverifiedSslPeersCheckBox));
        result.add(new ImmutableEntry<String, JComponent>("Security mode",_securityModeSelector));
        result.add(new ImmutableEntry<String, JComponent>("DataHub username", _usernameTextField));
        result.add(new ImmutableEntry<String, JComponent>("DataHub password", _passwordTextField));
        result.add(new ImmutableEntry<String, JComponent>("DataHub tenant name", _tenantNameTextField));
        result.add(new ImmutableEntry<String, JComponent>(null, _urlLabel));
        result.add(new ImmutableEntry<String, JComponent>(null, _testButton));
        return result;
    }

    public static void main(String[] args) {
        LookAndFeelManager.get().init();
        UserPreferences userPreferences = new UserPreferencesImpl(null);
        WindowContext windowContext = new DCWindowContext(null, userPreferences, null);
        DatahubDatastoreDialog dialog = new DatahubDatastoreDialog(windowContext, null, null, userPreferences);

        dialog.open();
    }

}
