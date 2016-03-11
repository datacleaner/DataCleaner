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
package org.datacleaner.util;

import org.datacleaner.configuration.DataCleanerEnvironment;
import org.datacleaner.configuration.RemoteServerConfiguration;
import org.datacleaner.configuration.RemoteServerData;
import org.datacleaner.configuration.RemoteServerDataImpl;
import org.datacleaner.descriptors.CompositeDescriptorProvider;
import org.datacleaner.descriptors.RemoteDescriptorProvider;
import org.datacleaner.descriptors.RemoteDescriptorProviderImpl;
import org.datacleaner.restclient.ComponentRESTClient;

/**
 * Utilities for better work with remote servers.
 */
public class RemoteServersUtils {

    /**
     * It creates new RemoteServerData and Remote server provider
     *
     * @param serverName
     * @param serverUrl
     * @param userName
     * @param password
     */
    public static void addRemoteServer(
            DataCleanerEnvironment env, String serverName, String serverUrl, String userName, String password) {
        if (RemoteDescriptorProvider.DATACLOUD_SERVER_NAME.equals(serverName)) {
            serverUrl = RemoteDescriptorProvider.DATACLOUD_URL;
            getMutableServerConfig(env).addServer(serverName, null, userName, password);
        } else {
            getMutableServerConfig(env).addServer(serverName, serverUrl, userName, password);
        }

        RemoteServerData remoteServerData = new RemoteServerDataImpl(serverUrl, serverName, userName, password);

        if(!(env.getDescriptorProvider() instanceof CompositeDescriptorProvider)){
            throw new IllegalStateException("DescriptorProvider is not instance of CompositeDescriptorProvider class.");
        }
        final CompositeDescriptorProvider descriptorProvider = (CompositeDescriptorProvider) env.getDescriptorProvider();
        descriptorProvider.addDelegate(new RemoteDescriptorProviderImpl(remoteServerData));
        descriptorProvider.refresh();
    }

    /**
     * Update remote server credentials
     *
     * @param serverName
     * @param userName
     * @param password
     */
    public static void updateRemoteServerCredentials(DataCleanerEnvironment env, String serverName, String userName, String password){
        getMutableServerConfig(env).updateServerCredentials(serverName, userName, password);
        env.getDescriptorProvider().refresh();
    }

    public static void checkServerWithCredentials(String url, String username, String password) throws Exception{
        new ComponentRESTClient(url, username, password);
    }

    private static MutableRemoteServerConfigurationImpl getMutableServerConfig(DataCleanerEnvironment env) {
        RemoteServerConfiguration remServerConfig = env.getRemoteServerConfiguration();
        if(! (remServerConfig instanceof MutableRemoteServerConfigurationImpl)) {
            throw new UnsupportedOperationException("Remote server config is not instance of class " + MutableRemoteServerConfigurationImpl.class);
        }
        return (MutableRemoteServerConfigurationImpl)remServerConfig;
    }
}
