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

import java.io.Serializable;

import org.apache.metamodel.util.HasName;

/**
 * Represents an abstract node (typically a {@link RepositoryFile} or
 * {@link RepositoryFolder}) in the {@link Repository}.
 */
public interface RepositoryNode extends Serializable, HasName, Comparable<RepositoryNode> {

    /**
     * Gets the parent folder of this node.
     *
     * @return the parent folder of this node.
     */
    RepositoryFolder getParent();

    /**
     * Gets the name of this node.
     *
     * @return the name of this node.
     */
    String getName();

    /**
     * Gets the qualified path of this node.
     *
     * @return the qualified path of this node.
     */
    String getQualifiedPath();

    /**
     * Attempts to delete the repository node.
     *
     * @throws IllegalStateException
     *             if the node is not deletable, eg. if it is a read-only file,
     *             a non-empty directory or similar.
     */
    void delete() throws IllegalStateException;
}
