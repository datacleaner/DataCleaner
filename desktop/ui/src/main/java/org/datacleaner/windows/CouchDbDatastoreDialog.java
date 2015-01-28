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
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPasswordField;
import javax.swing.event.DocumentEvent;

import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.util.SimpleTableDef;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.connection.CouchDbDatastore;
import org.datacleaner.connection.UpdateableDatastoreConnection;
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
import org.datacleaner.widgets.TableDefinitionOptionSelectionPanel;
import org.jdesktop.swingx.JXTextField;

public class CouchDbDatastoreDialog extends AbstractDatastoreDialog<CouchDbDatastore> implements SchemaFactory {

    private static final long serialVersionUID = 1L;

    private final JXTextField _hostnameTextField;
    private final JXTextField _portTextField;
    private final JXTextField _usernameTextField;
    private final JPasswordField _passwordField;
    private final JCheckBox _sslCheckBox;
    private final TableDefinitionOptionSelectionPanel _tableDefinitionWidget;

    @Inject
    public CouchDbDatastoreDialog(WindowContext windowContext, MutableDatastoreCatalog catalog,
            @Nullable CouchDbDatastore originalDatastore, UserPreferences userPreferences) {
        super(originalDatastore, catalog, windowContext, userPreferences);

        _hostnameTextField = WidgetFactory.createTextField();
        _portTextField = WidgetFactory.createTextField();
        _portTextField.setDocument(new NumberDocument(false));
        _usernameTextField = WidgetFactory.createTextField();
        _passwordField = WidgetFactory.createPasswordField();
        
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
        _passwordField.getDocument().addDocumentListener(new DCDocumentListener() {
            @Override
            protected void onChange(DocumentEvent event) {
                validateAndUpdate();
            }
        });

        _sslCheckBox = new JCheckBox("Connect via SSL", false);
        _sslCheckBox.setOpaque(false);
        _sslCheckBox.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);

        if (originalDatastore == null) {
            _hostnameTextField.setText("localhost");
            _portTextField.setText("" + CouchDbDatastore.DEFAULT_PORT);
            _tableDefinitionWidget = new TableDefinitionOptionSelectionPanel(windowContext, this, null);
        } else {
            _datastoreNameTextField.setText(originalDatastore.getName());
            _datastoreNameTextField.setEnabled(false);
            _hostnameTextField.setText(originalDatastore.getHostname());
            _portTextField.setText(originalDatastore.getPort() + "");
            _sslCheckBox.setSelected(originalDatastore.isSslEnabled());
            _usernameTextField.setText(originalDatastore.getUsername());
            _passwordField.setText(new String(originalDatastore.getPassword()));
            _tableDefinitionWidget = new TableDefinitionOptionSelectionPanel(windowContext, this,
                    originalDatastore.getTableDefs());
        }
    }

    @Override
    public String getWindowTitle() {
        return "CouchDB database";
    }

    @Override
    protected String getBannerTitle() {
        return "CouchDB database";
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
        
        final String keyspace = _usernameTextField.getText();
        if (StringUtils.isNullOrEmpty(keyspace)) {
            setStatusError("Please enter username");
            return false;
        }
        
        // No password field validating as sometimes passwords are empty strings

        setStatusValid();
        return true;
    }

    protected CouchDbDatastore createDatastore() {
        final String name = _datastoreNameTextField.getText();
        final String hostname = _hostnameTextField.getText();
        final Integer port = Integer.parseInt(_portTextField.getText());
        final boolean sslEnabled = _sslCheckBox.isSelected();
        final String username = _usernameTextField.getText();
        final String password = new String(_passwordField.getPassword());
        final SimpleTableDef[] tableDefs = _tableDefinitionWidget.getTableDefs();
        return new CouchDbDatastore(name, hostname, port, username, password, sslEnabled, tableDefs);
    }

    @Override
    public Schema createSchema() {
        final CouchDbDatastore datastore = createDatastore();
        try (final UpdateableDatastoreConnection con = datastore.openConnection()) {
            Schema schema = con.getDataContext().getDefaultSchema();
            return schema;
        }
    }

    @Override
    protected String getDatastoreIconPath() {
        return IconUtils.COUCHDB_IMAGEPATH;
    }
    
    @Override
    protected List<Entry<String, JComponent>> getFormElements() {
        List<Entry<String, JComponent>> result = super.getFormElements();
        result.add(new ImmutableEntry<String, JComponent>("Hostname", _hostnameTextField));
        result.add(new ImmutableEntry<String, JComponent>("Port", _portTextField));
        result.add(new ImmutableEntry<String, JComponent>("Connect via SSL", _sslCheckBox));
        result.add(new ImmutableEntry<String, JComponent>("Username", _usernameTextField));
        result.add(new ImmutableEntry<String, JComponent>("Password", _passwordField));
        result.add(new ImmutableEntry<String, JComponent>("Schema model", _tableDefinitionWidget));
        return result;
    }
}
