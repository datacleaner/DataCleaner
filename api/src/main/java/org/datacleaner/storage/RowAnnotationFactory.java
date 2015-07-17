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

import org.datacleaner.api.Component;
import org.datacleaner.api.Provided;

/**
 * The RowAnnotationFactory represents a mechanism used to annotate/label rows
 * of data during execution. Typically these annotations will be stored on disk
 * and thus provide a convenient storage mechanism for situations where a
 * component needs to manage a set of labels but where storing them in
 * collections would be too complicated and would fill up memory.
 * 
 * The RowAnnotationFactory is injectable into any {@link Component} (analyzer,
 * transformer, filter) using the {@link Provided} annotation.
 */
public interface RowAnnotationFactory extends RowAnnotationSampleContainer, RowAnnotationHandler {

    /**
     * Creates a new annotation
     * 
     * @return a new annotation
     */
    public RowAnnotation createAnnotation();

}
