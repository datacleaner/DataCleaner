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

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.util.List;

import org.apache.metamodel.DataContext;
import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.elasticsearch.nativeclient.ElasticSearchDataContext;
import org.apache.metamodel.elasticsearch.rest.ElasticSearchRestDataContext;
import org.apache.metamodel.util.SimpleTableDef;
import org.datacleaner.util.StringUtils;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.Node;

import com.google.common.base.Strings;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;

/**
 * Datastore providing access to an ElasticSearch index.
 */
public class ElasticSearchDatastore extends UsageAwareDatastore<UpdateableDataContext> implements UpdateableDatastore {

    public enum ClientType {
        NODE("Join cluster as a node"), TRANSPORT("Connect via Transport protocol"), REST("Connect via REST protocol");

        private String _humanReadableName;

        ClientType(final String humanReadableName) {
            _humanReadableName = humanReadableName;
        }

        @Override
        public String toString() {
            return _humanReadableName;
        }
    }

    public static final int DEFAULT_PORT = 9200;
    public static final int TRANSPORT_PORT = 9300;
    private static final long serialVersionUID = 1L;
    private final SimpleTableDef[] _tableDefs;
    private final ClientType _clientType;
    private final String _indexName;
    private final String _hostname;
    private final Integer _port;
    private final String _clusterName;
    private final String _username;
    private final String _password;
    private final boolean _ssl;
    private final String _keystorePath;
    private final String _keystorePassword;

    public ElasticSearchDatastore(final String name, final ClientType clientType, final String hostname,
            final Integer port, final String clusterName, final String indexName) {
        this(name, clientType, hostname, port, clusterName, indexName, null, null, null, false, null, null);
    }

    public ElasticSearchDatastore(final String name, final ClientType clientType, final String hostname,
            final Integer port, final String clusterName, final String indexName, final String username,
            final String password, final boolean ssl, final String keystorePath, final String keystorePassword) {
        this(name, clientType, hostname, port, clusterName, indexName, null, username, password, ssl, keystorePath,
                keystorePassword);
    }

    public ElasticSearchDatastore(final String name, final ClientType clientType, final String hostname,
            final Integer port, final String clusterName, final String indexName, final SimpleTableDef[] tableDefs,
            final String username, final String password, final boolean ssl, final String keystorePath,
            final String keystorePassword) {
        super(name);
        _hostname = hostname;
        _port = port;
        _clusterName = clusterName;
        _indexName = indexName;
        _tableDefs = tableDefs;
        _username = username;
        _password = password;
        _ssl = ssl;
        _clientType = clientType;
        _keystorePath = keystorePath;
        _keystorePassword = keystorePassword;
    }

    @Override
    public PerformanceCharacteristics getPerformanceCharacteristics() {
        return new PerformanceCharacteristicsImpl(true, false);
    }

    @Override
    protected UsageAwareDatastoreConnection<UpdateableDataContext> createDatastoreConnection() {
        final DataContext dataContext;
        if (_tableDefs == null || _tableDefs.length == 0) {
            if (_clientType.equals(ClientType.NODE) || _clientType.equals(ClientType.TRANSPORT)) {
                final Client client = getClientForNodeAndTransportProtocol();
                dataContext = new ElasticSearchDataContext(client, _indexName);
                return createConnection(dataContext, client);
            } else {
                dataContext = new ElasticSearchRestDataContext(getClientForRestProtocol(), _indexName);
                return createConnection(dataContext, null);
            }
        } else {
            if (_clientType.equals(ClientType.NODE) || _clientType.equals(ClientType.TRANSPORT)) {
                final Client client = getClientForNodeAndTransportProtocol();
                dataContext = new ElasticSearchDataContext(client, _indexName, _tableDefs);
                return createConnection(dataContext, client);
            } else {
                dataContext = new ElasticSearchRestDataContext(getClientForRestProtocol(), _indexName, _tableDefs);
                return createConnection(dataContext, null);
            }
        }

    }

