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
package org.eobjects.analyzer.util.batch;

import org.eobjects.analyzer.util.batch.BatchSink;
import org.eobjects.analyzer.util.batch.BatchSource;

/**
 * Defines a batch transformation, to be implemented and supplied by the
 * consuming code.
 * 
 * @param <I>
 *            the transformation input type
 * @param <O>
 *            the transformation output type
 */
public interface BatchTransformation<I, O> {
    
    public void map(BatchSource<I> source, BatchSink<O> sink);
}
