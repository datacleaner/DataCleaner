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

import org.apache.metamodel.util.Resource;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.configuration.DataCleanerConfigurationUpdater;
import org.datacleaner.configuration.RemoteServerData;
import org.datacleaner.configuration.RemoteServerDataImpl;
import org.datacleaner.descriptors.CompositeDescriptorProvider;
import org.datacleaner.descriptors.DescriptorProvider;
import org.datacleaner.descriptors.RemoteDescriptorProviderImpl;
import org.datacleaner.repository.RepositoryFile;

/**
 * Utils for work with information about remote servers in configuration file
 */
public class RemoteServersConfigRW {
    private static final String SERVER_XML_PATH = "descriptor-providers:remote-components:server";

    final private DataCleanerConfiguration _configuration;

    public RemoteServersConfigRW(final DataCleanerConfiguration configuration) {
        _configuration = configuration;
    }

    public boolean isServerInConfig(String serverName) {
        if (serverName == null) {
            return false;
        }
        List<RemoteServerData> serverList =
                _configuration.getEnvironment().getRemoteServerConfiguration().getServerList();
        for (RemoteServerData remoteServerData : serverList) {
            String configServerName = remoteServerData.getServerName();
            if (configServerName == null) {
                continue;
            }
            if (configServerName.toLowerCase().equals(serverName.toLowerCase())) {
                return true;
            }
        }
        return false;
    }


    public void writeCredentialsToConfig(String serverName, String serverUrl, String userName, String password) {
        final DataCleanerConfigurationUpdater configurationUpdater = new DataCleanerConfigurationUpdater(
                getDataCleanerConfigurationFileResource());
        if (serverName != null) {
            configurationUpdater.createElement(SERVER_XML_PATH + ":name", serverName);
        }
        if (serverUrl != null) {
            configurationUpdater.createElement(SERVER_XML_PATH + ":url", serverUrl);
        }
        configurationUpdater.createElement(SERVER_XML_PATH + ":username", userName);
        configurationUpdater.createElement(SERVER_XML_PATH + ":password", password);
    }

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

    private Resource getDataCleanerConfigurationFileResource() {
        final RepositoryFile configurationFile = _configuration.getHomeFolder().toRepositoryFolder()
                .getFile(DataCleanerConfigurationImpl.DEFAULT_FILENAME);
        final Resource resource = configurationFile.toResource();
        return resource;
    }

}
