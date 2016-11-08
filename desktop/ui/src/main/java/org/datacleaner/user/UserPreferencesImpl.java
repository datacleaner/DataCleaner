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
package org.datacleaner.user;

import java.io.File;
import java.io.InvalidClassException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.metamodel.util.CollectionUtils;
import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.Func;
import org.datacleaner.connection.Datastore;
import org.datacleaner.database.UserDatabaseDriver;
import org.datacleaner.extensions.ExtensionPackage;
import org.datacleaner.reference.Dictionary;
import org.datacleaner.reference.StringPattern;
import org.datacleaner.reference.SynonymCatalog;
import org.datacleaner.util.ChangeAwareObjectInputStream;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.SystemProperties;
import org.datacleaner.util.VFSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

/**
 * Main implementation of {@link UserPreferences}.
 */
public class UserPreferencesImpl implements UserPreferences, Serializable {

    private static final long serialVersionUID = 6L;

    public static final String DEFAULT_FILENAME = "userpreferences.dat";

    private static final Logger logger = LoggerFactory.getLogger(UserPreferencesImpl.class);

    private transient FileObject _userPreferencesFile;

    private List<UserDatabaseDriver> databaseDrivers = new ArrayList<>();
    private List<ExtensionPackage> extensionPackages = new ArrayList<>();
    private List<Datastore> userDatastores = new ArrayList<>();
    private final List<Dictionary> userDictionaries = new ArrayList<>();
    private final List<StringPattern> userStringPatterns = new ArrayList<>();
    private List<SynonymCatalog> userSynonymCatalogs = new ArrayList<>();
    private Map<String, String> additionalProperties = new HashMap<>();

    private boolean proxyEnabled = false;
    private boolean proxyAuthenticationEnabled = false;
    private String proxyHostname;
    private int proxyPort = 8080;
    private String proxyUsername;
    private String proxyPassword;

    private List<File> recentJobFiles = new ArrayList<>();
    private File openDatastoreDirectory;
    private File configuredFileDirectory;
    private File analysisJobDirectory;
    private File saveDatastoreDirectory;
    private File saveDownloadedFilesDirectory;
    private File extensionsDirectory;

    private MonitorConnection monitorConnection;

    /**
     * Creates a new {@link UserPreferencesImpl} object which refers to a file,
     * but does NOT load the file contents.
     * 
     * @param userPreferencesFile
     */
    public UserPreferencesImpl(FileObject userPreferencesFile) {
        _userPreferencesFile = userPreferencesFile;
    }

    /**
     * Loads a user preferences file and initializes a
     * {@link UserPreferencesImpl} object using it.
     * 
     * @param userPreferencesFile
     * @param loadDatabaseDrivers
     * @return
     */
    public static UserPreferences load(final FileObject userPreferencesFile, final boolean loadDatabaseDrivers) {
        try {
            if (userPreferencesFile == null || !userPreferencesFile.exists()) {
                logger.info("User preferences file does not exist");
                return new UserPreferencesImpl(userPreferencesFile);
            }
        } catch (FileSystemException e1) {
            logger.debug("Could not determine if file exists: {}", userPreferencesFile);
        }

        ChangeAwareObjectInputStream inputStream = null;
        try {
            inputStream = new ChangeAwareObjectInputStream(userPreferencesFile.getContent().getInputStream());
            inputStream.addRenamedClass("org.datacleaner.user.UserPreferences", UserPreferencesImpl.class);
            UserPreferencesImpl result = (UserPreferencesImpl) inputStream.readObject();

            if (loadDatabaseDrivers) {
                List<UserDatabaseDriver> installedDatabaseDrivers = result.getDatabaseDrivers();
                for (UserDatabaseDriver userDatabaseDriver : installedDatabaseDrivers) {
                    try {
                        userDatabaseDriver.loadDriver();
                    } catch (IllegalStateException e) {
                        logger.error("Could not load database driver", e);
                    }
                }
            }

            result._userPreferencesFile = userPreferencesFile;
            result.refreshProxySettings();
            return result;
        } catch (InvalidClassException e) {
            logger.warn("User preferences file version does not match application version: {}", e.getMessage());
            return new UserPreferencesImpl(userPreferencesFile);
        } catch (Exception e) {
            logger.warn("Could not read user preferences file", e);
            return new UserPreferencesImpl(userPreferencesFile);
        } finally {
            FileHelper.safeClose(inputStream);
        }
    }

