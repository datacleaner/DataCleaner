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

import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.get.GetField;
import org.eobjects.analyzer.beans.api.Categorized;
import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.Initialize;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.beans.convert.ConvertToStringTransformer;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.util.StringUtils;

@TransformerBean("ElasticSearch document ID lookup")
@Description("Look up documents in ElasticSearch by providing a document ID")
@Categorized(ElasticSearchCategory.class)
public class ElasticSearchDocumentIdLookupTransformer implements Transformer<String> {

    @Configured
    InputColumn<?> documentId;

    @Configured
    String[] clusterHosts = { "localhost:9300" };

    @Configured
    String clusterName = "elasticsearch";

    @Configured
    String indexName;

    @Configured
    String documentType;

    @Configured
    @Description("Fields to return")
    String[] fields;

    private ElasticSearchClientFactory _clientFactory;

    @Initialize
    public void init() {
        _clientFactory = new ElasticSearchClientFactory(clusterHosts, clusterName);
    }

    @Override
    public OutputColumns getOutputColumns() {
        return new OutputColumns(fields);
    }

    @Override
    public String[] transform(InputRow row) {
        final String[] result = new String[fields.length];

        final String id = ConvertToStringTransformer.transformValue(row.getValue(documentId));
        if (StringUtils.isNullOrEmpty(id)) {
            return result;
        }

        final Client client = _clientFactory.create();
        try {
            final GetRequest request = new GetRequestBuilder(client).setId(id).setType(documentType).setFields(fields)
                    .setIndex(indexName).setOperationThreaded(false).request();
            final ActionFuture<GetResponse> getFuture = client.get(request);
            final GetResponse response = getFuture.actionGet();

            if (!response.isExists()) {
                return result;
            }

            for (int i = 0; i < fields.length; i++) {
                final String field = fields[i];
                final GetField valueGetter = response.getField(field);
                final Object value = valueGetter.getValue();
                result[i] = ConvertToStringTransformer.transformValue(value);
            }
        } finally {
            client.close();
        }

        return result;
    }
}
