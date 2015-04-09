/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Neopost - Customer Information Management
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
package org.datacleaner.widgets.database;

import org.datacleaner.util.StringUtils;

/**
 * {@link DatabaseConnectionPresenter} for Oracle database connections.
 * 
 * Some special formatting conventions used in the URL:
 * 
 * DATABASE = Service name
 * 
 * PARAM1 = SID
 * 
 * PARAM2 = Server name
 * 
 * PARAM3 = Instance name
 */
public class OracleDatabaseConnectionPresenter extends UrlTemplateDatabaseConnectionPresenter {

    private static final String URL_SIMPLE1 = "jdbc:oracle:thin:@HOSTNAME:PORT";
    private static final String URL_SIMPLE2 = "jdbc:oracle:thin:@HOSTNAME:PORT/DATABASE";
    private static final String URL_SID_BASED = "jdbc:oracle:thin:@HOSTNAME:PORT:PARAM1";
    private static final String URL_FULL = "jdbc:oracle:thin:@HOSTNAME:PORT/DATABASE:PARAM2/PARAM3";

    public OracleDatabaseConnectionPresenter() {
        super(URL_SIMPLE1, URL_SIMPLE2, URL_SID_BASED, URL_FULL);
    }

    @Override
    protected String getJdbcUrl(String hostname, int port, String database, String param1, String param2,
            String param3, String param4) {
        if (!StringUtils.isNullOrEmpty(param1)) {
            return replaceParameters(URL_SID_BASED, hostname, port, database, param1, param2, param3);
        }
        if (!StringUtils.isNullOrEmpty(param2) || !StringUtils.isNullOrEmpty(param3)) {
            return replaceParameters(URL_FULL, hostname, port, database, param1, param2, param3);
        }
        if (!StringUtils.isNullOrEmpty(database)) {
            return replaceParameters(URL_SIMPLE2, hostname, port, database, param1, param2, param3);
        }
        return replaceParameters(URL_SIMPLE1, hostname, port, database, param1, param2, param3);
    }

    private String replaceParameters(String url, String hostname, int port, String database, String param1,
            String param2, String param3) {
        url = url.replace("HOSTNAME", hostname);
        url = url.replace("PORT", Integer.toString(port));
        url = url.replace("DATABASE", database);
        url = url.replace("PARAM1", param1);
        url = url.replace("PARAM2", param2);
        url = url.replace("PARAM3", param3);
        return url;
    }

    @Override
    protected int getDefaultPort() {
        return 1521;
    }
    
    @Override
    protected boolean showDatabaseAboveCredentials() {
        return false;
    }

    @Override
    protected String getLabelForDatabase() {
        return "Service name";
    }

    @Override
    protected String getLabelForParam1() {
        return "System ID (SID)";
    }

    @Override
    protected String getLabelForParam2() {
        return "Server name";
    }

    @Override
    protected String getLabelForParam3() {
        return "Instance name";
    }
}
