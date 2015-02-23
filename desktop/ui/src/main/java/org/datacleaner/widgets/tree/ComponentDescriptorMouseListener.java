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

            if(SwingUtilities.isLeftMouseButton(e)){
                _analysisJobBuilder.addComponent(componentDescriptor);                
            } else if(SwingUtilities.isRightMouseButton(e)){
                JPopupMenu popup = new JPopupMenu();
    
                popup.setLabel(componentDescriptor.getDisplayName());
                JMenuItem addTableItem = WidgetFactory.createMenuItem("Add to source",
                        IconUtils.getDescriptorIcon(componentDescriptor));
    
                addTableItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        _analysisJobBuilder.addComponent(componentDescriptor);
                    }
                });
                popup.add(addTableItem);
                popup.show((Component) e.getSource(), e.getX(), e.getY());
            }
        }
    }
}
