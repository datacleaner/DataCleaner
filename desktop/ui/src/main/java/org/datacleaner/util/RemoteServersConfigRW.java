package org.datacleaner.util;

import java.util.List;

import org.apache.metamodel.util.Resource;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.configuration.DataCleanerConfigurationUpdater;
import org.datacleaner.configuration.RemoteServerData;
import org.datacleaner.repository.RepositoryFile;

/**
 * Utils for work with information about remote servers in configuration file
 */
public class RemoteServersConfigRW {
    private static final String SERVER_XML_PATH= "descriptor-providers:remote-components:server";

    final private DataCleanerConfiguration _configuration;

    public RemoteServersConfigRW(final DataCleanerConfiguration configuration) {
        _configuration = configuration;
    }

    public boolean isServerInConfig(String serverName){
        if(serverName == null){
            return false;
        }
        List<RemoteServerData> serverList =
                _configuration.getEnvironment().getRemoteServerConfiguration().getServerList();
        for (RemoteServerData remoteServerData : serverList) {
            String configServerName = remoteServerData.getServerName();
            if(configServerName == null){
                continue;
            }
            if(configServerName.toLowerCase().equals(serverName.toLowerCase())){
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

    private Resource getDataCleanerConfigurationFileResource() {
        final RepositoryFile configurationFile = _configuration.getHomeFolder().toRepositoryFolder()
                .getFile(DataCleanerConfigurationImpl.DEFAULT_FILENAME);
        final Resource resource = configurationFile.toResource();
        return resource;
    }

}
