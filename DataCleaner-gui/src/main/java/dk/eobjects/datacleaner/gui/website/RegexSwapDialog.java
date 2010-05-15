package dk.eobjects.datacleaner.gui.website;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.action.OpenBrowserAction;
import org.joda.time.DateTime;

import dk.eobjects.datacleaner.gui.GuiHelper;
import dk.eobjects.datacleaner.gui.dialogs.BanneredDialog;
import dk.eobjects.datacleaner.gui.dialogs.NamedRegexDialog;
import dk.eobjects.datacleaner.gui.widgets.DataCleanerTable;
import dk.eobjects.datacleaner.profiler.trivial.TimeAnalysisProfile;
import dk.eobjects.datacleaner.regexswap.Category;
import dk.eobjects.datacleaner.regexswap.Regex;
import dk.eobjects.datacleaner.regexswap.RegexSwapClient;

public class RegexSwapDialog extends BanneredDialog {

	private static final Object[] TABLE_HEADERS = new Object[] { "Name",
			"Good/bad votes", "Author" };
	private static final long serialVersionUID = 3585352325115158622L;
	private RegexSwapClient _client;
	private JTree _categoryTree;
	private DataCleanerTable _regexSelectionTable;
	private JTextArea _regexDescriptionLabel;
	private JButton _importRegexButton;
	private Regex _selectedRegex;
	private JButton _viewOnlineButton;

	@Override
	protected String getBannerIconLabel() {
		return "images/dialog_banner_regexswap.png";
	}

	public RegexSwapDialog() {
		super(700, 500);
	}

	@Override
	protected Component getContent() {
		_client = new RegexSwapClient(GuiHelper.getHttpClient());
		_categoryTree = createCategoryTree();
		JPanel regexSelectionTablePanel = createRegexSelectionTable();
		JPanel regexDetailsPanel = createRegexDetailsPanel();

		JSplitPane rightPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				regexSelectionTablePanel, regexDetailsPanel);
		rightPane.setDividerLocation(170);
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				new JScrollPane(_categoryTree), rightPane);
		splitPane.setDividerLocation(200);

		updateCategories();
		return splitPane;
	}

	private JPanel createRegexDetailsPanel() {
		JPanel panel = GuiHelper.createPanel().applyBorderLayout()
				.toComponent();

		_regexDescriptionLabel = GuiHelper.createLabelTextArea().applyBorder()
				.applyLightBackground().toComponent();
		_regexDescriptionLabel.setText("No regex selected");
		panel.add(new JScrollPane(_regexDescriptionLabel,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);

		JToolBar toolBar = GuiHelper.createToolBar();
		_importRegexButton = new JButton("Import regex", GuiHelper
				.getImageIcon("images/regexes.png"));
		_importRegexButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				NamedRegexDialog dialog = new NamedRegexDialog(_selectedRegex
						.getName(), _selectedRegex.getExpression());
				dialog.setVisible(true);
			}
		});
		_importRegexButton.setEnabled(false);
		toolBar.add(_importRegexButton);

		_viewOnlineButton = new JButton("View online", GuiHelper
				.getImageIcon("images/toolbar_visit_website.png"));
		_viewOnlineButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				new OpenBrowserAction("http://datacleaner.eobjects.org/regex/"
						+ _selectedRegex.getName()).actionPerformed(event);
			}

		});
		_viewOnlineButton.setEnabled(false);
		toolBar.add(_viewOnlineButton);

		panel.add(toolBar, BorderLayout.SOUTH);

		return panel;
	}

	private JPanel createRegexSelectionTable() {
		_regexSelectionTable = new DataCleanerTable();
		_regexSelectionTable.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						int selectedRow = _regexSelectionTable.getSelectedRow();
						if (selectedRow >= 0) {
							String regexName = (String) _regexSelectionTable
									.getValueAt(selectedRow, 0);
							Regex regex = _client.getRegexes().get(regexName);
							fireRegexSelected(regex);
						} else {
							fireRegexSelected(null);
						}
					}
				});

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
					_categoryTree.updateUI();
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
				try {
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
				} catch (IOException e) {
					_log.warn(e);
					if (JOptionPane.YES_OPTION == JOptionPane
							.showConfirmDialog(RegexSwapDialog.this,
									"Could not establish connection.\nError type: "
											+ e.getClass().getSimpleName()
											+ "\nError message: "
											+ e.getMessage() + "\n\nRetry?",
									"Connection error",
									JOptionPane.YES_NO_OPTION)) {
						_log.info("Retrying...");
						run();
					}
				}
			}
		}.start();
	}

	private void fireCategorySelected(final Category category) {
		try {
			_client.updateRegexes(category);
			List<Regex> regexes = category.getRegexes();

			DefaultTableModel tableModel = new DefaultTableModel(TABLE_HEADERS,
					regexes.size());
			if (!regexes.isEmpty()) {
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
		} catch (IOException e) {
			_log.warn(e);
			if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(this,
					"Could not establish connection.\nError type: "
							+ e.getClass().getSimpleName()
							+ "\nError message: " + e.getMessage()
							+ "\n\nRetry?", "Connection error",
					JOptionPane.YES_NO_OPTION)) {
				_log.info("Retrying...");
				fireCategorySelected(category);
			}
		}
	}

	private void fireRegexSelected(final Regex regex) {
		_selectedRegex = regex;
		if (regex == null) {
			_importRegexButton.setEnabled(false);
			_viewOnlineButton.setEnabled(false);
			_regexDescriptionLabel.setText("No regex selected");
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("EXPRESSION:\n");
			sb.append(regex.getExpression());
			sb.append("\n\nDESCRIPTION:\n");
			sb.append(regex.getDescription());
			sb.append("\n\nSUBMISSION DATE:\n");
			sb.append(new DateTime(regex.getTimestamp() * 1000)
					.toString(TimeAnalysisProfile.DATE_AND_TIME_PATTERN));

			_regexDescriptionLabel.setText(sb.toString());
			_regexDescriptionLabel.setSelectionStart(0);
			_regexDescriptionLabel.setSelectionEnd(0);
			_importRegexButton.setEnabled(true);
			_viewOnlineButton.setEnabled(true);
		}
	}

	@Override
	protected String getDialogTitle() {
		return "RegexSwap";
	}

}
