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
package org.datacleaner.job.runner;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.datacleaner.api.Concurrent;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputRowCollector;
import org.datacleaner.api.Transformer;
import org.datacleaner.data.TransformedInputRow;
import org.datacleaner.descriptors.ProvidedPropertyDescriptor;
import org.datacleaner.job.FilterOutcomes;
import org.datacleaner.job.TransformerJob;
import org.datacleaner.job.concurrent.ThreadLocalOutputRowCollector;
import org.datacleaner.job.concurrent.ThreadLocalOutputRowCollector.Listener;

/**
 * {@link RowProcessingConsumer} implementation for {@link Transformer}s.
 */
final class TransformerConsumer extends AbstractRowProcessingConsumer implements RowProcessingConsumer {

    private final Transformer _transformer;
    private final TransformerJob _transformerJob;
    private final InputColumn<?>[] _inputColumns;
    private final boolean _concurrent;
    private RowIdGenerator _idGenerator;

    public TransformerConsumer(Transformer transformer, TransformerJob transformerJob,
            InputColumn<?>[] inputColumns, RowProcessingPublisher publisher) {
        super(publisher, transformerJob, transformerJob);
        _transformer = transformer;
        _transformerJob = transformerJob;
        _inputColumns = inputColumns;
        _concurrent = determineConcurrent();
    }

    private boolean determineConcurrent() {
        Concurrent concurrent = _transformerJob.getDescriptor().getAnnotation(Concurrent.class);
        if (concurrent == null) {
            // transformers are by default concurrent
            return true;
        }
        return concurrent.value();
    }

    /**
     * Sets the row id generator to use, when creating new transformed records.
     * 
     * @param idGenerator
     */
    public void setRowIdGenerator(RowIdGenerator idGenerator) {
        _idGenerator = idGenerator;
    }

    @Override
    public boolean isConcurrent() {
        return _concurrent;
    }

    @Override
    public InputColumn<?>[] getRequiredInput() {
        return _inputColumns;
    }

    @Override
    public Transformer getComponent() {
        return _transformer;
    }

    @Override
    public InputColumn<?>[] getOutputColumns() {
        return _transformerJob.getOutput();
    }

    @Override
    public void consumeInternal(final InputRow row, final int distinctCount, final FilterOutcomes outcomes,
            final RowProcessingChain chain) {
        final InputColumn<?>[] outputColumns = getOutputColumns();

        registerListener(_transformer, row, outcomes, chain, outputColumns);

        try {
            final Object[] values = _transformer.transform(row);
            if (values == null) {
                return;
            }
            final TransformedInputRow resultRow;
            if (row instanceof TransformedInputRow) {
                // re-use existing transformed input row.
                resultRow = (TransformedInputRow) row;
            } else {
                resultRow = new TransformedInputRow(row);
            }
            addValuesToRow(resultRow, outputColumns, values);
            chain.processNext(resultRow, distinctCount, outcomes);
        } finally  {
            unregisterListener(_transformer);
        }
    }

    private void unregisterListener(Transformer transformer) {
        final Set<ProvidedPropertyDescriptor> outputRowCollectorProperties = _transformerJob.getDescriptor()
                .getProvidedPropertiesByType(OutputRowCollector.class);
        for (ProvidedPropertyDescriptor descriptor : outputRowCollectorProperties) {
            OutputRowCollector outputRowCollector = (OutputRowCollector) descriptor.getValue(transformer);
            if (outputRowCollector instanceof ThreadLocalOutputRowCollector) {
                ((ThreadLocalOutputRowCollector) outputRowCollector).removeListener();
            }
        }
    }

    private void registerListener(final Transformer transformer, final InputRow row, final FilterOutcomes outcomes,
            final RowProcessingChain chain, final InputColumn<?>[] outputColumns) {
        final Set<ProvidedPropertyDescriptor> outputRowCollectorProperties = _transformerJob.getDescriptor()
                .getProvidedPropertiesByType(OutputRowCollector.class);
        if (outputRowCollectorProperties == null || outputRowCollectorProperties.isEmpty()) {
            return;
        }

        final Listener listener = new Listener() {
            private AtomicInteger recordNumber = new AtomicInteger(0);

            @Override
            public void onValues(Object[] values) {
                int recordNo = recordNumber.incrementAndGet();
                boolean isFirst = recordNo == 1;
                final TransformedInputRow resultRow;
                if (isFirst) {
                    // retain the first record's id
                    resultRow = new TransformedInputRow(row);
                } else {
                    resultRow = new TransformedInputRow(row, getNextVirtualRowId(row, recordNo));
                }

                addValuesToRow(resultRow, outputColumns, values);

                FilterOutcomes clonedOutcomeSink = outcomes.clone();
                chain.processNext(resultRow, 1, clonedOutcomeSink);
            }
        };

        for (ProvidedPropertyDescriptor descriptor : outputRowCollectorProperties) {
            OutputRowCollector outputRowCollector = (OutputRowCollector) descriptor.getValue(transformer);
            if (outputRowCollector instanceof ThreadLocalOutputRowCollector) {
                ((ThreadLocalOutputRowCollector) outputRowCollector).setListener(listener);
            } else {
                throw new UnsupportedOperationException("Unsupported output row collector type: " + outputRowCollector);
            }
        }
    }

    private int getNextVirtualRowId(InputRow row, int recordNo) {
        if (_idGenerator == null) {
            // this can more or less never happen, except in test cases or in
            // cases where the consumers are programmatically being used outside
            // of an AnalysisRunner. There's a risk then here that we get the
            // same row ID twice, but that's life :-P
            int offset = Integer.MAX_VALUE;
            int hiLoIntervalOffset = row.getId() * 1000;
            return offset - hiLoIntervalOffset + recordNo;
        }
        return _idGenerator.nextVirtualRowId();
    }

    private void addValuesToRow(TransformedInputRow resultRow, final InputColumn<?>[] outputColumns, Object[] values) {
        assert outputColumns.length == values.length;

        // add output values to row.
        for (int i = 0; i < outputColumns.length; i++) {
            final Object value;
            if (i < values.length) {
                value = values[i];
            } else {
                value = null;
            }
            final InputColumn<?> column = outputColumns[i];
            resultRow.addValue(column, value);
        }
    }

    @Override
    public TransformerJob getComponentJob() {
        return _transformerJob;
    }

    @Override
    public String toString() {
        return "TransformerConsumer[" + _transformer + "]";
    }
}
