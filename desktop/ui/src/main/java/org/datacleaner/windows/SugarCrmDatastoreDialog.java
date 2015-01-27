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

import java.awt.Color;
import java.awt.FlowLayout;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JComponent;
import javax.swing.JPasswordField;
import javax.swing.event.DocumentEvent;

import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.connection.SugarCrmDatastore;
import org.datacleaner.guice.Nullable;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.user.MutableDatastoreCatalog;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.DCDocumentListener;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImmutableEntry;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.HelpIcon;
import org.jdesktop.swingx.JXTextField;

import com.google.inject.Inject;

/**
 * Datastore dialog for SugarCRM datastores
 */
public class SugarCrmDatastoreDialog extends AbstractDatastoreDialog<SugarCrmDatastore> {

    private static final long serialVersionUID = 1L;

    private final JXTextField _baseUrlTextField;
    private final JXTextField _usernameTextField;
    private final JPasswordField _passwordTextField;

    @Inject
    public SugarCrmDatastoreDialog(WindowContext windowContext, MutableDatastoreCatalog datastoreCatalog,
            @Nullable SugarCrmDatastore originalDatastore, UserPreferences userPreferences) {
        super(originalDatastore, datastoreCatalog, windowContext, userPreferences);

        _baseUrlTextField = WidgetFactory.createTextField("Base URL");
        _usernameTextField = WidgetFactory.createTextField("Username");
        _passwordTextField = WidgetFactory.createPasswordField();

        if (originalDatastore != null) {
            _datastoreNameTextField.setText(originalDatastore.getName());
            _datastoreNameTextField.setEditable(false);

            _baseUrlTextField.setText(originalDatastore.getBaseUrl());
            _usernameTextField.setText(originalDatastore.getUsername());
            _passwordTextField.setText(originalDatastore.getPassword());
        } else {
            _usernameTextField.setText("admin");
            _baseUrlTextField.setText("http://localhost/sugarcrm");
        }
        
        _baseUrlTextField.getDocument().addDocumentListener(new DCDocumentListener() {
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
    }

    @Override
    protected boolean validateForm() {
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
        
        final String baseUrl = _baseUrlTextField.getText();
        if (StringUtils.isNullOrEmpty(baseUrl)) {
            setStatusError("Please enter base URL");
            return false;
        }

        setStatusValid();
        return true;
    }

    @Override
    protected SugarCrmDatastore createDatastore() {
        final String name = _datastoreNameTextField.getText();
        final String username = _usernameTextField.getText();
        final char[] passwordChars = _passwordTextField.getPassword();
        final String password = String.valueOf(passwordChars);
        final String baseUrl = _baseUrlTextField.getText();

        return new SugarCrmDatastore(name, baseUrl, username, password);
    }

    @Override
    public String getWindowTitle() {
        return "SugarCRM datastore";
    }

    @Override
    protected String getBannerTitle() {
        return "SugarCRM system";
    }

    @Override
    protected String getDatastoreIconPath() {
        return IconUtils.SUGAR_CRM_IMAGEPATH;
    }
    
    @Override
    protected List<Entry<String, JComponent>> getFormElements() {
        List<Entry<String, JComponent>> result = super.getFormElements();
        
        DCPanel baseUrlPanel = new DCPanel(Color.WHITE);
        FlowLayout layout = (FlowLayout) baseUrlPanel.getLayout();
        layout.setVgap(0);
        layout.setHgap(0);
        HelpIcon baseUrlHelpIcon = new HelpIcon(
                "The base URL is the first part of any URL that you use when you access the SugarCRM system.");
        baseUrlHelpIcon.setBorder(WidgetUtils.BORDER_EMPTY);
        _baseUrlTextField.setBorder(WidgetUtils.BORDER_EMPTY);
        baseUrlPanel.add(_baseUrlTextField);
        baseUrlPanel.add(baseUrlHelpIcon);
        
        result.add(new ImmutableEntry<String, JComponent>("Salesforce security token", baseUrlPanel));
        
        result.add(new ImmutableEntry<String, JComponent>("Salesforce username", _usernameTextField));
        result.add(new ImmutableEntry<String, JComponent>("Salesforce password", _passwordTextField));
        return result;
    }

}
