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
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
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
import org.jdesktop.swingx.JXTextField;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

public class DatahubDatastoreDialog extends AbstractDatastoreDialog<DatahubDatastore> {

    private static final long serialVersionUID = 1L;

    private final JXTextField _hostTextField;
    private final JXTextField _portTextField;
    private final JCheckBox _httpsCheckBox;
    private final JXTextField _usernameTextField;
    private final JPasswordField _passwordTextField;
    private final JXTextField _tenantNameTextField;
    private final JCheckBox _acceptUnverifiedSslPeersCheckBox;
    private final JXTextField _securityModeTextField;
    private final JButton _testButton;
    
    public DatahubConnection createConnection() {
        int port = 8080;
        try {
            port = Integer.parseInt(_portTextField.getText());
        } catch (NumberFormatException e) {
            // do nothing, fall back to 8080.
        }

        final String username;
//        final char[] password;
        final String password;
        if (true) {
//            if (_authenticationCheckBox.isSelected()) {
            username = _usernameTextField.getText();
//            password = _passwordTextField.getPassword();
            password = _passwordTextField.getText();
        } else {
            username = null;
            password = null;
        }

        return new DatahubConnection(_hostTextField.getText(), port, username,
                password, _tenantNameTextField.getText(),
                /*_contextPathTextField.getText(), */_httpsCheckBox.isSelected(), _acceptUnverifiedSslPeersCheckBox.isSelected(), _securityModeTextField.getText() );
    }

    @Inject
    public DatahubDatastoreDialog(WindowContext windowContext, MutableDatastoreCatalog datastoreCatalog,
            @Nullable DatahubDatastore originalDatastore, UserPreferences userPreferences) {
        super(originalDatastore, datastoreCatalog, windowContext, userPreferences);

        _hostTextField = WidgetFactory.createTextField("Hostname");
        _portTextField = WidgetFactory.createTextField("Port");
        _usernameTextField = WidgetFactory.createTextField("Username");
        _passwordTextField = WidgetFactory.createPasswordField();
        _tenantNameTextField = WidgetFactory.createTextField("Tenant id");
        _securityModeTextField = WidgetFactory.createTextField("Security mode");

        final DCDocumentListener genericDocumentListener = new DCDocumentListener() {
            @Override
            protected void onChange(DocumentEvent event) {
                validateAndUpdate();
            }
        };
        _httpsCheckBox = new JCheckBox("https", false);
        _httpsCheckBox.setOpaque(false);
        _httpsCheckBox.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);

        _acceptUnverifiedSslPeersCheckBox = new JCheckBox("accept unverified SSL peers", false);
        _acceptUnverifiedSslPeersCheckBox.setOpaque(false);
        _acceptUnverifiedSslPeersCheckBox.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);

        _hostTextField.getDocument().addDocumentListener(genericDocumentListener);
        _portTextField.getDocument().addDocumentListener(genericDocumentListener);
        _usernameTextField.getDocument().addDocumentListener(genericDocumentListener);
        _passwordTextField.getDocument().addDocumentListener(genericDocumentListener);
        _securityModeTextField.getDocument().addDocumentListener(genericDocumentListener);
        
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
//                        logger.info("Ping request responded: {}", map);
                        JOptionPane.showMessageDialog(DatahubDatastoreDialog.this, "Connection successful!");
                    } else {
                        final String reasonPhrase = statusLine.getReasonPhrase();
                        WidgetUtils.showErrorMessage("Server reported error", "Server replied with status "
                                + statusLine.getStatusCode() + ":\n" + reasonPhrase);
                    }
                } catch (Exception e) {
                    // TODO: This dialog is shown behind the modal dialog
                    WidgetUtils
                            .showErrorMessage(
                                    "Connection failed",
                                    "Connecting to Datahub failed. Did you remember to fill in all the necessary fields?",
                                    e);
                }
            }

        });

        if (originalDatastore != null) {
            _hostTextField.setText(originalDatastore.getHost());
            _portTextField.setText(originalDatastore.getPort() + "");
            _httpsCheckBox.setSelected(originalDatastore.https());
            _acceptUnverifiedSslPeersCheckBox.setSelected(originalDatastore.acceptUnverifiedSslPeers());
            _securityModeTextField.setText(originalDatastore.getSecurityMode());

            _datastoreNameTextField.setText(originalDatastore.getName());
            _datastoreNameTextField.setEditable(false);

            _usernameTextField.setText(originalDatastore.getUsername());
            _passwordTextField.setText(originalDatastore.getPassword());
            _tenantNameTextField.setText(originalDatastore.getTenantName());
        }
    }

    @Override
    protected boolean validateForm() {
        final String host = _hostTextField.getText();
        if (StringUtils.isNullOrEmpty(host)) {
            setStatusError("Please enter Datahub host name");
            return false;
        }

        final String port = _portTextField.getText();
        if (StringUtils.isNullOrEmpty(port)) {
            setStatusError("Please enter Datahub port number");
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
        final boolean acceptUnverifiedSslPeersCheckBox = _acceptUnverifiedSslPeersCheckBox.isSelected();
        final String securityMode = _securityModeTextField.getText();

        return new DatahubDatastore(name, host, port, username, password, tenantName, https, acceptUnverifiedSslPeersCheckBox, securityMode);
    }

    @Override
    public String getWindowTitle() {
        return "HIquality Datahub datastore";
    }

    @Override
    protected String getBannerTitle() {
        return "HIquality Datahub";
    }

    @Override
    protected String getDatastoreIconPath() {
        return IconUtils.DATAHUB_IMAGEPATH;
    }

    @Override
    protected List<Entry<String, JComponent>> getFormElements() {
        List<Entry<String, JComponent>> result = super.getFormElements();
        result.add(new ImmutableEntry<String, JComponent>("Datahub hostname", _hostTextField));
        result.add(new ImmutableEntry<String, JComponent>("Datahub port", _portTextField));
        result.add(new ImmutableEntry<String, JComponent>("https", _httpsCheckBox));
        result.add(new ImmutableEntry<String, JComponent>("acceptUnverifiedSslPeers", _acceptUnverifiedSslPeersCheckBox));
        result.add(new ImmutableEntry<String, JComponent>("Security mode", _securityModeTextField));
        result.add(new ImmutableEntry<String, JComponent>("Datahub username", _usernameTextField));
        result.add(new ImmutableEntry<String, JComponent>("Datahub password", _passwordTextField));
        result.add(new ImmutableEntry<String, JComponent>("Datahub tenant name", _tenantNameTextField));
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
