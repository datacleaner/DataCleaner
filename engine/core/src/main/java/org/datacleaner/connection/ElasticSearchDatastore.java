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

import java.util.List;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.metamodel.DataContext;
import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.elasticsearch.rest.ElasticSearchRestClient;
import org.apache.metamodel.elasticsearch.rest.ElasticSearchRestDataContext;
import org.apache.metamodel.util.SimpleTableDef;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

import com.google.common.base.Strings;

/**
 * Datastore providing access to an ElasticSearch index.
 */
public class ElasticSearchDatastore extends UsageAwareDatastore<UpdateableDataContext> implements UpdateableDatastore {

    public enum ClientType {
        @Deprecated
        NODE("Join cluster as a node"), @Deprecated
        TRANSPORT("Connect via Transport protocol"),
        // the only currently supported client type
        REST("Connect via REST protocol");

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
    @Deprecated
    public static final int TRANSPORT_PORT = 9300;

    private static final long serialVersionUID = 1L;

    private final SimpleTableDef[] _tableDefs;
    private final ClientType _clientType;
    private final String _indexName;
    private final String _hostname;
    private final Integer _port;
    @Deprecated
    private final String _clusterName;
    private final String _username;
    private final String _password;
    private final boolean _ssl;
    @Deprecated
    private final String _keystorePath;
    @Deprecated
    private final String _keystorePassword;

    @Deprecated
    public ElasticSearchDatastore(final String name, final ClientType clientType, final String hostname,
            final Integer port, final String clusterName, final String indexName) {
        this(name, clientType, hostname, port, clusterName, indexName, null, null, null, false, null, null);
    }

    @Deprecated
    public ElasticSearchDatastore(final String name, final ClientType clientType, final String hostname,
            final Integer port, final String clusterName, final String indexName, final String username,
            final String password, final boolean ssl, final String keystorePath, final String keystorePassword) {
        this(name, clientType, hostname, port, clusterName, indexName, null, username, password, ssl, keystorePath,
                keystorePassword);
    }

    @Deprecated
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

    public ElasticSearchDatastore(final String name, final String hostname, final Integer port, final String indexName,
            final SimpleTableDef[] tableDefs, final String username, final String password, final boolean ssl) {
        super(name);
        _hostname = hostname;
        _port = port;
        _clusterName = null;
        _indexName = indexName;
        _tableDefs = tableDefs;
        _username = username;
        _password = password;
        _ssl = ssl;
        _clientType = ClientType.REST;
        _keystorePath = null;
        _keystorePassword = null;
    }

    @Override
    public PerformanceCharacteristics getPerformanceCharacteristics() {
        return new PerformanceCharacteristicsImpl(true, false);
    }

    @Override
    protected UsageAwareDatastoreConnection<UpdateableDataContext> createDatastoreConnection() {
        switch (_clientType) {
        case NODE:
        case TRANSPORT:
            throw new UnsupportedOperationException(
                    "Support for ElasticSearch 'node' or 'transport' clients has been dropped. "
                            + "Please reconfigure datastore to use HTTP connection.");
        default:
            final DataContext dataContext;
            if (_tableDefs == null || _tableDefs.length == 0) {
                dataContext = new ElasticSearchRestDataContext(getClientForRestProtocol(), _indexName);
            } else {
                dataContext = new ElasticSearchRestDataContext(getClientForRestProtocol(), _indexName, _tableDefs);
            }
            return new UpdateableDatastoreConnectionImpl<>((ElasticSearchRestDataContext) dataContext, this);
        }
    }

    private ElasticSearchRestClient getClientForRestProtocol() {
        final String scheme = _ssl ? "https" : "http";
        final HttpHost hosts = new HttpHost(_hostname, _port, scheme);
        final RestClientBuilder restClientBuilder = RestClient.builder(hosts);

        if (!Strings.isNullOrEmpty(_username)) {
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(_username, _password));
            restClientBuilder.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                @Override
                public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                    return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                }
            });
        }

        final ElasticSearchRestClient elasticSearchRestClient = new ElasticSearchRestClient(restClientBuilder.build());
        return elasticSearchRestClient;
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

    @Deprecated
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

    @Deprecated
    public String getKeystorePath() {
        return _keystorePath;
    }

    @Deprecated
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
        identifiers.add(_hostname);
        identifiers.add(_indexName);
        identifiers.add(_tableDefs);
    }
}
