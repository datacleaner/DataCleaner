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

import org.datacleaner.beans.api.Close;
import org.datacleaner.beans.api.Initialize;
import org.datacleaner.beans.api.Transformer;
import org.datacleaner.data.InputRow;

/**
 * An abstract {@link Transformer} that supports batch/chunked operations.
 */
public abstract class BatchTransformer implements Transformer, BatchTransformation<InputRow, Object[]> {

    private final BatchTransformationBuffer<InputRow, Object[]> _batchTransformationBuffer;

    public BatchTransformer() {
        _batchTransformationBuffer = new BatchTransformationBuffer<InputRow, Object[]>(this, getMaxBatchSize(),
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
        return _batchTransformationBuffer.transform(inputRow);
    }

}
