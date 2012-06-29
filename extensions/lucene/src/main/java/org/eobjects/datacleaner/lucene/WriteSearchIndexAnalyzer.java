/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Initialize;
import org.eobjects.analyzer.beans.writers.WriteBuffer;
import org.eobjects.analyzer.beans.writers.WriteBufferSizeOption;
import org.eobjects.analyzer.beans.writers.WriteDataResult;
import org.eobjects.analyzer.beans.writers.WriteDataResultImpl;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

@AnalyzerBean("Write to Lucene search index")
public class WriteSearchIndexAnalyzer implements Analyzer<WriteDataResult> {

    @Configured
    InputColumn<String>[] values;

    // TODO: Add converter
    @Configured
    SearchIndex searchIndex;

    @Configured
    String[] searchFields;

    @Configured
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
