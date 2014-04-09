/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.repository;

public abstract class AbstractRepositoryNode implements RepositoryNode {

    private static final long serialVersionUID = 1L;

    @Override
    public final int compareTo(RepositoryNode o) {
        if (o == null) {
            return 1;
        }
        return getQualifiedPath().compareTo(o.getQualifiedPath());
    }

    @Override
    public final String getQualifiedPath() {
        RepositoryFolder parent = getParent();
        if (parent == null || parent instanceof Repository) {
            return "/" + getName();
        }
        return parent.getQualifiedPath() + "/" + getName();
    }

    @Override
    public final int hashCode() {
        return getQualifiedPath().hashCode();
    }

    @Override
    public final boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof RepositoryNode) {
            String otherQualifiedPath = ((RepositoryNode) obj).getQualifiedPath();
            boolean equalPath = getQualifiedPath().equals(otherQualifiedPath);
            return equalPath;
        }

        return false;
    }

    @Override
    public final String toString() {
        return getQualifiedPath();
    }
}
