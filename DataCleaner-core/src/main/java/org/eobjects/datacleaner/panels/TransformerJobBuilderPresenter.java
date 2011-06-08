/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.panels;

import java.util.List;

import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;

/**
 * Interface for presenter widgets that present {@link TransformerJobBuilder}
 * objects.
 * 
 * @author Kasper Sørensen
 */
public interface TransformerJobBuilderPresenter extends ComponentJobBuilderPresenter {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TransformerJobBuilder<?> getJobBuilder();

	/**
	 * Invoked when the requirement for this transformer changes.
	 */
	public void onRequirementChanged();

	/**
	 * Invoked when the output columns of this transformer changes.
	 * 
	 * @param outputColumns
	 */
	public void onOutputChanged(List<MutableInputColumn<?>> outputColumns);
}
