package org.datacleaner.server;


import org.datacleaner.configuration.ServerInformation;

/**
 *
 */
public abstract class AbstractServerInformation implements ServerInformation {
    private final String _name;
    private final String _description;

    public AbstractServerInformation(String name, String description) {
        _name = name;
        _description = description;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public String getDescription() {
        return _description;
    }
}
