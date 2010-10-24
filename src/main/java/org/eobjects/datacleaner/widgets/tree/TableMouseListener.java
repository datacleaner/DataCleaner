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
import dk.eobjects.metamodel.schema.Table;

public final class TableMouseListener extends MouseAdapter implements MouseListener {

	private final AnalysisJobBuilder _analysisJobBuilder;
	private final SchemaTree _schemaTree;

	public TableMouseListener(SchemaTree schemaTree, AnalysisJobBuilder analysisJobBuilder) {
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
		int button = e.getButton();
		if (button == MouseEvent.BUTTON2 || button == MouseEvent.BUTTON3) {
			Object userObject = node.getUserObject();
			if (userObject instanceof Table) {
				final Table table = (Table) userObject;
				JPopupMenu popup = WidgetFactory.createPopupMenu().toComponent();
				popup.setLabel(table.getName());
				JMenuItem toggleTableItem = WidgetFactory.createMenuItem("Toggle table columns",
						"images/actions/toggle-source-table.png").toComponent();
				toggleTableItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						toggleTable(table);
					}
				});
				popup.add(toggleTableItem);

				JMenuItem previewMenuItem = WidgetFactory.createMenuItem("Preview table", "images/actions/preview_data.png")
						.toComponent();
				previewMenuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						DataContext dc = _analysisJobBuilder.getDataContextProvider().getDataContext();
						Query q = dc.query().from(table).select(table.getColumns()).toQuery();
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
	public void toggleTable(Table table) {
		Column[] columns = table.getColumns();
		for (Column column : columns) {
			if (_analysisJobBuilder.containsSourceColumn(column)) {
				_analysisJobBuilder.removeSourceColumn(column);
			} else {
				_analysisJobBuilder.addSourceColumn(column);
			}
		}
	}
}
