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

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.metamodel.MetaModelHelper;
import org.apache.metamodel.data.CachingDataSetHeader;
import org.apache.metamodel.data.DefaultRow;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.query.Query;
import org.datacleaner.api.HasOutputDataStreams;
import org.datacleaner.api.OutputDataStream;
import org.datacleaner.api.OutputRowCollector;
import org.datacleaner.data.MetaModelInputRow;
import org.datacleaner.job.OutputDataStreamJob;

/**
 * The type of {@link OutputRowCollector} used for {@link OutputDataStreamJob}
 * execution. This instance will eventually be passed into the
 * {@link HasOutputDataStreams#initializeOutputDataStream(org.datacleaner.api.OutputDataStream, org.apache.metamodel.query.Query, OutputRowCollector)}
 * method.
 */
public class OutputDataStreamRowCollector implements OutputRowCollector {

    private final OutputDataStreamJob _outputDataStreamJob;
    private final RowProcessingPublisher _publisher;
    private final CachingDataSetHeader _dataSetHeader;
    private final AtomicInteger _rowCounter;
    private ConsumeRowHandler _consumeRowHandler;

    public OutputDataStreamRowCollector(OutputDataStreamJob outputDataStreamJob, RowProcessingPublisher publisher) {
        _outputDataStreamJob = outputDataStreamJob;
        _publisher = publisher;
        _dataSetHeader = new CachingDataSetHeader(MetaModelHelper.createSelectItems(publisher.getTable().getColumns()));
        _rowCounter = new AtomicInteger();
    }

    @Override
    public void putValues(Object... values) {
        final DefaultRow row = new DefaultRow(_dataSetHeader, values);
        putRow(row);
    }

    @Override
    public void putRow(Row row) {
        MetaModelInputRow inputRow = new MetaModelInputRow(_rowCounter.incrementAndGet(), row);
        _consumeRowHandler.consumeRow(inputRow);
    }

    public void configure(HasOutputDataStreams hasOutputDataStreams) {
        _consumeRowHandler = _publisher.createConsumeRowHandler();
        final OutputDataStream outputDataStream = _outputDataStreamJob.getOutputDataStream();
        final Query query = new Query().from(outputDataStream.getTable()).selectAll();
        hasOutputDataStreams.initializeOutputDataStream(outputDataStream, query, this);
    }

}
