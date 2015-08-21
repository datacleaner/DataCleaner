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

import java.util.Collection;

import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.tasks.Task;

/**
 * This task will walk through all publishers and fire
 * {@link AnalysisListener#rowProcessingBegin(AnalysisJob, RowProcessingMetrics)}
 */
class FireRowProcessingBeginTask implements Task {
    private final RowProcessingMetrics _rowProcessingMetrics;
    private RowProcessingPublisher _rowProcessingPublisher;

    public FireRowProcessingBeginTask(final RowProcessingPublisher rowProcessingPublisher,
            final RowProcessingMetrics rowProcessingMetrics) {
        _rowProcessingPublisher = rowProcessingPublisher;
        _rowProcessingMetrics = rowProcessingMetrics;
    }

    @Override
    public void execute() throws Exception {
        executeInternal(_rowProcessingPublisher, _rowProcessingMetrics);
    }

    public void executeInternal(RowProcessingPublisher publisher, RowProcessingMetrics rowProcessingMetrics) {
        publisher.getAnalysisListener().rowProcessingBegin(publisher.getAnalysisJob(), rowProcessingMetrics);

        for (RowProcessingConsumer rowProcessingConsumer : publisher.getConsumers()) {
            final Collection<ActiveOutputDataStream> activeOutputDataStreams = rowProcessingConsumer.getActiveOutputDataStreams();
            for (ActiveOutputDataStream activeOutputDataStream : activeOutputDataStreams) {
                final RowProcessingPublisher activeOutputDataStreamPublisher = activeOutputDataStream.getPublisher();
                executeInternal(activeOutputDataStreamPublisher, activeOutputDataStreamPublisher.getRowProcessingMetrics());
            }
        }
    }
}
