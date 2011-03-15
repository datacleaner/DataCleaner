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
package org.eobjects.datacleaner.widgets.tree;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.datacleaner.actions.PreviewSourceDataActionListener;
import org.eobjects.datacleaner.actions.QuickAnalysisActionListener;
import org.eobjects.datacleaner.actions.SaveTableAsCsvFileActionListener;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.metamodel.schema.Column;
import org.eobjects.metamodel.schema.Table;

final class TableMouseListener extends MouseAdapter implements MouseListener {

	private final AnalysisJobBuilder _analysisJobBuilder;
	private final SchemaTree _schemaTree;
	private final Datastore _datastore;

	public TableMouseListener(SchemaTree schemaTree, Datastore datastore, AnalysisJobBuilder analysisJobBuilder) {
		_schemaTree = schemaTree;
		_datastore = datastore;
		_analysisJobBuilder = analysisJobBuilder;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		final TreePath path = _schemaTree.getSelectionPath();
		if (path == null) {
			return;
		}
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
		Object userObject = node.getUserObject();
		if (userObject instanceof Table) {
			final Table table = (Table) userObject;

			int button = e.getButton();

			if (button == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
				// double click = add table
				addTable(table);
			} else if (button == MouseEvent.BUTTON2 || button == MouseEvent.BUTTON3) {
				// right click = open popup menu

				boolean enableAddTable = false;
				boolean enableRemoveTable = false;

				final Column[] columns = table.getColumns();
				for (Column column : columns) {
					if (_analysisJobBuilder.containsSourceColumn(column)) {
						enableRemoveTable = true;
					} else {
						enableAddTable = true;
					}
					if (enableAddTable && enableRemoveTable) {
						break;
					}
				}

				final JPopupMenu popup = new JPopupMenu();
				popup.setLabel(table.getName());

				if (enableAddTable) {
					final JMenuItem addTableItem = WidgetFactory.createMenuItem("Add table to source",
							"images/actions/toggle-source-table.png");
					addTableItem.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							addTable(table);
						}
					});
					popup.add(addTableItem);
				}

				if (enableRemoveTable) {
					final JMenuItem removeTableItem = WidgetFactory.createMenuItem("Remove table from source",
							"images/actions/toggle-source-table.png");
					removeTableItem.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							removeTable(table);
						}
					});
					popup.add(removeTableItem);
				}

				final JMenuItem quickAnalysisMenuItem = WidgetFactory.createMenuItem("Quick analysis",
						"images/component-types/analyzer.png");
				quickAnalysisMenuItem.addActionListener(new QuickAnalysisActionListener(_datastore, table));
				popup.add(quickAnalysisMenuItem);

				final JMenuItem saveAsCsvFileMenuItem = WidgetFactory.createMenuItem("Save table as CSV file",
						"images/component-types/type_output_writer.png");
				saveAsCsvFileMenuItem.addActionListener(new SaveTableAsCsvFileActionListener(_datastore, table));
				popup.add(saveAsCsvFileMenuItem);

				final JMenuItem previewMenuItem = WidgetFactory.createMenuItem("Preview table",
						"images/actions/preview_data.png");
				previewMenuItem.addActionListener(new PreviewSourceDataActionListener(_analysisJobBuilder
						.getDataContextProvider(), columns));
				popup.add(previewMenuItem);

				popup.show((Component) e.getSource(), e.getX(), e.getY());
			}
		}
	}

	/**
	 * toggles whether or not the column is in the source selection
	 */
	public void addTable(Table table) {
		Column[] columns = table.getColumns();
		for (Column column : columns) {
			if (!_analysisJobBuilder.containsSourceColumn(column)) {
				_analysisJobBuilder.addSourceColumn(column);
			}
		}
	}

	/**
	 * toggles whether or not the column is in the source selection
	 */
	public void removeTable(Table table) {
		Column[] columns = table.getColumns();
		for (Column column : columns) {
			_analysisJobBuilder.removeSourceColumn(column);
		}
	}
}
