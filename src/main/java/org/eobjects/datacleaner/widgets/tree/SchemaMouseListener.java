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

import dk.eobjects.metamodel.schema.Schema;
import dk.eobjects.metamodel.schema.Table;

final class SchemaMouseListener extends MouseAdapter implements MouseListener {

	private final SchemaTree _schemaTree;
	private final AnalysisJobBuilder _analysisJobBuilder;

	public SchemaMouseListener(SchemaTree schemaTree, AnalysisJobBuilder analysisJobBuilder) {
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
		if (userObject instanceof Schema) {
			final Schema schema = (Schema) userObject;
			int button = e.getButton();

			if (button == MouseEvent.BUTTON2 || button == MouseEvent.BUTTON3) {
				JPopupMenu popup = new JPopupMenu();
				popup.setLabel(schema.getName());
				JMenuItem addTableItem = WidgetFactory.createMenuItem("Add all schema tables to source",
						"images/actions/toggle-source-table.png").toComponent();
				addTableItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						TableMouseListener tableMouseListener = new TableMouseListener(_schemaTree, _analysisJobBuilder);
						Table[] tables = schema.getTables();
						for (Table table : tables) {
							tableMouseListener.addTable(table);
						}
					}
				});
				popup.add(addTableItem);

				popup.show((Component) e.getSource(), e.getX(), e.getY());
			}
		}
	}
}
