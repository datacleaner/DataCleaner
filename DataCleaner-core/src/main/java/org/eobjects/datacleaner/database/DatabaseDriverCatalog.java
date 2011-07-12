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
package org.eobjects.datacleaner.database;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eobjects.analyzer.util.CollectionUtils;
import org.eobjects.analyzer.util.Function;
import org.eobjects.datacleaner.user.UserDatabaseDriver;
import org.eobjects.datacleaner.user.UserPreferences;

public class DatabaseDriverCatalog implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String DATABASE_NAME_JDBC_ODBC_BRIDGE = "JDBC-ODBC bridge";
	public static final String DATABASE_NAME_TERADATA = "Teradata";
	public static final String DATABASE_NAME_H2 = "H2";
	public static final String DATABASE_NAME_HSQLDB_HYPER_SQL = "Hsqldb/HyperSQL";
	public static final String DATABASE_NAME_MICROSOFT_SQL_SERVER_OFFICIAL = "Microsoft SQL Server (official)";
	public static final String DATABASE_NAME_ORACLE = "Oracle";
	public static final String DATABASE_NAME_APACHE_DERBY_EMBEDDED = "Apache Derby (embedded)";
	public static final String DATABASE_NAME_APACHE_DERBY_CLIENT = "Apache Derby (client)";
	public static final String DATABASE_NAME_SQLITE = "SQLite";
	public static final String DATABASE_NAME_SYBASE = "Sybase";
	public static final String DATABASE_NAME_MICROSOFT_SQL_SERVER_JTDS = "Microsoft SQL Server (JTDS)";
	public static final String DATABASE_NAME_POSTGRESQL = "PostgreSQL";
	public static final String DATABASE_NAME_SAP_DB = "SAP DB";
	public static final String DATABASE_NAME_FIREBIRD = "Firebird";
	public static final String DATABASE_NAME_INGRES = "Ingres";
	public static final String DATABASE_NAME_DB2 = "DB2";
	public static final String DATABASE_NAME_MYSQL = "MySQL";

	private final List<DatabaseDriverDescriptor> _databaseDrivers = new ArrayList<DatabaseDriverDescriptor>();

	public DatabaseDriverCatalog() {
		add(DATABASE_NAME_MYSQL, "images/datastore-types/databases/mysql.png", "com.mysql.jdbc.Driver",
				"http://repo1.maven.org/maven2/mysql/mysql-connector-java/5.1.13/mysql-connector-java-5.1.13.jar",
				"jdbc:mysql://<hostname>:3306/<database>");
		add(DATABASE_NAME_DB2, "images/datastore-types/databases/db2.png", "com.ibm.db2.jcc.DB2Driver", null,
				"jdbc:db2://<hostname>:<port>/<database>", "jdbc:db2j:net://<hostname>:<port>/<database>");
		add(DATABASE_NAME_INGRES, "images/datastore-types/databases/ingres.png", "com.ingres.jdbc.IngresDriver",
				"http://repo1.maven.org/maven2/com/ingres/jdbc/iijdbc/9.3-3.8.2/iijdbc-9.3-3.8.2.jar",
				"jdbc:ingres://<hostname>:II7/<database>");
		add(DATABASE_NAME_FIREBIRD,
				"images/datastore-types/databases/firebird.png",
				"org.firebirdsql.jdbc.FBDriver",
				// firebird's driver also depends on the j2ee spec
				new String[] { "http://repo1.maven.org/maven2/org/firebirdsql/jdbc/jaybird/2.1.6/jaybird-2.1.6.jar",
						"http://repo1.maven.org/maven2/geronimo-spec/geronimo-spec-j2ee/1.4-rc4/geronimo-spec-j2ee-1.4-rc4.jar" },
				new String[] { "jdbc:firebirdsql:<hostname>:<path/to/database>.fdb" });
		add(DATABASE_NAME_SAP_DB, "images/datastore-types/databases/sapdb.png", "com.sap.dbtech.jdbc.DriverSapDB", null,
				"jdbc:sapdb://<hostname>/<database>");
		add(DATABASE_NAME_POSTGRESQL, "images/datastore-types/databases/postgresql.png", "org.postgresql.Driver",
				"http://repo1.maven.org/maven2/postgresql/postgresql/8.4-702.jdbc4/postgresql-8.4-702.jdbc4.jar",
				"jdbc:postgresql://<hostname>:5432/<database>");
		add(DATABASE_NAME_MICROSOFT_SQL_SERVER_JTDS, "images/datastore-types/databases/microsoft.png",
				"net.sourceforge.jtds.jdbc.Driver",
				"http://repo1.maven.org/maven2/net/sourceforge/jtds/jtds/1.2.4/jtds-1.2.4.jar",
				"jdbc:jtds:sqlserver://<hostname>;useUnicode=true;characterEncoding=UTF-8",
				"jdbc:jtds:sqlserver://<hostname>/<database>;useUnicode=true;characterEncoding=UTF-8",
				"jdbc:jtds:sybase://<hostname>/<database>");
		add(DATABASE_NAME_SYBASE, "images/datastore-types/databases/sybase.png", "net.sourceforge.jtds.jdbc.Driver",
				"http://repo1.maven.org/maven2/net/sourceforge/jtds/jtds/1.2.4/jtds-1.2.4.jar",
				"jdbc:jtds:sybase://<hostname>/<database>");
		add(DATABASE_NAME_SQLITE, "images/datastore-types/databases/sqlite.png", "org.sqlite.JDBC",
				"http://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.6.20/sqlite-jdbc-3.6.20.jar",
				"jdbc:sqlite:<path/to/database>.db");
		add(DATABASE_NAME_APACHE_DERBY_CLIENT, "images/datastore-types/databases/derby.png",
				"org.apache.derby.jdbc.ClientDriver",
				"http://repo1.maven.org/maven2/org/apache/derby/derbyclient/10.6.2.1/derbyclient-10.6.2.1.jar",
				"jdbc:derby://<hostname>:1527/<path/to/database>");
		add(DATABASE_NAME_APACHE_DERBY_EMBEDDED, "images/datastore-types/databases/derby.png",
				"org.apache.derby.jdbc.EmbeddedDriver",
				"http://repo1.maven.org/maven2/org/apache/derby/derby/10.6.2.1/derby-10.6.2.1.jar", "jdbc:derby:<database>");
		add(DATABASE_NAME_ORACLE, "images/datastore-types/databases/oracle.png", "oracle.jdbc.OracleDriver", null,
				"jdbc:oracle:thin:@<hostname>:1521:<sid>");
		add(DATABASE_NAME_MICROSOFT_SQL_SERVER_OFFICIAL, "images/datastore-types/databases/microsoft.png",
				"com.microsoft.sqlserver.jdbc.SQLServerDriver", null,
				"jdbc:sqlserver://<hostname>:3341;databaseName=<database>",
				"jdbc:sqlserver://<hostname>:3341;databaseName=<database>;integratedSecurity=true");
		add(DATABASE_NAME_HSQLDB_HYPER_SQL, "images/datastore-types/databases/hsqldb.png", "org.hsqldb.jdbcDriver",
				"http://repo1.maven.org/maven2/hsqldb/hsqldb/1.8.0.10/hsqldb-1.8.0.10.jar",
				"jdbc:hsqldb:hsql://<hostname>:9001/<database>", "jdbc:hsqldb:file:<path/to/database>");
		add(DATABASE_NAME_H2, "images/datastore-types/databases/h2.png", "org.h2.Driver",
				"http://repo1.maven.org/maven2/com/h2database/h2/1.2.145/h2-1.2.145.jar", "jdbc:h2:<path/to/database>");
		add(DATABASE_NAME_TERADATA, "images/datastore-types/databases/teradata.png", "com.teradata.jdbc.TeraDriver", null,
				"jdbc:teradata:<hostname>", "jdbc:teradata:<hostname>/database=<database>");
		add(DATABASE_NAME_JDBC_ODBC_BRIDGE, "images/datastore-types/databases/odbc.png", "sun.jdbc.odbc.JdbcOdbcDriver",
				null, "jdbc:odbc:<data-source-name>");

		Collections.sort(_databaseDrivers);
	}

	public List<DatabaseDriverDescriptor> getDatabaseDrivers() {
		return _databaseDrivers;
	}

	public List<DatabaseDriverDescriptor> getInstalledWorkingDatabaseDrivers() {
		return CollectionUtils.filter(_databaseDrivers, new Function<DatabaseDriverDescriptor, Boolean>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Boolean run(DatabaseDriverDescriptor input) throws Exception {
				if (getState(input) == DatabaseDriverState.INSTALLED_WORKING) {
					return true;
				}
				return false;
			}
		});
	}

	private void add(String databaseName, String iconImagePath, String driverClassName, String[] downloadUrls,
			String[] urlTemplates) {
		_databaseDrivers.add(new DatabaseDescriptorImpl(databaseName, iconImagePath, driverClassName, downloadUrls,
				urlTemplates));
	}

	private void add(String databaseName, String iconImagePath, String driverClassName, String downloadUrl,
			String... urlTemplates) {
		String[] urls;
		if (downloadUrl == null) {
			urls = null;
		} else {
			urls = new String[] { downloadUrl };
		}
		_databaseDrivers.add(new DatabaseDescriptorImpl(databaseName, iconImagePath, driverClassName, urls, urlTemplates));
	}

	public DatabaseDriverState getState(DatabaseDriverDescriptor databaseDescriptor) {
		String driverClassName = databaseDescriptor.getDriverClassName();
		List<UserDatabaseDriver> drivers = UserPreferences.getInstance().getDatabaseDrivers();
		for (UserDatabaseDriver userDatabaseDriver : drivers) {
			if (userDatabaseDriver.getDriverClassName().equals(driverClassName)) {
				return userDatabaseDriver.getState();
			}
		}
		try {
			Class.forName(driverClassName);
			return DatabaseDriverState.INSTALLED_WORKING;
		} catch (ClassNotFoundException e) {
			return DatabaseDriverState.NOT_INSTALLED;
		} catch (Exception e) {
			return DatabaseDriverState.INSTALLED_NOT_WORKING;
		}
	}

	public String getIconImagePath(DatabaseDriverDescriptor dd) {
		String iconImagePath = null;
		if (dd != null) {
			iconImagePath = dd.getIconImagePath();
		}
		if (iconImagePath == null) {
			iconImagePath = "images/model/datastore.png";
		}
		return iconImagePath;
	}

	public DatabaseDriverDescriptor getDatabaseDriverByDriverDatabaseName(String databaseName) {
		if (databaseName == null) {
			return null;
		}
		for (DatabaseDriverDescriptor databaseDriver : _databaseDrivers) {
			if (databaseName.equals(databaseDriver.getDisplayName())) {
				return databaseDriver;
			}
		}
		return null;
	}

	public DatabaseDriverDescriptor getDatabaseDriverByDriverClassName(String driverClass) {
		if (driverClass == null) {
			return null;
		}
		for (DatabaseDriverDescriptor databaseDriver : _databaseDrivers) {
			if (driverClass.equals(databaseDriver.getDriverClassName())) {
				return databaseDriver;
			}
		}
		return null;
	}

	public boolean isInstalled(String databaseName) {
		DatabaseDriverDescriptor databaseDriver = getDatabaseDriverByDriverDatabaseName(databaseName);
		if (databaseDriver == null) {
			return false;
		}
		return getState(databaseDriver) == DatabaseDriverState.INSTALLED_WORKING;
	}
}
