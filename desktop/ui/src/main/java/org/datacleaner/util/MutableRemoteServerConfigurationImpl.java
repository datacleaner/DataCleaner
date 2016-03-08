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

import org.datacleaner.configuration.DomConfigurationWriter;
import org.datacleaner.configuration.RemoteServerConfigurationImpl;
import org.datacleaner.configuration.RemoteServerData;
import org.datacleaner.configuration.RemoteServerDataImpl;

public class MutableRemoteServerConfigurationImpl extends RemoteServerConfigurationImpl {

    private DomConfigurationWriter configWriter;

    public MutableRemoteServerConfigurationImpl(List<RemoteServerData> serverData, DomConfigurationWriter configurationWriter) {
        super(serverData);
        configWriter = configurationWriter;
    }

    public void addServer(String serverName, String url, String username, String password) {
        RemoteServerData serverData = new RemoteServerDataImpl(url, serverName, username, password);
        remoteServerDataList.add(serverData);
        configWriter.addRemoteServer(serverName, url, username, password);
    }

    public void updateServerCredentials(String serverName, String userName, String password) {
        RemoteServerData serverConfig = getServerConfig(serverName);
        if (serverConfig instanceof RemoteServerDataImpl) {
            RemoteServerDataImpl remoteServerDataImpl = (RemoteServerDataImpl) serverConfig;
            remoteServerDataImpl.setUsername(userName);
            remoteServerDataImpl.setPassword(password);
        } else {
            throw new UnsupportedOperationException(
                    "Update credentials failed. RemoteServerData is not instance of RemoteServerDataImpl");
        }
        configWriter.updateRemoteServerCredentials(serverName, userName, password);
    }
}
