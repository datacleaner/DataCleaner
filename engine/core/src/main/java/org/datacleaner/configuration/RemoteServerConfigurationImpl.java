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
package org.datacleaner.configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Class RemoteServerConfigurationImpl
 *
 */
public class RemoteServerConfigurationImpl implements RemoteServerConfiguration {
    private boolean showAll = false;
    private List<RemoteServerData> remoteServerDataList = new ArrayList<>();

    @Override
    public void setShowAllServers(boolean showAll) {
        this.showAll = showAll;
    }

    @Override
    public boolean showAllServers() {
        return showAll;
    }

    @Override
    public List<RemoteServerData> getServerList() {
        return remoteServerDataList;
    }

    @Override
    public void addServer(RemoteServerData remoteServerData) {
        remoteServerDataList.add(remoteServerData);
    }

    @Override
    public boolean isEmpty() {
        return remoteServerDataList == null || remoteServerDataList.isEmpty();
    }
}
