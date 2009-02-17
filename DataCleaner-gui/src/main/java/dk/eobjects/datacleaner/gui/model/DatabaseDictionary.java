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
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;

import dk.eobjects.datacleaner.catalog.ColumnDictionary;
import dk.eobjects.datacleaner.catalog.IDictionary;
import dk.eobjects.datacleaner.gui.setup.GuiConfiguration;
import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.JdbcDataContextFactory;
import dk.eobjects.metamodel.schema.Column;

public class DatabaseDictionary implements IDictionary {

	private static final long serialVersionUID = -997656251671163257L;
	private String _name;
	private String _namedConnectionName;
	private String _schemaName;
	private String _tableName;
	private String _columnName;
	private transient ColumnDictionary _columnDictionary;

	public DatabaseDictionary() {
	}

	public DatabaseDictionary(String name, String namedConnectionName,
			String schemaName, String tableName, String columnName) {
		_name = name;
		_namedConnectionName = namedConnectionName;
		_schemaName = schemaName;
		_tableName = tableName;
		_columnName = columnName;
	}

	public String getName() {
		return _name;
	}

	public boolean[] isValid(String... values) {
		try {
			return getColumnDictionary().isValid(values);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private ColumnDictionary getColumnDictionary() throws SQLException,
			NullPointerException {
		if (_columnDictionary == null) {
			DataContext dc = null;
			Collection<NamedConnection> namedConnections = GuiConfiguration
					.getNamedConnections();
			for (Iterator<NamedConnection> it = namedConnections.iterator(); it
					.hasNext();) {
				NamedConnection namedConnection = it.next();
				if (_namedConnectionName.equals(namedConnection.getName())) {
					Connection connection = DriverManager.getConnection(
							namedConnection.getConnectionString(),
							namedConnection.getUsername(), namedConnection
									.getPassword());
					connection.setReadOnly(true);
					dc = JdbcDataContextFactory.getDataContext(connection,
							namedConnection.getCatalog());
				}
			}

			Column column = dc.getSchemaByName(_schemaName).getTableByName(
					_tableName).getColumnByName(_columnName);
			_columnDictionary = new ColumnDictionary(_name, dc, column);
		}
		return _columnDictionary;
	}

	public String getNamedConnectionName() {
		return _namedConnectionName;
	}

	public DatabaseDictionary setNamedConnectionName(String namedConnectionName) {
		_namedConnectionName = namedConnectionName;
		return this;
	}

	public String getSchemaName() {
		return _schemaName;
	}

	public DatabaseDictionary setSchemaName(String schemaName) {
		_schemaName = schemaName;
		return this;
	}

	public String getTableName() {
		return _tableName;
	}

	public DatabaseDictionary setTableName(String tableName) {
		_tableName = tableName;
		return this;
	}

	public String getColumnName() {
		return _columnName;
	}

	public DatabaseDictionary setColumnName(String columnName) {
		_columnName = columnName;
		return this;
	}

	public DatabaseDictionary setName(String name) {
		_name = name;
		return this;
	}

	@Override
	public int hashCode() {
		return _name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return this == obj;
	}
}