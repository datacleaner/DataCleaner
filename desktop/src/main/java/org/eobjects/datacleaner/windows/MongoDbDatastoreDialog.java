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

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPasswordField;

import org.eobjects.analyzer.connection.MongoDbDatastore;
import org.eobjects.analyzer.connection.UpdateableDatastoreConnection;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.guice.Nullable;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.user.MutableDatastoreCatalog;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.NumberDocument;
import org.eobjects.datacleaner.util.SchemaFactory;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.DCLabel;
import org.eobjects.datacleaner.widgets.TableDefinitionOptionSelectionPanel;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.util.SimpleTableDef;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.VerticalLayout;

public class MongoDbDatastoreDialog extends AbstractDialog implements SchemaFactory {

	private static final long serialVersionUID = 1L;

	private static final ImageManager imageManager = ImageManager.get();

	private final MutableDatastoreCatalog _catalog;
	private final MongoDbDatastore _originalDatastore;

	private final JXTextField _hostnameTextField;
	private final JXTextField _portTextField;
	private final JXTextField _databaseNameTextField;
	private final JXTextField _usernameTextField;
	private final JPasswordField _passwordField;
	private final JXTextField _datastoreNameTextField;
	private final TableDefinitionOptionSelectionPanel _tableDefinitionWidget;

	@Inject
	public MongoDbDatastoreDialog(WindowContext windowContext, MutableDatastoreCatalog catalog,
			@Nullable MongoDbDatastore datastore) {
		super(windowContext, imageManager.getImage("images/window/banner-datastores.png"));
		_catalog = catalog;
		_originalDatastore = datastore;

		_datastoreNameTextField = WidgetFactory.createTextField();
		_hostnameTextField = WidgetFactory.createTextField();
		_portTextField = WidgetFactory.createTextField();
		_portTextField.setDocument(new NumberDocument(false));
		_databaseNameTextField = WidgetFactory.createTextField();
		_usernameTextField = WidgetFactory.createTextField();
		_passwordField = WidgetFactory.createPasswordField();

		if (_originalDatastore == null) {
			_hostnameTextField.setText("localhost");
			_portTextField.setText("27017");
			_tableDefinitionWidget = new TableDefinitionOptionSelectionPanel(windowContext, this, null);
		} else {
			_datastoreNameTextField.setText(_originalDatastore.getName());
			_datastoreNameTextField.setEnabled(false);
			_hostnameTextField.setText(_originalDatastore.getHostname());
			_portTextField.setText(_originalDatastore.getPort() + "");
			_databaseNameTextField.setText(_originalDatastore.getDatabaseName());
			_usernameTextField.setText(_originalDatastore.getUsername());
			_passwordField.setText(new String(_originalDatastore.getPassword()));
			final SimpleTableDef[] tableDefs = _originalDatastore.getTableDefs();
			_tableDefinitionWidget = new TableDefinitionOptionSelectionPanel(windowContext, this, tableDefs);
		}
	}

	@Override
	public String getWindowTitle() {
		return "MongoDB database";
	}

	@Override
	protected String getBannerTitle() {
		return "MongoDB database";
	}

	@Override
	protected boolean isWindowResizable() {
		return true;
	}

	@Override
	protected int getDialogWidth() {
		return 400;
	}

	@Override
	protected JComponent getDialogContent() {
		final DCPanel formPanel = new DCPanel();
		formPanel.setBorder(WidgetUtils.BORDER_EMPTY);
		
		int row = 0;
		WidgetUtils.addToGridBag(DCLabel.bright("Datastore name:"), formPanel, 0, row);
		WidgetUtils.addToGridBag(_datastoreNameTextField, formPanel, 1, row);
		row++;

		WidgetUtils.addToGridBag(DCLabel.bright("Hostname:"), formPanel, 0, row);
		WidgetUtils.addToGridBag(_hostnameTextField, formPanel, 1, row);
		row++;

		WidgetUtils.addToGridBag(DCLabel.bright("Port:"), formPanel, 0, row);
		WidgetUtils.addToGridBag(_portTextField, formPanel, 1, row);
		row++;

		WidgetUtils.addToGridBag(DCLabel.bright("Database name:"), formPanel, 0, row);
		WidgetUtils.addToGridBag(_databaseNameTextField, formPanel, 1, row);
		row++;

		WidgetUtils.addToGridBag(DCLabel.bright("Username:"), formPanel, 0, row);
		WidgetUtils.addToGridBag(_usernameTextField, formPanel, 1, row);
		row++;

		WidgetUtils.addToGridBag(DCLabel.bright("Password:"), formPanel, 0, row);
		WidgetUtils.addToGridBag(_passwordField, formPanel, 1, row);
		row++;

		WidgetUtils.addToGridBag(DCLabel.bright("Schema model:"), formPanel, 0, row);
		WidgetUtils.addToGridBag(_tableDefinitionWidget, formPanel, 1, row);
		row++;

		final JButton saveButton = WidgetFactory.createButton("Save datastore", "images/model/datastore.png");
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MongoDbDatastore datastore = createDatastore();

				if (_originalDatastore != null) {
					_catalog.removeDatastore(_originalDatastore);
				}
				_catalog.addDatastore(datastore);
				MongoDbDatastoreDialog.this.dispose();
			}
		});

		final DCPanel buttonPanel = new DCPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 4, 0));
		buttonPanel.add(saveButton);

		final DCPanel centerPanel = new DCPanel();
		centerPanel.setLayout(new VerticalLayout(4));
		centerPanel.add(formPanel);
		centerPanel.add(buttonPanel);

		return centerPanel;
	}

	protected MongoDbDatastore createDatastore() {
		final String name = _datastoreNameTextField.getText();
		final String hostname = _hostnameTextField.getText();
		final Integer port = Integer.parseInt(_portTextField.getText());
		final String databaseName = _databaseNameTextField.getText();
		final String username = _usernameTextField.getText();
		final char[] password = _passwordField.getPassword();
		final SimpleTableDef[] tableDefs = _tableDefinitionWidget.getTableDefs();
		return new MongoDbDatastore(name, hostname, port, databaseName, username, password, tableDefs);
	}

	@Override
	public Schema createSchema() {
		final MongoDbDatastore datastore = createDatastore();
		try (final UpdateableDatastoreConnection con = datastore.openConnection()) {
			final Schema schema = con.getDataContext().getDefaultSchema();
			return schema;
		}
	}
}