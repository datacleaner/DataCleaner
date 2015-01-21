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
package org.datacleaner.job.builder;

import java.util.List;

import org.datacleaner.data.MutableInputColumn;

/**
 * Listener interface for receiving notifications when transformers are being
 * added, removed or modified in a way which changes their output.
 * 
 * 
 */
public interface TransformerChangeListener {

	public void onAdd(TransformerJobBuilder<?> transformerJobBuilder);

	public void onRemove(TransformerJobBuilder<?> transformerJobBuilder);

	public void onConfigurationChanged(TransformerJobBuilder<?> transformerJobBuilder);

	public void onRequirementChanged(TransformerJobBuilder<?> transformerJobBuilder);

	/**
	 * This method will be invoked each time a change in a transformer's output
	 * columns is observed.
	 * 
	 * Note that this method will also be invoked with an empty list if a
	 * transformer is being removed. This is to make it easier for listeners to
	 * handle updates on output columns using a single listening-method.
	 * 
	 * @param transformerJobBuilder
	 * @param outputColumns
	 */
	public void onOutputChanged(TransformerJobBuilder<?> transformerJobBuilder, List<MutableInputColumn<?>> outputColumns);
}
