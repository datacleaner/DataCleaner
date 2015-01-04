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
package org.eobjects.analyzer.connection;

import java.util.Arrays;

import junit.framework.TestCase;

public class MongoDbDatastoreTest extends TestCase {

    public void testCreateAndCompare() throws Exception {
        MongoDbDatastore ds1 = new MongoDbDatastore("mongo", "localhost", 27017, "db", "user", "pw");
        MongoDbDatastore ds2 = new MongoDbDatastore("mongo", null, null, "db", "user", "pw");

        assertEquals(ds1, ds2);

        assertEquals("localhost", ds1.getHostname());
        assertEquals(27017, ds1.getPort());
        assertEquals("db", ds1.getDatabaseName());
        assertEquals("mongo", ds1.getName());
        assertEquals("user", ds1.getUsername());
        assertEquals("[p, w]", Arrays.toString(ds1.getPassword()));
        assertEquals(null, ds1.getTableDefs());
        
        assertTrue(ds1.getPerformanceCharacteristics().isQueryOptimizationPreferred());
    }
}
