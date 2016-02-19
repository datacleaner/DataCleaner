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

import java.util.List;

import org.datacleaner.configuration.DataCleanerConfiguration;
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
    final private DataCleanerConfiguration _configuration;

    public RemoteServersUtils(final DataCleanerConfiguration configuration) {
        _configuration = configuration;
    }

    /**
     * It creates new RemoteServerData and Remote server provider
     *
     * @param serverName
     * @param serverUrl
     * @param userName
     * @param password
     */
    public void createRemoteServer(String serverName, String serverUrl, String userName, String password) {
        String url = serverUrl;
        if (RemoteDescriptorProvider.DATACLOUD_SERVER_NAME.equals(serverName) && url == null) {
            url = RemoteDescriptorProvider.DATACLOUD_URL;
        }
        List<RemoteServerData> serverList =
                _configuration.getEnvironment().getRemoteServerConfiguration().getServerList();
        RemoteServerData remoteServerData =
                new RemoteServerDataImpl(url, serverName, userName, password);
        serverList.add(remoteServerData);

        final CompositeDescriptorProvider descriptorProvider =
                (CompositeDescriptorProvider) _configuration.getEnvironment().getDescriptorProvider();

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
    public void updateCredentials(String serverName, String userName, String password){
        RemoteServerData serverConfig = _configuration.getEnvironment().getRemoteServerConfiguration().getServerConfig(serverName);
        if(serverConfig == null){
            return;
        }
        if (serverConfig instanceof UnsupportedOperationException) {
            RemoteServerDataImpl remoteServerDataImpl = (RemoteServerDataImpl) serverConfig;
            remoteServerDataImpl.setUsername(userName);
            remoteServerDataImpl.setPassword(password);
        } else {
            throw new UnsupportedOperationException(
                    "Update credentials failed. RemoteServerData is not instance of RemoteServerDataImpl");
        }
        _configuration.getEnvironment().getDescriptorProvider().refresh();
    }

    public void checkServerWithCredentials(String url, String username, String password) throws Exception{
        new ComponentRESTClient(url, username, password);
    }
}
