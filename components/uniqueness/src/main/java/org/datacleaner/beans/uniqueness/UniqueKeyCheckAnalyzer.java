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
package org.datacleaner.beans.uniqueness;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Named;

import org.apache.metamodel.csv.CsvConfiguration;
import org.apache.metamodel.csv.CsvDataContext;
import org.apache.metamodel.csv.CsvWriter;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.util.Action;
import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.ToStringComparator;
import org.datacleaner.api.Analyzer;
import org.datacleaner.api.Concurrent;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.Initialize;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.util.WriteBuffer;
import org.datacleaner.util.sort.SortMergeWriter;

@Named("Unique key check")
@Description("Check your keys (or other fields) for uniqueness")
@Concurrent(true)
public class UniqueKeyCheckAnalyzer implements Analyzer<UniqueKeyCheckAnalyzerResult> {

    private static final int BUFFER_SIZE = 20000;

    private static final CsvConfiguration CSV_CONFIGURATION = new CsvConfiguration();

    @Configured
    InputColumn<?> column;

    private final int _bufferSize;
    private WriteBuffer _writeBuffer;
    private SortMergeWriter<String, Writer> _sorter;
    private AtomicInteger _rowCount;
    private AtomicInteger _nullCount;

    public UniqueKeyCheckAnalyzer() {
        this(BUFFER_SIZE);
    }

    public UniqueKeyCheckAnalyzer(int bufferSize) {
        _bufferSize = bufferSize;
    }

    @Initialize
    public void init() {
        _rowCount = new AtomicInteger();
        _nullCount = new AtomicInteger();
        _sorter = new SortMergeWriter<String, Writer>(_bufferSize, ToStringComparator.getComparator()) {
            private final CsvWriter csvWriter = new CsvWriter(CSV_CONFIGURATION);

            @Override
            protected void writeHeader(Writer writer) throws IOException {
                final String line = csvWriter.buildLine(new String[] { "text", "count" });
                writer.write(line);
            }

            @Override
            protected void writeRow(Writer writer, String row, int count) throws IOException {
                if (count > 1) {
                    final String line = csvWriter.buildLine(new String[] { row, "" + count });
                    writer.write(line);
                    writer.write('\n');
                }
            }

            @Override
            protected Writer createWriter(File file) {
                return FileHelper.getBufferedWriter(file);
            }
        };
        _writeBuffer = new WriteBuffer(_bufferSize, new Action<Iterable<Object[]>>() {
            @Override
            public void run(Iterable<Object[]> rows) throws Exception {
                for (Object[] objects : rows) {
                    final String string = (String) objects[0];
                    _sorter.append(string);
                }
            }
        });
    }

    @Override
    public void run(InputRow row, int distinctCount) {
        final Object value = row.getValue(column);

        _rowCount.addAndGet(distinctCount);

        if (value == null) {
            _nullCount.addAndGet(distinctCount);
        } else {
            String str = value.toString();

            for (int i = 0; i < distinctCount; i++) {
                _writeBuffer.addToBuffer(new Object[] { str });
            }
        }
    }

    @Override
    public UniqueKeyCheckAnalyzerResult getResult() {
        _writeBuffer.flushBuffer();

        File file;
        try {
            file = File.createTempFile("UniqueKeyCheckAnalyzer", ".txt");
        } catch (Exception e) {
            File tempDir = FileHelper.getTempDir();
            file = new File(tempDir, "UniqueKeyCheckAnalyzer-" + System.currentTimeMillis() + ".txt");
        }

        _sorter.write(file);

        final AtomicInteger nonUniques = new AtomicInteger();

        final Map<String, Integer> samples = new LinkedHashMap<String, Integer>();

        final CsvDataContext dataContext = new CsvDataContext(file, CSV_CONFIGURATION);
        try (final DataSet dataSet = dataContext.query().from(dataContext.getDefaultSchema().getTable(0))
                .select("text", "count").execute()) {
            int i = 0;
            while (dataSet.next()) {
                final String text = (String) dataSet.getRow().getValue(0);
                final String countStr = (String) dataSet.getRow().getValue(1);
                final int count = Integer.parseInt(countStr);
                if (i < 1000) {
                    // only build up to 1000 records in the sample
                    samples.put(text, count);
                }
                nonUniques.addAndGet(count);
                i++;
            }
        }

        final int nonUniqueCount = nonUniques.get();
        final int rowCount = _rowCount.get();
        final int nullCount = _nullCount.get();
        final int uniqueCount = rowCount - nullCount - nonUniqueCount;

        return new UniqueKeyCheckAnalyzerResult(rowCount, uniqueCount, nonUniqueCount, nullCount, samples);
    }
}
