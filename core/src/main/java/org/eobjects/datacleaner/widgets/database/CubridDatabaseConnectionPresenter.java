package org.eobjects.datacleaner.widgets.database;

public class CubridDatabaseConnectionPresenter extends UrlTemplateDatabaseConnectionPresenter {

	public CubridDatabaseConnectionPresenter() {
		super("jdbc:cubrid:HOSTNAME:PORT:DATABASE:::");
	}

	@Override
	protected int getDefaultPort() {
		return 30000;
	}

	@Override
	protected String getJdbcUrl(String hostname, int port, String database, String param1, String param2,
			String param3, String param4) {
		return "jdbc:cubrid:" + hostname + ":" + port + ":" + database + ":::";
	}

}
