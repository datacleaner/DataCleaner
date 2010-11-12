package org.eobjects.datacleaner.widgets.result;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import org.eobjects.analyzer.beans.api.RendererBean;
import org.eobjects.analyzer.beans.similarity.SimilarityGroup;
import org.eobjects.analyzer.result.AnnotatedRowsResult;
import org.eobjects.analyzer.result.SimilarityResult;
import org.eobjects.analyzer.result.renderer.Renderer;
import org.eobjects.analyzer.result.renderer.SwingRenderingFormat;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.jdesktop.swingx.JXTree;
import org.jdesktop.swingx.renderer.DefaultTreeRenderer;

@RendererBean(SwingRenderingFormat.class)
public class SimilarityResultSwingRenderer implements Renderer<SimilarityResult, JComponent> {

	@Override
	public JComponent render(SimilarityResult result) {
		final DCPanel panel = new DCPanel();
		panel.setLayout(new BorderLayout());

		final DefaultTreeRenderer rendererDelegate = new DefaultTreeRenderer();

		final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Similarity groups");

		final List<SimilarityGroup> similarityGroups = result.getSimilarityGroups();
		for (SimilarityGroup sg : similarityGroups) {
			rootNode.add(new DefaultMutableTreeNode(sg));
		}

		final DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);

		final JXTree tree = new JXTree();
		tree.setCellRenderer(new TreeCellRenderer() {
			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
					boolean leaf, int row, boolean hasFocus) {
				if (value instanceof DefaultMutableTreeNode) {
					final Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
					if (userObject instanceof SimilarityGroup) {
						final SimilarityGroup similarityGroup = (SimilarityGroup) userObject;
						final String[] values = similarityGroup.getValues();

						final StringBuilder sb = new StringBuilder();
						sb.append(values.length);
						sb.append(": [");
						for (int i = 0; i < values.length; i++) {
							if (i != 0) {
								sb.append(',');
							}
							sb.append('\"');
							sb.append(values[i]);
							sb.append('\"');
							if (sb.length() > 17) {
								sb.delete(17, sb.length());
								sb.append(",...");
								break;
							}
						}
						sb.append(']');

						value = sb.toString();
					}
				}
				return rendererDelegate.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
			}
		});
		tree.setModel(treeModel);

		final DCPanel centerPanel = new DCPanel(WidgetUtils.BG_COLOR_BRIGHT, WidgetUtils.BG_COLOR_LESS_BRIGHT);
		centerPanel.setBorder(WidgetUtils.BORDER_EMPTY);
		centerPanel.setLayout(new BorderLayout());

		final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.add(WidgetUtils.scrolleable(tree));
		splitPane.add(WidgetUtils.scrolleable(centerPanel));
		splitPane.setDividerLocation(180);
		panel.add(splitPane, BorderLayout.CENTER);

		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				TreePath path = tree.getPathForLocation(e.getX(), e.getY());
				if (path == null) {
					return;
				}
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
				Object userObject = node.getUserObject();
				if (userObject instanceof SimilarityGroup) {
					SimilarityGroup similarValues = (SimilarityGroup) userObject;
					AnnotatedRowsResult annotatedRowsResult = similarValues.getAnnotatedRows();
					AnnotatedRowsResultSwingRenderer renderer = new AnnotatedRowsResultSwingRenderer();
					JPanel comp = renderer.render(annotatedRowsResult);
					centerPanel.removeAll();
					centerPanel.add(comp, BorderLayout.NORTH);
					centerPanel.updateUI();
				}
			}
		});

		return panel;
	}

}
