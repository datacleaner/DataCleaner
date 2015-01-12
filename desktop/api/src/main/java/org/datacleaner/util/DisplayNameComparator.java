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

import java.io.Serializable;
import java.util.Comparator;

import org.datacleaner.descriptors.ComponentDescriptor;

/**
 * Comparator of {@link ComponentDescriptor} objects, which sorts based on
 * display name.
 */
public class DisplayNameComparator implements Comparator<ComponentDescriptor<?>>, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public int compare(ComponentDescriptor<?> o1, ComponentDescriptor<?> o2) {
        return o1.getDisplayName().compareTo(o2.getDisplayName());
    }

}
