package org.datacleaner.tenantloader;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Datacleaner workspaces - file.
 */
@XmlRootElement
public class WorkspaceConfiguration {
    List<String> workspaces = new ArrayList<>();
    String defaultWorkspace;
    boolean showDialog = true;

    /**
     * Returns list of home paths
     * @return
     */
    @XmlElementWrapper(name = "workspaces")
    @XmlElement(name = "workspace")
    public List<String> getWorkspaces() {
        return workspaces;
    }

    /**
     * Info about start with default.
     *
     * @return
     */
    public String getDefaultWorkspace() {
        return defaultWorkspace;
    }

    /**
     * Set default start.
     *
     * @param defaultWorkspace
     */
    @XmlElement
    public void setDefaultWorkspace(final String defaultWorkspace) {
        this.defaultWorkspace = defaultWorkspace;
    }

    public boolean isShowDialog() {
        return showDialog;
    }

    @XmlElement
    public void setShowDialog(final boolean showDialog) {
        this.showDialog = showDialog;
    }
}
