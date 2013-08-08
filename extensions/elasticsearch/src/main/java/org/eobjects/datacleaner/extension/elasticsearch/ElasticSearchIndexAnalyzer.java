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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.count.CountRequest;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.deletebyquery.DeleteByQueryResponse;
import org.elasticsearch.action.deletebyquery.IndexDeleteByQueryResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.eobjects.analyzer.beans.api.Analyzer;
import org.eobjects.analyzer.beans.api.AnalyzerBean;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Initialize;
import org.eobjects.analyzer.beans.api.NumberProperty;
import org.eobjects.analyzer.beans.writers.WriteBuffer;
import org.eobjects.analyzer.beans.writers.WriteDataResult;
import org.eobjects.analyzer.beans.writers.WriteDataResultImpl;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;

@AnalyzerBean("ElasticSearch indexer")
public class ElasticSearchIndexAnalyzer implements Analyzer<WriteDataResult> {

    @Configured
    String[] clusterHosts = { "localhost:9200" };

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

    public IndexDeleteByQueryResponse truncateIndex() throws InterruptedException, ExecutionException {
        Client client = _clientFactory.create();
        try {
            QueryBuilder queryBuilder = new MatchAllQueryBuilder();
            ListenableActionFuture<DeleteByQueryResponse> response = client.prepareDeleteByQuery(indexName)
                    .setTypes(documentType).setQuery(queryBuilder).execute();
            DeleteByQueryResponse deleteByQueryResponse = response.get();
            IndexDeleteByQueryResponse indexResult = deleteByQueryResponse.getIndex(indexName);
            return indexResult;
        } finally {
            client.close();
        }
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
        if (indexCount > 0) {
            // refresh after all the bulks have run
            final Client client = _clientFactory.create();
            try {
                client.admin().indices().refresh(new RefreshRequest(indexName)).actionGet();
            } finally {
                client.close();
            }
        }

        final WriteDataResult result = new WriteDataResultImpl(indexCount, 0, 0);
        return result;
    }

    public long getDocumentCount() throws Exception {
        Client client = _clientFactory.create();
        try {
            ActionFuture<CountResponse> response = client.count(new CountRequest(indexName).types(documentType));
            CountResponse countResponse = response.get();
            return countResponse.getCount();
        } finally {
            client.close();
        }
    }

}
