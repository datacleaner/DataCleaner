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

import javax.inject.Inject;

import org.apache.metamodel.data.Row;

/**
 * An {@link OutputRowCollector} is a consumer of output rows from
 * {@link Transformer}s, as well as {@link Analyzer}s implementing
 * {@link HasOutputDataStreams}.
 * 
 * Usually {@link Transformer}s don't need to interact directly with an
 * {@link OutputRowCollector}, because the returned values of the
 * {@link Transformer#transform(org.datacleaner.data.InputRow)} method
 * automatically get's channeled to the {@link OutputRowCollector} by the
 * framework.
 * 
 * But some advanced {@link Transformer}s may have a need to put several rows
 * into the stream of output rows, and for this, a {@link Transformer} can
 * inject an {@link OutputRowCollector} in order to generate multiple records.
 * 
 * {@link OutputRowCollector} can be injected into {@link Transformer}s using
 * the {@link Provided} and {@link Inject} annotations.
 */
public interface OutputRowCollector {

    /**
     * Puts transformed values into the output stream.
     * 
     * @param values
     *            an array of output values, equivalent to the return type of
     *            {@link Transformer#transform(org.datacleaner.data.InputRow)}
     */
    public void putValues(Object... values);

    /**
     * Puts a row (containing values) into the output stream.
     * 
     * @param row
     *            a row containing values to put into the output stream.
     */
    public void putRow(Row row);
}
