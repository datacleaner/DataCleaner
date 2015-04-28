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
package org.datacleaner.widgets.tree;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.inject.Inject;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.datacleaner.actions.ComponentReferenceDocumentationActionListener;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.WidgetFactory;

public class ComponentDescriptorMouseListener extends MouseAdapter {
    private final SchemaTree _schemaTree;
    private final AnalysisJobBuilder _analysisJobBuilder;

    @Inject
    protected ComponentDescriptorMouseListener(SchemaTree schemaTree, AnalysisJobBuilder analysisJobBuilder) {
        _schemaTree = schemaTree;
        _analysisJobBuilder = analysisJobBuilder;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        TreePath path = _schemaTree.getPathForLocation(e.getX(), e.getY());
        if (path == null) {
            return;
        }
        final DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        final Object userObject = node.getUserObject();

        if (userObject instanceof ComponentDescriptor<?>) {
            final ComponentDescriptor<?> componentDescriptor = (ComponentDescriptor<?>) userObject;

            if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() > 1) {
                _analysisJobBuilder.addComponent(componentDescriptor);
            } else if (SwingUtilities.isRightMouseButton(e)) {

                final JMenuItem addTableItem = WidgetFactory.createMenuItem("Add component",
                        IconUtils.getDescriptorIcon(componentDescriptor, IconUtils.ICON_SIZE_MENU_ITEM, false));
                addTableItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        _analysisJobBuilder.addComponent(componentDescriptor);
                    }
                });

                final JMenuItem referenceDocumentationItem = WidgetFactory.createMenuItem("Component description",
                        IconUtils.ACTION_HELP);
                referenceDocumentationItem.addActionListener(new ComponentReferenceDocumentationActionListener(
                        _analysisJobBuilder.getConfiguration(), componentDescriptor));

                final JPopupMenu popup = new JPopupMenu(componentDescriptor.getDisplayName());
                popup.add(addTableItem);
                popup.add(referenceDocumentationItem);
                popup.show((Component) e.getSource(), e.getX(), e.getY());
            }
        }
    }
}
