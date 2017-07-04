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

import java.io.File;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.datacleaner.repository.file.FileRepositoryFolder;
import org.datacleaner.user.DataCleanerHome;
import org.datacleaner.util.StringUtils;

import static org.datacleaner.user.DataCleanerHome.HOME_PROPERTY_NAME;

/**
 * Manager for work with workspace configuration file
 */
public class WorkspaceManager {
    private static final String GLOBAL_CONFIG_FILE_NAME = "global_config.xml";

    private WorkspaceConfiguration _workspaceConfiguration;
    private JAXBContext _jaxbContext;

    public WorkspaceManager() throws JAXBException {
        _jaxbContext = JAXBContext.newInstance(WorkspaceConfiguration.class);
        loadConfiguration();
    }

    private void loadConfiguration() throws JAXBException {
        final File confFile = getConfigurationFile();
        if (!confFile.isFile()) {
            String defaultHomePath = DataCleanerHome.getAsDataCleanerHomeFolder().toFile().getAbsolutePath();
            _workspaceConfiguration = new WorkspaceConfiguration();
            _workspaceConfiguration.getWorkspaces().add(defaultHomePath);
            // in previous version, there were sub-folders with version numbers.
            // Provide them as option to choose from.
            File homeBase = DataCleanerHome.getDefaultHomeBase();
            if(homeBase.isDirectory()) {
                for(File subFolder: homeBase.listFiles()) {
                    if(subFolder.getName().matches("[0-9\\.]*")) {
                        _workspaceConfiguration.getWorkspaces().add(subFolder.getAbsolutePath());
                    }
                }
            }
        } else {
            final Unmarshaller jaxbUnmarshaller = _jaxbContext.createUnmarshaller();
            _workspaceConfiguration = (WorkspaceConfiguration) jaxbUnmarshaller.unmarshal(confFile);
        }
    }

    /**
     * Returns default workspace to start without dialog
     * 
     * @return
     */
    public String getDefaultWorkspace() {
        return _workspaceConfiguration.getDefaultWorkspace();
    }

    /**
     * Set default workspace to start without dialog
     *
     * @param path
     */
    public void setDefaultWorkspace(String path) {
        _workspaceConfiguration.setDefaultWorkspace(path);
        addWorkspacePath(path);
    }

    /**
     * Add workspace path to list
     *
     * @param path
     */
    public void addWorkspacePath(String path) {
        final List<String> paths = _workspaceConfiguration.getWorkspaces();
        if (!paths.contains(path)) {
            paths.add(path);
        }
    }

    /**
     * Return all workspaces
     *
     * @return
     */
    public List<String> getWorkspacePaths() {
        return _workspaceConfiguration.getWorkspaces();
    }

    /**
     * Will be show dialog after next start?
     *
     * @param show
     */
    public void setShowDialog(boolean show) {
        _workspaceConfiguration.setShowDialog(show);
    }

    /**
     * Can be show dialog?
     *
     * @return
     */
    public boolean showDialog() {
        return _workspaceConfiguration.isShowDialog();
    }

    /**
     * Removes workspace from list
     * 
     * @param path
     */
    public void removeWorkspace(String path) {
        if (StringUtils.isNullOrEmpty(path)) {
            return;
        }
        if (path.equals(_workspaceConfiguration.getDefaultWorkspace())) {
            _workspaceConfiguration.setDefaultWorkspace(null);
        }
        _workspaceConfiguration.getWorkspaces().remove(path);
    }

    /**
     * Sets env variable to start of Datacleaner
     * 
     * @param workspace
     */
    public void setWorkspaceToRun(String workspace) {
        if (!StringUtils.isNullOrEmpty(workspace)) {
            System.setProperty(HOME_PROPERTY_NAME, workspace);
        }
    }

    /**
     * Save current configuration to file in base workspace
     *
     * @throws JAXBException
     */
    public void save() throws JAXBException {
        final Marshaller jaxbMarshaller = _jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        final File configurationFile = getConfigurationFile();
        if (configurationFile.exists()) {
            configurationFile.delete();
        }
        new FileRepositoryFolder(null, configurationFile.getParentFile()).createFile(GLOBAL_CONFIG_FILE_NAME, out -> {
            jaxbMarshaller.marshal(_workspaceConfiguration, out);
        });
    }

    private File getConfigurationFile() {
        return new File(DataCleanerHome.getDefaultHomeBase() + File.separator + GLOBAL_CONFIG_FILE_NAME);
    }
}
