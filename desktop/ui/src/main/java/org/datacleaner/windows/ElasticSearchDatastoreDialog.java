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
import java.util.List;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPasswordField;
import javax.swing.event.DocumentEvent;

import org.apache.metamodel.schema.Schema;
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
import org.jdesktop.swingx.JXTextField;

public class ElasticSearchDatastoreDialog extends AbstractDatastoreDialog<ElasticSearchDatastore> implements
        SchemaFactory {

    private static final long serialVersionUID = 1L;

    private static final ElasticSearchDatastore.ClientType DEFAULT_CLIENT_TYPE = ElasticSearchDatastore.ClientType.TRANSPORT;

    private final JComboBox<ClientType> _clientTypeComboBox;
    private final JXTextField _hostnameTextField;
    private final JXTextField _portTextField;
    private final JXTextField _clusterNameTextField;
    private final JXTextField _indexNameTextField;
    private final JXTextField _usernameTextField;
    private final JPasswordField _passwordTextField;

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
        _passwordTextField = WidgetFactory.createPasswordField();

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
                        _passwordTextField.setEnabled(false);
                        _passwordTextField.setText("");
                    } else {
                        _hostnameTextField.setEnabled(true);
                        _portTextField.setEnabled(true);
                        _usernameTextField.setEnabled(true);
                        _passwordTextField.setEnabled(true);

                        if (originalDatastore != null) {
                            _hostnameTextField.setText(originalDatastore.getHostname());
                            _portTextField.setText("" + originalDatastore.getPort());
                            _usernameTextField.setText(originalDatastore.getUsername());
                            _passwordTextField.setText(originalDatastore.getPassword());
                        } else {
                            _hostnameTextField.setText("localhost");
                            _portTextField.setText("9300");
                        }
                    }
                }

            }
        });
        _datastoreNameTextField.getDocument().addDocumentListener(new DCDocumentListener() {
            @Override
            protected void onChange(DocumentEvent event) {
                validateAndUpdate();
            }
        });
        _hostnameTextField.getDocument().addDocumentListener(new DCDocumentListener() {
            @Override
            protected void onChange(DocumentEvent event) {
                validateAndUpdate();
            }
        });
        _portTextField.getDocument().addDocumentListener(new DCDocumentListener() {
            @Override
            protected void onChange(DocumentEvent event) {
                validateAndUpdate();
            }
        });
        _clusterNameTextField.getDocument().addDocumentListener(new DCDocumentListener() {
            @Override
            protected void onChange(DocumentEvent event) {
                validateAndUpdate();
            }
        });
        _indexNameTextField.getDocument().addDocumentListener(new DCDocumentListener() {
            @Override
            protected void onChange(DocumentEvent event) {
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
            _passwordTextField.setText(originalDatastore.getPassword());
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
        final String password = new String(_passwordTextField.getPassword());
        if (StringUtils.isNullOrEmpty(username) && StringUtils.isNullOrEmpty(password)) {
            return new ElasticSearchDatastore(name, hostname, port, clusterName, indexName, selectedClientType);
        } else {
            return new ElasticSearchDatastore(name, hostname, port, clusterName, indexName, username, password,
                    selectedClientType);
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
        result.add(new ImmutableEntry<String, JComponent>("Username", _usernameTextField));
        result.add(new ImmutableEntry<String, JComponent>("Password", _passwordTextField));
        return result;
    }

}
