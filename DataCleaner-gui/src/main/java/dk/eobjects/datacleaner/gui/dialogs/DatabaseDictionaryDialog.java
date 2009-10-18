/**
 *  This file is part of DataCleaner.
 *
 *  DataCleaner is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DataCleaner is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DataCleaner.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.eobjects.datacleaner.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.MatteBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import dk.eobjects.datacleaner.data.DataContextSelection;
import dk.eobjects.datacleaner.gui.GuiHelper;
import dk.eobjects.datacleaner.gui.model.DatabaseDictionary;
import dk.eobjects.datacleaner.gui.model.NamedConnection;
import dk.eobjects.datacleaner.gui.setup.GuiConfiguration;
import dk.eobjects.datacleaner.gui.setup.GuiSettings;
import dk.eobjects.datacleaner.gui.widgets.SchemaTree;
import dk.eobjects.datacleaner.util.ReflectionHelper;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Schema;
import dk.eobjects.metamodel.schema.Table;
import dk.eobjects.metamodel.schema.TableType;

public class DatabaseDictionaryDialog extends BanneredDialog {

	private static final long serialVersionUID = -9126796364763624387L;
	private DatabaseDictionary _dictionary;
	private JComboBox _namedConnectionComboBox;
	private DataContextSelection _dataContextSelection;
	private JTextField _nameField;
	private JButton _saveButton;
	private Schema _schema;
	private Table _table;
	private Column _column;
	private SchemaTree _schemaTree;

	public DatabaseDictionaryDialog(DatabaseDictionary dictionary) {
		super(600, 400);

		_dataContextSelection = new DataContextSelection();

		_saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				GuiSettings settings = GuiSettings.getSettings();
				String name = _nameField.getText();
				if (name.length() > 2) {
					String namedConnection = (String) _namedConnectionComboBox.getSelectedItem();
					String schemaName = _schema.getName();
					String tableName = _table.getName();
					String columnName = _column.getName();
					if (_dictionary == null) {
						_dictionary = new DatabaseDictionary(name, namedConnection, schemaName, tableName, columnName);
						settings.getDictionaries().add(_dictionary);
					} else {
						_dictionary.setName(name).setNamedConnectionName(namedConnection).setSchemaName(schemaName)
								.setTableName(tableName).setColumnName(columnName);
					}
					GuiSettings.saveSettings(settings);
					dispose();
				} else {
					GuiHelper.showErrorMessage("Dictionary name required",
							"Please provide a name of minimum 3 characters for your dictionary.",
							new IllegalArgumentException(name));
				}
			}
		});

		_schemaTree = new SchemaTree(_dataContextSelection);
		_schemaTree.setBorder(new MatteBorder(0, 1, 1, 0, Color.BLACK));
		_schemaTree.addTreeSelectionListener(new TreeSelectionListener() {

			public void valueChanged(TreeSelectionEvent e) {
				TreePath path = _schemaTree.getSelectionPath();
				if (path != null && path.getPathCount() >= 4) {
					_schema = SchemaTree.getSchema(_dataContextSelection.getDataContext(), path);
					_table = SchemaTree.getTable(_schema, path);
					_column = SchemaTree.getColumn(_table, path);
					if (_column != null) {
						_saveButton.setEnabled(true);
					}
				} else {
					_saveButton.setEnabled(false);
				}
			}
		});
		JScrollPane scrollPane = new JScrollPane(_schemaTree);
		Dimension d = new Dimension();
		d.width = 240;
		scrollPane.setPreferredSize(d);
		scrollPane.setSize(d);
		add(scrollPane, BorderLayout.EAST);

		JTextArea aboutDatabaseDictionaries = GuiHelper.createLabelTextArea().toComponent();
		aboutDatabaseDictionaries.setText("Database dictionaries are dictionaries based on columns in databases. "
				+ "To use this kind of dictionary you need to register your database as a "
				+ "named connection in the configuration file, located at "
				+ GuiConfiguration.getDataCleanerHome().getAbsolutePath());
		add(aboutDatabaseDictionaries, BorderLayout.SOUTH);

		_dictionary = dictionary;

		updateDialog();
	}

	private void updateDialog() {
		if (_dictionary != null) {
			try {
				_namedConnectionComboBox.setSelectedItem(_dictionary.getNamedConnectionName());
				DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) _schemaTree.getModel().getRoot();
				_schemaTree.expandPath(new TreePath(rootNode.getPath()));

				DefaultMutableTreeNode schemaNode = getSubNode(rootNode, _dictionary.getSchemaName());
				_schemaTree.expandPath(new TreePath(schemaNode.getPath()));
				
				DefaultMutableTreeNode tableNode = getSubNode(schemaNode, _dictionary.getTableName());
				_schemaTree.expandPath(new TreePath(tableNode.getPath()));
				
				DefaultMutableTreeNode columnNode = getSubNode(tableNode, _dictionary.getColumnName());
				_schemaTree.expandPath(new TreePath(columnNode.getPath()));
				
				_schemaTree
						.setSelectionPath(new TreePath(new Object[] { rootNode, schemaNode, tableNode, columnNode }));

				_nameField.setText(_dictionary.getName());
			} catch (Exception e) {
				GuiHelper
						.showErrorMessage("Could not load Database dictionary",
								"Please verify that the dictionary metadata is correct...\nNamed connection: "
										+ _dictionary.getNamedConnectionName() + "\nSchema:"
										+ _dictionary.getSchemaName() + "\nTable: " + _dictionary.getTableName()
										+ "\nColumn: " + _dictionary.getColumnName(), e);
				dispose();
			}
		}
	}

	private DefaultMutableTreeNode getSubNode(DefaultMutableTreeNode node, String name) {
		if (name == null) {
			name = SchemaTree.UNNAMED_SCHEMA_STRING;
		}
		for (int i = 0; i < node.getChildCount(); i++) {
			DefaultMutableTreeNode subNode = (DefaultMutableTreeNode) node.getChildAt(i);
			if (name.equals(subNode.getUserObject())) {
				return subNode;
			}
		}
		return null;
	}

	@Override
	protected Component getContent() {
		JPanel panel = GuiHelper.createPanel().toComponent();
		JLabel header = new JLabel("Database dictionary");
		header.setFont(GuiHelper.FONT_HEADER);
		GuiHelper.addToGridBag(header, panel, 0, 0, 2, 1);

		GuiHelper.addToGridBag(new JLabel("Dictionary name:"), panel, 0, 1);
		_nameField = new JTextField();
		GuiHelper.addToGridBag(_nameField, panel, 1, 1);

		GuiHelper.addToGridBag(new JLabel("Named connection:"), panel, 0, 2);

		final NamedConnection[] namedConnections = GuiConfiguration.getNamedConnections().toArray(
				new NamedConnection[0]);
		Object[] connectionNames = ReflectionHelper.getProperties(namedConnections, "name");

		_namedConnectionComboBox = new JComboBox(connectionNames);
		_namedConnectionComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				_dataContextSelection.selectNothing();
				NamedConnection namedConnection = namedConnections[_namedConnectionComboBox.getSelectedIndex()];
				if (namedConnection.getConnectionString() != null) {
					try {
						String[] typesString = namedConnection.getTableTypes();
						TableType[] tableTypes;
						if (typesString != null && typesString.length > 0) {
							tableTypes = new TableType[typesString.length];
							for (int i = 0; i < typesString.length; i++) {
								tableTypes[i] = TableType.valueOf(typesString[i]);
							}
						} else {
							tableTypes = new TableType[] { TableType.TABLE };
						}
						_dataContextSelection
								.selectDatabase(namedConnection.getConnectionString(), namedConnection.getCatalog(),
										namedConnection.getUsername(), namedConnection.getPassword(), tableTypes);
					} catch (Exception e) {
						GuiHelper.showErrorMessage("Could not open connection",
								"An error occurred while trying to open connection to the database.", e);
					}
				}
			}
		});
		GuiHelper.addToGridBag(_namedConnectionComboBox, panel, 1, 2);

		_saveButton = new JButton("Save dictionary", GuiHelper.getImageIcon("images/dictionaries.png"));
		_saveButton.setEnabled(false);
		GuiHelper.addToGridBag(_saveButton, panel, 1, 3, 2, 1);

		return panel;
	}

	@Override
	protected String getDialogTitle() {
		return "Database dictionary";
	}

}