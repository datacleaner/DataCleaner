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

import dk.eobjects.metamodel.schema.Column;

public final class ColumnMouseListener extends MouseAdapter implements MouseListener {

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
			if (e.getClickCount() == 1) {
				int button = e.getButton();
				if (button == MouseEvent.BUTTON2 || button == MouseEvent.BUTTON3) {
					JPopupMenu popup = WidgetFactory.createPopupMenu().toComponent();
					popup.setLabel(column.getName());
					JMenuItem toggleColumnItem = WidgetFactory.createMenuItem(null,
							"images/actions/toggle-source-column.png").toComponent();
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
					popup.show((Component) e.getSource(), e.getX(), e.getY());
				}
			} else if (e.getClickCount() == 2) {
				toggleColumn(column);
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
