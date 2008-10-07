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
package dk.eobjects.datacleaner.gui.widgets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import dk.eobjects.datacleaner.catalog.TextFileDictionary;
import dk.eobjects.datacleaner.data.ColumnSelection;
import dk.eobjects.datacleaner.data.DataContextSelection;
import dk.eobjects.datacleaner.gui.DataCleanerGui;
import dk.eobjects.datacleaner.gui.GuiHelper;
import dk.eobjects.datacleaner.gui.model.ExtensionFilter;
import dk.eobjects.datacleaner.gui.setup.GuiSettings;
import dk.eobjects.datacleaner.gui.windows.PreviewDataWindow;
import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Schema;
import dk.eobjects.metamodel.schema.Table;

/**
 * MouseListener for the SchemaTree
 */
public class SchemaTreeMouseListener extends MouseAdapter {

	private JTree _tree;
	private DataContextSelection _dataContextSelection;
	private ColumnSelection _columnSelection;

	public SchemaTreeMouseListener(JTree tree,
			DataContextSelection dataContextSelection,
			ColumnSelection columnSelection) {
		super();
		_tree = tree;
		_dataContextSelection = dataContextSelection;
		_columnSelection = columnSelection;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		int selRow = _tree.getRowForLocation(e.getX(), e.getY());
		if (selRow != -1) {
			TreePath path = _tree.getPathForLocation(e.getX(), e.getY());
			_tree.setSelectionPath(path);
			Schema schema = SchemaTree.getSchema(_dataContextSelection
					.getDataContext(), path);
			Table table = SchemaTree.getTable(schema, path);
			Column column = SchemaTree.getColumn(table, path);

			if (e.getClickCount() == 1) {
				int button = e.getButton();
				if (button == MouseEvent.BUTTON2
						|| button == MouseEvent.BUTTON3) {
					JPopupMenu popup = handleRightClick(schema, table, column);
					if (popup != null) {
						popup.show(_tree, e.getX(), e.getY());
					}
				}
			} else if (e.getClickCount() == 2) {
				handleDoubleClick(schema, table, column);
			}
		}
	}

	/**
	 * Handles right clicks on the schema tree, which creates context-based
	 * popups.
	 */
	private JPopupMenu handleRightClick(final Schema schema, final Table table,
			final Column column) {
		if (schema != null) {
			if (table != null) {
				if (column == null) {
					// A table has been right clicked
					return getRightClickTablePopup(table);
				}
				// If a column has been right clicked
				return getRightClickColumnPopup(column);
			}
			// If a schema has been right clicked
			return getRightClickSchemaPopup(schema);
		}
		return null;
	}

	private JPopupMenu getRightClickSchemaPopup(final Schema schema) {
		JPopupMenu popup = new JPopupMenu(schema.getName());
		JMenuItem toggleColumnsItem = GuiHelper.createMenuItem(
				"Toggle schema columns in data selection",
				"images/toolbar_toggle_data.png").toComponent();
		toggleColumnsItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Table[] tables = schema.getTables();
				for (int i = 0; i < tables.length; i++) {
					_columnSelection.toggleTable(tables[i]);
				}
			}
		});
		popup.add(toggleColumnsItem);
		return popup;
	}

	private JPopupMenu getRightClickColumnPopup(final Column column) {
		JPopupMenu popup = new JPopupMenu(column.getName());
		JMenuItem toggleColumnItem = GuiHelper.createMenuItem(null,
				"images/toolbar_toggle_data.png").toComponent();
		toggleColumnItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_columnSelection.toggleColumn(column);
			}
		});
		if (_columnSelection.isSelected(column)) {
			toggleColumnItem.setText("Remove column from data selection");
		} else {
			toggleColumnItem.setText("Add column to data selection");
		}
		popup.add(toggleColumnItem);

		JMenuItem createDictionaryItem = GuiHelper.createMenuItem(
				"Create dictionary from column", "images/dictionaries.png")
				.toComponent();
		createDictionaryItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser f = new JFileChooser((new File(
						GuiSettings.DICTIONARIES_SAMPLES)));
				f.setSelectedFile(new File("my_dictionary.txt"));
				f.addChoosableFileFilter(new ExtensionFilter(
						"DataCleaner text-file dictionary (.txt)", "txt"));
				if (f.showSaveDialog(_tree) == JFileChooser.APPROVE_OPTION) {
					File file = f.getSelectedFile();
					boolean saveFile = true;
					if (file.exists()) {
						if (JOptionPane.showConfirmDialog(_tree,
								"A file with the filename '" + file.getName()
										+ "' already exists. Overwrite?",
								"Overwrite?", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
							saveFile = false;
						}
					}
					if (saveFile) {
						TextFileDictionary dictionary = TextFileDictionary
								.createTextFileDictionary(file.getName(),
										column, _dataContextSelection
												.getDataContext(), file);
						GuiSettings settings = GuiSettings.getSettings();
						settings.getDictionaries().add(dictionary);
						GuiSettings.saveSettings(settings);
					}
				}
			}
		});

		popup.add(createDictionaryItem);

		return popup;
	}

	private JPopupMenu getRightClickTablePopup(final Table table) {
		JPopupMenu popup = new JPopupMenu(table.getName());
		JMenuItem toggleColumnsItem = GuiHelper.createMenuItem(
				"Toggle table columns in data selection",
				"images/toolbar_toggle_data.png").toComponent();
		toggleColumnsItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_columnSelection.toggleTable(table);
			}
		});
		popup.add(toggleColumnsItem);

		JMenuItem previewTableItem = GuiHelper.createMenuItem("Preview table",
				"images/toolbar_preview_data.png").toComponent();
		previewTableItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DataContext dataContext = _dataContextSelection
						.getDataContext();
				DataCleanerGui.getMainWindow().addWindow(
						new PreviewDataWindow(table, table.getColumns(),
								dataContext, 400));
			}
		});
		popup.add(previewTableItem);
		return popup;
	}

	/**
	 * Handles double clicks on the schema tree, which toggles the selected
	 * metadata item in the data selection
	 */
	private void handleDoubleClick(final Schema schema, final Table table,
			final Column column) {
		if (table != null) {
			if (column == null) {
				_columnSelection.toggleTable(table);
			} else {
				_columnSelection.toggleColumn(column);
			}
		}
	}
}