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

/**
 * Represents a component that is capable of combining multiple input data
 * streams and producing a number of {@link OutputDataStream}s as well.
 */
public abstract class MultiStreamComponent implements Transformer, HasOutputDataStreams {

    private final OutputColumns transformerOutputColumns = OutputColumns.NO_OUTPUT_COLUMNS;
    private final Object[] transformerOutputValues = new Object[0];

    @Override
    public final OutputColumns getOutputColumns() {
        return transformerOutputColumns;
    }

    @Override
    public final Object[] transform(InputRow inputRow) {
        run(inputRow);
        return transformerOutputValues;
    }

    /**
     * Method invoked for each {@link InputRow} that this component is
     * consuming. The row may pertain to either of the configured input data
     * streams.
     * 
     * @param inputRow
     */
    protected abstract void run(InputRow inputRow);

}
