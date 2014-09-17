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
package org.eobjects.datacleaner.widgets.database;

import javax.swing.JComponent;
import javax.swing.JPasswordField;

import org.eobjects.analyzer.connection.JdbcDatastore;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.DCLabel;
import org.eobjects.datacleaner.windows.JdbcDatastoreDialog;
import org.jdesktop.swingx.JXTextField;

/**
 * Abstract implementation of {@link DatabaseConnectionPresenter}, which only
 * provides the username/password (credentials) part.
 */
public abstract class AbstractDatabaseConnectionPresenter implements DatabaseConnectionPresenter {

	private final JXTextField _usernameTextField;
	private final JPasswordField _passwordField;

	public AbstractDatabaseConnectionPresenter() {
		_usernameTextField = createTextField("Username");
		_passwordField = WidgetFactory.createPasswordField(JdbcDatastoreDialog.TEXT_FIELD_WIDTH);
	}

	/**
	 * Creates a text field as per the default design of a
	 * {@link DatabaseConnectionPresenter}.
	 * 
	 * @param promptText
	 * @return
	 */
	protected static JXTextField createTextField(String promptText) {
		return WidgetFactory.createTextField(promptText, JdbcDatastoreDialog.TEXT_FIELD_WIDTH);
	}

	@Override
	public boolean initialize(JdbcDatastore datastore) {
		_usernameTextField.setText(datastore.getUsername());
		_passwordField.setText(datastore.getPassword());
		return true;
	}

	@Override
	public final JComponent getWidget() {
		DCPanel panel = new DCPanel();

		int row = layoutGridBagAboveCredentials(panel);

		row++;
		WidgetUtils.addToGridBag(DCLabel.dark("Username:"), panel, 0, row);
		WidgetUtils.addToGridBag(_usernameTextField, panel, 1, row, 1.0, 0.0);

		row++;
		WidgetUtils.addToGridBag(DCLabel.dark("Password:"), panel, 0, row);
		WidgetUtils.addToGridBag(_passwordField, panel, 1, row, 1.0, 0.0);

		layoutGridBagBelowCredentials(panel, row);

		return panel;
	}

	/**
	 * Lays out components in a panel with a gridbag layout.
	 * 
	 * @param panel
	 * @return the latest row number in the grid bag
	 */
	protected int layoutGridBagAboveCredentials(DCPanel panel) {
		return -1;
	};

	/**
	 * Lays out components in a panel with a gridbag layout.
	 * 
	 * @param panel
	 * @param row
	 *            the latest row number in the grid bag
	 */
	protected void layoutGridBagBelowCredentials(DCPanel panel, int row) {
	};

	public JXTextField getUsernameTextField() {
		return _usernameTextField;
	}

	public JPasswordField getPasswordField() {
		return _passwordField;
	}

	@Override
	public final String getUsername() {
		return _usernameTextField.getText();
	}

	@Override
	public final String getPassword() {
		return new String(_passwordField.getPassword());
	}

}
