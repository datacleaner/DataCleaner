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

import javax.swing.JComponent;
import javax.swing.JPasswordField;
import javax.swing.event.DocumentEvent;

import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.connection.DatahubDatastore;
import org.datacleaner.guice.Nullable;
import org.datacleaner.user.MutableDatastoreCatalog;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.DCDocumentListener;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImmutableEntry;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.WidgetFactory;
import org.jdesktop.swingx.JXTextField;

import com.google.inject.Inject;

public class DatahubDatastoreDialog extends AbstractDatastoreDialog<DatahubDatastore> {

    private static final long serialVersionUID = 1L;

    private final JXTextField _hostTextField;
    private final JXTextField _portTextField;
    private final JXTextField _usernameTextField;
    private final JPasswordField _passwordTextField;

    @Inject
    public DatahubDatastoreDialog(WindowContext windowContext, MutableDatastoreCatalog datastoreCatalog,
            @Nullable DatahubDatastore originalDatastore, UserPreferences userPreferences) {
        super(originalDatastore, datastoreCatalog, windowContext, userPreferences);

        _hostTextField = WidgetFactory.createTextField("Host name");
        _portTextField = WidgetFactory.createTextField("Port number");
        _usernameTextField = WidgetFactory.createTextField("Username");
        _passwordTextField = WidgetFactory.createPasswordField();

        final DCDocumentListener genericDocumentListener = new DCDocumentListener() {
            @Override
            protected void onChange(DocumentEvent event) {
                validateAndUpdate();
            }
        };
        _hostTextField.getDocument().addDocumentListener(genericDocumentListener);
        _portTextField.getDocument().addDocumentListener(genericDocumentListener);
        _usernameTextField.getDocument().addDocumentListener(genericDocumentListener);
        _passwordTextField.getDocument().addDocumentListener(genericDocumentListener);

        if (originalDatastore != null) {
            _hostTextField.setText(originalDatastore.getHost());
            _portTextField.setText(originalDatastore.getPort());
            _datastoreNameTextField.setText(originalDatastore.getName());
            _datastoreNameTextField.setEditable(false);

            _usernameTextField.setText(originalDatastore.getUsername());
            _passwordTextField.setText(originalDatastore.getPassword());
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

        setStatusValid();
        return true;
    }

    @Override
    protected DatahubDatastore createDatastore() {
        final String host = _hostTextField.getText();
        final String port = _portTextField.getText();
        final String name = _datastoreNameTextField.getText();
        final String username = _usernameTextField.getText();
        final char[] passwordChars = _passwordTextField.getPassword();
        final String password = String.valueOf(passwordChars);

        return new DatahubDatastore(name, host, port, username, password);
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
        result.add(new ImmutableEntry<String, JComponent>("Datahub portnumber", _portTextField));
        result.add(new ImmutableEntry<String, JComponent>("Datahub username", _usernameTextField));
        result.add(new ImmutableEntry<String, JComponent>("Datahub password", _passwordTextField));
        return result;
    }

}
