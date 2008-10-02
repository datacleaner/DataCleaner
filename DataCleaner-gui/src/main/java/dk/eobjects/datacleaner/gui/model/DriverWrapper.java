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

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;

/**
 * This class simply wraps a JDBC driver. It is nescesary to wrap them since
 * DriverManager will only accept Driver instances that have the same
 * ClassLoader as the DriverManager it self and we use dynamic class loading for
 * the drivers.
 */
public class DriverWrapper implements Driver {

	private Driver _driver;

	public DriverWrapper(Driver driver) {
		_driver = driver;
	}

	public boolean acceptsURL(String url) throws SQLException {
		return _driver.acceptsURL(url);
	}

	public Connection connect(String url, Properties info) throws SQLException {
		return _driver.connect(url, info);
	}

	public int getMajorVersion() {
		return _driver.getMajorVersion();
	}

	public int getMinorVersion() {
		return _driver.getMinorVersion();
	}

	public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
			throws SQLException {
		return _driver.getPropertyInfo(url, info);
	}

	public boolean jdbcCompliant() {
		return _driver.jdbcCompliant();
	}
}