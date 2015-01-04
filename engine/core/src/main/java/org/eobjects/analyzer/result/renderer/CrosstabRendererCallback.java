/**
 * AnalyzerBeans
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
package org.eobjects.analyzer.result.renderer;

import java.util.List;

import org.eobjects.analyzer.result.Crosstab;
import org.eobjects.analyzer.result.CrosstabDimension;
import org.eobjects.analyzer.result.ResultProducer;

public interface CrosstabRendererCallback<E> {

	public void beginTable(Crosstab<?> crosstab, List<CrosstabDimension> horizontalDimensions,
			List<CrosstabDimension> verticalDimensions);

	public void endTable();

	public void beginRow();

	public void endRow();

	public void horizontalHeaderCell(String category, CrosstabDimension dimension, int width);

	public void verticalHeaderCell(String category, CrosstabDimension dimension, int height);

	public void valueCell(Object value, ResultProducer drillToDetailResultProducer);

	public E getResult();

	public void emptyHeader(CrosstabDimension verticalDimension, CrosstabDimension horizontalDimension);
}
