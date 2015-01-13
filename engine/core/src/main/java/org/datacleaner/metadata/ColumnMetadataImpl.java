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

/**
 * Default (immutable) implementation of {@link ColumnMetadata}
 */
public class ColumnMetadataImpl extends AbstractHasMetadataAnnotations implements ColumnMetadata {

    private final String _columnName;

    public ColumnMetadataImpl(String columnName, Collection<? extends MetadataAnnotation> annotations) {
        super(annotations);
        _columnName = columnName;
    }

    @Override
    public String getName() {
        return _columnName;
    }

}
