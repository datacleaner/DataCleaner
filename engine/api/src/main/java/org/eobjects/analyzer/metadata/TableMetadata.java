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
package org.eobjects.analyzer.metadata;

import java.util.List;

import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.HasName;

/**
 * Defines metadata about a {@link Table}.
 */
public interface TableMetadata extends HasName, HasMetadataAnnotations, HasColumnMetadata {

    /**
     * Gets {@link ColumnGroupMetadata} by the name of the group.
     * 
     * @param groupName
     * @return a {@link ColumnGroupMetadata} object, or null if no group
     *         matching the name was found
     */
    public ColumnGroupMetadata getColumnGroupMetadataByName(String groupName);

    /**
     * Gets all {@link ColumnGroupMetadata} objects available.
     * 
     * @return
     */
    public List<ColumnGroupMetadata> getColumnGroupMetadata();
}
