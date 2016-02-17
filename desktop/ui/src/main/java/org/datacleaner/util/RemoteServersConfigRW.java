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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Utils for work with information about remote servers in configuration file
 */
public class RemoteServersConfigRW {
    private static final String REMOTE_COMPONENTS_XPATH = "configuration/descriptor-providers/remote-components";
    private static final String REMOTE_SERVER_XPATH = "configuration/descriptor-providers/remote-components/server";

    private final DataCleanerConfiguration _configuration;
    private final DataCleanerConfigurationUpdater _configurationUpdate;

    public RemoteServersConfigRW(final DataCleanerConfiguration configuration) {
        _configuration = configuration;
        _configurationUpdate = new DataCleanerConfigurationUpdater(getDataCleanerConfigurationFileResource());
    }

    public void writeCredentialsToConfig(String serverName, String serverUrl, String userName, String password) {
        final String serverXPath = _configurationUpdate.createChild(REMOTE_COMPONENTS_XPATH, "server");
        if (serverName != null) {
            final String nameXpath = _configurationUpdate.createChild(serverXPath, "name");
            _configurationUpdate.update(nameXpath, serverName);
        }
        if (serverUrl != null) {
            final String urlXpath = _configurationUpdate.createChild(serverXPath, "url");
            _configurationUpdate.update(urlXpath, serverUrl);
        }
        final String usernameXpath = _configurationUpdate.createChild(serverXPath, "username");
        _configurationUpdate.update(usernameXpath, userName);

        final String passwordXpath = _configurationUpdate.createChild(serverXPath, "password");
        _configurationUpdate.update(passwordXpath, password);
        _configurationUpdate.write();
    }

    public void updateCredentials(String serverName, String userName, String password) {
        String xpathForRemoteServer = getXpathForRemoteServer(serverName);
        _configurationUpdate.update(xpathForRemoteServer + "/username", userName);
        _configurationUpdate.update(xpathForRemoteServer + "/password", password);
        _configurationUpdate.write();

    }

    private String getXpathForRemoteServer(String serverName) {
        NodeList servers = _configurationUpdate.findElementToUpdate(REMOTE_SERVER_XPATH);
        for (int i = 0; i < servers.getLength(); i++) {
            Node server = servers.item(i);
            NodeList childNodes = server.getChildNodes();
            for (int j = 0; j < childNodes.getLength(); j++) {
                if (childNodes.item(j).getNodeName().equals("name") && serverName
                        .equals(childNodes.item(j).getTextContent())) {
                    int indexOfServer = i + 1;
                    return REMOTE_COMPONENTS_XPATH + "/server[" + indexOfServer + "]";
                }
            }
        }
        return null;
    }

    Resource getDataCleanerConfigurationFileResource() {
        final RepositoryFile configurationFile = _configuration.getHomeFolder().toRepositoryFolder()
                .getFile(DataCleanerConfigurationImpl.DEFAULT_FILENAME);
        final Resource resource = configurationFile.toResource();
        return resource;
    }

}
