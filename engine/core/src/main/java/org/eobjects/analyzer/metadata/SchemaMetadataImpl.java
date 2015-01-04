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
package org.eobjects.analyzer.metadata;

import java.util.Collection;
import java.util.List;

import org.apache.metamodel.schema.Table;

import com.google.common.collect.ImmutableList;

/**
 * Default (immutable) implementation of {@link SchemaMetadata}.
 */
public final class SchemaMetadataImpl extends AbstractHasMetadataAnnotations implements SchemaMetadata {

    private final ImmutableList<TableMetadata> _tableMetadata;
    private final String _schemaName;

    public SchemaMetadataImpl(String schemaName, Collection<? extends TableMetadata> tableMetadata,
            Collection<? extends MetadataAnnotation> annotations) {
        super(annotations);
        _schemaName = schemaName;
        _tableMetadata = ImmutableList.copyOf(tableMetadata);
    }

    @Override
    public String getName() {
        return _schemaName;
    }

    @Override
    public TableMetadata getTableMetadataByName(String tableName) {
        return getByName(tableName, _tableMetadata);
    }

    @Override
    public TableMetadata getTableMetadata(Table table) {
        if (table == null) {
            return null;
        }
        final String tableName = table.getName();
        return getTableMetadataByName(tableName);
    }

    @Override
    public List<TableMetadata> getTableMetadata() {
        return _tableMetadata;
    }

}
