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
package org.datacleaner.monitor.cluster;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.datacleaner.cluster.ClusterManager;
import org.datacleaner.cluster.http.HttpClusterManager;
import org.datacleaner.monitor.shared.model.TenantIdentifier;

/**
 * Factory for HTTP based {@link ClusterManager}s.
 */
public class HttpClusterManagerFactory implements ClusterManagerFactory {

    private List<String> slaveServerUrls;
    private String username;
    private String password;

    @Override
    public ClusterManager getClusterManager(TenantIdentifier tenant) {
        final HttpClient httpClient = HttpClients.custom().useSystemProperties()
                .setConnectionManager(new PoolingHttpClientConnectionManager()).build();

        final HttpClientContext context = HttpClientContext.create();

        if (username != null && password != null) {
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

            final UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
            credentialsProvider.setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT), credentials);
            
            context.setCredentialsProvider(credentialsProvider);
        }

        // use the server list
        final List<String> finalEndpoints = new ArrayList<String>();
        for (String endpoint : slaveServerUrls) {
            if (!endpoint.endsWith("/")) {
                endpoint = endpoint + "/";
            }
            endpoint = endpoint + "repository/" + tenant.getId() + "/cluster_slave_endpoint";
            finalEndpoints.add(endpoint);
        }

        return new HttpClusterManager(httpClient, context, finalEndpoints);
    }

    public void setSlaveServerUrls(List<String> slaveServerUrls) {
        this.slaveServerUrls = slaveServerUrls;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
