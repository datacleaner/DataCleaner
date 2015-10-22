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
package org.datacleaner.util.batch;

import java.util.Collection;

import javax.inject.Inject;

import org.datacleaner.api.Close;
import org.datacleaner.api.Initialize;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputRowCollector;
import org.datacleaner.api.Provided;
import org.datacleaner.api.Transformer;

/**
 * Added by jakub on 20.10.15
 */
public abstract class BatchRowCollectingTransformer implements Transformer, BatchTransformation<InputRow, Collection<Object[]>> {

    @Provided
    @Inject
    OutputRowCollector outputRowCollector;

    private final BatchTransformationBuffer<InputRow, Collection<Object[]>> _batchTransformationBuffer;

    public BatchRowCollectingTransformer() {
        _batchTransformationBuffer = new BatchTransformationBuffer<InputRow, Collection<Object[]>>(this, getMaxBatchSize(),
                getFlushIntervalMillis());
    }

    /**
     * Overrideable method to define the number of milliseconds between flushes
     * of the batch buffer.
     *
     * @return
     */
    protected int getFlushIntervalMillis() {
        return BatchTransformationBuffer.DEFAULT_FLUSH_INTERVAL;
    }

    /**
     * Overrideable method to define the maximum batch size of the batch buffer.
     *
     * @return
     */
    protected int getMaxBatchSize() {
        return BatchTransformationBuffer.DEFAULT_MAX_BATCH_SIZE;
    }

    @Initialize
    public final void initialize() {
        _batchTransformationBuffer.start();
    }

    @Close
    public final void close() {
        _batchTransformationBuffer.shutdown();
    }

    @Override
    public final Object[] transform(InputRow inputRow) {
        Collection<Object[]> outputRows = _batchTransformationBuffer.transform(inputRow);
        for(Object[] row: outputRows) {
            outputRowCollector.putValues(row);
        }
        return null;
    }

}
