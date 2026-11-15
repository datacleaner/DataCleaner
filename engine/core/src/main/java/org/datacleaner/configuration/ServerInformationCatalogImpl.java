/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Free Software Foundation, Inc.
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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.datacleaner.metamodel.util.CollectionUtils;
import org.datacleaner.metamodel.util.HasNameMapper;

public class ServerInformationCatalogImpl implements ServerInformationCatalog {
    private static final long serialVersionUID = 1L;

    private final Collection<ServerInformation> _servers;

    public ServerInformationCatalogImpl(final Collection<ServerInformation> servers) {
        if (servers == null) {
            throw new IllegalArgumentException("servers cannot be null");
        }
        _servers = servers;
    }

    public ServerInformationCatalogImpl(final ServerInformation... servers) {
        final List<ServerInformation> serversList = new ArrayList<>();
        Collections.addAll(serversList, servers);
        _servers = serversList;
    }

    @Override
    public String[] getServerNames() {
        final List<String> names = CollectionUtils.map(_servers, new HasNameMapper());
        Collections.sort(names);
        return names.toArray(new String[names.size()]);
    }

    @Override
    public ServerInformation getServer(final String name) {
        if (name != null) {
            for (final ServerInformation ds : _servers) {
                if (name.equals(ds.getName())) {
                    return ds;
                }
            }
        }
        return null;
    }
}
