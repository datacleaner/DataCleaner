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
package org.datacleaner.metadata;

import java.util.List;

import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.HasName;

/**
 * Defines metadata about a {@link Schema}.
 */
public interface SchemaMetadata extends HasName, HasMetadataAnnotations {

    /**
     * Gets {@link TableMetadata} about a particular {@link Table}.
     * 
     * @param tableName
     * @return a {@link TableMetadata} object, or null if no metadata is defined
     *         about the table
     */
    public TableMetadata getTableMetadataByName(String tableName);

    /**
     * Gets {@link TableMetadata} about a particular {@link Table}.
     * 
     * @param table
     * @return a {@link TableMetadata} object, or null if no metadata is defined
     *         about the table
     */
    public TableMetadata getTableMetadata(Table table);

    /**
     * Gets all available {@link TableMetadata} objects.
     * 
     * @return
     */
    public List<TableMetadata> getTableMetadata();
}
