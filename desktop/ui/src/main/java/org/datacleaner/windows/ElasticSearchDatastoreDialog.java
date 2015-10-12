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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.List;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.event.DocumentEvent;

import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.util.Resource;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.connection.ElasticSearchDatastore;
import org.datacleaner.connection.ElasticSearchDatastore.ClientType;
import org.datacleaner.guice.Nullable;
import org.datacleaner.user.MutableDatastoreCatalog;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.DCDocumentListener;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImmutableEntry;
import org.datacleaner.util.NumberDocument;
import org.datacleaner.util.SchemaFactory;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.FileSelectionListener;
import org.datacleaner.widgets.FilenameTextField;
import org.datacleaner.widgets.ResourceTypePresenter;
import org.jdesktop.swingx.JXTextField;

public class ElasticSearchDatastoreDialog extends AbstractDatastoreDialog<ElasticSearchDatastore> implements
        SchemaFactory {

    private static final long serialVersionUID = 1L;

    private static final ElasticSearchDatastore.ClientType DEFAULT_CLIENT_TYPE =
            ElasticSearchDatastore.ClientType.TRANSPORT;
    private static final boolean DEFAULT_SSL = false;

    private final JComboBox<ClientType> _clientTypeComboBox;
    private final JXTextField _hostnameTextField;
    private final JXTextField _portTextField;
    private final JXTextField _clusterNameTextField;
    private final JXTextField _indexNameTextField;
    private final JXTextField _usernameTextField;
    private final JPasswordField _passwordField;
    private final JCheckBox _sslCheckBox;
    private final FilenameTextField _keystorePathField;
    private final JPasswordField _keystorePasswordField;

    @Inject
    public ElasticSearchDatastoreDialog(WindowContext windowContext, MutableDatastoreCatalog catalog,
            @Nullable final ElasticSearchDatastore originalDatastore, UserPreferences userPreferences) {
        super(originalDatastore, catalog, windowContext, userPreferences);

        setSaveButtonEnabled(false);

        _clientTypeComboBox = new JComboBox<>(ClientType.values());
        _clientTypeComboBox.setSelectedItem(DEFAULT_CLIENT_TYPE);

        // Both NODE and TRANSPORT
        _clusterNameTextField = WidgetFactory.createTextField();
        _indexNameTextField = WidgetFactory.createTextField();

        // Only TRANSPORT
        _hostnameTextField = WidgetFactory.createTextField();
        _portTextField = WidgetFactory.createTextField();
        _portTextField.setDocument(new NumberDocument(false));
        _usernameTextField = WidgetFactory.createTextField();
        _passwordField = WidgetFactory.createPasswordField();
        _keystorePathField = new FilenameTextField(userPreferences.getOpenDatastoreDirectory(), true);
        // FIXME: Hack-ish way to make it fit...
        final double columns = WidgetFactory.TEXT_FIELD_COLUMNS * 0.6;
        _keystorePathField.getTextField().setColumns((int) columns);
        _keystorePathField.setEnabled(false);
        _keystorePasswordField = WidgetFactory.createPasswordField();
        _keystorePasswordField.setEnabled(false);

        _clientTypeComboBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                final int eventType = e.getStateChange();
                if (eventType == ItemEvent.SELECTED) {
                    final ElasticSearchDatastore.ClientType newSelectedItem = (ClientType) e.getItem();
                    if (newSelectedItem.equals(ElasticSearchDatastore.ClientType.NODE)) {
                        _hostnameTextField.setEnabled(false);
                        _hostnameTextField.setText("");
                        _portTextField.setEnabled(false);
                        _portTextField.setText("");
                        _usernameTextField.setEnabled(false);
                        _usernameTextField.setText("");
                        _passwordField.setEnabled(false);
                        _passwordField.setText("");
                        _sslCheckBox.setEnabled(false);
                        _sslCheckBox.setSelected(DEFAULT_SSL);
                        _keystorePathField.setEnabled(false);
                        _keystorePasswordField.setEnabled(false);
                    } else {
                        _hostnameTextField.setEnabled(true);
                        _portTextField.setEnabled(true);
                        _usernameTextField.setEnabled(true);
                        _passwordField.setEnabled(true);
                        _sslCheckBox.setEnabled(true);
                        if (_sslCheckBox.isSelected()) {
                            _keystorePathField.setEnabled(true);
                            _keystorePasswordField.setEnabled(_keystorePathField.getResource() != null);
                        }

                        if (originalDatastore != null) {
                            if (StringUtils.isNullOrEmpty(originalDatastore.getHostname())) {
                                _hostnameTextField.setText("localhost");
                            } else {
                                _hostnameTextField.setText(originalDatastore.getHostname());
                            }
                            if (originalDatastore.getPort() == null) {
                                _portTextField.setText("9300");
                            } else {
                                _portTextField.setText("" + originalDatastore.getPort());
                            }
                            _usernameTextField.setText(originalDatastore.getUsername());
                            _passwordField.setText(originalDatastore.getPassword());
                            _sslCheckBox.setSelected(originalDatastore.getSsl());
                            _keystorePathField.setFilename(originalDatastore.getKeystorePath());
                            _keystorePasswordField.setText(originalDatastore.getKeystorePassword());
                        } else {
                            _hostnameTextField.setText("localhost");
                            _portTextField.setText("9300");
                            _sslCheckBox.setSelected(DEFAULT_SSL);
                        }
                    }
                }

            }
        });


        final DCDocumentListener verifyAndUpdateDocumentListener = new DCDocumentListener() {

            @Override
            protected void onChange(DocumentEvent event) {
                validateAndUpdate();
            }
        };

        _sslCheckBox = new JCheckBox("Enable SSL", DEFAULT_SSL);
        _sslCheckBox.setOpaque(false);
        _sslCheckBox.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
        _sslCheckBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                int stateChange = e.getStateChange();

                if (stateChange == ItemEvent.SELECTED) {
                    _keystorePathField.setEnabled(true);
                    _keystorePasswordField.setEnabled(_keystorePathField.getResource() != null);
                    validateAndUpdate();
                }

                if (stateChange == ItemEvent.DESELECTED) {
                    _keystorePathField.setEnabled(false);
                    _keystorePasswordField.setEnabled(false);
                    validateAndUpdate();
                }
            }
        });

        _keystorePathField.addListener(new ResourceTypePresenter.Listener() {
            @Override
            public void onResourceSelected(final ResourceTypePresenter<?> presenter, final Resource resource) {
                _keystorePasswordField.setEnabled(true);
            }

            @Override
            public void onPathEntered(final ResourceTypePresenter<?> presenter, final String path) {
                _keystorePasswordField.setEnabled(false);
            }
        });

        _datastoreNameTextField.getDocument().addDocumentListener(verifyAndUpdateDocumentListener);
        _hostnameTextField.getDocument().addDocumentListener(verifyAndUpdateDocumentListener);
        _portTextField.getDocument().addDocumentListener(verifyAndUpdateDocumentListener);
        _clusterNameTextField.getDocument().addDocumentListener(verifyAndUpdateDocumentListener);
        _indexNameTextField.getDocument().addDocumentListener(verifyAndUpdateDocumentListener);
        _usernameTextField.getDocument().addDocumentListener(verifyAndUpdateDocumentListener);
        _passwordField.getDocument().addDocumentListener(verifyAndUpdateDocumentListener);
        _keystorePathField.getTextField().getDocument().addDocumentListener(verifyAndUpdateDocumentListener);
        _keystorePathField.addFileSelectionListener(new FileSelectionListener() {

            @Override
            public void onSelected(FilenameTextField filenameTextField, File file) {
                validateAndUpdate();

            }
        });

        if (originalDatastore == null) {
            _hostnameTextField.setText("localhost");
            _portTextField.setText("9300");
        } else {
            _clientTypeComboBox.setSelectedItem(originalDatastore.getClientType());
            _datastoreNameTextField.setText(originalDatastore.getName());
            _datastoreNameTextField.setEnabled(false);
            _hostnameTextField.setText(originalDatastore.getHostname());
            _portTextField.setText(originalDatastore.getPort() + "");
            _clusterNameTextField.setText(originalDatastore.getClusterName());
            _indexNameTextField.setText(originalDatastore.getIndexName());
            _usernameTextField.setText(originalDatastore.getUsername());
            _passwordField.setText(originalDatastore.getPassword());
            _sslCheckBox.setSelected(originalDatastore.getSsl());
            _keystorePathField.setFilename(originalDatastore.getKeystorePath());
            _keystorePasswordField.setText(originalDatastore.getKeystorePassword());
        }
    }

    @Override
    protected boolean validateForm() {
        final String datastoreName = _datastoreNameTextField.getText();
        if (StringUtils.isNullOrEmpty(datastoreName)) {
            setStatusError("Please enter a datastore name");
            return false;
        }

        if (ElasticSearchDatastore.ClientType.TRANSPORT.equals(_clientTypeComboBox.getSelectedItem())) {
            final String hostname = _hostnameTextField.getText();
            if (StringUtils.isNullOrEmpty(hostname)) {
                setStatusError("Please enter hostname");
                return false;
            }

            final String port = _portTextField.getText();
            if (StringUtils.isNullOrEmpty(port)) {
                setStatusError("Please enter port number");
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
        }

        final String clusterName = _clusterNameTextField.getText();
        if (StringUtils.isNullOrEmpty(clusterName)) {
            setStatusError("Please enter cluster name");
            return false;
        }

        final String indexName = _indexNameTextField.getText();
        if (StringUtils.isNullOrEmpty(indexName)) {
            setStatusError("Please enter index name");
            return false;
        }

        if (_sslCheckBox.isSelected()) {
            if (StringUtils.isNullOrEmpty(_usernameTextField.getText())) {
                setStatusError("Please enter the username");
                return false;
            }

            if (StringUtils.isNullOrEmpty(new String(_passwordField.getPassword()))) {
                setStatusError("Please enter the password");
                return false;
            }
        }

        setStatusValid();
        return true;
    }

    @Override
    public String getWindowTitle() {
        return "ElasticSearch index";
    }

    @Override
    protected String getBannerTitle() {
        return "ElasticSearch index";
    }

    @Override
    protected boolean isWindowResizable() {
        return true;
    }

    @Override
    protected int getDialogWidth() {
        return 400;
    }

    protected ElasticSearchDatastore createDatastore() {
        final String name = _datastoreNameTextField.getText();
        final String hostname = _hostnameTextField.getText();
        final ElasticSearchDatastore.ClientType selectedClientType = (ClientType) _clientTypeComboBox.getSelectedItem();
        final Integer port;
        if (ElasticSearchDatastore.ClientType.TRANSPORT.equals(selectedClientType)) {
            port = Integer.parseInt(_portTextField.getText());
        } else {
            port = null;
        }
        final String clusterName = _clusterNameTextField.getText();
        final String indexName = _indexNameTextField.getText();
        final String username = _usernameTextField.getText();
        final String password = new String(_passwordField.getPassword());
        final boolean ssl = _sslCheckBox.isSelected();
        final String keystorePath = _keystorePathField.getFilename();
        final String keystorePassword = new String(_keystorePasswordField.getPassword());
        if (StringUtils.isNullOrEmpty(username) && StringUtils.isNullOrEmpty(password)
                && StringUtils.isNullOrEmpty(keystorePath) && StringUtils.isNullOrEmpty(keystorePassword)) {
            return new ElasticSearchDatastore(name, selectedClientType, hostname, port, clusterName, indexName);
        } else {
            return new ElasticSearchDatastore(name, selectedClientType, hostname, port, clusterName, indexName,
                    username, password, ssl, keystorePath, keystorePassword);
        }
    }

    @Override
    public Schema createSchema() {
        final ElasticSearchDatastore datastore = createDatastore();
        try (final DatastoreConnection con = datastore.openConnection()) {
            final Schema schema = con.getDataContext().getDefaultSchema();
            return schema;
        }
    }

    @Override
    protected String getDatastoreIconPath() {
        return IconUtils.ELASTICSEARCH_IMAGEPATH;
    }

    @Override
    protected List<Entry<String, JComponent>> getFormElements() {
        List<Entry<String, JComponent>> result = super.getFormElements();
        result.add(new ImmutableEntry<String, JComponent>("Cluster name", _clusterNameTextField));
        result.add(new ImmutableEntry<String, JComponent>("Index name", _indexNameTextField));
        result.add(new ImmutableEntry<String, JComponent>("Client type", _clientTypeComboBox));
        result.add(new ImmutableEntry<String, JComponent>("Hostname", _hostnameTextField));
        result.add(new ImmutableEntry<String, JComponent>("Port", _portTextField));
        result.add(new ImmutableEntry<String, JComponent>("Credentials, if needed", new JLabel()));
        result.add(new ImmutableEntry<String, JComponent>("Username", _usernameTextField));
        result.add(new ImmutableEntry<String, JComponent>("Password", _passwordField));
        result.add(new ImmutableEntry<String, JComponent>("", _sslCheckBox));
        result.add(new ImmutableEntry<String, JComponent>("Keystore path", _keystorePathField));
        result.add(new ImmutableEntry<String, JComponent>("Keystore password", _keystorePasswordField));
        return result;
    }
}
