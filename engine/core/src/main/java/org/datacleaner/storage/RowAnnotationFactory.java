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

import java.util.Map;

import org.datacleaner.data.InputColumn;
import org.datacleaner.data.InputRow;

/**
 * The RowAnnotationFactory represents a mechanism used to annotate/label rows
 * of data during execution. Typically these annotations will be stored on disk
 * and thus provide a convenient storage mechanism for situations where a
 * component needs to manage a set of labels but where storing them in
 * collections would be too complicated and would fill up memory.
 * 
 * The RowAnnotationFactory is injectable into any row processing component
 * (analyzer, transformer, filter) using the @Provided annotation.
 * 
 * @see Provided
 * 
 * 
 */
public interface RowAnnotationFactory {

    /**
     * Creates a new annotation
     * 
     * @return a new annotation
     */
    public RowAnnotation createAnnotation();
    
    /**
     * Annotates an array of rows (all assumed to have distinct count = 1).
     * 
     * @param rows
     * @param annotation
     */
    public void annotate(InputRow[] rows, RowAnnotation annotation);

    /**
     * Annotates/labels a row with an annotation. The row will be retrievable
     * using the getRows(...) method later in the process.
     * 
     * @param row
     * @param distinctCount
     * @param annotation
     */
    public void annotate(InputRow row, int distinctCount, RowAnnotation annotation);

    /**
     * Removes/resets all annotations of a specific kind. This method can be
     * used for situations where eg. an analyzer is annotating extreme values
     * (highest/lowest values etc.) and the threshold is changing, cancelling
     * all previous annotations.
     * 
     * @param annotation
     */
    public void reset(RowAnnotation annotation);

    /**
     * Gets all the available rows with a given annotation.
     * 
     * @param annotation
     * @return
     */
    public InputRow[] getRows(RowAnnotation annotation);

    /**
     * Gets a summarized view of the distinct values and their counts for a
     * single column and annotation.
     * 
     * @param annotation
     * @param inputColumn
     * @return
     */
    public Map<Object, Integer> getValueCounts(RowAnnotation annotation, InputColumn<?> inputColumn);

    /**
     * Transfers registered annotated rows from one annotation to the other.
     * 
     * @param from
     * @param to
     */
    public void transferAnnotations(RowAnnotation from, RowAnnotation to);
}
