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

import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.datacleaner.widgets.builder.WidgetFactory;
import org.eobjects.datacleaner.windows.DataSetWindow;

import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.query.Query;
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
				JMenuItem toggleColumnItem = WidgetFactory.createMenuItem(null, "images/actions/toggle-source-column.png")
						.toComponent();
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

				JMenuItem previewMenuItem = WidgetFactory
						.createMenuItem("Preview column", "images/actions/preview_data.png").toComponent();
				previewMenuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						DataContext dc = _analysisJobBuilder.getDataContextProvider().getDataContext();
						Query q = dc.query().from(column.getTable()).select(column).toQuery();
						DataSetWindow window = new DataSetWindow(q, dc, 400);
						window.setVisible(true);
					}
				});
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
