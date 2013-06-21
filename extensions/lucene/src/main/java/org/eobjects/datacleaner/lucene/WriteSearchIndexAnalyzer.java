/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.lucene;

import java.util.concurrent.atomic.AtomicInteger;

import org.eobjects.analyzer.beans.api.Analyzer;
import org.eobjects.analyzer.beans.api.AnalyzerBean;
import org.eobjects.analyzer.beans.api.Categorized;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Convertable;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.Initialize;
import org.eobjects.analyzer.beans.writers.WriteBuffer;
import org.eobjects.analyzer.beans.writers.WriteBufferSizeOption;
import org.eobjects.analyzer.beans.writers.WriteDataCategory;
import org.eobjects.analyzer.beans.writers.WriteDataResult;
import org.eobjects.analyzer.beans.writers.WriteDataResultImpl;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

@AnalyzerBean("Write to Lucene index")
@Description("Writes data to a Lucene index, making it searchable at a later stage.")
@Categorized(WriteDataCategory.class)
public class WriteSearchIndexAnalyzer implements Analyzer<WriteDataResult> {

    @Configured(order = 1, value = "Value(s) and field name(s) in index")
    InputColumn<String>[] values;

    @Configured(order = 2)
    String[] searchFields;

    @Configured(order = 10)
    @Convertable(SearchIndexConverter.class)
    @Description("Search index to write to.")
    SearchIndex searchIndex;

    @Configured(order = 20)
    @Description("How much data to buffer before committing batches of data. Large batches often perform better, but require more memory.")
    WriteBufferSizeOption bufferSize = WriteBufferSizeOption.LARGE;

    private WriteBuffer _writeBuffer;
    private AtomicInteger _counter;

    @Initialize
    public void init() {
        final int numColumns = searchFields.length;
        _writeBuffer = new WriteBuffer(bufferSize.calculateBufferSize(numColumns), new WriteSearchIndexAction(
                searchIndex, searchFields));
        _counter = new AtomicInteger();
    }

    @Override
    public void run(InputRow row, int distinctCount) {
        final Object[] rowData = new Object[searchFields.length];
        for (int i = 0; i < searchFields.length; i++) {
            final String value = row.getValue(values[i]);
            rowData[i] = value;
        }
        _writeBuffer.addToBuffer(rowData);
        _counter.incrementAndGet();
    }

    @Override
    public WriteDataResult getResult() {
        _writeBuffer.flushBuffer();
        return new WriteDataResultImpl(_counter.get(), (Datastore) null, "SearchIndex", searchIndex.getName());
    }

}
