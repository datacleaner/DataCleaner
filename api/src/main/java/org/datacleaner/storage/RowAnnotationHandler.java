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

import org.datacleaner.api.InputRow;

/**
 * Represents a component that is capable of connecting {@link RowAnnotation} to
 * {@link InputRow}s, typically to publish them via a
 * {@link RowAnnotationSampleContainer}.
 */
public interface RowAnnotationHandler {

    /**
     * Annotates/labels a row with an annotation. The row will be sampled and
     * usually retrievable using the getRows(...) method later in the process.
     * 
     * @param row
     * @param annotation
     */
    public void annotate(InputRow row, RowAnnotation annotation);

    public void annotate(InputRow row, int distinctCount, RowAnnotation annotation);

    /**
     * Transfers registered annotated rows from one annotation to the other.
     * 
     * @param from
     * @param to
     */
    public void transferAnnotations(RowAnnotation from, RowAnnotation to);

    /**
     * Removes/resets all annotations of a specific kind. This method can be
     * used for situations where eg. an analyzer is annotating extreme values
     * (highest/lowest values etc.) and the threshold is changing, cancelling
     * all previous annotations.
     * 
     * @param annotation
     */
    public void resetAnnotation(RowAnnotation annotation);
}
