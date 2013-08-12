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

import java.util.Map;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Initialize;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.util.StringUtils;

@TransformerBean("ElasticSearch full text search")
public class ElasticSearchFullSearchTransformer implements Transformer<Object> {

    @Configured
    InputColumn<String> searchInput;

    @Configured
    String[] clusterHosts = { "localhost:9300" };

    @Configured
    String clusterName = "elasticsearch";

    @Configured
    String indexName;

    @Configured
    String documentType;

    private ElasticSearchClientFactory _clientFactory;

    @Initialize
    public void init() {
        _clientFactory = new ElasticSearchClientFactory(clusterHosts, clusterName);
    }

    @Override
    public OutputColumns getOutputColumns() {
        String[] names = new String[] { "Document ID", "Document" };
        Class<?>[] types = new Class[] { String.class, Map.class };
        return new OutputColumns(names, types);
    }

    @Override
    public Object[] transform(InputRow row) {
        final Object[] result = new Object[2];

        final String input = row.getValue(searchInput);
        if (StringUtils.isNullOrEmpty(input)) {
            return result;
        }

        final Client client = _clientFactory.create();
        try {
            final QueryBuilder query = QueryBuilders.queryString(input);
            final SearchRequestBuilder searchRequestBuilder = new SearchRequestBuilder(client).setIndices(indexName)
                    .setTypes(documentType).setQuery(query).setSize(1).setSearchType(SearchType.QUERY_AND_FETCH)
                    .setExplain(true);

            final SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();
            final SearchHits hits = searchResponse.getHits();
            if (hits.getTotalHits() == 0) {
                return result;
            }

            final SearchHit hit = hits.getAt(0);
            result[0] = hit.getId();
            result[1] = hit.sourceAsMap();

        } finally {
            client.close();
        }

        return result;
    }
}
