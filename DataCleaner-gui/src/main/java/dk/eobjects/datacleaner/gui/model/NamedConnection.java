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

/**
 * A named connection represents a users connection that has been given a name
 * for easy of use.
 * 
 * @see dk.eobjects.datacleaner.gui.dialogs.OpenDatabaseDialog
 */
public class NamedConnection {

	private String _name;
	private String _connectionString;
	private String _username;
	private String _password;
	private String _catalog;
	private String[] _tableTypes = { "TABLE" };

	@Deprecated
	private String _driverClass;

	public String[] getTableTypes() {
		return _tableTypes;
	}

	public void setTableTypes(String... tableTypes) {
		_tableTypes = tableTypes;
	}

	public String getName() {
		return _name;
	}

	public void setName(String name) {
		_name = name;
	}

	/**
	 * @deprecated Driver classes are no longer used in the named connections
	 */
	@Deprecated
	public String getDriverClass() {
		return _driverClass;
	}

	/**
	 * @deprecated Driver classes are no longer used in the named connections
	 */
	@Deprecated
	public void setDriverClass(String driverClass) {
		_driverClass = driverClass;
	}

	public String getConnectionString() {
		return _connectionString;
	}

	public void setConnectionString(String connectionString) {
		_connectionString = connectionString;
	}

	public String getUsername() {
		return _username;
	}

	public void setUsername(String username) {
		_username = username;
	}

	public String getPassword() {
		return _password;
	}

	public void setPassword(String password) {
		_password = password;
	}

	public String getCatalog() {
		return _catalog;
	}

	public void setCatalog(String catalog) {
		_catalog = catalog;
	}

	public void setUrl(String url) {
		setConnectionString(url);
	}

	public String getUrl() {
		return getConnectionString();
	}
}