    @Override
    public void save() {
        if (_userPreferencesFile == null) {
            logger.debug("Not saving user preferences, since no user preferences file has been provided");
            return;
        }

        logger.info("Saving user preferences to {}", _userPreferencesFile.getName().getPath());

        ObjectOutputStream outputStream = null;
        try {
            OutputStream fileOutputStream = _userPreferencesFile.getContent().getOutputStream();
            outputStream = new ObjectOutputStream(fileOutputStream);
            outputStream.writeObject(this);
            outputStream.flush();
        } catch (Exception e) {
            logger.warn("Unexpected error while saving user preferences", e);
            throw new IllegalStateException(e);
        } finally {
            FileHelper.safeClose(outputStream);
        }
    }

    @Override
    public File getOpenDatastoreDirectory() {
        if (openDatastoreDirectory == null) {
            openDatastoreDirectory = VFSUtils.toFile(DataCleanerHome.get());
        }
        return openDatastoreDirectory;
    }

    @Override
    public void setOpenDatastoreDirectory(File openFileDir) {
        this.openDatastoreDirectory = openFileDir;
    }

    @Override
    public File getConfiguredFileDirectory() {
        if (configuredFileDirectory == null) {
            configuredFileDirectory = VFSUtils.toFile(DataCleanerHome.get());
        }
        return configuredFileDirectory;
    }

    @Override
    public void setConfiguredFileDirectory(File openPropertyFileDirectory) {
        this.configuredFileDirectory = openPropertyFileDirectory;
    }

    @Override
    public File getAnalysisJobDirectory() {
        if (analysisJobDirectory == null) {
            analysisJobDirectory = getRelativeDirectory("jobs");
        }
        return analysisJobDirectory;
    }

    @Override
    public void setAnalysisJobDirectory(File saveFileDirectory) {
        this.analysisJobDirectory = saveFileDirectory;
    }

    @Override
    public File getSaveDatastoreDirectory() {
        if (saveDatastoreDirectory == null) {
            saveDatastoreDirectory = getRelativeDirectory("datastores");
        }
        return saveDatastoreDirectory;
    }

    private File getRelativeDirectory(String name) {
        File dataCleanerHome = VFSUtils.toFile(DataCleanerHome.get());
        File directory = new File(dataCleanerHome, name);
        if (!directory.exists()) {
            directory.mkdir();
        }
        return directory;
    }

    @Override
    public void setSaveDatastoreDirectory(File saveDatastoreDirectory) {
        this.saveDatastoreDirectory = saveDatastoreDirectory;
    }

    @Override
    public void addRecentJobFile(FileObject fileObject) {
        final File file = VFSUtils.toFile(fileObject);
        if (file != null) {
            if (recentJobFiles.contains(file)) {
                recentJobFiles.remove(file);
            }
            recentJobFiles.add(0, file);
        }
    }

    @Override
    public List<FileObject> getRecentJobFiles() {
        if (recentJobFiles == null || recentJobFiles.isEmpty()) {
            recentJobFiles = new ArrayList<>();
            final File dcHome = VFSUtils.toFile(DataCleanerHome.get());

            final List<String> demoJobPaths = DataCleanerHome.getAllInitialFiles();
            for (String demoJobPath : demoJobPaths) {
                recentJobFiles.add(new File(dcHome, demoJobPath));
            }
        }

        List<FileObject> fileObjectList = CollectionUtils.map(recentJobFiles, new Func<File, FileObject>() {
            @Override
            public FileObject eval(File file) {
                try {
                    return VFSUtils.getFileSystemManager().toFileObject(file);
                } catch (FileSystemException e) {
                    throw new IllegalStateException(e);
                }
            }
        });
        return fileObjectList;
    }

    @Override
    public List<Datastore> getUserDatastores() {
        if (userDatastores == null) {
            userDatastores = new ArrayList<Datastore>();
        }
        return userDatastores;
    }

    @Override
    public List<Dictionary> getUserDictionaries() {
        return userDictionaries;
    }

    @Override
    public List<SynonymCatalog> getUserSynonymCatalogs() {
        if (userSynonymCatalogs == null) {
            userSynonymCatalogs = new ArrayList<SynonymCatalog>();
        }
        return userSynonymCatalogs;
    }

