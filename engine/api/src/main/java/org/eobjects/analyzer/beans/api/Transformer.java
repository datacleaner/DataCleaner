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
package org.eobjects.analyzer.beans.api;

import org.eobjects.analyzer.data.InputRow;

/**
 * Interface for components that transform data before analysis. Transformers
 * work pretty much the same way as row processing analyzers, except that their
 * output is not a finished result, but rather a new, enhanced, row with more
 * values than the incoming row.
 * 
 * The transform(InputRow) method will be invoked on the transformer for each
 * row in a configured datastore. To retrieve the values from the row
 * InputColumn instances must be used as qualifiers. These InputColumns needs to
 * be injected (either a single instance or an array) using the @Configured
 * annotation. If no @Configured InputColumns are found in the class, the
 * transformer will not be able to execute.
 * 
 * Use of the @TransformerBean annotation is required for transformers in order
 * to be automatically discovered.
 * 
 * While the above description covers most common usage of the transformer
 * interface, there are however a few ways to build even more advanced
 * transformations:
 * <ul>
 * <li>Transformers can inject an {@link OutputRowCollector} in order to output
 * multiple records.</li>
 * <li>Transformers can specify that their output type is Object and specify
 * more specific types in the {@link #getOutputColumns()} method.</li> </li>
 * 
 * @see TransformerBean
 * @see Configured
 * @see OutputRowCollector
 * @see OutputColumns
 * 
 * @param <E>
 *            the type of the new/transformed values
 */
public interface Transformer<E> {

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
	public E[] transform(InputRow inputRow);
}