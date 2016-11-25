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
 * {@link DatabaseConnectionPresenter} for MS SQL Server database connections
 */
public class SQLServerDatabaseConnectionPresenter extends UrlTemplateDatabaseConnectionPresenter {

    public SQLServerDatabaseConnectionPresenter() {
        super("jdbc:jtds:sqlserver://HOSTNAME:PORT/DATABASE;instance=PARAM1;useUnicode=true;characterEncoding=UTF-8",
                "jdbc:jtds:sqlserver://HOSTNAME:PORT/DATABASE;useUnicode=true;characterEncoding=UTF-8");
    }

    @Override
    protected String getJdbcUrl(final String hostname, final int port, final String database, final String param1,
            final String param2, final String param3, final String param4) {
        if (StringUtils.isNullOrEmpty(param1)) {
            return "jdbc:jtds:sqlserver://" + hostname + ":" + port + "/" + database
                    + ";useUnicode=true;characterEncoding=UTF-8";
        }
        return "jdbc:jtds:sqlserver://" + hostname + ":" + port + "/" + database + ";instance=" + param1
                + ";useUnicode=true;characterEncoding=UTF-8";
    }

    @Override
    protected int getDefaultPort() {
        return 1433;
    }

    @Override
    protected String getLabelForParam1() {
        return "Instance (optional)";
    }

}
