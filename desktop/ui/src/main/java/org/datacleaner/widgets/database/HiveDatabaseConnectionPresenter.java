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

public class HiveDatabaseConnectionPresenter extends UrlTemplateDatabaseConnectionPresenter {

    public HiveDatabaseConnectionPresenter() {
        super("jdbc:hive2://HOSTNAME:PORT/DATABASE");
    }

    @Override
    protected int getDefaultPort() {
        // no port involved
        return 10000;
    }

    @Override
    protected String getJdbcUrl(final String hostname, final int port, final String database, final String param1,
            final String param2, final String param3, final String param4) {
        return "jdbc:hive2://" + hostname + ":" + port + "/" + database;
    }

}
