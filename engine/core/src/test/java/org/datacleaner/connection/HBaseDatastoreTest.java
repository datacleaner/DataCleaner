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
package org.datacleaner.connection;

import junit.framework.TestCase;

public class HBaseDatastoreTest extends TestCase {

    public void testGetters() throws Exception {
        HBaseDatastore ds1 = new HBaseDatastore("hbase1", "localhost", 1234);
        assertEquals("hbase1", ds1.getName());
        assertEquals("localhost", ds1.getZookeeperHostname());
        assertEquals(1234, ds1.getZookeeperPort());
        assertEquals(null, ds1.getTableDefs());
        
        PerformanceCharacteristics performanceCharacteristics = ds1.getPerformanceCharacteristics();
        assertEquals(false, performanceCharacteristics.isNaturalRecordOrderConsistent());
        assertEquals(true, performanceCharacteristics.isQueryOptimizationPreferred());
    }
}
