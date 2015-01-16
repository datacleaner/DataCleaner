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
package org.datacleaner.api;

import javax.inject.Named;

/**
 * Interface for {@link Component}s that transform data before analysis.
 * 
 * See {@link Component} for general information about all components. Like all
 * components, {@link Analyzer} require a {@link Named} annotation in order to
 * be discovered.
 * 
 * A {@link Transformer} will process incoming rows and produce new fields which
 * are appended onto the existing rows. The {@link #transform(InputRow)} method
 * will be invoked on the {@link Transformer} for each row in a data stream.
 * 
 * While the above description covers most common usage of the transformer
 * interface there are a few ways to build even more advanced transformations.
 * 
 * Transformers can inject an {@link OutputRowCollector} in order to output
 * multiple records. The transform method should in such a case return null.
 * This also allows a {@link Transformer} to completely <i>swallow</i> a record,
 * not producing any output row for it, mean that it will not travel further in
 * the data stream. In many cases that's not a good thing (you might rather want
 * to build a {@link Filter} then) but it is possible.
 * 
 * @see OutputRowCollector
 * @see OutputColumns
 * 
 * @since 4.0
 */
public interface Transformer extends Component {

    /**
     * Gets the output columns (given the current configuration) of this
     * transformer.
     * 
     * @return an object with the information needed to create the output
     *         columns
     */
    public OutputColumns getOutputColumns();

    /**
     * Transforms a row of input values to the corresponding transformed values
     * 
     * @param inputRow
     * @return an array of transformed values.
     */
    public Object[] transform(InputRow inputRow);
}
