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

import org.eobjects.analyzer.connection.Datastore;
import org.apache.metamodel.schema.Schema;

/**
 * Defines metadata about a {@link Datastore}.
 */
public interface DatastoreMetadata extends HasMetadataAnnotations {

    /**
     * Gets {@link SchemaMetadata} for a particular schema
     * 
     * @param schemaName
     * @return a {@link SchemaMetadata} object, or null if no metadata is
     *         defined about the schema
     */
    public SchemaMetadata getSchemaMetadataByName(String schemaName);

    /**
     * Gets {@link SchemaMetadata} for a particular schema
     * 
     * @param schema
     * @return a {@link SchemaMetadata} object, or null if no metadata is
     *         defined about the schema
     */
    public SchemaMetadata getSchemaMetadata(Schema schema);

    /**
     * Gets all available {@link SchemaMetadata} objects.
     * 
     * @return
     */
    public List<SchemaMetadata> getSchemaMetadata();
}
