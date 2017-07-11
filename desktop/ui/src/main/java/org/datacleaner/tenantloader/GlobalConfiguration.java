package org.datacleaner.tenantloader;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "datacleanerConfiguration")
public class GlobalConfiguration {

    private WorkspaceConfiguration workspaceConfiguration = new WorkspaceConfiguration();

    public WorkspaceConfiguration getWorkspaceConfiguration() {
        return workspaceConfiguration;
    }

    @XmlElement(name = "workspaceConfiguration")
    public void setWorkspaceConfiguration(WorkspaceConfiguration workspaceConfiguration) {
        this.workspaceConfiguration = workspaceConfiguration;
    }
}
