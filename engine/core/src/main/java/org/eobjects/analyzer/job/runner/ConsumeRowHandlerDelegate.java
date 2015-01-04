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
package org.eobjects.analyzer.job.runner;

import java.util.ArrayList;
import java.util.List;

import org.eobjects.analyzer.data.InputRow;

/**
 * Delegate execution object for {@link ConsumeRowHandler}. Contains state
 * particular to processing of a single consumer in the chain, and also handles
 * recursive logic coming from {@link RowProcessingChain} callbacks.
 */
final class ConsumeRowHandlerDelegate implements RowProcessingChain {

    private final List<RowProcessingConsumer> _consumers;
    private final InputRow _row;
    private final int _consumerIndex;
    private final FilterOutcomes _outcomes;
    private final List<InputRow> _resultRecords;
    private final List<FilterOutcomes> _resultOutcomes;

    public ConsumeRowHandlerDelegate(final List<RowProcessingConsumer> consumers, final InputRow row,
            final int consumerIndex, final FilterOutcomes outcomes) {
        this(consumers, row, consumerIndex, outcomes, new ArrayList<InputRow>(1), new ArrayList<FilterOutcomes>(1));
    }

    private ConsumeRowHandlerDelegate(final List<RowProcessingConsumer> consumers, final InputRow row,
            final int consumerIndex, final FilterOutcomes outcomes, final List<InputRow> resultRecords,
            final List<FilterOutcomes> resultOutcomes) {
        _consumers = consumers;
        _row = row;
        _consumerIndex = consumerIndex;
        _outcomes = outcomes;
        _resultRecords = resultRecords;
        _resultOutcomes = resultOutcomes;
    }

    public ConsumeRowResult consume() {
        final RowProcessingConsumer consumer = _consumers.get(_consumerIndex);

        final boolean process = consumer.satisfiedForConsume(_outcomes, _row);
        if (process) {
            if (consumer.isConcurrent()) {
                consumer.consume(_row, 1, _outcomes, this);
            } else {
                synchronized (consumer) {
                    consumer.consume(_row, 1, _outcomes, this);
                }
            }
        } else {
            // jump to the next step
            processNext(_row, 1, _outcomes);
        }

        return new ConsumeRowResult(_resultRecords, _resultOutcomes);
    }

    @Override
    public void processNext(final InputRow row, final int distinctCount, final FilterOutcomes outcomes) {
        final int nextIndex = _consumerIndex + 1;
        if (nextIndex >= _consumers.size()) {
            // finished!
            _resultRecords.add(row);
            _resultOutcomes.add(outcomes);
            return;
        }

        final ConsumeRowHandlerDelegate subDelegate = new ConsumeRowHandlerDelegate(_consumers, row, nextIndex,
                outcomes, _resultRecords, _resultOutcomes);
        subDelegate.consume();
    }

}
