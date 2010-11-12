package org.eobjects.datacleaner.widgets.result;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import org.eobjects.analyzer.beans.api.RendererBean;
import org.eobjects.analyzer.beans.similarity.SimilarValues;
import org.eobjects.analyzer.result.SimilarityResult;
import org.eobjects.analyzer.result.renderer.Renderer;
import org.eobjects.analyzer.result.renderer.SwingRenderingFormat;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.table.DCTable;
import org.jdesktop.swingx.JXTree;
import org.jdesktop.swingx.renderer.DefaultTreeRenderer;

@RendererBean(SwingRenderingFormat.class)
public class SimilarityResultSwingRenderer implements Renderer<SimilarityResult, JComponent> {

	@Override
	public JComponent render(SimilarityResult result) {
		final DCPanel panel = new DCPanel();
		panel.setLayout(new BorderLayout());

		final DCTable table = new DCTable();

		final DefaultTreeRenderer rendererDelegate = new DefaultTreeRenderer();

		final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Similarity groups");

		final Set<SimilarValues> similarValues = result.getSimilarValues();
		for (SimilarValues sv : similarValues) {
			rootNode.add(new DefaultMutableTreeNode(sv));
		}

		final DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);

		final JXTree tree = new JXTree();
		tree.setModel(treeModel);
		tree.setCellRenderer(new TreeCellRenderer() {
			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
					boolean leaf, int row, boolean hasFocus) {
				if (value instanceof SimilarValues) {
					String[] values = ((SimilarValues) value).getValues();
					value = "\"" + values[0] + "\" (" + values.length + ")";
				}
				return rendererDelegate.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
			}
		});

		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				TreePath path = tree.getPathForLocation(e.getX(), e.getY());
				if (path == null) {
					return;
				}
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
				Object userObject = node.getUserObject();
				if (userObject instanceof SimilarValues) {
					SimilarValues similarValues = (SimilarValues) userObject;
					String[] values = similarValues.getValues();

					TableModel tableModel = new DefaultTableModel(new String[] { "Value" }, values.length);
					for (int i = 0; i < values.length; i++) {
						tableModel.setValueAt(values[i], i, 0);
					}

					table.setModel(tableModel);
				}
			}
		});

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.add(WidgetUtils.scrolleable(tree));
		splitPane.add(table.toPanel());
		splitPane.setDividerLocation(180);
		panel.add(splitPane, BorderLayout.CENTER);

		return panel;
	}

}
