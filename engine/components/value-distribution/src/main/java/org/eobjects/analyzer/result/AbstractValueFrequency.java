/**
 * AnalyzerBeans
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
package org.eobjects.analyzer.result;

import org.eobjects.analyzer.util.NullTolerableComparator;

/**
 * Abstract {@link ValueFrequency} implementation.
 */
public abstract class AbstractValueFrequency implements ValueFrequency {

    private static final long serialVersionUID = 1L;

    @Override
    public final String toString() {
        return "[" + getName() + "->" + getCount() + "]";
    }

    @Override
    public final int hashCode() {
        return getCount();
    }

    @Override
    public final boolean equals(Object obj) {
        if (obj instanceof ValueFrequency) {
            int count = ((ValueFrequency) obj).getCount();
            if (count == getCount()) {
                String name = ((ValueFrequency) obj).getName();
                if (name != null && name.equals(getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public final int compareTo(ValueFrequency o) {
        int diff = o.getCount() - getCount();
        if (diff == 0) {
            int c1 = isComposite() ? 1 : 0;
            int c2 = o.isComposite() ? 1 : 0;
            diff = c1 - c2;
            if (diff == 0) {
                diff = NullTolerableComparator.get(String.class).compare(getName(), o.getName());
            }
        }
        return diff;
    }
}
