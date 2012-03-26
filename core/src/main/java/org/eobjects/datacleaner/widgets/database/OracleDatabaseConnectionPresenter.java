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
package org.eobjects.datacleaner.widgets.database;

/**
 * {@link DatabaseConnectionPresenter} for Oracle database connections
 */
public class OracleDatabaseConnectionPresenter extends UrlTemplateDatabaseConnectionPresenter implements
		DatabaseConnectionPresenter {

	public OracleDatabaseConnectionPresenter() {
		super("jdbc:oracle:thin:@HOSTNAME:PORT:DATABASE");
	}

	@Override
	protected String getJdbcUrl(String hostname, int port, String database, String param1, String param2,
			String param3, String param4) {
		return "jdbc:oracle:thin:@" + hostname + ":" + port + ":" + database;
	}

	@Override
	protected int getDefaultPort() {
		return 1521;
	}

	@Override
	protected String getLabelForDatabase() {
		return "System ID (SID)";
	}

}
