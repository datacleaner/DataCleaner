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
package org.datacleaner.storage;

import java.util.List;

import org.datacleaner.api.InputRow;

/**
 * A component for retrieving sample {@link InputRow}s that are annotated using
 * with a {@link RowAnnotation}.
 */
public interface RowAnnotationSampleContainer {

    /**
     * Determines if there are sample rows available for a specific
     * {@link RowAnnotation}.
     * 
     * @param annotation
     * @return
     */
    public boolean hasSampleRows(RowAnnotation annotation);

    /**
     * Gets all the available sample rows with a given annotation.
     * 
     * @param annotation
     * @return
     */
    public List<InputRow> getSampleRows(RowAnnotation annotation);
}
