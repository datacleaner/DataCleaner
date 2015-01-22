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

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.connection.SalesforceDatastore;
import org.datacleaner.guice.Nullable;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.user.MutableDatastoreCatalog;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.widgets.DescriptionLabel;
import org.datacleaner.widgets.HelpIcon;
import org.jdesktop.swingx.JXTextField;

import com.google.inject.Inject;

/**
 * Datastore dialog for Salesforce.com datastores
 */
public class SalesforceDatastoreDialog extends AbstractDatastoreDialog<SalesforceDatastore> {

    private static final long serialVersionUID = 1L;

    private final JXTextField _datastoreNameTextField;
    private final JXTextField _usernameTextField;
    private final JPasswordField _passwordTextField;
    private final JXTextField _securityTokenTextField;

    @Inject
    public SalesforceDatastoreDialog(WindowContext windowContext, MutableDatastoreCatalog datastoreCatalog,
            @Nullable SalesforceDatastore originalDatastore, UserPreferences userPreferences) {
        super(originalDatastore, datastoreCatalog, windowContext, userPreferences,
                "images/window/banner-salesforce.png");

        _datastoreNameTextField = WidgetFactory.createTextField("Datastore name");
        _usernameTextField = WidgetFactory.createTextField("Username");
        _passwordTextField = WidgetFactory.createPasswordField();
        _securityTokenTextField = WidgetFactory.createTextField("Security token");

        if (originalDatastore != null) {
            _datastoreNameTextField.setText(originalDatastore.getName());
            _datastoreNameTextField.setEditable(false);

            _usernameTextField.setText(originalDatastore.getUsername());
            _passwordTextField.setText(originalDatastore.getPassword());
            _securityTokenTextField.setText(originalDatastore.getSecurityToken());
        }
    }

    @Override
    protected JComponent getDialogContent() {
        final DCPanel formPanel = new DCPanel();
        formPanel.setBorder(WidgetUtils.BORDER_TOP_PADDING);

        int row = 1;
        WidgetUtils.addToGridBag(DCLabel.bright("Datastore name:"), formPanel, 1, row);
        WidgetUtils.addToGridBag(_datastoreNameTextField, formPanel, 2, row);

        row++;
        WidgetUtils.addToGridBag(new JSeparator(SwingConstants.HORIZONTAL), formPanel, 1, row, 3, 1);

        row++;
        WidgetUtils.addToGridBag(DCLabel.bright("Salesforce username:"), formPanel, 1, row);
        WidgetUtils.addToGridBag(_usernameTextField, formPanel, 2, row);

        row++;
        WidgetUtils.addToGridBag(DCLabel.bright("Salesforce password:"), formPanel, 1, row);
        WidgetUtils.addToGridBag(_passwordTextField, formPanel, 2, row);

        row++;
        WidgetUtils.addToGridBag(DCLabel.bright("Salesforce security token:"), formPanel, 1, row);
        WidgetUtils.addToGridBag(_securityTokenTextField, formPanel, 2, row);
        HelpIcon securityTokenHelpIcon = new HelpIcon(
                "Your security token is set on Salesforce.com by going to: <b><i>Your Name</i> | Setup | My Personal Information | Reset Security Token</b>.<br/>This security token is needed in order to use the Salesforce.com web services.");
        WidgetUtils.addToGridBag(securityTokenHelpIcon, formPanel, 3, row);

        final DCPanel buttonPanel = getButtonPanel();

        final DCPanel outerPanel = new DCPanel();
        outerPanel.setLayout(new BorderLayout());
        outerPanel.add(formPanel, BorderLayout.CENTER);
        outerPanel.add(buttonPanel, BorderLayout.SOUTH);

        final DescriptionLabel descriptionLabel = new DescriptionLabel();
        descriptionLabel.setText("Configure your Salesforce.com account in this dialog.");
        outerPanel.add(descriptionLabel, BorderLayout.NORTH);

        return outerPanel;
    }

    @Override
    protected SalesforceDatastore createDatastore() {
        final String name = _datastoreNameTextField.getText();
        final String username = _usernameTextField.getText();
        final char[] passwordChars = _passwordTextField.getPassword();
        final String password = String.valueOf(passwordChars);
        final String securityToken = _securityTokenTextField.getText();

        return new SalesforceDatastore(name, username, password, securityToken);
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

}
