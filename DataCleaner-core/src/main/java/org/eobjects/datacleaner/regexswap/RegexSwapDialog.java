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
package org.eobjects.datacleaner.regexswap;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.List;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.user.MutableReferenceDataCatalog;
import org.eobjects.datacleaner.util.HttpXmlUtils;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.CollapsibleTreePanel;
import org.eobjects.datacleaner.widgets.DCLabel;
import org.eobjects.datacleaner.widgets.table.DCTable;
import org.eobjects.datacleaner.windows.AbstractDialog;
import org.jdesktop.swingx.JXTree;
import org.jdesktop.swingx.action.OpenBrowserAction;
import org.jdesktop.swingx.renderer.DefaultTreeRenderer;
import org.jdesktop.swingx.renderer.WrappingIconPanel;
import org.joda.time.DateTime;

/**
 * A dialog for browsing the online RegexSwap repository.
 * 
 * @author Kasper Sørensen
 */
public class RegexSwapDialog extends AbstractDialog {

	private static final long serialVersionUID = 1L;

	private static final Object[] TABLE_HEADERS = new Object[] { "Name", "Good/bad votes", "Author" };
	private static final ImageManager imageManager = ImageManager.getInstance();
	private final RegexSwapClient _client;
	private final JXTree _categoryTree;
	private final DCTable _regexSelectionTable;
	private final DCLabel _regexDescriptionLabel;
	private final JButton _importRegexButton;
	private final JButton _viewOnlineButton;
	private final TreeCellRenderer _treeRendererDelegate;
	private final MutableReferenceDataCatalog _referenceDataCatalog;
	private Regex _selectedRegex;

