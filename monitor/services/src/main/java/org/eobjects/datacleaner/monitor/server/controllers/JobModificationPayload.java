package org.eobjects.datacleaner.monitor.server.controllers;

import java.io.Serializable;

public class JobModificationPayload implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private Boolean overwrite;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setOverwrite(Boolean overwrite) {
        this.overwrite = overwrite;
    }

    public Boolean getOverwrite() {
        return overwrite;
    }
}
