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
package org.eobjects.analyzer.util;

import java.util.Comparator;

import org.eobjects.analyzer.job.ComponentJob;
import org.apache.metamodel.util.ToStringComparator;

/**
 * Compares and sorts {@link ComponentJob}s for visual presentation
 */
public class ComponentJobComparator implements Comparator<ComponentJob> {

    @Override
    public int compare(ComponentJob o1, ComponentJob o2) {
        int diff = o1.getDescriptor().compareTo(o2.getDescriptor());
        if (diff == 0) {
            diff = ToStringComparator.getComparator().compare(o1.getName(), o2.getName());
        }
        if (diff == 0) {
            diff = ToStringComparator.getComparator().compare(o1, o2);
        }
        if (diff == 0) {
            diff = o1.hashCode() - o2.hashCode();
        }
        return diff;
    }

}
