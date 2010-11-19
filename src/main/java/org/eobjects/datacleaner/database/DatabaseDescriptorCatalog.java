package org.eobjects.datacleaner.database;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DatabaseDescriptorCatalog implements Serializable {

	private static final long serialVersionUID = 1L;

	private final List<DatabaseDriverDescriptor> _descriptors = new ArrayList<DatabaseDriverDescriptor>();

	public DatabaseDescriptorCatalog() {
		add("MySQL",
				null,
				"com.mysql.jdbc.Driver",
				"http://mirrors.ibiblio.org/pub/mirrors/maven2/mysql/mysql-connector-java/5.1.13/mysql-connector-java-5.1.13.jar",
				"jdbc:mysql://<hostname>:3306/<database>");
		add("DB2", null, "com.ibm.db2.jcc.DB2Driver", null, "jdbc:db2://<hostname>:<port>/<database>",
				"jdbc:db2j:net://<hostname>:<port>/<database>");
		add("Ingres", null, "com.ingres.jdbc.IngresDriver",
				"http://mirrors.ibiblio.org/pub/mirrors/maven2/com/ingres/jdbc/iijdbc/9.3-3.8.2/iijdbc-9.3-3.8.2.jar",
				"jdbc:ingres://<hostname>:II7/<database>");
		add("Firebird",
				null,
				"org.firebirdsql.jdbc.FBDriver",
				// firebird's driver also depends on the j2ee spec
				new String[] {
						"http://mirrors.ibiblio.org/pub/mirrors/maven2/org/firebirdsql/jdbc/jaybird/2.1.6/jaybird-2.1.6.jar",
						"http://mirrors.ibiblio.org/pub/mirrors/maven2/geronimo-spec/geronimo-spec-j2ee/1.4-rc4/geronimo-spec-j2ee-1.4-rc4.jar" },
				new String[] { "jdbc:firebirdsql:<hostname>:<path/to/database>.fdb" });
		add("SAP DB", null, "com.sap.dbtech.jdbc.DriverSapDB", null, "jdbc:sapdb://<hostname>/<database>");
		add("PostgreSQL",
				null,
				"org.postgresql.Driver",
				"http://mirrors.ibiblio.org/pub/mirrors/maven2/postgresql/postgresql/8.4-702.jdbc4/postgresql-8.4-702.jdbc4.jar",
				"jdbc:postgresql://<hostname>:5432/<database>");
		add("JTDS (Microsoft SQL Server & Sybase)", null, "net.sourceforge.jtds.jdbc.Driver",
				"http://mirrors.ibiblio.org/pub/mirrors/maven2/net/sourceforge/jtds/jtds/1.2.4/jtds-1.2.4.jar",
				"jdbc:jtds:sqlserver://<hostname>:1434;useUnicode=true;characterEncoding=UTF-8",
				"jdbc:jtds:sqlserver://<hostname>:1434/<database>;useUnicode=true;characterEncoding=UTF-8",
				"jdbc:jtds:sybase://<hostname>:7100/<database>");
		add("SQLite", null, "org.sqlite.JDBC",
				"http://mirrors.ibiblio.org/pub/mirrors/maven2/org/xerial/sqlite-jdbc/3.6.20/sqlite-jdbc-3.6.20.jar",
				"jdbc:sqlite:<path/to/database>.db");
		add("Apache Derby (client)",
				null,
				"org.apache.derby.jdbc.ClientDriver",
				"http://mirrors.ibiblio.org/pub/mirrors/maven2/org/apache/derby/derbyclient/10.6.2.1/derbyclient-10.6.2.1.jar",
				"jdbc:derby://<hostname>:1527/<path/to/database>");
		add("Apache Derby (embedded)", null, "org.apache.derby.jdbc.EmbeddedDriver",
				"http://mirrors.ibiblio.org/pub/mirrors/maven2/org/apache/derby/derby/10.6.2.1/derby-10.6.2.1.jar",
				"jdbc:derby:<database>");
		add("Oracle database", null, "oracle.jdbc.OracleDriver", null, "jdbc:oracle:thin:@<hostname>:1521:<schema>");
		add("Microsoft SQL Server", null, "com.microsoft.sqlserver.jdbc.SQLServerDriver", null,
				"jdbc:sqlserver://<hostname>:3341;databaseName=<database>",
				"jdbc:sqlserver://<hostname>:3341;databaseName=<database>;integratedSecurity=true");
		add("Hsqldb/HyperSQL", null, "org.hsqldb.jdbcDriver",
				"http://mirrors.ibiblio.org/pub/mirrors/maven2/hsqldb/hsqldb/1.8.0.10/hsqldb-1.8.0.10.jar",
				"jdbc:hsqldb:hsql://<hostname>:9001/<database>", "jdbc:hsqldb:file:<path/to/database>");
		add("H2", null, "org.h2.Driver",
				"http://mirrors.ibiblio.org/pub/mirrors/maven2/com/h2database/h2/1.2.145/h2-1.2.145.jar",
				"jdbc:h2:<path/to/database>");
		add("Teradata", null, "com.teradata.jdbc.TeraDriver", null, "jdbc:teradata:<hostname>",
				"jdbc:teradata:<hostname>/database=<database>");
		add("JDBC-ODBC bridge", null, "sun.jdbc.odbc.JdbcOdbcDriver", null, "jdbc:odbc:<data-source-name>");

		Collections.sort(_descriptors);
	}

	public List<DatabaseDriverDescriptor> getDescriptors() {
		return _descriptors;
	}

	private void add(String databaseName, String iconImagePath, String driverClassName, String[] downloadUrls,
			String[] urlTemplates) {
		_descriptors
				.add(new DatabaseDescriptorImpl(databaseName, iconImagePath, driverClassName, downloadUrls, urlTemplates));
	}

	private void add(String databaseName, String iconImagePath, String driverClassName, String downloadUrl,
			String... urlTemplates) {
		String[] urls;
		if (downloadUrl == null) {
			urls = null;
		} else {
			urls = new String[] { downloadUrl };
		}
		_descriptors.add(new DatabaseDescriptorImpl(databaseName, iconImagePath, driverClassName, urls, urlTemplates));
	}
}
