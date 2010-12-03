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

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.reference.DatastoreDictionary;
import org.eobjects.datacleaner.actions.PreviewSourceDataActionListener;
import org.eobjects.datacleaner.user.MutableReferenceDataCatalog;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WindowManager;
import org.eobjects.datacleaner.windows.DatastoreDictionaryDialog;

import dk.eobjects.metamodel.schema.Column;

final class ColumnMouseListener extends MouseAdapter implements MouseListener {

	private final AnalysisJobBuilder _analysisJobBuilder;
	private final SchemaTree _schemaTree;

	public ColumnMouseListener(SchemaTree schemaTree, AnalysisJobBuilder analysisJobBuilder) {
		_schemaTree = schemaTree;
		_analysisJobBuilder = analysisJobBuilder;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		TreePath path = _schemaTree.getPathForLocation(e.getX(), e.getY());
		if (path == null) {
			return;
		}
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
		Object userObject = node.getUserObject();
		if (userObject instanceof Column) {
			final Column column = (Column) userObject;
			int button = e.getButton();

			if (button == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
				// double click = toggle column
				toggleColumn(column);
			} else if (button == MouseEvent.BUTTON2 || button == MouseEvent.BUTTON3) {
				// right click = open popup menu
				JPopupMenu popup = new JPopupMenu();
				popup.setLabel(column.getName());
				JMenuItem toggleColumnItem = WidgetFactory.createMenuItem(null, "images/actions/toggle-source-column.png");
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
				popup.add(toggleColumnItem);

				JMenuItem createDictionaryItem = WidgetFactory.createMenuItem("Create dictionary from column",
						"images/model/dictionary.png");
				createDictionaryItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						AnalyzerBeansConfiguration conf = WindowManager.getInstance().getMainWindow().getConfiguration();
						MutableReferenceDataCatalog referenceDataCatalog = (MutableReferenceDataCatalog) conf
								.getReferenceDataCatalog();
						DatastoreCatalog datastoreCatalog = conf.getDatastoreCatalog();
						String datastoreName = _analysisJobBuilder.getDataContextProvider().getDatastore().getName();
						DatastoreDictionary dictionary = new DatastoreDictionary(column.getName(), datastoreCatalog,
								datastoreName, column.getQualifiedLabel());
						DatastoreDictionaryDialog dialog = new DatastoreDictionaryDialog(dictionary, referenceDataCatalog,
								datastoreCatalog);
						dialog.setVisible(true);
					}
				});
				popup.add(createDictionaryItem);

				JMenuItem previewMenuItem = WidgetFactory
						.createMenuItem("Preview column", "images/actions/preview_data.png");
				previewMenuItem.addActionListener(new PreviewSourceDataActionListener(_analysisJobBuilder
						.getDataContextProvider(), column));
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
