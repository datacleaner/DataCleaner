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

import java.util.Collection;
import java.util.List;

import org.apache.metamodel.schema.Column;

import com.google.common.collect.ImmutableList;

/**
 * Default (immutable) implementation of {@link TableMetadata}.
 */
public final class TableMetadataImpl extends AbstractHasMetadataAnnotations implements TableMetadata {

    private final String _tableName;
    private final ImmutableList<ColumnMetadata> _columnMetadata;
    private final ImmutableList<ColumnGroupMetadata> _columnGroupMetadata;

    public TableMetadataImpl(String tableName, Collection<? extends ColumnMetadata> columnMetadata,
            Collection<? extends ColumnGroupMetadata> columnGroupMetadata,
            Collection<? extends MetadataAnnotation> annotations) {
        super(annotations);
        _tableName = tableName;
        _columnMetadata = ImmutableList.copyOf(columnMetadata);
        _columnGroupMetadata = ImmutableList.copyOf(columnGroupMetadata);
    }

    @Override
    public String getName() {
        return _tableName;
    }

    @Override
    public ColumnMetadata getColumnMetadataByName(String columnName) {
        return getByName(columnName, _columnMetadata);
    }

    @Override
    public ColumnMetadata getColumnMetadata(Column column) {
        if (column == null) {
            return null;
        }
        final String columnName = column.getName();
        return getColumnMetadataByName(columnName);
    }

    @Override
    public List<ColumnMetadata> getColumnMetadata() {
        return _columnMetadata;
    }

    @Override
    public ColumnGroupMetadata getColumnGroupMetadataByName(String groupName) {
        return getByName(groupName, _columnGroupMetadata);
    }

    @Override
    public List<ColumnGroupMetadata> getColumnGroupMetadata() {
        return _columnGroupMetadata;
    }

}
