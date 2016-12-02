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

public class H2DatabaseConnectionPresenter extends UrlTemplateDatabaseConnectionPresenter {

    public H2DatabaseConnectionPresenter() {
        super("jdbc:h2:PARAM1");
    }

    @Override
    protected int getDefaultPort() {
        // no port involved
        return 0;
    }

    @Override
    protected String getLabelForParam1() {
        return "Path to database";
    }

    @Override
    protected String getJdbcUrl(final String hostname, final int port, final String database, final String param1,
            final String param2, final String param3, final String param4) {
        return "jdbc:h2:" + param1;
    }

}
