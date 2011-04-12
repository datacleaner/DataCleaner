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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.Main;
import org.eobjects.datacleaner.util.ResourceManager;

/**
 * Incapsulation of the DATACLEANER_HOME folder. This folder is resolved using
 * the following ordered approach:
 * 
 * <ol>
 * <li>If a DATACLEANER_HOME environment variable exists, it will be used.</li>
 * <li>If the application is running in Java WebStart mode, a sandbox folder
 * will be used.</li>
 * <li>If none of the above, the current folder "." will be used.</li>
 * </ol>
 * 
 * @author Kasper Sørensen
 */
public final class DataCleanerHome {

	private static final File _dataCleanerHome;

	static {
		File candidate = null;

		String env = System.getenv("DATACLEANER_HOME");
		if (!StringUtils.isNullOrEmpty(env)) {
			candidate = new File(env);
		}

		// to find out if web start is running, use system property
		// http://lopica.sourceforge.net/faq.html#under
		boolean webstartMode = System.getProperty("javawebstart.version") != null;

		if (!isUsable(candidate)) {
			if (webstartMode) {
				// in web start, the default folder will be in user.home
				String userHomePath = System.getProperty("user.home");
				if (userHomePath == null) {
					throw new IllegalStateException("Could not determine user home directory: " + candidate.getPath());
				}
				candidate = new File(userHomePath + File.separatorChar + ".datacleaner" + File.separatorChar + Main.VERSION);

			} else {
				// in normal mode, the default folder will be in the working
				// directory
				candidate = new File(".");
			}
		}

		if (!isUsable(candidate)) {
			if (!candidate.exists() && !candidate.mkdirs()) {
				throw new IllegalStateException("Could not create DataCleaner home directory: " + candidate.getPath());
			}

			copyIfNonExisting(candidate, "conf.xml");
		}

		_dataCleanerHome = candidate;
	}

	/**
	 * @return a file reference to the DataCleaner home folder.
	 */
	public static File get() {
		return _dataCleanerHome;
	}

	private static void copyIfNonExisting(File directory, String filename) {
		File file = new File(directory, filename);
		if (file.exists()) {
			return;
		}
		ResourceManager resourceManager = ResourceManager.getInstance();
		URL url = resourceManager.getUrl("datacleaner-home/" + filename);

		InputStream is = null;
		OutputStream os = null;
		try {
			is = url.openStream();
			os = new FileOutputStream(file);

			final int bufferSize = 1024;
			byte[] buffer = new byte[bufferSize];
			for (int read = is.read(buffer); read > 0 && read <= bufferSize; read = is.read(buffer)) {
				os.write(buffer, 0, read);
			}
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				// do nothing
			}
			try {
				if (os != null) {
					os.close();
				}
			} catch (IOException e) {
				// do nothing
			}
		}
	}

	private static boolean isUsable(File candidate) {
		if (candidate != null) {
			if (candidate.exists() && candidate.isDirectory()) {
				File conf = new File(candidate, "conf.xml");
				if (conf.exists() && conf.isFile()) {
					return true;
				}
			}
		}
		return false;
	}
}
