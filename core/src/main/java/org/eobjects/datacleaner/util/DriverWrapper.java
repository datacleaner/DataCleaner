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
package org.eobjects.datacleaner.util;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * This class simply wraps a JDBC driver. It is nescesary to wrap them since
 * DriverManager will only accept Driver instances that have the same
 * ClassLoader as the DriverManager it self and sometimes we use dynamic class
 * loading for the drivers.
 */
public final class DriverWrapper implements Driver {

	private final Driver _driver;

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

	public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
		return _driver.getPropertyInfo(url, info);
	}

	public boolean jdbcCompliant() {
		return _driver.jdbcCompliant();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DriverWrapper) {
			obj = ((DriverWrapper) obj)._driver;
		}
		if (obj instanceof Driver) {
			return _driver.equals(obj);
		}
		return false;
	}

	public int hashCode() {
		return _driver.hashCode();
	};

	@Override
	public String toString() {
		return _driver.toString();
	}

	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		throw new SQLFeatureNotSupportedException();
	}
}