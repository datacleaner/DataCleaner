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

import java.util.Collection;
import java.util.List;

import org.apache.metamodel.schema.Schema;

import com.google.common.collect.ImmutableList;

/**
 * Default (immutable) implementation of {@link DatastoreMetadata}.
 */
public final class DatastoreMetadataImpl extends AbstractHasMetadataAnnotations implements DatastoreMetadata {

    private final ImmutableList<SchemaMetadata> _schemaMetadata;

    public DatastoreMetadataImpl(Collection<? extends SchemaMetadata> schemaMetadata,
            Collection<? extends MetadataAnnotation> annotations) {
        super(annotations);
        _schemaMetadata = ImmutableList.copyOf(schemaMetadata);
    }

    @Override
    public SchemaMetadata getSchemaMetadataByName(String schemaName) {
        return getByName(schemaName, _schemaMetadata);
    }

    @Override
    public SchemaMetadata getSchemaMetadata(Schema schema) {
        if (schema == null) {
            return null;
        }
        final String schemaName = schema.getName();
        return getSchemaMetadataByName(schemaName);
    }

    @Override
    public List<SchemaMetadata> getSchemaMetadata() {
        return _schemaMetadata;
    }

}
