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
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.apache.http.impl.client.CloseableHttpClient;
import org.datacleaner.connection.Datastore;
import org.datacleaner.database.UserDatabaseDriver;
import org.datacleaner.extensions.ExtensionPackage;
import org.datacleaner.reference.Dictionary;
import org.datacleaner.reference.StringPattern;
import org.datacleaner.reference.SynonymCatalog;

/**
 * Defines the settings provided at runtime by the user in DataCleaner
 */
public interface UserPreferences {

    public void save();

    public File getExtensionsDirectory();
    
    public void setExtensionsDirectory(File directory);

    public File getOpenDatastoreDirectory();

    public void setOpenDatastoreDirectory(File openFileDir);

    public File getConfiguredFileDirectory();

    public void setConfiguredFileDirectory(File openPropertyFileDirectory);

    public File getAnalysisJobDirectory();

    public void setAnalysisJobDirectory(File saveFileDirectory);

    public File getSaveDatastoreDirectory();

    public void setSaveDatastoreDirectory(File saveDatastoreDirectory);

    public void addRecentJobFile(FileObject file);

    public List<FileObject> getRecentJobFiles();

    public List<Datastore> getUserDatastores();

    public List<Dictionary> getUserDictionaries();

    public List<SynonymCatalog> getUserSynonymCatalogs();

    public List<UserDatabaseDriver> getDatabaseDrivers();

    public List<StringPattern> getUserStringPatterns();

    public boolean isProxyEnabled();

    public void setProxyEnabled(boolean proxyEnabled);

    public String getProxyHostname();

    public void setProxyHostname(String proxyHostname);

    public int getProxyPort();

    public void setProxyPort(int proxyPort);

    public String getProxyUsername();

    public void setProxyUsername(String proxyUsername);

    public String getProxyPassword();

    public void setProxyPassword(String proxyPassword);

    public boolean isProxyAuthenticationEnabled();

    public void setProxyAuthenticationEnabled(boolean proxyAuthenticationEnabled);

    /**
     * Creates a HTTP client based on the user preferences with regard to
     * proxying and more.
     * 
     * @return a HTTP client
     */
    public CloseableHttpClient createHttpClient();

    public MonitorConnection getMonitorConnection();

    public void setMonitorConnection(MonitorConnection connection);

    public List<ExtensionPackage> getExtensionPackages();

    public void setExtensionPackages(List<ExtensionPackage> extensionPackages);

    public Map<String, String> getAdditionalProperties();

    public File getSaveDownloadedFilesDirectory();

    public void setSaveDownloadedFilesDirectory(File directory);

    public void addExtensionPackage(ExtensionPackage extensionPackage);

    public void removeExtensionPackage(ExtensionPackage extensionPackage);
}
