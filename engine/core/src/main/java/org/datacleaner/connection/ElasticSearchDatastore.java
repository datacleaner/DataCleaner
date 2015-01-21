/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Neopost - Customer Information Management
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
package org.datacleaner.connection;

import org.apache.metamodel.elasticsearch.ElasticSearchDataContext;
import org.apache.metamodel.util.SimpleTableDef;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

/**
 * Datastore providing access to an ElasticSearch index.
 */
public class ElasticSearchDatastore extends UsageAwareDatastore<ElasticSearchDataContext> {

    private static final long serialVersionUID = 1L;

    public static final int DEFAULT_PORT = 9300;

    private final SimpleTableDef[] _tableDefs;
    private final String _indexName;
    private final String _hostname;
    private final int _port;
    private final String _clusterName;

    public ElasticSearchDatastore(String name, String hostname, int port, String clusterName, String indexName) {
        this(name, hostname, port, clusterName, indexName, null);
    }

    public ElasticSearchDatastore(String name, String hostname, int port, String clusterName, String indexName,
            SimpleTableDef[] tableDefs) {
        super(name);
        _hostname = hostname;
        _port = port;
        _clusterName = clusterName;
        _indexName = indexName;
        _tableDefs = tableDefs;
    }

    @Override
    public PerformanceCharacteristics getPerformanceCharacteristics() {
        return new PerformanceCharacteristicsImpl(true, false);
    }

    @Override
    protected UsageAwareDatastoreConnection<ElasticSearchDataContext> createDatastoreConnection() {
        final Builder settingsBuilder = ImmutableSettings.builder();
        settingsBuilder.put("name", "AnalyzerBeans");
        settingsBuilder.put("cluster.name", _clusterName);

        final Settings settings = settingsBuilder.build();
        final TransportClient client = new TransportClient(settings);
        client.addTransportAddress(new InetSocketTransportAddress(_hostname, _port));

        final ElasticSearchDataContext dataContext;
        if (_tableDefs == null || _tableDefs.length == 0) {
            dataContext = new ElasticSearchDataContext(client, _indexName);
        } else {
            dataContext = new ElasticSearchDataContext(client, _indexName, _tableDefs);
        }
        return new DatastoreConnectionImpl<ElasticSearchDataContext>(dataContext, this);
    }

    public SimpleTableDef[] getTableDefs() {
        return _tableDefs;
    }

    public String getHostname() {
        return _hostname;
    }

    public int getPort() {
        return _port;
    }

    public String getClusterName() {
        return _clusterName;
    }

    public String getIndexName() {
        return _indexName;
    }
    

    @Override
    public String toString() {
        return "ElasticSearchDatastore[name=" + getName() + "]";
    }
}
