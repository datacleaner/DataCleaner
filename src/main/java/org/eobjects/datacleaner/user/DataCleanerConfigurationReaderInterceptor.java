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
import java.io.IOException;

import org.eobjects.analyzer.configuration.ConfigurationReaderInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration reader interceptor that is aware of the DataCleaner
 * environment.
 * 
 * @author Kasper Sørensen
 */
public class DataCleanerConfigurationReaderInterceptor implements ConfigurationReaderInterceptor {

	private static final Logger logger = LoggerFactory.getLogger(DataCleanerConfigurationReaderInterceptor.class);

	private final File _dataCleanerHome;

	public DataCleanerConfigurationReaderInterceptor(File dataCleanerHome) {
		_dataCleanerHome = dataCleanerHome;
	}

	@Override
	public String createFilename(String filename) {
		if (filename == null) {
			return null;
		}
		File file = new File(filename);
		if (!file.isAbsolute()) {
			file = new File(_dataCleanerHome, filename);
			try {
				filename = file.getCanonicalPath();
			} catch (IOException e) {
				logger.warn("Could not get canonical path for relative file: " + filename, e);
				filename = file.getAbsolutePath();
			}
		}
		return filename;
	}

	@Override
	public String getTemporaryStorageDirectory() {
		return UserPreferences.getInstance().getSaveDatastoreDirectory().getAbsolutePath();
	}

}
