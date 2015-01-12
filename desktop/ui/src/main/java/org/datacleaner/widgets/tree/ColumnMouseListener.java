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
package org.datacleaner.widgets.tree;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.inject.Inject;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.reference.DatastoreDictionary;
import org.datacleaner.actions.PreviewSourceDataActionListener;
import org.datacleaner.actions.QuickAnalysisActionListener;
import org.datacleaner.guice.InjectorBuilder;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.windows.DatastoreDictionaryDialog;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;

import com.google.inject.Injector;

final class ColumnMouseListener extends MouseAdapter implements MouseListener {

	private final AnalysisJobBuilder _analysisJobBuilder;
	private final SchemaTree _schemaTree;
	private final InjectorBuilder _injectorBuilder;

	@Inject
	protected ColumnMouseListener(SchemaTree schemaTree, AnalysisJobBuilder analysisJobBuilder,
			InjectorBuilder injectorBuilder) {
		_schemaTree = schemaTree;
		_analysisJobBuilder = analysisJobBuilder;
		_injectorBuilder = injectorBuilder;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		final TreePath path = _schemaTree.getSelectionPath();
		if (path == null) {
			return;
		}
		final DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
		final Object userObject = node.getUserObject();
		if (userObject instanceof Column) {
			final Column column = (Column) userObject;
			int button = e.getButton();

			if (button == MouseEvent.BUTTON1 && e.getClickCount() > 1) {
				// double click = toggle column
				toggleColumn(column);
			} else if (button == MouseEvent.BUTTON2 || button == MouseEvent.BUTTON3) {
				// right click = open popup menu
				final JMenuItem toggleColumnItem = WidgetFactory.createMenuItem(null,
						"images/actions/toggle-source-column.png");
				if (_analysisJobBuilder.containsSourceColumn(column)) {
					toggleColumnItem.setText("Remove column from source");
				} else {
					toggleColumnItem.setText("Add column to source");
				}
				toggleColumnItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						toggleColumn(column);
					}
				});

				final JMenuItem createDictionaryItem = WidgetFactory.createMenuItem("Create dictionary from column",
						"images/model/dictionary.png");
				createDictionaryItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						String datastoreName = _analysisJobBuilder.getDatastoreConnection().getDatastore().getName();
						DatastoreDictionary dictionary = new DatastoreDictionary(column.getName(), datastoreName, column
								.getQualifiedLabel());

						Injector injector = _injectorBuilder.with(DatastoreDictionary.class, dictionary).createInjector();

						DatastoreDictionaryDialog dialog = injector.getInstance(DatastoreDictionaryDialog.class);
						dialog.setVisible(true);
					}
				});

				final JMenuItem quickAnalysisMenuItem = WidgetFactory.createMenuItem("Quick analysis",
						"images/component-types/analyzer.png");

				Injector injector = _injectorBuilder.with(Column[].class, new Column[] { column }).with(Table.class, null)
						.createInjector();
				QuickAnalysisActionListener quickAnalysisActionListener = injector
						.getInstance(QuickAnalysisActionListener.class);

				quickAnalysisMenuItem.addActionListener(quickAnalysisActionListener);

				final JMenuItem previewMenuItem = WidgetFactory.createMenuItem("Preview column",
						"images/actions/preview_data.png");
				previewMenuItem.addActionListener(new PreviewSourceDataActionListener(_schemaTree.getWindowContext(),
						_schemaTree.getDatastore(), column));

				final JPopupMenu popup = new JPopupMenu();
				popup.setLabel(column.getName());
				popup.add(toggleColumnItem);
				popup.add(createDictionaryItem);
				popup.add(quickAnalysisMenuItem);
				popup.add(previewMenuItem);
				popup.show((Component) e.getSource(), e.getX(), e.getY());
			}
		}
	}

	/**
	 * toggles whether or not the column is in the source selection
	 */
	public void toggleColumn(Column column) {
		if (_analysisJobBuilder.containsSourceColumn(column)) {
			_analysisJobBuilder.removeSourceColumn(column);
		} else {
			_analysisJobBuilder.addSourceColumn(column);
		}
	}
}
