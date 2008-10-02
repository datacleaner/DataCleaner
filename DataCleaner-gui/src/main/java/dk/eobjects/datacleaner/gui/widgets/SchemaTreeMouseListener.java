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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import dk.eobjects.datacleaner.data.ColumnSelection;
import dk.eobjects.datacleaner.data.DataContextSelection;
import dk.eobjects.datacleaner.gui.DataCleanerGui;
import dk.eobjects.datacleaner.gui.GuiHelper;
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
			DataContextSelection dataContextSelection, ColumnSelection columnSelection) {
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
					JPopupMenu popup = new JPopupMenu(table.getName());
					JMenuItem toggleColumnsItem = new JMenuItem(
							new AbstractAction() {

								private static final long serialVersionUID = -7810055611434508216L;

								public void actionPerformed(ActionEvent e) {
									_columnSelection.toggleTable(table);
								}
							});
					toggleColumnsItem
							.setText("Toggle table columns in data selection");
					toggleColumnsItem.setIcon(GuiHelper
							.getImageIcon("images/toolbar_toggle_data.png"));
					popup.add(toggleColumnsItem);
					JMenuItem previewTableItem = new JMenuItem(
							new AbstractAction() {

								private static final long serialVersionUID = 8582750767677925420L;

								public void actionPerformed(ActionEvent arg0) {
									DataContext dataContext = _dataContextSelection
											.getDataContext();
									DataCleanerGui.getMainWindow().addWindow(
											new PreviewDataWindow(table, table
													.getColumns(), dataContext,
													400));
								}
							});
					previewTableItem.setIcon(GuiHelper
							.getImageIcon("images/toolbar_preview_data.png"));
					previewTableItem.setText("Preview table");
					popup.add(previewTableItem);
					return popup;
				}
				JPopupMenu popup = new JPopupMenu(column.getName());
				JMenuItem toggleColumnItem = new JMenuItem(
						new AbstractAction() {

							private static final long serialVersionUID = -7810055611434508216L;

							public void actionPerformed(ActionEvent e) {
								_columnSelection.toggleColumn(column);
							}
						});
				if (_columnSelection.isSelected(column)) {
					toggleColumnItem
							.setText("Remove column from data selection");
				} else {
					toggleColumnItem.setText("Add column to data selection");
				}
				toggleColumnItem.setIcon(GuiHelper
						.getImageIcon("images/toolbar_toggle_data.png"));
				popup.add(toggleColumnItem);
				return popup;
			}
			JPopupMenu popup = new JPopupMenu(schema.getName());
			JMenuItem toggleColumnsItem = new JMenuItem(new AbstractAction() {

				private static final long serialVersionUID = -7810055611434508216L;

				public void actionPerformed(ActionEvent e) {
					Table[] tables = schema.getTables();
					for (int i = 0; i < tables.length; i++) {
						_columnSelection.toggleTable(tables[i]);
					}
				}
			});
			toggleColumnsItem
					.setText("Toggle schema columns in data selection");
			toggleColumnsItem.setIcon(GuiHelper
					.getImageIcon("images/toolbar_toggle_data.png"));
			popup.add(toggleColumnsItem);
			return popup;
		}
		return null;
	}

	/**
	 * Handles double clicks on the schema tree, which toggles the selected
	 * metadata item in the data selection
	 */
	private void handleDoubleClick(
			@SuppressWarnings("unused") final Schema schema, final Table table,
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