    @Override
    public List<UserDatabaseDriver> getDatabaseDrivers() {
        if (databaseDrivers == null) {
            databaseDrivers = new ArrayList<UserDatabaseDriver>();
        }
        return databaseDrivers;
    }

    @Override
    public List<StringPattern> getUserStringPatterns() {
        return userStringPatterns;
    }

    @Override
    public boolean isProxyEnabled() {
        return proxyEnabled;
    }

    @Override
    public void setProxyEnabled(boolean proxyEnabled) {
        this.proxyEnabled = proxyEnabled;
        refreshProxySettings();
    }

    @Override
    public String getProxyHostname() {
        return proxyHostname;
    }

    @Override
    public void setProxyHostname(String proxyHostname) {
        this.proxyHostname = proxyHostname;
        refreshProxySettings();
    }

    @Override
    public int getProxyPort() {
        return proxyPort;
    }

    @Override
    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
        refreshProxySettings();
    }

    @Override
    public String getProxyUsername() {
        return proxyUsername;
    }

    @Override
    public void setProxyUsername(String proxyUsername) {
        this.proxyUsername = proxyUsername;
        refreshProxySettings();
    }

    @Override
    public String getProxyPassword() {
        return proxyPassword;
    }

    @Override
    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
        refreshProxySettings();
    }

    @Override
    public boolean isProxyAuthenticationEnabled() {
        return proxyAuthenticationEnabled;
    }

    @Override
    public void setProxyAuthenticationEnabled(boolean proxyAuthenticationEnabled) {
        this.proxyAuthenticationEnabled = proxyAuthenticationEnabled;
        refreshProxySettings();
    }

    private void refreshProxySettings() {
        if (System.getProperty("http.proxyHost") != null) {
            if (!"true".equals(System.getProperty("http.proxy.setByDataCleaner"))) {
                // proxy was already configured by command line
                return;
            }
        }

        System.setProperty("http.proxy.setByDataCleaner", "true");

        String proxyHost = getProxyHostname();
        int proxyPort = getProxyPort();
        String username = getProxyUsername();
        String password = getProxyPassword();

        if (isProxyEnabled() && proxyHost != null) {
            logger.debug("Setting proxy host={}, port={}", proxyHost, proxyPort);
            System.setProperty("http.proxyHost", proxyHost);
            System.setProperty("http.proxyPort", "" + proxyPort);
            System.setProperty("https.proxyHost", proxyHost);
            System.setProperty("https.proxyPort", "" + proxyPort);
            if (isProxyAuthenticationEnabled() && username != null && password != null) {
                logger.debug("Setting proxy username={}, password", username);

                System.setProperty("http.proxyUser", username);
                System.setProperty("http.proxyPassword", password);
                System.setProperty("https.proxyUser", username);
                System.setProperty("https.proxyPassword", password);
            } else {
                logger.debug("Clearing proxy username, password");

                System.clearProperty("http.proxyUser");
                System.clearProperty("http.proxyPassword");
                System.clearProperty("https.proxyUser");
                System.clearProperty("https.proxyPassword");
            }
        } else {
            logger.debug("Clearing proxy host, port, username, password");

            System.clearProperty("http.proxyHost");
            System.clearProperty("http.proxyPort");
            System.clearProperty("https.proxyHost");
            System.clearProperty("https.proxyPort");

            System.clearProperty("http.proxyUser");
            System.clearProperty("http.proxyPassword");
            System.clearProperty("https.proxyUser");
            System.clearProperty("https.proxyPassword");
        }
    }

    @Override
    public List<ExtensionPackage> getExtensionPackages() {
        if (extensionPackages == null) {
            extensionPackages = new ArrayList<ExtensionPackage>();
        }
        return extensionPackages;
    }

    @Override
    public void setExtensionPackages(List<ExtensionPackage> extensionPackages) {
        this.extensionPackages = extensionPackages;
    }

    @Override
    public void removeExtensionPackage(ExtensionPackage extensionPackage) {
        if (extensionPackages == null) {
            extensionPackages = new ArrayList<ExtensionPackage>();
        }
        extensionPackages.remove(extensionPackage);
    }

    @Override
    public void addExtensionPackage(ExtensionPackage extensionPackage) {
        if (extensionPackages == null) {
            extensionPackages = new ArrayList<ExtensionPackage>();
        }
        extensionPackages.add(extensionPackage);
    }

    @Override
    public Map<String, String> getAdditionalProperties() {
        if (additionalProperties == null) {
            additionalProperties = new HashMap<String, String>();
        }
        return additionalProperties;
    }

    @Override
    public void setMonitorConnection(MonitorConnection connection) {
        this.monitorConnection = connection;
    }

    @Override
    public MonitorConnection getMonitorConnection() {
        if (monitorConnection == null) {
            final String hostname = System.getProperty(SystemProperties.MONITOR_HOSTNAME);
            if (!StringUtils.isNullOrEmpty(hostname)) {
                final int port = Integer.parseInt(System.getProperty(SystemProperties.MONITOR_PORT));
                final String contextPath = System.getProperty(SystemProperties.MONITOR_CONTEXT);
                final String tenant = System.getProperty(SystemProperties.MONITOR_TENANT);
                final boolean isHttps = "true".equals(System.getProperty(SystemProperties.MONITOR_HTTPS));
                final String username = System.getProperty(SystemProperties.MONITOR_USERNAME);
                final String encodedPassword = null;
                final MonitorConnection con = new MonitorConnection(this, hostname, port, contextPath, isHttps, tenant,
                        username, encodedPassword);
                return con;
            }
        }
        return monitorConnection;
    }

    @Override
    public File getSaveDownloadedFilesDirectory() {
        if (saveDownloadedFilesDirectory == null) {
            saveDownloadedFilesDirectory = VFSUtils.toFile(DataCleanerHome.get());
        }
        return saveDownloadedFilesDirectory;
    }

    @Override
    public void setSaveDownloadedFilesDirectory(File directory) {
        this.saveDownloadedFilesDirectory = directory;
    }

    @Override
    public File getExtensionsDirectory() {
        if (extensionsDirectory == null) {
            extensionsDirectory = getRelativeDirectory("extensions");
        }
        return extensionsDirectory;
    }

    @Override
    public void setExtensionsDirectory(File directory) {
        this.extensionsDirectory = directory;
    }

    @Override
    public CloseableHttpClient createHttpClient() {
        final HttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        final RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
        
        
        if (isProxyEnabled()) {
            // set up HTTP proxy
            final String proxyHostname = getProxyHostname();
            final int proxyPort = getProxyPort();

            try {
                final HttpHost proxy = new HttpHost(proxyHostname, proxyPort);
                requestConfigBuilder.setProxy(proxy);

                if (isProxyAuthenticationEnabled()) {
                    final AuthScope authScope = new AuthScope(proxyHostname, proxyPort);
                    final String proxyUsername = getProxyUsername();
                    final UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(proxyUsername,
                            getProxyPassword());
                    credentialsProvider.setCredentials(authScope, credentials);

                    final int backslashIndex = proxyUsername.lastIndexOf('\\');
                    final String ntUsername;
                    final String ntDomain;
                    if (backslashIndex != -1) {
                        ntUsername = proxyUsername.substring(backslashIndex + 1);
                        ntDomain = proxyUsername.substring(0, backslashIndex);
                    } else {
                        ntUsername = proxyUsername;
                        ntDomain = System.getProperty("datacleaner.proxy.domain");
                    }

                    String workstation = System.getProperty("datacleaner.proxy.workstation");
                    if (Strings.isNullOrEmpty(workstation)) {
                        String computername = InetAddress.getLocalHost().getHostName();
                        workstation = computername;
                    }

                    NTCredentials ntCredentials = new NTCredentials(ntUsername, getProxyPassword(), workstation,
                            ntDomain);
                    AuthScope ntAuthScope = new AuthScope(proxyHostname, proxyPort, AuthScope.ANY_REALM, "ntlm");
                    credentialsProvider.setCredentials(ntAuthScope, ntCredentials);
                }
            } catch (Exception e) {
                // ignore proxy creation and return http client without it
                logger.error("Unexpected error occurred while initializing HTTP proxy", e);
            }
        }
        
        final RequestConfig requestConfig = requestConfigBuilder.build();
        final CloseableHttpClient httpClient = HttpClients.custom().useSystemProperties().setConnectionManager(connectionManager).setDefaultCredentialsProvider(credentialsProvider).setDefaultRequestConfig(requestConfig).build();
        return httpClient;
    }
}
