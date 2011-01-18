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
import java.io.Serializable;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.eobjects.analyzer.util.ReflectionUtils;
import org.eobjects.datacleaner.database.DatabaseDriverState;
import org.eobjects.datacleaner.database.DriverWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UserDatabaseDriver implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(UserDatabaseDriver.class);

	private transient Driver _driverInstance;
	private transient Driver _registeredDriver;
	private transient boolean _loaded = false;
	private final File[] _files;
	private final String _driverClassName;

	public UserDatabaseDriver(File[] files, String driverClassName) {
		if (files == null) {
			throw new IllegalStateException("Driver file(s) cannot be null");
		}
		if (driverClassName == null) {
			throw new IllegalStateException("Driver class name cannot be null");
		}
		_files = files;
		_driverClassName = driverClassName;
	}

	public String getDriverClassName() {
		return _driverClassName;
	}

	public File[] getFiles() {
		return _files;
	}

	public UserDatabaseDriver loadDriver() throws IllegalStateException {
		try {
			final URL[] urls = new URL[_files.length];
			for (int i = 0; i < urls.length; i++) {
				URL url = _files[i].toURI().toURL();
				logger.debug("Using URL: {}", url);
				urls[i] = url;
			}

			final ClassLoader parentClassLoader = Thread.currentThread().getContextClassLoader();

			// removing the security manager is nescesary for classes in
			// external jar files to have privileges to do eg. system property
			// lookups etc.
			System.setSecurityManager(null);

			final URLClassLoader driverClassLoader = AccessController.doPrivileged(new PrivilegedAction<URLClassLoader>() {
				@Override
				public URLClassLoader run() {
					return new URLClassLoader(urls, parentClassLoader);
				}
			});

			Class<?> loadedClass = Class.forName(_driverClassName, true, driverClassLoader);
			logger.info("Loaded class: {}", loadedClass.getName());

			if (ReflectionUtils.is(loadedClass, Driver.class)) {
				_driverInstance = (Driver) loadedClass.newInstance();
				_registeredDriver = new DriverWrapper(_driverInstance);
				DriverManager.registerDriver(_registeredDriver);
			} else {
				throw new IllegalStateException("Class is not a Driver class: " + _driverClassName);
			}
			_loaded = true;
			return this;
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public void unloadDriver() {
		try {
			DriverManager.deregisterDriver(_registeredDriver);
			_registeredDriver = null;
			_driverInstance = null;
			_loaded = false;
		} catch (SQLException e) {
			logger.error("Exception occurred while unloading driver: " + _driverClassName, e);
		}
	}

	public boolean isLoaded() {
		return _loaded;
	}

	public DatabaseDriverState getState() {
		if (_loaded) {
			return DatabaseDriverState.INSTALLED_WORKING;
		}
		return DatabaseDriverState.INSTALLED_NOT_WORKING;
	}
}
