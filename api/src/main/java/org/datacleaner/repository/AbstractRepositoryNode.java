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
package org.datacleaner.repository;

public abstract class AbstractRepositoryNode implements RepositoryNode {

    private static final long serialVersionUID = 1L;

    @Override
    public final int compareTo(final RepositoryNode o) {
        if (o == null) {
            return 1;
        }
        return getQualifiedPath().compareTo(o.getQualifiedPath());
    }

    /**
     * {@inheritDoc}
     *
     * This implementation of the method can be used as a reference. It uses the
     * parent's qualified path and appends '/' and this node's own name to it.
     * Can also be overridden by subclasses in case a more effective way is
     * possible.
     */
    @Override
    public String getQualifiedPath() {
        final RepositoryFolder parent = getParent();
        if (parent == null || parent instanceof Repository) {
            return '/' + getName();
        }
        return parent.getQualifiedPath() + '/' + getName();
    }

    @Override
    public final int hashCode() {
        return getQualifiedPath().hashCode();
    }

    @Override
    public final boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof RepositoryNode) {
            final String otherQualifiedPath = ((RepositoryNode) obj).getQualifiedPath();
            return getQualifiedPath().equals(otherQualifiedPath);
        }

        return false;
    }

    @Override
    public final String toString() {
        return getQualifiedPath();
    }
}
