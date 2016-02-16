package org.datacleaner.configuration;


import java.io.Serializable;

public interface ServerCatalog extends Serializable{
    /**
     * Determines if a server by a specific name is contained in the
     * {@link ServerCatalog}.
     *
     * @param name
     * @return
     */
    public default boolean containsServer(String name) {
        return getServer(name) != null;
    };

    /**
     * Gets all the names of the servers in this datastore catalog.
     *
     * @return
     */
    public String[] getServerNames();

    /**
     * Gets a server by it's name. If no such server is found, null will
     * be returned.
     *
     * @param name
     * @return
     */
    public ServerInformation getServer(String name);
}
