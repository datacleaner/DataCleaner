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

import java.io.Closeable;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.metamodel.query.Query;
import org.apache.metamodel.query.SelectItem;
import org.datacleaner.api.HasOutputDataStreams;
import org.datacleaner.api.OutputDataStream;
import org.datacleaner.job.OutputDataStreamJob;

public class ActiveOutputDataStream implements Closeable {

    private final OutputDataStreamJob _outputDataStreamJob;
    private final RowProcessingPublisher _publisher;
    private final HasOutputDataStreams _component;
    private final CountDownLatch _countDownLatch;
    private OutputDataStreamRowCollector _outputRowCollector;

    public ActiveOutputDataStream(OutputDataStreamJob outputDataStreamJob, RowProcessingPublisher publisher,
            HasOutputDataStreams component) {
        _outputDataStreamJob = outputDataStreamJob;
        _publisher = publisher;
        _component = component;
        _countDownLatch = new CountDownLatch(1);
    }

    public RowProcessingPublisher getPublisher() {
        return _publisher;
    }

    public HasOutputDataStreams getComponent() {
        return _component;
    }

    public OutputDataStreamJob getOutputDataStreamJob() {
        return _outputDataStreamJob;
    }

    public void initialize(Query query) {
        final List<SelectItem> selectItems = query.getSelectClause().getItems();
        final ConsumeRowHandler consumeRowHandler = _publisher.createConsumeRowHandler();
        _outputRowCollector = new OutputDataStreamRowCollector(selectItems, consumeRowHandler);
        final OutputDataStream outputDataStream = _outputDataStreamJob.getOutputDataStream();
        _component.initializeOutputDataStream(outputDataStream, query, _outputRowCollector);
    }

    public void await() throws InterruptedException {
        _countDownLatch.await();
    }

    @Override
    public void close() {
        _countDownLatch.countDown();
    }
}
