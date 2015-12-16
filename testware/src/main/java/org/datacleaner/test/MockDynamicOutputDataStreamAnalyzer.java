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

import java.util.List;

import javax.inject.Named;

import org.apache.metamodel.query.Query;
import org.datacleaner.api.Analyzer;
import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.Configured;
import org.datacleaner.api.HasOutputDataStreams;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputDataStream;
import org.datacleaner.api.OutputRowCollector;
import org.datacleaner.job.output.OutputDataStreamBuilder;
import org.datacleaner.job.output.OutputDataStreams;

@Named("Mock dynamic output data stream analyzer")
public class MockDynamicOutputDataStreamAnalyzer implements Analyzer<MockDynamicOutputDataStreamAnalyzer>,
        HasOutputDataStreams, AnalyzerResult {

    private static final long serialVersionUID = 1L;

    private transient OutputRowCollector collector;

    @Configured
    String streamName;

    @Configured
    InputColumn<?>[] columns;

    @Override
    public OutputDataStream[] getOutputDataStreams() {
        final OutputDataStreamBuilder streamBuilder = OutputDataStreams.pushDataStream(streamName);
        for (InputColumn<?> column : columns) {
            streamBuilder.withColumnLike(column);
        }
        return new OutputDataStream[] { streamBuilder.toOutputDataStream() };
    }

    @Override
    public void initializeOutputDataStream(OutputDataStream outputDataStream, Query query,
            OutputRowCollector outputRowCollector) {
        collector = outputRowCollector;
    }

    @Override
    public void run(InputRow row, int distinctCount) {
        if (row.getId() % 2 == 0) {
            final List<Object> values = row.getValues(columns);
            collector.putValues(values);
        }
    }

    @Override
    public MockDynamicOutputDataStreamAnalyzer getResult() {
        return this;
    }

}
