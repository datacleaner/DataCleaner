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
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.reference.DatastoreDictionary;
import org.eobjects.datacleaner.actions.PreviewSourceDataActionListener;
import org.eobjects.datacleaner.actions.QuickAnalysisActionListener;
import org.eobjects.datacleaner.user.DCConfiguration;
import org.eobjects.datacleaner.user.MutableReferenceDataCatalog;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.windows.DatastoreDictionaryDialog;
import org.eobjects.metamodel.schema.Column;

final class ColumnMouseListener extends MouseAdapter implements MouseListener {

	private final AnalysisJobBuilder _analysisJobBuilder;
	private final SchemaTree _schemaTree;
	private final Datastore _datastore;

	public ColumnMouseListener(SchemaTree schemaTree, Datastore datastore, AnalysisJobBuilder analysisJobBuilder) {
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
		final DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
		final Object userObject = node.getUserObject();
		if (userObject instanceof Column) {
			final Column column = (Column) userObject;
			int button = e.getButton();

			if (button == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
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
						AnalyzerBeansConfiguration conf = DCConfiguration.get();
						MutableReferenceDataCatalog referenceDataCatalog = (MutableReferenceDataCatalog) conf
								.getReferenceDataCatalog();
						DatastoreCatalog datastoreCatalog = conf.getDatastoreCatalog();
						String datastoreName = _analysisJobBuilder.getDataContextProvider().getDatastore().getName();
						DatastoreDictionary dictionary = new DatastoreDictionary(column.getName(), datastoreName, column
								.getQualifiedLabel());
						DatastoreDictionaryDialog dialog = new DatastoreDictionaryDialog(dictionary, referenceDataCatalog,
								datastoreCatalog, _schemaTree.getwindowContext());
						dialog.setVisible(true);
					}
				});

				final JMenuItem quickAnalysisMenuItem = WidgetFactory.createMenuItem("Quick analysis",
						"images/component-types/analyzer.png");
				quickAnalysisMenuItem.addActionListener(new QuickAnalysisActionListener(_datastore, column, _schemaTree
						.getwindowContext()));

				final JMenuItem previewMenuItem = WidgetFactory.createMenuItem("Preview column",
						"images/actions/preview_data.png");
				previewMenuItem.addActionListener(new PreviewSourceDataActionListener(_schemaTree.getwindowContext(),
						_analysisJobBuilder.getDataContextProvider(), column));

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
