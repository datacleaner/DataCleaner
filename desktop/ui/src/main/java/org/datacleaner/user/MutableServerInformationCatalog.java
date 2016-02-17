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
package org.datacleaner.user;

import org.datacleaner.configuration.DomConfigurationWriter;
import org.datacleaner.configuration.ServerInformation;
import org.datacleaner.configuration.ServerInformationCatalog;
import org.datacleaner.util.StringUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * Mutable/modifyable implementation of the {@link ServerInformationCatalog} interface. Used to
 * allow the user to change the catalog of ServerInformation at runtime. This server information
 * catalog wraps an immutable instance, which typically represents what is
 * configured in datacleaner's xml file.
 */

public class MutableServerInformationCatalog implements ServerInformationCatalog {
    private final DomConfigurationWriter _configurationWriter;
    private final List<ServerInformationChangeListener> _listeners = new LinkedList<>();

    private final UserPreferences _userPreferences;

    public MutableServerInformationCatalog(ServerInformationCatalog immutableDelegate, DomConfigurationWriter configurationWriter, UserPreferences userPreferences) {
        _configurationWriter = configurationWriter;
        _userPreferences = userPreferences;
        String[] serverNames = immutableDelegate.getServerNames();

        for (String name : serverNames) {
            if (containsServer(name)) {
                // remove any copies of the server - the immutable (XML)
                // version should always win
                removeServer(getServer(name), false);
            }
            addServerInformation(immutableDelegate.getServer(name), false);
        }

    }

    public void removeServer(ServerInformation ds) {
        removeServer(ds, true);
    }

    private synchronized void removeServer(ServerInformation serverInformation, boolean externalize) {
        final List<ServerInformation> userServers = _userPreferences.getUserServers();
        if (userServers.remove(serverInformation)) {
            for (ServerInformationChangeListener listener : _listeners) {
                listener.onRemove(serverInformation);
            }
        }
        if (externalize) {
            _configurationWriter.removeServerInformation(serverInformation.getName());
            _userPreferences.save();
        }
    }

    public void addServerInformation(ServerInformation serverInformation) {
        addServerInformation(serverInformation, true);
    }

    private synchronized void addServerInformation(ServerInformation sI, boolean externalize) {
        final String name = sI.getName();
        if (StringUtils.isNullOrEmpty(name)) {
            throw new IllegalArgumentException("Server has no name!");
        }
        final List<ServerInformation> serverInformations = _userPreferences.getUserServers();
        for (ServerInformation serverInformation : serverInformations) {
            if (name.equals(serverInformation.getName())) {
                throw new IllegalArgumentException("A server with the name '" + name + "' already exists!");
            }
        }

        serverInformations.add(sI);
        for (ServerInformationChangeListener listener : _listeners) {
            listener.onAdd(sI);
        }

        if (externalize) {
            if (_configurationWriter.isExternalizable(sI)) {
                _configurationWriter.externalize(sI);
            }
            _userPreferences.save();
        }
    }

    @Override
    public String[] getServerNames() {
        final List<ServerInformation> serverInformations = _userPreferences.getUserServers();
        String[] names = new String[serverInformations.size()];
        for (int i = 0; i < names.length; i++) {
            names[i] = serverInformations.get(i).getName();
        }
        return names;
    }

    @Override
    public ServerInformation getServer(final String name) {
        if (name == null) {
            return null;
        }
        final List<ServerInformation> serverInformations = _userPreferences.getUserServers();
        for (ServerInformation serverInformation : serverInformations) {
            if (name.equals(serverInformation.getName())) {
                return serverInformation;
            }
        }
        return null;
    }

    public void addListener(ServerInformationChangeListener listener) {
        _listeners.add(listener);
    }
    public void removeListener(ServerInformationChangeListener listener) {
        _listeners.remove(listener);
    }
}
