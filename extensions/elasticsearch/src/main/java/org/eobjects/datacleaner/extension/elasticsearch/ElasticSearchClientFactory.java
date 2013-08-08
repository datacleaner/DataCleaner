package org.eobjects.datacleaner.extension.elasticsearch;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;

public class ElasticSearchClientFactory {

    private final TransportAddress[] _transportAddresses;
    private final Settings _settings;

    public ElasticSearchClientFactory(String[] hosts, String clusterName) {
        _transportAddresses = new TransportAddress[hosts.length];
        for (int i = 0; i < hosts.length; i++) {
            String hostname = hosts[i].trim();
            int port = 9300;

            int indexOfColon = hostname.indexOf(":");
            if (indexOfColon != -1) {
                port = Integer.parseInt(hostname.substring(indexOfColon + 1));
                hostname = hostname.substring(0, indexOfColon);
            }
            InetSocketTransportAddress transportAddress = new InetSocketTransportAddress(hostname, port);
            _transportAddresses[i] = transportAddress;
        }
        _settings = ImmutableSettings.builder().put("name", "DataCleaner").put("cluster.name", clusterName).build();
    }

    public Client create() {
        final TransportClient transportClient = new TransportClient(_settings, false);

        for (TransportAddress transportAddress : _transportAddresses) {
            transportClient.addTransportAddress(transportAddress);
        }

        return transportClient;
    }
}
