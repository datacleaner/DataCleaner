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
package org.datacleaner.util;

import java.util.Comparator;

import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.RemoteTransformerDescriptorImpl;

/**
 * @since 20. 11. 2015
 */
public class ComponentDescriptorComparator implements Comparator<ComponentDescriptor> {
    /**
     * The order of names is ascending.
     * The order of priorities is descending.
     * @param firstDescriptor
     * @param secondDescriptor
     * @return
     */
    @Override
    public int compare(ComponentDescriptor firstDescriptor, ComponentDescriptor secondDescriptor) {
        boolean sameNames = (firstDescriptor.getDisplayName().equals(secondDescriptor.getDisplayName()));
        boolean atLeastOneRemote = (firstDescriptor instanceof RemoteTransformerDescriptorImpl || secondDescriptor instanceof RemoteTransformerDescriptorImpl);

        if (sameNames && atLeastOneRemote) {
            boolean bothRemote = (firstDescriptor instanceof RemoteTransformerDescriptorImpl && secondDescriptor instanceof RemoteTransformerDescriptorImpl);

            if (bothRemote) {
                return ((RemoteTransformerDescriptorImpl) secondDescriptor).getServerPriority().compareTo(
                        ((RemoteTransformerDescriptorImpl) firstDescriptor).getServerPriority());
            }
            else {
                return (secondDescriptor instanceof RemoteTransformerDescriptorImpl) ? -1 : 1;
            }
        } else {
            return firstDescriptor.getDisplayName().compareTo(secondDescriptor.getDisplayName());
        }
    }
}
