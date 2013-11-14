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
package org.eobjects.datacleaner.extension.elasticsearch;

import java.util.concurrent.atomic.AtomicInteger;

import org.eobjects.analyzer.beans.api.Analyzer;
import org.eobjects.analyzer.beans.api.AnalyzerBean;
import org.eobjects.analyzer.beans.api.Categorized;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.Initialize;
import org.eobjects.analyzer.beans.api.NumberProperty;
import org.eobjects.analyzer.beans.writers.WriteBuffer;
import org.eobjects.analyzer.beans.writers.WriteDataResult;
import org.eobjects.analyzer.beans.writers.WriteDataResultImpl;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

@AnalyzerBean("ElasticSearch indexer")
@Description("Consumes records and indexes them in a ElasticSearch search index.")
@Categorized(ElasticSearchCategory.class)
public class ElasticSearchIndexAnalyzer implements Analyzer<WriteDataResult> {

    @Configured
    String[] clusterHosts = { "localhost:9300" };

    @Configured
    String clusterName = "elasticsearch";

    @Configured
    String indexName;

    @Configured
    String documentType;

    @Configured
    InputColumn<String> idColumn;

    @Configured
    String[] fields;

    @Configured
    InputColumn<?>[] values;

    @Configured
    boolean createIndex = false;

    @Configured
    @NumberProperty(negative = false, zero = false)
    int bulkIndexSize = 2000;

    private ElasticSearchClientFactory _clientFactory;
    private AtomicInteger _counter;
    private WriteBuffer _writeBuffer;

    @Initialize
    public void init() {
        _clientFactory = new ElasticSearchClientFactory(clusterHosts, clusterName);

        _counter = new AtomicInteger(0);
        _writeBuffer = new WriteBuffer(bulkIndexSize, new ElasticSearchIndexFlushAction(_clientFactory, fields,
                indexName, documentType));
    }

    @Override
    public void run(InputRow row, int distinctCount) {
        final Object[] record = new Object[values.length + 1];
        final String id = row.getValue(idColumn);
        record[0] = id;
        for (int i = 0; i < values.length; i++) {
            Object value = row.getValue(values[i]);
            record[i + 1] = value;
        }
        _writeBuffer.addToBuffer(record);
        _counter.incrementAndGet();
    }

    @Override
    public WriteDataResult getResult() {
        _writeBuffer.flushBuffer();

        final int indexCount = _counter.get();
        final WriteDataResult result = new WriteDataResultImpl(indexCount, 0, 0);
        return result;
    }
}
