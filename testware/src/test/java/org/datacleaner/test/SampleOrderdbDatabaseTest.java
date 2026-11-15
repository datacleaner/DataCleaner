/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Free Software Foundation, Inc.
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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import junit.framework.TestCase;

public class SampleOrderdbDatabaseTest extends TestCase {

    private static final String URL = "jdbc:hsqldb:res:orderdb;readonly=true";

    public void testOpenSampleDatabaseWithCurrentDriver() throws Exception {
        assertEquals(2, getMajorVersion());

        try (Connection connection = DriverManager.getConnection(URL, "SA", "")) {
            try (Statement statement = connection.createStatement()) {
                assertCount(statement, "CUSTOMERS", 214);
                assertCount(statement, "EMPLOYEES", 23);
                assertCount(statement, "OFFICES", 7);
                assertCount(statement, "ORDERDETAILS", 2996);
                assertCount(statement, "ORDERFACT", 2996);
                assertCount(statement, "ORDERS", 326);
                assertCount(statement, "PAYMENTS", 272);
                assertCount(statement, "PRODUCTS", 110);
            }
        }
    }

    private int getMajorVersion() throws Exception {
        try (Connection connection = DriverManager.getConnection(URL, "SA", "")) {
            return connection.getMetaData().getDatabaseMajorVersion();
        }
    }

    private void assertCount(Statement statement, String table, int expected) throws Exception {
        try (ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM " + table)) {
            assertTrue(resultSet.next());
            assertEquals("Unexpected row count in table " + table, expected, resultSet.getInt(1));
        }
    }
}
