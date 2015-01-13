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

import java.util.Map;

import org.apache.metamodel.util.HasName;

/**
 * Represents an annotation of a metadata element such as a
 * {@link TableMetadata}, {@link ColumnGroupMetadata} or {@link ColumnMetadata}.
 * An annotation is used to
 */
public interface MetadataAnnotation extends HasName {

    /**
     * Gets the name of the annotation.
     */
    @Override
    public String getName();

    /**
     * Gets any parameters set on the annotation. Parameters may be used to
     * specify further details and behaviour for the annotations.
     * 
     * @return
     */
    public Map<String, String> getParameters();
}
