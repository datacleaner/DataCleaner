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
