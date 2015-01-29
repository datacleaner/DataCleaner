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
package org.datacleaner.components.categories;

import org.datacleaner.api.ComponentSuperCategory;

/**
 * Abstract implementation of {@link ComponentSuperCategory}. This
 * implementation assumes that all instances of a category class are equal,
 * which is also the recommended approach.
 */
public abstract class AbstractComponentSuperCategory implements ComponentSuperCategory {

    private static final long serialVersionUID = 1L;

    @Override
    public String getName() {
        String simpleName = getClass().getSimpleName();
        if (simpleName.endsWith("SuperCategory")) {
            simpleName = simpleName.substring(0, simpleName.length() - "SuperCategory".length());
        }
        return simpleName;
    }

    @Override
    public final boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        return getClass().equals(obj.getClass());
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public final String toString() {
        return getName();
    }

    @Override
    public int compareTo(ComponentSuperCategory o) {
        if (equals(o)) {
            return 0;
        }
        int sortIndex1 = getSortIndex();
        int sortIndex2 = o.getSortIndex();
        int diff = sortIndex1 - sortIndex2;
        if (diff == 0) {
            diff = getName().compareTo(o.getName());
        }
        return diff;
    }
}
