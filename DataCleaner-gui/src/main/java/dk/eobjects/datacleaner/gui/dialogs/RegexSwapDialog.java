package dk.eobjects.datacleaner.gui.dialogs;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import dk.eobjects.datacleaner.gui.widgets.DataCleanerTable;
import dk.eobjects.datacleaner.regexswap.Category;
import dk.eobjects.datacleaner.regexswap.Regex;
import dk.eobjects.datacleaner.regexswap.RegexSwapClient;

public class RegexSwapDialog extends BanneredDialog {

	private static final Object[] TABLE_HEADERS = new Object[] { "Name",
			"Good/bad votes", "Author" };
	private static final long serialVersionUID = 3585352325115158622L;
	private RegexSwapClient _client;
	private JTree _categoryTree;
	private JPanel _regexDetailsPanel;
	private DataCleanerTable _regexSelectionTable;

	public RegexSwapDialog() {
		super(600, 400);
	}

	@Override
	protected Component getContent() {
		_client = new RegexSwapClient();
		_categoryTree = createCategoryTree();
		JPanel regexSelectionTablePanel = createRegexSelectionTable();
		_regexDetailsPanel = createRegexDetailsPanel();

		JSplitPane rightPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				regexSelectionTablePanel, _regexDetailsPanel);
		rightPane.setDividerLocation(200);
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				_categoryTree, rightPane);
		splitPane.setDividerLocation(200);

		updateCategories();
		return splitPane;
	}

	private JPanel createRegexDetailsPanel() {
		// TODO
		return new JPanel();
	}

	private JPanel createRegexSelectionTable() {
		_regexSelectionTable = new DataCleanerTable();
		_regexSelectionTable.setModel(new DefaultTableModel(TABLE_HEADERS, 0));

		return _regexSelectionTable.toPanel();
	}

	private JTree createCategoryTree() {
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(
				"Loading categories ...");
		rootNode.add(new DefaultMutableTreeNode("Downloading from RegexSwap"));

		_categoryTree = new JTree(rootNode);

		_categoryTree.setCellRenderer(new DefaultTreeCellRenderer() {

			private static final long serialVersionUID = -458305430764244341L;

			@Override
			public Component getTreeCellRendererComponent(JTree tree,
					Object value, boolean selected, boolean expanded,
					boolean leaf, int row, boolean hasFocus) {
				Component result;
				Object userObject = ((DefaultMutableTreeNode) value)
						.getUserObject();
				if (userObject instanceof Category) {
					// Used to render categories
					Category category = (Category) userObject;
					result = super
							.getTreeCellRendererComponent(tree, category
									.getName(), selected, expanded, leaf, row,
									hasFocus);
					JComponent component = (JComponent) result;
					component.setToolTipText(category.getDescription());
				} else if (userObject instanceof JLabel) {
					result = (JLabel) userObject;
				} else {
					// Default renderer
					result = super.getTreeCellRendererComponent(tree, value,
							selected, expanded, leaf, row, hasFocus);
				}
				return result;
			}
		});

		_categoryTree.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				int selRow = _categoryTree
						.getRowForLocation(e.getX(), e.getY());
				if (selRow != -1) {
					TreePath path = _categoryTree.getPathForLocation(e.getX(),
							e.getY());
					_categoryTree.setSelectionPath(path);
					if (path.getPathCount() == 2) {
						// A category is selected
						DefaultMutableTreeNode node = (DefaultMutableTreeNode) path
								.getPathComponent(1);
						Category category = (Category) node.getUserObject();
						fireCategorySelected(category);
					}
				}
			}
		});

		return _categoryTree;
	}

	private void updateCategories() {
		new Thread() {
			@Override
			public void run() {
				DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(
						"Categories");
				_client.updateCategories();
				Map<String, Category> categories = _client.getCategories();
				for (Category category : categories.values()) {
					DefaultMutableTreeNode categoryNode = new DefaultMutableTreeNode(
							category);
					rootNode.add(categoryNode);
				}

				TreeModel treeModel = new DefaultTreeModel(rootNode);
				_categoryTree.setModel(treeModel);
			}
		}.start();
	}

	private void fireCategorySelected(final Category category) {
		new Thread() {
			@Override
			public void run() {
				_client.updateRegexes(category);
				List<Regex> regexes = category.getRegexes();

				//TODO: Weird, weird runtime exception happening sometimes
				DefaultTableModel tableModel;
				if (regexes.isEmpty()) {
					tableModel = new DefaultTableModel(TABLE_HEADERS, 1);
					tableModel.setValueAt("n/a", 0, 0);
					tableModel.setValueAt("n/a", 0, 1);
					tableModel.setValueAt("n/a", 0, 2);
				} else {
					tableModel = new DefaultTableModel(TABLE_HEADERS, regexes
							.size());
					for (int i = 0; i < regexes.size(); i++) {
						Regex regex = regexes.get(i);
						tableModel.setValueAt(regex.getName(), i, 0);
						tableModel.setValueAt(regex.getPositiveVotes() + "/"
								+ regex.getNegativeVotes(), i, 1);
						tableModel.setValueAt(regex.getAuthor(), i, 2);
					}
				}
				synchronized (_regexSelectionTable) {
					_regexSelectionTable.setModel(tableModel);
					_regexSelectionTable.updateUI();
				}
			}
		}.start();
	}

	@Override
	protected String getDialogTitle() {
		return "RegexSwap";
	}

}
