/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Neopost - Customer Information Management
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
package org.datacleaner.regexswap;

import java.awt.BorderLayout;
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
import javax.swing.JToolBar;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.apache.metamodel.util.SharedExecutorService;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.reference.regexswap.Category;
import org.datacleaner.reference.regexswap.Regex;
import org.datacleaner.reference.regexswap.RegexSwapClient;
import org.datacleaner.reference.regexswap.RegexSwapStringPattern;
import org.datacleaner.user.MutableReferenceDataCatalog;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.CollapsibleTreePanel;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.widgets.table.DCTable;
import org.datacleaner.windows.AbstractDialog;
import org.jdesktop.swingx.JXTree;
import org.jdesktop.swingx.renderer.DefaultTreeRenderer;
import org.jdesktop.swingx.renderer.WrappingIconPanel;
import org.joda.time.DateTime;

/**
 * A dialog for browsing the online RegexSwap repository.
 */
public class RegexSwapDialog extends AbstractDialog {

    private static final long serialVersionUID = 1L;

    private static final Object[] TABLE_HEADERS = new Object[] { "Name", "Good/bad votes", "Author" };
    private static final ImageManager imageManager = ImageManager.get();
    private final RegexSwapClient _client;
    private final JXTree _categoryTree;
    private final DCTable _regexSelectionTable;
    private final DCLabel _regexDescriptionLabel;
    private final JButton _importRegexButton;
    private final TreeCellRenderer _treeRendererDelegate;
    private final MutableReferenceDataCatalog _referenceDataCatalog;
    private Regex _selectedRegex;

    public RegexSwapDialog(final MutableReferenceDataCatalog referenceDataCatalog, final WindowContext windowContext,
            final UserPreferences userPreferences) {
        super(windowContext, imageManager.getImage(IconUtils.STRING_PATTERN_REGEXSWAP_IMAGEPATH));
        _referenceDataCatalog = referenceDataCatalog;
        _client = new RegexSwapClient(userPreferences.createHttpClient());
        _regexDescriptionLabel = DCLabel.brightMultiLine("No regex selected");

        _importRegexButton = new JButton("Import regex", imageManager.getImageIcon(IconUtils.ACTION_SAVE_DARK));
        _importRegexButton.addActionListener(e -> {
            final RegexSwapStringPattern stringPattern = new RegexSwapStringPattern(_selectedRegex);
            if (_referenceDataCatalog.containsStringPattern(stringPattern.getName())) {
                JOptionPane.showMessageDialog(RegexSwapDialog.this,
                        "You already have a string pattern with the name '" + stringPattern.getName() + "'.");
            } else {
                _referenceDataCatalog.addStringPattern(stringPattern);
                RegexSwapDialog.this.dispose();
            }
        });
        _importRegexButton.setEnabled(false);
        _importRegexButton.setOpaque(false);
        _importRegexButton.setFocusPainted(false);
        _importRegexButton.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);

        _regexSelectionTable = new DCTable();
        _regexSelectionTable.getSelectionModel().addListSelectionListener(e -> {
            final int selectedRow = _regexSelectionTable.getSelectedRow();
            if (selectedRow >= 0) {
                final String regexName = (String) _regexSelectionTable.getValueAt(selectedRow, 0);
                final Regex regex = _client.getRegexByName(regexName);
                onRegexSelected(regex);
            } else {
                onRegexSelected(null);
            }
        });

        _regexSelectionTable.setModel(new DefaultTableModel(TABLE_HEADERS, 0));

        final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Loading categories ...");
        rootNode.add(new DefaultMutableTreeNode("Downloading from RegexSwap"));

        _treeRendererDelegate = new DefaultTreeRenderer();

        _categoryTree = new JXTree(rootNode);
        _categoryTree.setOpaque(false);
        _categoryTree.setCellRenderer((tree, value, selected, expanded, leaf, row, hasFocus) -> {
            Icon icon = null;

            final JComponent result;
            final Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
            if (userObject instanceof Category) {
                // Used to render categories
                final Category category = (Category) userObject;
                result = (JComponent) _treeRendererDelegate
                        .getTreeCellRendererComponent(tree, category.getName(), selected, expanded, leaf, row,
                                hasFocus);
                result.setToolTipText(category.getDescription());
                icon = imageManager.getImageIcon(IconUtils.FILE_SEARCH, IconUtils.ICON_SIZE_SMALL);
            } else if (userObject instanceof JLabel) {
                result = (JLabel) userObject;
            } else {
                // Default renderer
                result = (JComponent) _treeRendererDelegate
                        .getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

                if ("Categories".equals(userObject)) {
                    icon = imageManager.getImageIcon(IconUtils.FILE_FOLDER, IconUtils.ICON_SIZE_SMALL);
                }
            }

            final boolean opaque = hasFocus || selected;

            result.setOpaque(opaque);
            if (result instanceof WrappingIconPanel) {
                final WrappingIconPanel wip = (WrappingIconPanel) result;
                wip.getComponent().setOpaque(opaque);

                if (icon != null) {
                    wip.setIcon(icon);
                }
            }
            return result;
        });

