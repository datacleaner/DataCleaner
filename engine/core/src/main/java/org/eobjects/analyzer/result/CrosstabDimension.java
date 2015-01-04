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
package org.eobjects.analyzer.result;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class CrosstabDimension implements Serializable, Iterable<String> {

    private static final long serialVersionUID = 1L;

    private List<String> categories = new ArrayList<String>();
    private String name;

    public CrosstabDimension(String name) {
        if (name.contains("|")) {
            throw new IllegalArgumentException("Dimensions cannot contain the character '^'");
        }
        this.name = name;
    }

    public void addCategories(Collection<String> categories) {
        for (String category : categories) {
            addCategory(category);
        }
    }

    public CrosstabDimension addCategory(String category) {
        if (!categories.contains(category)) {
            categories.add(category);
        }
        return this;
    }

    public String getName() {
        return name;
    }

    public boolean containsCategory(String category) {
        return categories.contains(category);
    }

    public List<String> getCategories() {
        return Collections.unmodifiableList(categories);
    }

    @Override
    public Iterator<String> iterator() {
        return getCategories().iterator();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((categories == null) ? 0 : categories.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CrosstabDimension other = (CrosstabDimension) obj;
        if (categories == null) {
            if (other.categories != null)
                return false;
        } else if (!categories.equals(other.categories))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    public int getCategoryCount() {
        return categories.size();
    }

    @Override
    public String toString() {
        return "CrosstabDimension[name=" + name + ", categories=" + categories + "]";
    }
}
