package org.eobjects.datacleaner.extension.elasticsearch;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

public class ElasticSearchClientFactory {

    private final String[] _hosts;
    private final String _clusterName;

    public ElasticSearchClientFactory(String[] hosts, String clusterName) {
        _hosts = hosts;
        _clusterName = clusterName;
    }

    public Client create() {
        Settings settings = ImmutableSettings.builder().put("name", "DataCleaner").put("cluster.name", _clusterName)
                .build();
        TransportClient transportClient = new TransportClient(settings, false);

        for (String clusterHost : _hosts) {
            String hostname = clusterHost.trim();
            int port = 9300;

            int indexOfColon = hostname.indexOf(":");
            if (indexOfColon != -1) {
                port = Integer.parseInt(hostname.substring(indexOfColon + 1));
                hostname = hostname.substring(0, indexOfColon);
            }
            transportClient.addTransportAddress(new InetSocketTransportAddress(hostname, port));
        }

        return transportClient;
    }
}
