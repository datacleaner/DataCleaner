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

import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.eobjects.metamodel.util.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WriteBuffer flush action for writing documents to the elastic search index.
 */
public class ElasticSearchIndexFlushAction implements Action<Iterable<Object[]>> {

    private static final Logger logger = LoggerFactory.getLogger(ElasticSearchIndexFlushAction.class);

    private final ElasticSearchClientFactory _clientFactory;
    private final String[] _fields;
    private final String _indexName;
    private final String _documentType;

    public ElasticSearchIndexFlushAction(ElasticSearchClientFactory clientFactory, String[] fields, String indexName,
            String documentType) {
        _clientFactory = clientFactory;
        _fields = fields;
        _indexName = indexName;
        _documentType = documentType;
    }

    @Override
    public void run(Iterable<Object[]> rows) throws Exception {
        Client client = _clientFactory.create();
        try {
            BulkRequestBuilder bulkRequestBuilder = new BulkRequestBuilder(client);

            for (Object[] row : rows) {
                final String id = (String) row[0];
                final Map<String, Object> map = new HashMap<String, Object>();
                for (int i = 1; i < row.length; i++) {
                    String field = _fields[i - 1];
                    Object value = row[i];
                    if (value != null) {
                        map.put(field, value);
                    }
                }
                logger.debug("Indexing record ({}): {}", id, map);
                final IndexRequest indexRequest = new IndexRequest(_indexName, _documentType, id);
                indexRequest.source(map);
                bulkRequestBuilder.add(indexRequest);
            }

            // execute and block until done.
            BulkResponse response;
            try {
                response = bulkRequestBuilder.execute().actionGet();
            } catch (NoNodeAvailableException e) {
                // retry after a short wait
                Thread.sleep(100);
                response = bulkRequestBuilder.execute().actionGet();
            }
            if (response.hasFailures()) {
                throw new IllegalStateException(response.buildFailureMessage());
            }
        } finally {
            client.close();
        }
    }

}
