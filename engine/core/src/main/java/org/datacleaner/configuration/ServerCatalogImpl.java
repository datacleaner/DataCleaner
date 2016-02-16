package org.datacleaner.configuration;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.metamodel.util.CollectionUtils;
import org.apache.metamodel.util.HasNameMapper;

public class ServerCatalogImpl implements ServerCatalog {
    private static final long serialVersionUID = 1L;

    private final Collection<ServerInformation> _servers;

    public ServerCatalogImpl(Collection<ServerInformation> servers) {
        if (servers == null) {
            throw new IllegalArgumentException("datastores cannot be null");
        }
        _servers = servers;
    }

    public ServerCatalogImpl(ServerInformation... servers) {
        _servers = new ArrayList<>();
        for (ServerInformation datastore : servers) {
            _servers.add(datastore);
        }
    }

    @Override
    public String[] getServerNames() {
        List<String> names = CollectionUtils.map(_servers, new HasNameMapper());
        Collections.sort(names);
        return names.toArray(new String[names.size()]);
    }

    @Override
    public ServerInformation getServer(String name) {
        if (name != null) {
            for (ServerInformation ds : _servers) {
                if (name.equals(ds.getName())) {
                    return ds;
                }
            }
        }
        return null;
    }
}
