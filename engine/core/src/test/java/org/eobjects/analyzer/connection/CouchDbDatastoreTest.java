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

import junit.framework.TestCase;

public class CouchDbDatastoreTest extends TestCase {

    public void testCreateAndCompare() throws Exception {
        CouchDbDatastore ds1 = new CouchDbDatastore("couch", "localhost", CouchDbDatastore.DEFAULT_PORT, "user", "pw", true, null);
        CouchDbDatastore ds2 = new CouchDbDatastore("couch", "localhost", null, "user", "pw", true, null);

        assertEquals(ds1, ds2);

        assertEquals("localhost", ds1.getHostname());
        assertEquals(CouchDbDatastore.DEFAULT_PORT, ds1.getPort());
        assertEquals(true, ds1.isSslEnabled());
        assertEquals("couch", ds1.getName());
        assertEquals("user", ds1.getUsername());
        assertEquals("pw", ds1.getPassword());
        assertEquals(null, ds1.getTableDefs());
        
        assertTrue(ds1.getPerformanceCharacteristics().isQueryOptimizationPreferred());
    }
}
