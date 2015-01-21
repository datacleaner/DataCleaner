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

import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;

import org.apache.metamodel.DataContext;
import org.apache.metamodel.util.MutableRef;

public class UsageAwareDatastoreConnectionTest extends TestCase {

    public void testGetUsageCount() throws Exception {
        CsvDatastore ds = new CsvDatastore("foo", "src/test/resources/employees.csv");
        assertFalse(ds.isDatastoreConnectionOpen());

        UpdateableDatastoreConnectionLease connection1 = (UpdateableDatastoreConnectionLease) ds.openConnection();

        try (UsageAwareDatastoreConnection<?> usageAware = (UsageAwareDatastoreConnection<?>) connection1.getDelegate();) {

            assertTrue(ds.isDatastoreConnectionOpen());
            assertEquals(1, usageAware.getUsageCount());

            DatastoreConnection con2 = ds.openConnection();
            assertEquals(2, usageAware.getUsageCount());

            DatastoreConnection con3 = ds.openConnection();
            assertEquals(3, usageAware.getUsageCount());

            con3.close();

            assertTrue(ds.isDatastoreConnectionOpen());
            assertEquals(2, usageAware.getUsageCount());

            // call the same close method twice!
            con2.close();
            con2.close();

            assertTrue(ds.isDatastoreConnectionOpen());
            assertEquals(1, usageAware.getUsageCount());
        }

        assertFalse(ds.isDatastoreConnectionOpen());
    }

    public void testCloseByGarbageCollection() throws Exception {
        CsvDatastore ds = new CsvDatastore("foo", "src/test/resources/employees.csv");
        assertFalse(ds.isDatastoreConnectionOpen());

        DatastoreConnectionLease con1 = (DatastoreConnectionLease) ds.openConnection();
        DatastoreConnectionLease con2 = (DatastoreConnectionLease) ds.openConnection();

        assertTrue(ds.isDatastoreConnectionOpen());
        assertSame(con1.getDelegate(), con2.getDelegate());

        con1 = null;
        con2 = null;

        // invoke GC
        System.gc();
        System.runFinalization();

        assertFalse(ds.isDatastoreConnectionOpen());
    }

    public void testCloseNoRaceConditions() throws Exception {
        final int threadCount = 5000;
        final Thread[] threads = new Thread[threadCount];
        final AtomicInteger raceConditions = new AtomicInteger();

        class TestConnection extends UsageAwareDatastoreConnection<DataContext> {

            public TestConnection() {
                super(null);
            }

            private final AtomicInteger _closeCount = new AtomicInteger();

            @Override
            public SchemaNavigator getSchemaNavigator() {
                throw new UnsupportedOperationException();
            }

            @Override
            public DataContext getDataContext() {
                throw new UnsupportedOperationException();
            }

            @Override
            protected void closeInternal() {
                try {
                    Thread.sleep(14);
                } catch (InterruptedException e) {
                }
                int closeCount = _closeCount.incrementAndGet();
                if (closeCount != 1) {
                    raceConditions.incrementAndGet();
                }
            }
        }

        final AtomicInteger creations = new AtomicInteger();
        final AtomicInteger reuses = new AtomicInteger();
        final MutableRef<TestConnection> conRef = new MutableRef<TestConnection>();

        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread() {
                @Override
                public void run() {
                    TestConnection con = conRef.get();
                    if (con != null && con.requestUsage()) {
                        reuses.incrementAndGet();
                    } else {
                        con = new TestConnection();
                        conRef.set(con);
                        creations.incrementAndGet();
                    }
                    try {
                        Thread.sleep((long) (Math.random() * 10));
                    } catch (InterruptedException e) {
                    }
                    con.close();
                }
            };
        }

        for (int i = 0; i < threads.length; i++) {
            if (raceConditions.get() > 0) {
                break;
            }
            threads[i].start();
        }

        assertTrue(creations.get() > 0);
        assertTrue(creations.get() < threadCount);
        assertTrue(reuses.get() > 0);
        assertEquals("Found " + raceConditions
                + " race conditions! Object creation and close() method is not thread safe!", 0, raceConditions.get());
    }
}
