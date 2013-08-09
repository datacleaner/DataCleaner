package org.eobjects.datacleaner.extension.elasticsearch;

import java.util.concurrent.ExecutionException;

import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.count.CountRequest;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.deletebyquery.DeleteByQueryResponse;
import org.elasticsearch.action.deletebyquery.IndexDeleteByQueryResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;

public class ElasticSearchTestServer {

    public static final int HTTP_PORT = 9205;
    public static final String INDEX_NAME = "testindex";
    public static final String CLUSTER_NAME = "testcluster";
    public static final String TRANSPORT_PORT = "9300";
    public static final String DOCUMENT_TYPE = "testdoc";

    public static void main(String[] args) throws Exception {
        ElasticSearchTestServer server = new ElasticSearchTestServer();
        server.startup();

        Thread.sleep(60 * 1000);

        server.close();
    }

    private Node _node;

    public ElasticSearchTestServer() {
    }

    public void startup() throws Exception {
        ImmutableSettings.Builder settings = ImmutableSettings.settingsBuilder();
        settings.put("node.name", "testnode");
        settings.put("path.data", "target/search-data");
        settings.put("http.enabled", true);
        settings.put("http.port", HTTP_PORT);
        settings.put("transport.tcp.port", TRANSPORT_PORT);
        _node = NodeBuilder.nodeBuilder().settings(settings).clusterName(CLUSTER_NAME).data(true).local(false).node();

        Client client = _node.client();
        IndicesAdminClient indicesAdmin = client.admin().indices();
        if (!indicesAdmin.exists(new IndicesExistsRequest(INDEX_NAME)).actionGet().isExists()) {
            indicesAdmin.create(new CreateIndexRequest(INDEX_NAME)).actionGet();
        }
        client.close();

        System.out.println("--- ElasticSearchTestServer started ---");
    }

    public Client getClient() {
        return _node.client();
    }

    public IndexDeleteByQueryResponse truncateIndex() throws InterruptedException, ExecutionException {
        Client client = getClient();
        try {
            QueryBuilder queryBuilder = new MatchAllQueryBuilder();
            ListenableActionFuture<DeleteByQueryResponse> response = client.prepareDeleteByQuery(INDEX_NAME)
                    .setTypes(DOCUMENT_TYPE).setQuery(queryBuilder).execute();
            DeleteByQueryResponse deleteByQueryResponse = response.get();
            IndexDeleteByQueryResponse indexResult = deleteByQueryResponse.getIndex(INDEX_NAME);
            return indexResult;
        } finally {
            client.close();
        }
    }

    public long getDocumentCount() throws Exception {
        Client client = getClient();
        try {
            client.admin().indices().refresh(new RefreshRequest(INDEX_NAME)).actionGet();

            ActionFuture<CountResponse> response = client.count(new CountRequest(INDEX_NAME).types(DOCUMENT_TYPE));
            CountResponse countResponse = response.get();
            return countResponse.getCount();
        } finally {
            client.close();
        }
    }

    public void close() {
        _node.close();

        System.out.println("--- ElasticSearchTestServer closed ---");
    }
}
