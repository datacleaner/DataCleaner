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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserPreferences implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final File userPreferencesFile = new File("userpreferences.dat");
	private static final Logger logger = LoggerFactory.getLogger(UserPreferences.class);

	private static UserPreferences instance;

	private File datastoreDirectory = new File(".");
	private File configuredFileDirectory = new File(".");
	private File analysisJobDirectory = new File(".");

	public static UserPreferences getInstance() {
		if (instance == null) {
			synchronized (UserPreferences.class) {
				if (instance == null) {
					if (userPreferencesFile.exists()) {
						ObjectInputStream inputStream = null;
						try {
							inputStream = new ObjectInputStream(new FileInputStream(userPreferencesFile));
							instance = (UserPreferences) inputStream.readObject();
						} catch (Exception e) {
							logger.warn("Could not read user preferences file", e);
							instance = new UserPreferences();
						} finally {
							if (inputStream != null) {
								try {
									inputStream.close();
								} catch (Exception e) {
									throw new IllegalStateException(e);
								}
							}
						}
					} else {
						instance = new UserPreferences();
					}
				}
			}
		}
		return instance;
	}

	private UserPreferences() {
		// prevent instantiation
	}

	public void save() {
		ObjectOutputStream outputStream = null;
		try {
			outputStream = new ObjectOutputStream(new FileOutputStream(userPreferencesFile));
			outputStream.writeObject(this);
			outputStream.flush();
		} catch (Exception e) {
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

	public File getDatastoreDirectory() {
		return datastoreDirectory;
	}

	public void setDatastoreDirectory(File openFileDir) {
		this.datastoreDirectory = openFileDir;
	}

	public File getConfiguredFileDirectory() {
		return configuredFileDirectory;
	}

	public void setConfiguredFileDirectory(File openPropertyFileDirectory) {
		this.configuredFileDirectory = openPropertyFileDirectory;
	}

	public File getAnalysisJobDirectory() {
		return analysisJobDirectory;
	}

	public void setAnalysisJobDirectory(File saveFileDirectory) {
		this.analysisJobDirectory = saveFileDirectory;
	}
}
