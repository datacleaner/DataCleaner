/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.windows;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import org.eobjects.analyzer.connection.SalesforceDatastore;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.guice.Nullable;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.user.MutableDatastoreCatalog;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.DCLabel;
import org.eobjects.datacleaner.widgets.DescriptionLabel;
import org.eobjects.datacleaner.widgets.HelpIcon;
import org.jdesktop.swingx.JXTextField;

import com.google.inject.Inject;

/**
 * Datastore dialog for Salesforce.com datastores
 */
public class SalesforceDatastoreDialog extends AbstractDialog {

    private static final long serialVersionUID = 1L;

    private static final ImageManager imageManager = ImageManager.getInstance();

    private final MutableDatastoreCatalog _datastoreCatalog;
    private final SalesforceDatastore _originalDatastore;
    private final JXTextField _datastoreNameTextField;
    private final JXTextField _usernameTextField;
    private final JPasswordField _passwordTextField;
    private final JXTextField _securityTokenTextField;

    @Inject
    public SalesforceDatastoreDialog(WindowContext windowContext, MutableDatastoreCatalog datastoreCatalog,
            @Nullable SalesforceDatastore originalDatastore) {
        super(windowContext, imageManager.getImage("images/window/banner-salesforce.png"));
        _datastoreCatalog = datastoreCatalog;
        _originalDatastore = originalDatastore;

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

        final JButton saveButton = WidgetFactory.createButton("Save datastore", "images/model/datastore.png");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SalesforceDatastore datastore = createDatastore();

                if (_originalDatastore != null) {
                    _datastoreCatalog.removeDatastore(_originalDatastore);
                }
                _datastoreCatalog.addDatastore(datastore);
                SalesforceDatastoreDialog.this.dispose();
            }
        });

        final DCPanel buttonPanel = new DCPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        buttonPanel.add(saveButton);

        final DCPanel outerPanel = new DCPanel();
        outerPanel.setLayout(new BorderLayout());
        outerPanel.add(formPanel, BorderLayout.CENTER);
        outerPanel.add(buttonPanel, BorderLayout.SOUTH);

        final DescriptionLabel descriptionLabel = new DescriptionLabel();
        descriptionLabel.setText("Configure your Salesforce.com account in this dialog.");
        outerPanel.add(descriptionLabel, BorderLayout.NORTH);

        return outerPanel;
    }

    private SalesforceDatastore createDatastore() {
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
    protected int getDialogWidth() {
        return 400;
    }

}
