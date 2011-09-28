/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.user;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InvalidClassException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.reference.Dictionary;
import org.eobjects.analyzer.reference.StringPattern;
import org.eobjects.analyzer.reference.SynonymCatalog;
import org.eobjects.analyzer.util.ChangeAwareObjectInputStream;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.actions.LoginChangeListener;
import org.eobjects.metamodel.util.FileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserPreferencesImpl implements UserPreferences {

	private static final long serialVersionUID = 6L;

	private static final Logger logger = LoggerFactory.getLogger(UserPreferencesImpl.class);

	private transient File _userPreferencesFile;
	private transient List<LoginChangeListener> loginChangeListeners;

	private List<UserDatabaseDriver> databaseDrivers = new ArrayList<UserDatabaseDriver>();
	private List<ExtensionPackage> extensionPackages = new ArrayList<ExtensionPackage>();
	private List<Datastore> userDatastores = new ArrayList<Datastore>();
	private List<Dictionary> userDictionaries = new ArrayList<Dictionary>();
	private List<StringPattern> userStringPatterns = new ArrayList<StringPattern>();
	private List<SynonymCatalog> userSynonymCatalogs = new ArrayList<SynonymCatalog>();
	private Map<String, String> additionalProperties = new HashMap<String, String>();

	private String username;

	private boolean proxyEnabled = false;
	private boolean proxyAuthenticationEnabled = false;
	private String proxyHostname;
	private int proxyPort = 8080;
	private String proxyUsername;
	private String proxyPassword;

	private List<File> recentJobFiles = new ArrayList<File>();
	private File openDatastoreDirectory;
	private File configuredFileDirectory;
	private File analysisJobDirectory;
	private File saveDatastoreDirectory;

	private QuickAnalysisStrategy quickAnalysisStrategy = new QuickAnalysisStrategy();

	public UserPreferencesImpl(File userPreferencesFile) {
		_userPreferencesFile = userPreferencesFile;
	}

	public static UserPreferences load(final File userPreferencesFile, final boolean loadDatabaseDrivers) {
		if (userPreferencesFile == null || !userPreferencesFile.exists()) {
			logger.info("User preferences file does not exist");
			return new UserPreferencesImpl(userPreferencesFile);
		}
		
		ChangeAwareObjectInputStream inputStream = null;
		try {
			inputStream = new ChangeAwareObjectInputStream(new FileInputStream(userPreferencesFile));
			inputStream.addRenamedClass("org.eobjects.datacleaner.user.UserPreferences", UserPreferencesImpl.class);
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

	public void save() {
		logger.info("Saving user preferences to {}", _userPreferencesFile.getAbsolutePath());
		ObjectOutputStream outputStream = null;
		try {
			outputStream = new ObjectOutputStream(new FileOutputStream(_userPreferencesFile));
			outputStream.writeObject(this);
			outputStream.flush();
		} catch (Exception e) {
			logger.warn("Unexpected error while saving user preferences", e);
			throw new IllegalStateException(e);
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (Exception e) {
					throw new IllegalStateException(e);
				}
			}
		}
	}

	private List<LoginChangeListener> getLoginChangeListeners() {
		if (loginChangeListeners == null) {
			loginChangeListeners = new ArrayList<LoginChangeListener>();
		}
		return loginChangeListeners;
	}

	public void addLoginChangeListener(LoginChangeListener listener) {
		getLoginChangeListeners().add(listener);
	}

	public void removeLoginChangeListener(LoginChangeListener listener) {
		getLoginChangeListeners().add(listener);
	}

	public File getOpenDatastoreDirectory() {
		if (openDatastoreDirectory == null) {
			openDatastoreDirectory = DataCleanerHome.get();
		}
		return openDatastoreDirectory;
	}

	public void setOpenDatastoreDirectory(File openFileDir) {
		this.openDatastoreDirectory = openFileDir;
	}

	public File getConfiguredFileDirectory() {
		if (configuredFileDirectory == null) {
			configuredFileDirectory = DataCleanerHome.get();
		}
		return configuredFileDirectory;
	}

	public void setConfiguredFileDirectory(File openPropertyFileDirectory) {
		this.configuredFileDirectory = openPropertyFileDirectory;
	}

	public File getAnalysisJobDirectory() {
		if (analysisJobDirectory == null) {
			analysisJobDirectory = DataCleanerHome.get();
		}
		return analysisJobDirectory;
	}

	public void setAnalysisJobDirectory(File saveFileDirectory) {
		this.analysisJobDirectory = saveFileDirectory;
	}

	public File getSaveDatastoreDirectory() {
		if (saveDatastoreDirectory == null) {
			saveDatastoreDirectory = new File(DataCleanerHome.get(), "datastores");
		}
		return saveDatastoreDirectory;
	}

	public void setSaveDatastoreDirectory(File saveDatastoreDirectory) {
		this.saveDatastoreDirectory = saveDatastoreDirectory;
	}

	public void setUsername(String username) {
		this.username = username;

		List<LoginChangeListener> listeners = getLoginChangeListeners();
		for (LoginChangeListener listener : listeners) {
			listener.onLoginStateChanged(isLoggedIn(), username);
		}
	}

	public String getUsername() {
		return username;
	}

	public boolean isLoggedIn() {
		return !StringUtils.isNullOrEmpty(getUsername());
	}

	public void addRecentJobFile(File file) {
		if (recentJobFiles.contains(file)) {
			recentJobFiles.remove(file);
		}
		recentJobFiles.add(0, file);
	}

	public List<File> getRecentJobFiles() {
		return recentJobFiles;
	}

	public List<Datastore> getUserDatastores() {
		if (userDatastores == null) {
			userDatastores = new ArrayList<Datastore>();
		}
		return userDatastores;
	}

	public List<Dictionary> getUserDictionaries() {
		if (userDictionaries == null) {
			userDictionaries = new ArrayList<Dictionary>();
		}
		return userDictionaries;
	}

	public List<SynonymCatalog> getUserSynonymCatalogs() {
		if (userSynonymCatalogs == null) {
			userSynonymCatalogs = new ArrayList<SynonymCatalog>();
		}
		return userSynonymCatalogs;
	}

	public List<UserDatabaseDriver> getDatabaseDrivers() {
		if (databaseDrivers == null) {
			databaseDrivers = new ArrayList<UserDatabaseDriver>();
		}
		return databaseDrivers;
	}

	public List<StringPattern> getUserStringPatterns() {
		if (userStringPatterns == null) {
			userStringPatterns = new ArrayList<StringPattern>();
		}
		return userStringPatterns;
	}

	public boolean isProxyEnabled() {
		return proxyEnabled;
	}

	public void setProxyEnabled(boolean proxyEnabled) {
		this.proxyEnabled = proxyEnabled;
	}

	public String getProxyHostname() {
		return proxyHostname;
	}

	public void setProxyHostname(String proxyHostname) {
		this.proxyHostname = proxyHostname;
	}

	public int getProxyPort() {
		return proxyPort;
	}

	public void setProxyPort(int proxyPort) {
		this.proxyPort = proxyPort;
	}

	public String getProxyUsername() {
		return proxyUsername;
	}

	public void setProxyUsername(String proxyUsername) {
		this.proxyUsername = proxyUsername;
	}

	public String getProxyPassword() {
		return proxyPassword;
	}

	public void setProxyPassword(String proxyPassword) {
		this.proxyPassword = proxyPassword;
	}

	public boolean isProxyAuthenticationEnabled() {
		return proxyAuthenticationEnabled;
	}

	public void setProxyAuthenticationEnabled(boolean proxyAuthenticationEnabled) {
		this.proxyAuthenticationEnabled = proxyAuthenticationEnabled;
	}

	public QuickAnalysisStrategy getQuickAnalysisStrategy() {
		return quickAnalysisStrategy;
	}

	public void setQuickAnalysisStrategy(QuickAnalysisStrategy quickAnalysisStrategy) {
		this.quickAnalysisStrategy = quickAnalysisStrategy;
	}

	public List<ExtensionPackage> getExtensionPackages() {
		if (extensionPackages == null) {
			extensionPackages = new ArrayList<ExtensionPackage>();
		}
		return extensionPackages;
	}

	public void setExtensionPackages(List<ExtensionPackage> extensionPackages) {
		this.extensionPackages = extensionPackages;
	}

	public Map<String, String> getAdditionalProperties() {
		if (additionalProperties == null) {
			additionalProperties = new HashMap<String, String>();
		}
		return additionalProperties;
	}
}
