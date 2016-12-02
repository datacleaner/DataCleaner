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
package org.datacleaner.extension.output;

import org.apache.metamodel.query.Query;
import org.datacleaner.api.Configured;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.MultiStreamComponent;
import org.datacleaner.api.OutputDataStream;
import org.datacleaner.api.OutputRowCollector;
import org.datacleaner.job.output.OutputDataStreamBuilder;
import org.datacleaner.job.output.OutputDataStreams;

/**
 * Test transformer which produces two output streams one containing the even rows going in and the other one
 * containing the uneven rows going in.
 */
public class MultiStreamTestTransformer extends MultiStreamComponent {
    static final String OUTPUT_STREAM_EVEN = "Even rows";
    static final String OUTPUT_STREAM_UNEVEN = "Uneven rows";
    @Configured
    InputColumn<?>[] _valueColumns;
    private OutputRowCollector _evenRowCollector;
    private OutputRowCollector _unevenRowCollector;

    private boolean _even = false;

    @Override
    public void initializeOutputDataStream(final OutputDataStream dataStream, final Query query,
            final OutputRowCollector collector) {
        if (dataStream.getName().equals(OUTPUT_STREAM_EVEN)) {
            _evenRowCollector = collector;
        } else {
            _unevenRowCollector = collector;
        }
    }

    @Override
    public OutputDataStream[] getOutputDataStreams() {
        final OutputDataStreamBuilder evenData = OutputDataStreams.pushDataStream(OUTPUT_STREAM_EVEN);
        final OutputDataStreamBuilder unevenData = OutputDataStreams.pushDataStream(OUTPUT_STREAM_UNEVEN);

        for (final InputColumn<?> column : _valueColumns) {
            evenData.withColumnLike(column);
            unevenData.withColumnLike(column);
        }

        return new OutputDataStream[] { evenData.toOutputDataStream(), unevenData.toOutputDataStream() };
    }


    @Override
    protected void run(final InputRow row) {
        if (_even && _evenRowCollector != null) {
            _evenRowCollector.putValues(row.getValues(_valueColumns).toArray());
        } else {
            if (!_even && _unevenRowCollector != null) {
                _unevenRowCollector.putValues(row.getValues(_valueColumns).toArray());
            }
        }
        _even = !_even;
    }
}
