package org.datacleaner.widgets.tree;

import java.util.Collections;
import java.util.Comparator;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import org.datacleaner.descriptors.ComponentDescriptor;

public class SortedDefaultMutableTreeModel extends DefaultMutableTreeNode{

    private static final long serialVersionUID = 1L;

    private static final Comparator<DefaultMutableTreeNode> comp = new Comparator<DefaultMutableTreeNode>() {
        public int compare(DefaultMutableTreeNode o1, DefaultMutableTreeNode o2) {
             final ComponentDescriptor<?> descriptor1 = (ComponentDescriptor<?>) o1.getUserObject(); 
             final ComponentDescriptor<?> descriptor2 = (ComponentDescriptor<?>) o2.getUserObject(); 
             return descriptor1.getDisplayName().compareTo(descriptor2.getDisplayName()); 
        }
    };

    public SortedDefaultMutableTreeModel(Object object) {
        super(object);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void insert(final MutableTreeNode newChild, final int childIndex) {
        super.insert(newChild, childIndex);
        Collections.sort(this.children, comp);
    }
}
