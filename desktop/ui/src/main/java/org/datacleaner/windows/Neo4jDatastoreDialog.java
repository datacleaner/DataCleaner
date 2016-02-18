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

import java.util.List;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.swing.JComponent;
import javax.swing.JPasswordField;
import javax.swing.event.DocumentEvent;

import org.apache.metamodel.schema.Schema;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.connection.Neo4jDatastore;
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

public class Neo4jDatastoreDialog extends AbstractDatastoreDialog<Neo4jDatastore> implements SchemaFactory {

    private static final long serialVersionUID = 1L;

    private final JXTextField _hostnameTextField;
    private final JXTextField _portTextField;
    private final JXTextField _usernameTextField;
    private final JPasswordField _passwordTextField;

    @Inject
    public Neo4jDatastoreDialog(WindowContext windowContext, MutableDatastoreCatalog catalog,
            @Nullable Neo4jDatastore originalDatastore, UserPreferences userPreferences) {
        super(originalDatastore, catalog, windowContext, userPreferences);

        setSaveButtonEnabled(false);

        _hostnameTextField = WidgetFactory.createTextField();
        _portTextField = WidgetFactory.createTextField();
        _portTextField.setDocument(new NumberDocument(false));
        _usernameTextField = WidgetFactory.createTextField();
        _passwordTextField = WidgetFactory.createPasswordField(20);

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
        _usernameTextField.getDocument().addDocumentListener(new DCDocumentListener() {
            @Override
            protected void onChange(DocumentEvent event) {
                validateAndUpdate();
            }
        });
        _passwordTextField.getDocument().addDocumentListener(new DCDocumentListener() {
            @Override
            protected void onChange(DocumentEvent event) {
                validateAndUpdate();
            }
        });
        if (originalDatastore == null) {
            _hostnameTextField.setText("localhost");
            _portTextField.setText("" + Neo4jDatastore.DEFAULT_PORT);
            _usernameTextField.setText("neo4j");
            _passwordTextField.setText("neo4j");
        } else {
            _datastoreNameTextField.setText(originalDatastore.getName());
            _datastoreNameTextField.setEnabled(false);
            _hostnameTextField.setText(originalDatastore.getHostname());
            _portTextField.setText(originalDatastore.getPort() + "");
            _usernameTextField.setText(originalDatastore.getUsername());
            _passwordTextField.setText(new String(originalDatastore.getPassword()));
        }
    }

    @Override
    protected boolean validateForm() {
        final String datastoreName = _datastoreNameTextField.getText();
        if (StringUtils.isNullOrEmpty(datastoreName)) {
            setStatusError("Please enter a datastore name");
            return false;
        }

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

        final String username = _usernameTextField.getText();
        if (StringUtils.isNullOrEmpty(username)) {
            setStatusError("Please enter username");
            return false;
        }

        setStatusValid();
        return true;
    }

    @Override
    public String getWindowTitle() {
        return "Neo4j graph database";
    }

    @Override
    protected String getBannerTitle() {
        return "Neo4j graph database";
    }

    @Override
    protected boolean isWindowResizable() {
        return true;
    }

    @Override
    protected int getDialogWidth() {
        return 400;
    }

    protected Neo4jDatastore createDatastore() {
        final String datastoreName = _datastoreNameTextField.getText();
        final String hostname = _hostnameTextField.getText();
        final Integer port = Integer.parseInt(_portTextField.getText());
        final String username = _usernameTextField.getText();
        final String password = String.valueOf(_passwordTextField.getPassword());
        return new Neo4jDatastore(datastoreName, hostname, port.intValue(), username, password);
    }

    @Override
    public Schema createSchema() {
        final Neo4jDatastore datastore = createDatastore();
        try (final DatastoreConnection connection = datastore.openConnection()) {
            final Schema defaultSchema = connection.getDataContext().getDefaultSchema();
            return defaultSchema;
        }
    }

    @Override
    protected String getDatastoreIconPath() {
        return IconUtils.NEO4J_IMAGEPATH;
    }

    @Override
    protected List<Entry<String, JComponent>> getFormElements() {
        List<Entry<String, JComponent>> result = super.getFormElements();
        result.add(new ImmutableEntry<String, JComponent>("Hostname", _hostnameTextField));
        result.add(new ImmutableEntry<String, JComponent>("Port", _portTextField));
        result.add(new ImmutableEntry<String, JComponent>("Username", _usernameTextField));
        result.add(new ImmutableEntry<String, JComponent>("Password", _passwordTextField));
        return result;
    }

}
