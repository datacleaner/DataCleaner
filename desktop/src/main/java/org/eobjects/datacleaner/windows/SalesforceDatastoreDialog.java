/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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

import javax.swing.JComponent;
import javax.swing.JPasswordField;

import org.eobjects.analyzer.connection.SalesforceDatastore;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.guice.Nullable;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.jdesktop.swingx.JXTextField;

import com.google.inject.Inject;

public class SalesforceDatastoreDialog extends AbstractDialog {

    private static final long serialVersionUID = 1L;

    private final JXTextField _datastoreNameTextField;
    private final JXTextField _usernameTextField;
    private final JPasswordField _passwordTextField;
    private final JXTextField _securityTokenTextField;

    @Inject
    public SalesforceDatastoreDialog(WindowContext windowContext, @Nullable SalesforceDatastore existingDatastore) {
        super(windowContext, ImageManager.getInstance().getImage(IconUtils.SALESFORCE_IMAGEPATH));

        _datastoreNameTextField = WidgetFactory.createTextField("Datastore name");
        _usernameTextField = WidgetFactory.createTextField("Username");
        _passwordTextField = WidgetFactory.createPasswordField();
        _securityTokenTextField = WidgetFactory.createTextField("Security token");

        if (existingDatastore != null) {
            _datastoreNameTextField.setText(existingDatastore.getName());
            _datastoreNameTextField.setEditable(false);

            _usernameTextField.setText(existingDatastore.getUsername());
            _passwordTextField.setText(existingDatastore.getPassword());
            _securityTokenTextField.setText(existingDatastore.getSecurityToken());
        }
    }

    @Override
    protected JComponent getDialogContent() {
        // TODO Auto-generated method stub
        return new DCPanel();
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
