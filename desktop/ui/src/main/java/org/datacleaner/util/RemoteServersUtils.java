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
import org.datacleaner.descriptors.RemoteDescriptorProviderImpl;

/**
 * Utilities for better work with remote servers.
 */
public class RemoteServersUtils {
    final private DataCleanerConfiguration _configuration;

    public RemoteServersUtils(final DataCleanerConfiguration configuration) {
        _configuration = configuration;
    }

    public RemoteServerData getServerConfig(String serverName) {
        if (serverName == null) {
            return null;
        }
        List<RemoteServerData> serverList =
                _configuration.getEnvironment().getRemoteServerConfiguration().getServerList();
        for (RemoteServerData remoteServerData : serverList) {
            String configServerName = remoteServerData.getServerName();
            if (configServerName == null) {
                continue;
            }
            if (configServerName.toLowerCase().equals(serverName.toLowerCase())) {
                return remoteServerData;
            }
        }
        return null;
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
        List<RemoteServerData> serverList =
                _configuration.getEnvironment().getRemoteServerConfiguration().getServerList();
        int priority = serverList.size() == 0 ? 1 : serverList.get(serverList.size() - 1).getServerPriority();

        RemoteServerData remoteServerData =
                new RemoteServerDataImpl(serverUrl, serverName, priority, userName, password);
        serverList.add(remoteServerData);

        final CompositeDescriptorProvider descriptorProvider =
                (CompositeDescriptorProvider) _configuration.getEnvironment().getDescriptorProvider();

        descriptorProvider.addDelegate(new RemoteDescriptorProviderImpl(remoteServerData));
    }

    /**
     * Update remote server credentials
     *
     * @param serverName
     * @param userName
     * @param password
     */
    public void updateRemoteServerCredentials(String serverName, String userName, String password){
        RemoteServerData serverConfig = getServerConfig(serverName);
        if(serverConfig == null){
            return;
        }
        serverConfig.setUsername(userName);
        serverConfig.setPassword(password);
    }
}
