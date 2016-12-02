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
package org.datacleaner.widgets;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.border.EmptyBorder;

import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.WidgetUtils;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.VerticalLayout;

/**
 * Panel that contains a schema tree and allows for a "hide/show" mechanism
 *
 * @author Kasper Sørensen
 */
public class CollapsibleTreePanel extends DCPanel {

    private static final long serialVersionUID = 1L;

    private static final ImageManager imageManager = ImageManager.get();

    private final JXCollapsiblePane _collapsibleTreePane;
    private final JButton _toggleTreeViewButton;

    public CollapsibleTreePanel(final JComponent treeComponent) {
        _collapsibleTreePane = new JXCollapsiblePane(JXCollapsiblePane.Direction.LEFT);
        _collapsibleTreePane.getContentPane().setBackground(WidgetUtils.BG_COLOR_DARK);
        _collapsibleTreePane.add(treeComponent);
        _collapsibleTreePane.setAnimated(false);

        _toggleTreeViewButton = new JButton(imageManager.getImageIcon("images/widgets/tree-panel-collapse.png"));
        _toggleTreeViewButton.setBorder(null);
        _toggleTreeViewButton.setOpaque(false);
        _toggleTreeViewButton.setContentAreaFilled(false);
        _toggleTreeViewButton.addActionListener(e -> {
            final boolean collapsed = _collapsibleTreePane.isCollapsed();
            if (collapsed) {
                _toggleTreeViewButton.setIcon(imageManager.getImageIcon("images/widgets/tree-panel-collapse.png"));
                _toggleTreeViewButton.setBorder(null);
            } else {
                _toggleTreeViewButton.setIcon(imageManager.getImageIcon("images/widgets/tree-panel-expand.png"));
                _toggleTreeViewButton.setBorder(new EmptyBorder(0, 2, 0, 0));
            }
            _collapsibleTreePane.setCollapsed(!collapsed);
        });

        final DCPanel collapseButtonPanel = new DCPanel();
        collapseButtonPanel.setOpaque(true);
        collapseButtonPanel.setBackground(WidgetUtils.BG_COLOR_DARK);
        collapseButtonPanel.setLayout(new VerticalLayout(4));
        collapseButtonPanel.setBorder(null);
        collapseButtonPanel.add(_toggleTreeViewButton);

        setLayout(new BorderLayout());
        add(_collapsibleTreePane, BorderLayout.CENTER);
        add(collapseButtonPanel, BorderLayout.EAST);
    }

    public boolean isCollapsed() {
        return _collapsibleTreePane.isCollapsed();
    }

    public void setCollapsed(final boolean collapsed) {
        if (collapsed != _collapsibleTreePane.isCollapsed()) {
            _toggleTreeViewButton.doClick();
        }
    }
}