	public RegexSwapDialog(MutableReferenceDataCatalog referenceDataCatalog) {
		super(imageManager.getImage("images/window/banner-string-patterns.png"));
		_referenceDataCatalog = referenceDataCatalog;
		_client = new RegexSwapClient(HttpXmlUtils.getHttpClient());
		_regexDescriptionLabel = DCLabel.brightMultiLine("No regex selected");

		_importRegexButton = new JButton("Import regex",
				imageManager.getImageIcon("images/model/stringpattern_regexswap.png"));
		_importRegexButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				RegexSwapStringPattern stringPattern = new RegexSwapStringPattern(_selectedRegex);
				if (_referenceDataCatalog.containsStringPattern(stringPattern.getName())) {
					JOptionPane.showMessageDialog(RegexSwapDialog.this, "You already have a string pattern with the name '"
							+ stringPattern.getName() + "'.");
				} else {
					_referenceDataCatalog.addStringPattern(stringPattern);
					RegexSwapDialog.this.dispose();
				}
			}
		});
		_importRegexButton.setEnabled(false);
		_importRegexButton.setOpaque(false);
		_importRegexButton.setFocusPainted(false);
		_importRegexButton.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);

		_viewOnlineButton = new JButton("View online", imageManager.getImageIcon("images/actions/website.png"));
		_viewOnlineButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				OpenBrowserAction actionListener = new OpenBrowserAction(_selectedRegex.createWebsiteUrl());
				actionListener.actionPerformed(event);
			}

		});
		_viewOnlineButton.setEnabled(false);
		_viewOnlineButton.setOpaque(false);
		_viewOnlineButton.setFocusPainted(false);
		_viewOnlineButton.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);

		_regexSelectionTable = new DCTable();
		_regexSelectionTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				int selectedRow = _regexSelectionTable.getSelectedRow();
				if (selectedRow >= 0) {
					String regexName = (String) _regexSelectionTable.getValueAt(selectedRow, 0);
					Regex regex = _client.getRegexByName(regexName);
					onRegexSelected(regex);
				} else {
					onRegexSelected(null);
				}
			}
		});

		_regexSelectionTable.setModel(new DefaultTableModel(TABLE_HEADERS, 0));

		final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Loading categories ...");
		rootNode.add(new DefaultMutableTreeNode("Downloading from RegexSwap"));

		_treeRendererDelegate = new DefaultTreeRenderer();

		_categoryTree = new JXTree(rootNode);
		_categoryTree.setOpaque(false);
		_categoryTree.setCellRenderer(new TreeCellRenderer() {

			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
					boolean leaf, int row, boolean hasFocus) {
				Icon icon = null;

				JComponent result;
				Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
				if (userObject instanceof Category) {
					// Used to render categories
					Category category = (Category) userObject;
					result = (JComponent) _treeRendererDelegate.getTreeCellRendererComponent(tree, category.getName(),
							selected, expanded, leaf, row, hasFocus);
					result.setToolTipText(category.getDescription());
					icon = imageManager.getImageIcon("images/filetypes/search-folder.png", IconUtils.ICON_SIZE_SMALL);
				} else if (userObject instanceof JLabel) {
					result = (JLabel) userObject;
				} else {
					// Default renderer
					result = (JComponent) _treeRendererDelegate.getTreeCellRendererComponent(tree, value, selected,
							expanded, leaf, row, hasFocus);

					if ("Categories".equals(userObject)) {
						icon = imageManager.getImageIcon("images/filetypes/folder.png", IconUtils.ICON_SIZE_SMALL);
					}
				}

				final boolean opaque = hasFocus || selected;

				result.setOpaque(opaque);
				if (result instanceof WrappingIconPanel) {
					WrappingIconPanel wip = (WrappingIconPanel) result;
					wip.getComponent().setOpaque(opaque);

					if (icon != null) {
						wip.setIcon(icon);
					}
				}
				return result;
			}
		});

		_categoryTree.addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				int selRow = _categoryTree.getRowForLocation(e.getX(), e.getY());
				if (selRow != -1) {
					TreePath path = _categoryTree.getPathForLocation(e.getX(), e.getY());
					_categoryTree.setSelectionPath(path);
					_categoryTree.updateUI();
					if (path.getPathCount() == 2) {
						// A category is selected
						DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getPathComponent(1);
						Object userObject = node.getUserObject();
						if (userObject instanceof Category) {
							Category category = (Category) userObject;
							fireCategorySelected(category);
						}
					}
				}
			}
		});
	}

	@Override
	protected JComponent getDialogContent() {
		final JPanel regexDetailsPanel = createRegexDetailsPanel();

		final DCPanel treePanel = new DCPanel(WidgetUtils.BG_COLOR_BRIGHTEST, WidgetUtils.BG_COLOR_BRIGHT);
		treePanel.setBorder(WidgetUtils.BORDER_WIDE);
		treePanel.setLayout(new BorderLayout());
		treePanel.add(WidgetUtils.scrolleable(_categoryTree), BorderLayout.CENTER);

		final DCPanel panel = new DCPanel(WidgetUtils.BG_COLOR_LESS_DARK, WidgetUtils.BG_COLOR_DARK);
		panel.setLayout(new BorderLayout());
		panel.add(new CollapsibleTreePanel(treePanel), BorderLayout.WEST);
		panel.add(regexDetailsPanel, BorderLayout.CENTER);

		updateCategories();

		return panel;
	}

	private JPanel createRegexDetailsPanel() {
		final JToolBar toolBar = WidgetFactory.createToolBar();
		toolBar.add(Box.createHorizontalGlue());
		toolBar.add(_importRegexButton);
		toolBar.add(_viewOnlineButton);

		final DCPanel toolBarPanel = new DCPanel(WidgetUtils.BG_COLOR_DARK, WidgetUtils.BG_COLOR_LESS_DARK);
		toolBarPanel.setLayout(new BorderLayout());
		toolBarPanel.add(toolBar, BorderLayout.CENTER);

		final DCPanel panel = new DCPanel(WidgetUtils.BG_COLOR_DARK, WidgetUtils.BG_COLOR_DARK);
		panel.setLayout(new BorderLayout());
		panel.add(_regexSelectionTable.toPanel(), BorderLayout.NORTH);
		panel.add(WidgetUtils.scrolleable(_regexDescriptionLabel), BorderLayout.CENTER);
		panel.add(toolBarPanel, BorderLayout.SOUTH);
		return panel;
	}

	private void updateCategories() {
		new Thread() {
			@Override
			public void run() {
				DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Categories");
				_client.refreshCategories();
				Collection<Category> categories = _client.getCategories();
				for (Category category : categories) {
					DefaultMutableTreeNode categoryNode = new DefaultMutableTreeNode(category);
					rootNode.add(categoryNode);
				}

				TreeModel treeModel = new DefaultTreeModel(rootNode);
				_categoryTree.setModel(treeModel);
			}
		}.start();
	}

	private void fireCategorySelected(final Category category) {
		List<Regex> regexes = _client.getRegexes(category);

		DefaultTableModel tableModel = new DefaultTableModel(TABLE_HEADERS, regexes.size());
		if (!regexes.isEmpty()) {
			for (int i = 0; i < regexes.size(); i++) {
				Regex regex = regexes.get(i);
				tableModel.setValueAt(regex.getName(), i, 0);
				tableModel.setValueAt(regex.getPositiveVotes() + "/" + regex.getNegativeVotes(), i, 1);
				tableModel.setValueAt(regex.getAuthor(), i, 2);
			}
		}
		synchronized (_regexSelectionTable) {
			_regexSelectionTable.setModel(tableModel);
			_regexSelectionTable.updateUI();
		}
	}

	private void onRegexSelected(final Regex regex) {
		_selectedRegex = regex;
		if (regex == null) {
			_importRegexButton.setEnabled(false);
			_viewOnlineButton.setEnabled(false);
			_regexDescriptionLabel.setText("No regex selected");
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("<b>Expression</b>:\n");
			sb.append(regex.getExpression());
			sb.append("\n\n<b>Description</b>:\n");
			sb.append(regex.getDescription());
			sb.append("\n\n<b>Submission date</b>:\n");
			sb.append(new DateTime(regex.getTimestamp() * 1000).toString());

			_regexDescriptionLabel.setText(sb.toString());
			_importRegexButton.setEnabled(true);
			_viewOnlineButton.setEnabled(true);
		}
	}

	@Override
	protected String getBannerTitle() {
		return "RegexSwap browser";
	}

	@Override
	protected int getDialogWidth() {
		return 650;
	}

	@Override
	public String getWindowTitle() {
		return "RegexSwap browser";
	}

	@Override
	protected boolean isWindowResizable() {
		return true;
	}
}
