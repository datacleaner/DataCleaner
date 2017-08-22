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
package org.datacleaner.test;

import java.util.List;

import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreConnection;

import junit.framework.TestCase;

public class TestHelperTest extends TestCase {

    public void testCreateSampleDatabaseDatastore() throws Exception {
        final Datastore ds = TestHelper.createSampleDatabaseDatastore("foo");
        assertEquals("foo", ds.getName());

        final DatastoreConnection con = ds.openConnection();
        final List<String> tableNames = con.getSchemaNavigator().getDefaultSchema().getTableNames();
        assertEquals("[CUSTOMERS, EMPLOYEES, OFFICES, ORDERDETAILS, ORDERFACT, ORDERS, PAYMENTS, PRODUCTS]",
                tableNames.toString());

        con.close();
    }
}
