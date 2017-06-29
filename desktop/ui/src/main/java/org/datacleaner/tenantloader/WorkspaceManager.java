package org.datacleaner.tenantloader;

import java.io.File;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.datacleaner.configuration.DataCleanerHomeFolder;
import org.datacleaner.repository.RepositoryFile;
import org.datacleaner.repository.file.FileRepositoryFile;
import org.datacleaner.util.StringUtils;

import static org.datacleaner.user.DataCleanerHome.HOME_PROPERTY_NAME;

/**
 * Manager for work with workspace configuration file
 */
public class WorkspaceManager {
    private static final String TENANT_CONFIGURATION_FILE_NAME = ".datacleaner_workspace";

    private DataCleanerHomeFolder _defaultHomeFolder;
    private WorkspaceConfiguration _workspaceConfiguration;
    private JAXBContext _jaxbContext;

    public WorkspaceManager(final DataCleanerHomeFolder defaultHomeFolder) throws JAXBException {
        _defaultHomeFolder = defaultHomeFolder;
        _jaxbContext = JAXBContext.newInstance(WorkspaceConfiguration.class);
        loadConfiguration(defaultHomeFolder.toFile().getAbsolutePath());
    }

    private void loadConfiguration(String basicHomePath) throws JAXBException {
        final File confFile = getConfigurationFile();
        if (confFile == null) {
            _workspaceConfiguration = new WorkspaceConfiguration();
            _workspaceConfiguration.getWorkspaces().add(basicHomePath);
        } else {
            Unmarshaller jaxbUnmarshaller = _jaxbContext.createUnmarshaller();
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
        Marshaller jaxbMarshaller = _jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        final File configurationFile = getConfigurationFile();
        if (configurationFile != null) {
            configurationFile.delete();
        }
        _defaultHomeFolder.toRepositoryFolder().createFile(TENANT_CONFIGURATION_FILE_NAME, out -> {
            jaxbMarshaller.marshal(_workspaceConfiguration, out);
        });
    }

    private File getConfigurationFile() {
        final RepositoryFile conf = _defaultHomeFolder.toRepositoryFolder().getFile(TENANT_CONFIGURATION_FILE_NAME);
        if (conf == null || !(conf instanceof FileRepositoryFile)) {
            return null;
        } else {
            return ((FileRepositoryFile) conf).getFile();
        }
    }
}
