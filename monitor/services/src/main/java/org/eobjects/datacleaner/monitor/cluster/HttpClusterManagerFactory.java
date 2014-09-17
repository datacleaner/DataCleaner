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
package org.eobjects.datacleaner.monitor.cluster;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.auth.params.AuthPNames;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.eobjects.analyzer.cluster.ClusterManager;
import org.eobjects.analyzer.cluster.http.HttpClusterManager;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;

/**
 * Factory for HTTP based {@link ClusterManager}s.
 */
public class HttpClusterManagerFactory implements ClusterManagerFactory {

    private List<String> slaveServerUrls;
    private String username;
    private String password;

    @Override
    public ClusterManager getClusterManager(TenantIdentifier tenant) {
        final DefaultHttpClient httpClient = new DefaultHttpClient(new PoolingClientConnectionManager());
        if (username != null && password != null) {
            final CredentialsProvider credentialsProvider = httpClient.getCredentialsProvider();
            final UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
            final List<String> authpref = new ArrayList<String>();
            authpref.add(AuthPolicy.BASIC);
            authpref.add(AuthPolicy.DIGEST);
            httpClient.getParams().setParameter(AuthPNames.PROXY_AUTH_PREF, authpref);
            credentialsProvider.setCredentials(new AuthScope(null, -1), credentials);
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

        return new HttpClusterManager(httpClient, finalEndpoints);
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
