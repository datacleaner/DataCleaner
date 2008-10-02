/**
 *  This file is part of DataCleaner.
 *
 *  DataCleaner is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DataCleaner is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DataCleaner.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.eobjects.datacleaner.gui.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Represents a database driver consisting of a filename (referencing the JAR
 * file) and a class name (the driver class)
 * 
 * Note: DatabaseDrivers are contained BOTH in GuiSettings and GuiConfiguration -
 * GuiSettings contains the dynamically loaded drivers (which are interchangable
 * during runtime) and GuiConfiguration contains the statically loaded drivers.
 */
public class DatabaseDriver implements Serializable {

	private static final long serialVersionUID = -3439901946268007295L;
	private static final Log _log = LogFactory.getLog(DatabaseDriver.class);

	private transient URLClassLoader _classLoader;
	private transient boolean _loaded = false;
	private transient Driver _driverInstance;
	private transient Driver _registeredDriver;
	private String _filename;
	private String _driverClass;
	private String _name;

	public DatabaseDriver() {
	}

	public DatabaseDriver(String name, String driverClass) {
		_name = name;
		_driverClass = driverClass;
	}

	public DatabaseDriver(File file, String driverClass) {
		_filename = file.getAbsolutePath();
		_driverClass = driverClass;
	}

	public DatabaseDriver loadDriver() throws FileNotFoundException,
			IllegalStateException, ClassNotFoundException,
			InstantiationException, IllegalAccessException, SQLException {
		if (_driverClass == null) {
			throw new IllegalStateException(
					"Class name must be set before loading driver");
		}
		if (_filename == null) {
			// The driver is already in the classpath
			_registeredDriver = (Driver) Class.forName(_driverClass)
					.newInstance();
			_driverInstance = _registeredDriver;
			DriverManager.registerDriver(_registeredDriver);
			_loaded = true;
		} else {
			// Load the driver from a jar file
			try {
				URL url = new File(_filename).toURI().toURL();
				if (_log.isDebugEnabled()) {
					_log.debug("Using URL: " + url);
				}
				_classLoader = new URLClassLoader(new URL[] { url },
						ClassLoader.getSystemClassLoader());
				Class<?> loadedClass = Class.forName(_driverClass, true,
						_classLoader);
				_log.info("Loaded class: " + loadedClass.getName());
				if (Driver.class.isAssignableFrom(loadedClass)) {
					_driverInstance = (Driver) loadedClass.newInstance();
					_registeredDriver = new DriverWrapper(_driverInstance);
					DriverManager.registerDriver(_registeredDriver);
					_loaded = true;
				} else {
					throw new IllegalStateException(
							"Class is not a Driver class: " + _driverClass);
				}
			} catch (MalformedURLException e) {
				throw new FileNotFoundException(_filename);
			}
		}
		return this;
	}

	public void unloadDriver() {
		try {
			DriverManager.deregisterDriver(_registeredDriver);
			_registeredDriver = null;
			_driverInstance = null;
			_loaded = false;
		} catch (SQLException e) {
			_log.error(e);
		}
	}

	public boolean isLoaded() {
		return _loaded;
	}

	@Override
	public String toString() {
		return "DatabaseDriver[name=" + _name + ",filename=" + _filename
				+ ",driverClass=" + _driverClass + "]";
	}

	public String getFilename() {
		return _filename;
	}

	public void setFilename(String filename) {
		_filename = filename;
	}

	public String getDriverClass() {
		return _driverClass;
	}

	public void setDriverClass(String driverClass) {
		_driverClass = driverClass;
	}

	public String getName() {
		return _name;
	}

	public void setName(String name) {
		_name = name;
	}
}