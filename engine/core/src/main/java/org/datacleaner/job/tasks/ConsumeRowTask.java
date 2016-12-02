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
package org.datacleaner.job.tasks;

import org.datacleaner.api.InputRow;
import org.datacleaner.job.runner.AnalysisListener;
import org.datacleaner.job.runner.ConsumeRowHandler;
import org.datacleaner.job.runner.RowProcessingConsumer;
import org.datacleaner.job.runner.RowProcessingMetrics;

/**
 * A {@link Task} that dispatches ("consumes") a record to all relevant
 * {@link RowProcessingConsumer}s (eg. analyzerbeans components).
 */
public final class ConsumeRowTask implements Task {

    private final RowProcessingMetrics _rowProcessingMetrics;
    private final InputRow _row;
    private final AnalysisListener _analysisListener;
    private final ConsumeRowHandler _consumeRowHandler;
    private final int _rowNumber;

    public ConsumeRowTask(final ConsumeRowHandler consumeRowHandler, final RowProcessingMetrics rowProcessingMetrics,
            final InputRow row, final AnalysisListener analysisListener, final int rowNumber) {
        _consumeRowHandler = consumeRowHandler;
        _rowProcessingMetrics = rowProcessingMetrics;
        _row = row;
        _analysisListener = analysisListener;
        _rowNumber = rowNumber;
    }

    @Override
    public void execute() {
        _consumeRowHandler.consumeRow(_row).getRows();
        _analysisListener.rowProcessingProgress(_rowProcessingMetrics.getAnalysisJobMetrics().getAnalysisJob(),
                _rowProcessingMetrics, _row, _rowNumber);
    }

}
