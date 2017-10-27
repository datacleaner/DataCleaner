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

    void save();

    File getExtensionsDirectory();

    void setExtensionsDirectory(File directory);

    File getOpenDatastoreDirectory();

    void setOpenDatastoreDirectory(File openFileDir);

    File getConfiguredFileDirectory();

    void setConfiguredFileDirectory(File openPropertyFileDirectory);

    File getAnalysisJobDirectory();

    void setAnalysisJobDirectory(File saveFileDirectory);

    File getSaveDatastoreDirectory();

    void setSaveDatastoreDirectory(File saveDatastoreDirectory);

    void addRecentJobFile(FileObject file);

    List<FileObject> getRecentJobFiles();

    List<Datastore> getUserDatastores();

    List<Dictionary> getUserDictionaries();

    List<SynonymCatalog> getUserSynonymCatalogs();

    List<UserDatabaseDriver> getDatabaseDrivers();

    List<StringPattern> getUserStringPatterns();

    boolean isProxyEnabled();

    void setProxyEnabled(boolean proxyEnabled);

    String getProxyHostname();

    void setProxyHostname(String proxyHostname);

    int getProxyPort();

    void setProxyPort(int proxyPort);

    String getProxyUsername();

    void setProxyUsername(String proxyUsername);

    String getProxyPassword();

    void setProxyPassword(String proxyPassword);

    boolean isProxyAuthenticationEnabled();

    void setProxyAuthenticationEnabled(boolean proxyAuthenticationEnabled);

    /**
     * Creates a HTTP client based on the user preferences with regard to
     * proxying and more.
     *
     * @return a HTTP client
     */
    CloseableHttpClient createHttpClient();

    List<ExtensionPackage> getExtensionPackages();

    void setExtensionPackages(List<ExtensionPackage> extensionPackages);

    Map<String, String> getAdditionalProperties();

    File getSaveDownloadedFilesDirectory();

    void setSaveDownloadedFilesDirectory(File directory);

    void addExtensionPackage(ExtensionPackage extensionPackage);

    void removeExtensionPackage(ExtensionPackage extensionPackage);
}
