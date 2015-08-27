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
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JComponent;
import javax.swing.JPasswordField;
import javax.swing.event.DocumentEvent;

import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.connection.SalesforceDatastore;
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
import org.datacleaner.widgets.DCComboBox;
import org.datacleaner.widgets.HelpIcon;
import org.jdesktop.swingx.JXTextField;

import com.google.common.base.Strings;
import com.google.inject.Inject;

/**
 * Datastore dialog for Salesforce.com datastores
 */
public class SalesforceDatastoreDialog extends AbstractDatastoreDialog<SalesforceDatastore> {
    private static final String DEFAULT_SALESFORCE_LABEL = "Default SalesForce.com endpoint";
    private static final String DEFAULT_SALESFORCE_URL = "";
    private static final String TEST_SALESFORCE_LABEL = "Test SalesForce.com endpoint";
    private static final String TEST_SALESFORCE_URL = "https://test.salesforce.com/services/Soap/u/28.0";

    private static final long serialVersionUID = 1L;

    private final JXTextField _usernameTextField;
    private final JPasswordField _passwordTextField;
    private final JXTextField _securityTokenTextField;
    private final DCComboBox<String> _endpointUrlComboBox;

    @Inject
    public SalesforceDatastoreDialog(WindowContext windowContext, MutableDatastoreCatalog datastoreCatalog,
            @Nullable SalesforceDatastore originalDatastore, UserPreferences userPreferences) {
        super(originalDatastore, datastoreCatalog, windowContext, userPreferences);

        _usernameTextField = WidgetFactory.createTextField("Username");
        _passwordTextField = WidgetFactory.createPasswordField();
        _securityTokenTextField = WidgetFactory.createTextField("Security token");
        _endpointUrlComboBox = new DCComboBox<>(Arrays.asList(DEFAULT_SALESFORCE_LABEL, TEST_SALESFORCE_LABEL));
        final DCDocumentListener genericDocumentListener = new DCDocumentListener() {
            @Override
            protected void onChange(DocumentEvent event) {
                validateAndUpdate();
            }
        };

        _usernameTextField.getDocument().addDocumentListener(genericDocumentListener);
        _passwordTextField.getDocument().addDocumentListener(genericDocumentListener);
        _securityTokenTextField.getDocument().addDocumentListener(genericDocumentListener);
        _endpointUrlComboBox.addListener(new DCComboBox.Listener<String>() {
            @Override
            public void onItemSelected(final String item) {
                validateAndUpdate();
            }
        });


        if (originalDatastore != null) {
            _datastoreNameTextField.setText(originalDatastore.getName());
            _datastoreNameTextField.setEditable(false);

            _usernameTextField.setText(originalDatastore.getUsername());
            _passwordTextField.setText(originalDatastore.getPassword());
            _securityTokenTextField.setText(originalDatastore.getSecurityToken());
            final String originalDatastoreEndpointUrl = originalDatastore.getEndpointUrl();
            setComboBoxLabelFromUrl(originalDatastoreEndpointUrl);
        }
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

        final String securityToken = _securityTokenTextField.getText();
        if (StringUtils.isNullOrEmpty(securityToken)) {
            setStatusError("Please enter Salesforce security token");
            return false;
        }

        final String endpointUrl = getUrlFromComboBox();

        if (!StringUtils.isNullOrEmpty(endpointUrl)) {
            if (!endpointUrl.startsWith("http") || !endpointUrl.contains("://")) {
                setStatusError("Not a valid endpoint URL");
                return false;
            }

            try {
                URI.create(endpointUrl);
            } catch (Exception e) {
                setStatusError("Not a valid endpoint URL: " + e.getMessage());
                return false;
            }
        }

        setStatusValid();
        return true;
    }

    private void setComboBoxLabelFromUrl(final String originalDatastoreEndpointUrl) {
        if (DEFAULT_SALESFORCE_URL.equals(originalDatastoreEndpointUrl)) {
            _endpointUrlComboBox.setSelectedItem(DEFAULT_SALESFORCE_LABEL);
        } else if (TEST_SALESFORCE_URL.equals(originalDatastoreEndpointUrl)) {
            _endpointUrlComboBox.setSelectedItem(TEST_SALESFORCE_LABEL);
        } else {
            _endpointUrlComboBox.addItem(originalDatastoreEndpointUrl);
            _endpointUrlComboBox.setSelectedItem(originalDatastoreEndpointUrl);
        }
    }

    private String getUrlFromComboBox() {
        final String selectedItem = _endpointUrlComboBox.getSelectedItem();

        final String endpointUrl;
        if (DEFAULT_SALESFORCE_LABEL.equals(selectedItem)) {
          endpointUrl = DEFAULT_SALESFORCE_URL;
        } else if (TEST_SALESFORCE_LABEL.equals(selectedItem)) {
            endpointUrl = TEST_SALESFORCE_URL;
        } else {
            endpointUrl = selectedItem;
        }
        return endpointUrl;
    }

    @Override
    protected SalesforceDatastore createDatastore() {
        final String name = _datastoreNameTextField.getText();
        final String username = _usernameTextField.getText();
        final char[] passwordChars = _passwordTextField.getPassword();
        final String password = String.valueOf(passwordChars);
        final String securityToken = _securityTokenTextField.getText();
        final String endpointUrl = Strings.emptyToNull(getUrlFromComboBox());

        return new SalesforceDatastore(name, username, password, securityToken, endpointUrl);
    }

    @Override
    public String getWindowTitle() {
        return "Salesforce.com datastore";
    }

    @Override
    protected String getBannerTitle() {
        return "Salesforce.com account";
    }

    @Override
    protected String getDatastoreIconPath() {
        return IconUtils.SALESFORCE_IMAGEPATH;
    }

    @Override
    protected List<Entry<String, JComponent>> getFormElements() {
        List<Entry<String, JComponent>> result = super.getFormElements();
        result.add(new ImmutableEntry<String, JComponent>("Salesforce username", _usernameTextField));
        result.add(new ImmutableEntry<String, JComponent>("Salesforce password", _passwordTextField));

        DCPanel securityTokenPanel = new DCPanel(Color.WHITE);
        FlowLayout layout = (FlowLayout) securityTokenPanel.getLayout();
        layout.setVgap(0);
        layout.setHgap(0);
        HelpIcon securityTokenHelpIcon = new HelpIcon(
                "Your security token is set on Salesforce.com by going to: <b><i>Your Name</i> | Setup | My Personal Information | Reset Security Token</b>.<br/>This security token is needed in order to use the Salesforce.com web services.");
        securityTokenHelpIcon.setBorder(WidgetUtils.BORDER_EMPTY);
        _securityTokenTextField.setBorder(WidgetUtils.BORDER_EMPTY);
        securityTokenPanel.add(_securityTokenTextField);
        securityTokenPanel.add(securityTokenHelpIcon);

        result.add(new ImmutableEntry<String, JComponent>("Salesforce security token", securityTokenPanel));

        result.add(new ImmutableEntry<String, JComponent>("Endpoint", _endpointUrlComboBox));
        return result;
    }

}