        _categoryTree.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(final MouseEvent e) {
                final int selRow = _categoryTree.getRowForLocation(e.getX(), e.getY());
                if (selRow != -1) {
                    final TreePath path = _categoryTree.getPathForLocation(e.getX(), e.getY());
                    _categoryTree.setSelectionPath(path);
                    _categoryTree.updateUI();
                    if (path.getPathCount() == 2) {
                        // A category is selected
                        final DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getPathComponent(1);
                        final Object userObject = node.getUserObject();
                        if (userObject instanceof Category) {
                            final Category category = (Category) userObject;
                            fireCategorySelected(category);
                        }
                    }
                }
            }
        });
    }

    @Override
    protected JComponent getDialogContent() {
        final DCPanel regexDetailsPanel = createRegexDetailsPanel();

        final DCPanel treePanel = new DCPanel(WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        treePanel.setBorder(WidgetUtils.BORDER_WIDE_ALTERNATIVE);
        treePanel.setLayout(new BorderLayout());
        treePanel.add(WidgetUtils.scrolleable(_categoryTree), BorderLayout.CENTER);

        final DCPanel panel = new DCPanel(WidgetUtils.COLOR_ALTERNATIVE_BACKGROUND);
        panel.setLayout(new BorderLayout());
        panel.add(new CollapsibleTreePanel(treePanel), BorderLayout.WEST);
        panel.add(regexDetailsPanel, BorderLayout.CENTER);

        updateCategories();

        return panel;
    }

    private DCPanel createRegexDetailsPanel() {
        final JToolBar toolBar = WidgetFactory.createToolBar();
        toolBar.add(Box.createHorizontalGlue());
        toolBar.add(_importRegexButton);

        final DCPanel toolBarPanel = new DCPanel(WidgetUtils.COLOR_ALTERNATIVE_BACKGROUND);
        toolBarPanel.setLayout(new BorderLayout());
        toolBarPanel.add(toolBar, BorderLayout.CENTER);

        final DCPanel panel = new DCPanel(WidgetUtils.COLOR_ALTERNATIVE_BACKGROUND);
        panel.setLayout(new BorderLayout());
        panel.add(_regexSelectionTable.toPanel(), BorderLayout.NORTH);
        panel.add(WidgetUtils.scrolleable(_regexDescriptionLabel), BorderLayout.CENTER);
        panel.add(toolBarPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void updateCategories() {
        SharedExecutorService.get().submit((Runnable) () -> {
            final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Categories");
            _client.refreshCategories();
            final Collection<Category> categories = _client.getCategories();
            for (final Category category : categories) {
                final DefaultMutableTreeNode categoryNode = new DefaultMutableTreeNode(category);
                rootNode.add(categoryNode);
            }

            final TreeModel treeModel = new DefaultTreeModel(rootNode);
            _categoryTree.setModel(treeModel);
        });
    }

    private void fireCategorySelected(final Category category) {
        final List<Regex> regexes = _client.getRegexes(category);

        final DefaultTableModel tableModel = new DefaultTableModel(TABLE_HEADERS, regexes.size());
        if (!regexes.isEmpty()) {
            for (int i = 0; i < regexes.size(); i++) {
                final Regex regex = regexes.get(i);
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
            _regexDescriptionLabel.setText("No regex selected");
        } else {
            final StringBuilder sb = new StringBuilder();
            sb.append("<b>Expression</b>:\n");
            sb.append(regex.getExpression());
            sb.append("\n\n<b>Description</b>:\n");
            sb.append(regex.getDescription());
            sb.append("\n\n<b>Submission date</b>:\n");
            sb.append(new DateTime(regex.getTimestamp() * 1000).toString());

            _regexDescriptionLabel.setText(sb.toString());
            _importRegexButton.setEnabled(true);
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
