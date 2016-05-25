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

import java.util.Collections;
import java.util.Comparator;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import org.datacleaner.descriptors.ComponentDescriptor;

public class SortedDefaultMutableTreeModel extends DefaultMutableTreeNode {

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
