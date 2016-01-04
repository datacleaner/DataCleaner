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

/**
 * @since 15. 10. 2015
 */
public class RemoteServerDataImpl implements RemoteServerData {

    private static final RemoteServerData NO_SERVER = new RemoteServerDataImpl(null, null, -1, null, null);

    public static RemoteServerData noServer() {
        return NO_SERVER;
    }

    private final String serverName;
    private final int serverPriority;
    private final String url;
    private final String username;
    private final String password;

    public RemoteServerDataImpl(String url, String serverName, int serverPriority, String username, String password) {
        this.url = url == null ? null : url.replaceAll("/+$", "");
        this.serverName = serverName == null ? null : serverName;
        this.serverPriority = serverPriority;
        this.username = username;
        this.password = password;
    }

    @Override
    public int getServerPriority() {
        return serverPriority;
    }

    @Override
    public String getServerName() {
        return serverName;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }
}
