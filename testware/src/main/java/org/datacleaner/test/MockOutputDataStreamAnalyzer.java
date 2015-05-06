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
package org.datacleaner.test;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Named;

import org.apache.metamodel.query.Query;
import org.apache.metamodel.schema.ColumnType;
import org.datacleaner.api.HasOutputDataStreams;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputDataStream;
import org.datacleaner.api.OutputRowCollector;
import org.datacleaner.job.output.OutputDataStreams;

@Named("Mock output data stream analyzer")
public class MockOutputDataStreamAnalyzer extends MockAnalyzer implements HasOutputDataStreams {

    private final OutputDataStream stream1 = OutputDataStreams.pushDataStream("foo bar records")
            .withColumn("foo", ColumnType.STRING).withColumn("bar", ColumnType.TIMESTAMP).toOutputDataStream();

    private final OutputDataStream stream2 = OutputDataStreams.pushDataStream("counter records")
            .withColumn("count", ColumnType.INTEGER).withColumn("uuid", ColumnType.STRING).toOutputDataStream();

    private OutputRowCollector collector1;
    private OutputRowCollector collector2;
    private AtomicInteger counter;

    @Override
    public void run(InputRow row, int distinctCount) {
        super.run(row, distinctCount);

        if (collector1 != null) {
            collector1.putValues("bar", new Date());
        }
        if (collector2 != null) {
            final int count = counter.incrementAndGet();
            final String uuid = UUID.randomUUID().toString();
            collector2.putValues(count, uuid);
        }
    }

    @Override
    public OutputDataStream[] getOutputDataStreams() {
        return new OutputDataStream[] { stream1, stream2 };
    }

    @Override
    public void initializeOutputDataStream(OutputDataStream outputDataStream, Query query,
            OutputRowCollector outputRowCollector) {
        if (outputDataStream.equals(stream1)) {
            collector1 = outputRowCollector;
        }
        if (outputDataStream.equals(stream2)) {
            collector2 = outputRowCollector;
            counter = new AtomicInteger();
        }
    }

}