    private UsageAwareDatastoreConnection<UpdateableDataContext> createConnection(final DataContext dataContext,
            final Client simpleclient) {
        switch (_clientType) {
        case NODE:
        case TRANSPORT:
            return new UpdateableDatastoreConnectionImpl<>((ElasticSearchDataContext) dataContext, this, simpleclient);
        case REST:
            return new UpdateableDatastoreConnectionImpl<>((ElasticSearchRestDataContext) dataContext, this);
        default:
            //do nothing
        }
        return null;
    }

    private Client getClientForNodeAndTransportProtocol() {
        switch (_clientType) {
        case NODE:
            return getClientForJoiningClusterAsNode();
        case TRANSPORT:
            return getClientForTransportProtocol();
        default:
            //do nothing
        }
        return null;
    }

    private JestClient getClientForRestProtocol() {
        final JestClientFactory factory = new JestClientFactory();
        HttpClientConfig.Builder builder =
                new HttpClientConfig.Builder("http://" + _hostname + ":" + _port).multiThreaded(true);
        if (!Strings.isNullOrEmpty(_username)) {
            builder = builder.defaultCredentials(_username, _password);
        }
        factory.setHttpClientConfig(builder.build());

        return factory.getObject();
    }

    private Client getClientForJoiningClusterAsNode() {
        final Client client;
        final Builder settingsBuilder = ImmutableSettings.builder();
        settingsBuilder.put("name", "DataCleaner");
        settingsBuilder.put("shield.enabled", false);
        final Settings settings = settingsBuilder.build();

        // .client(true) means no shards are stored on this node
        final Node node = nodeBuilder().clusterName(_clusterName).client(true).settings(settings).node();
        client = node.client();
        return client;
    }

    private Client getClientForTransportProtocol() {
        final Client client;
        final Builder settingsBuilder = ImmutableSettings.builder();
        settingsBuilder.put("name", "DataCleaner");
        settingsBuilder.put("cluster.name", _clusterName);
        if (!StringUtils.isNullOrEmpty(_username) && !StringUtils.isNullOrEmpty(_password)) {
            settingsBuilder.put("shield.user", _username + ":" + _password);
            if (_ssl) {
                if (!Strings.isNullOrEmpty(_keystorePath)) {
                    settingsBuilder.put("shield.ssl.keystore.path", _keystorePath);
                    settingsBuilder.put("shield.ssl.keystore.password", _keystorePassword);
                }
                settingsBuilder.put("shield.transport.ssl", "true");
            }
        }
        final Settings settings = settingsBuilder.build();

        client = new TransportClient(settings);
        ((TransportClient) client).addTransportAddress(new InetSocketTransportAddress(_hostname, _port));
        return client;
    }

    @Override
    public UpdateableDatastoreConnection openConnection() {
        final DatastoreConnection connection = super.openConnection();
        return (UpdateableDatastoreConnection) connection;
    }

    public SimpleTableDef[] getTableDefs() {
        return _tableDefs;
    }

    public ClientType getClientType() {
        return _clientType;
    }

    public String getHostname() {
        return _hostname;
    }

    public Integer getPort() {
        return _port;
    }

    public String getClusterName() {
        return _clusterName;
    }

    public String getIndexName() {
        return _indexName;
    }

    public String getUsername() {
        return _username;
    }

    public String getPassword() {
        return _password;
    }

    public boolean getSsl() {
        return _ssl;
    }

    public String getKeystorePath() {
        return _keystorePath;
    }

    public String getKeystorePassword() {
        return _keystorePassword;
    }

    @Override
    public String toString() {
        return "ElasticSearchDatastore[name=" + getName() + "]";
    }

    @Override
    protected void decorateIdentity(final List<Object> identifiers) {
        super.decorateIdentity(identifiers);
        identifiers.add(_clusterName);
        identifiers.add(_hostname);
        identifiers.add(_indexName);
        identifiers.add(_tableDefs);
    }
}
