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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Named;

import org.apache.metamodel.query.Query;
import org.apache.metamodel.schema.ColumnType;
import org.datacleaner.api.Analyzer;
import org.datacleaner.api.Configured;
import org.datacleaner.api.HasOutputDataStreams;
import org.datacleaner.api.Initialize;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputDataStream;
import org.datacleaner.api.OutputRowCollector;
import org.datacleaner.api.Validate;
import org.datacleaner.job.output.OutputDataStreams;
import org.datacleaner.result.ListResult;
import org.junit.Assert;

@Named("Mock output data stream analyzer")
public class MockOutputDataStreamAnalyzer implements Analyzer<ListResult<Integer>>, HasOutputDataStreams {

    private final OutputDataStream stream1 = OutputDataStreams.pushDataStream("foo bar records")
            .withColumn("foo", ColumnType.STRING).withColumn("bar", ColumnType.TIMESTAMP).toOutputDataStream();

    private final OutputDataStream stream2 = OutputDataStreams.pushDataStream("counter records")
            .withColumn("count", ColumnType.INTEGER).withColumn("uuid", ColumnType.STRING).toOutputDataStream();

    private OutputRowCollector collector1;
    private OutputRowCollector collector2;
    private AtomicInteger counter;
    private List<Integer> list;

    @Configured
    InputColumn<?> column;

    @Validate
    public void validate() {
        Assert.assertNull("Spec defines that initializeOutputDataStream(...) is not called before validation time",
                collector1);
        Assert.assertNull("Spec defines that initializeOutputDataStream(...) is not called before validation time",
                collector2);
    }

    @Initialize
    public void init() {
        list = new ArrayList<>();
    }

    @Override
    public OutputDataStream[] getOutputDataStreams() {
        return new OutputDataStream[] { stream1, stream2 };
    }

    @Override
    public void initializeOutputDataStream(OutputDataStream outputDataStream, Query query,
            OutputRowCollector outputRowCollector) {
        Assert.assertNotNull(outputDataStream);
        Assert.assertNotNull(query);
        Assert.assertNotNull(outputRowCollector);

        if (outputDataStream.equals(stream1)) {
            collector1 = outputRowCollector;
        } else if (outputDataStream.equals(stream2)) {
            collector2 = outputRowCollector;
            counter = new AtomicInteger();
        } else {
            Assert.fail("Unexpected outputDataStream: " + outputDataStream);
        }
    }

    @Override
    public void run(InputRow row, int distinctCount) {
        final int id = row.getId();
        if (id % 3 == 0) {
            // one third of the times we will write to our result list
            list.add(id);
        } else {
            // other times we will write to the collectors that are available
            if (collector1 != null) {
                collector1.putValues("bar", new Date());
            }
            if (collector2 != null) {
                final int count = counter.incrementAndGet();
                final String uuid = UUID.randomUUID().toString();
                collector2.putValues(count, uuid);
            }
        }
    }

    @Override
    public ListResult<Integer> getResult() {
        if (collector1 != null) {
            collector1.putValues("baz", null);
        }
        if (collector2 != null) {
            final int count = counter.incrementAndGet();
            final String uuid = UUID.randomUUID().toString();
            collector2.putValues(count, uuid);
        }
        return new ListResult<>(list);
    }
}
