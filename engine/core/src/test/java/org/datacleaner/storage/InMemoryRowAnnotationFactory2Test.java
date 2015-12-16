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
package org.datacleaner.storage;

import java.util.concurrent.atomic.AtomicInteger;

import org.datacleaner.data.MockInputRow;

import junit.framework.TestCase;

public class InMemoryRowAnnotationFactory2Test extends TestCase {

    public void testRunningOutOfStorage() throws Exception {
        final int maxRecords = 5;
        final int maxSets = 2;
        final InMemoryRowAnnotationFactory2 f = new InMemoryRowAnnotationFactory2(maxSets, maxRecords);

        final AtomicInteger idCounter = new AtomicInteger();

        {
            RowAnnotation a1 = f.createAnnotation();
            assertNotNull(a1);
            assertFalse(f.hasSampleRows(a1));

            f.annotate(new MockInputRow(idCounter.incrementAndGet()), a1);
            f.annotate(new MockInputRow(idCounter.incrementAndGet()), a1);
            f.annotate(new MockInputRow(idCounter.incrementAndGet()), a1);
            assertTrue(f.hasSampleRows(a1));
            assertEquals(3, f.getSampleRows(a1).size());

            f.annotate(new MockInputRow(idCounter.incrementAndGet()), a1);
            f.annotate(new MockInputRow(idCounter.incrementAndGet()), a1);
            f.annotate(new MockInputRow(idCounter.incrementAndGet()), a1);
            assertTrue(f.hasSampleRows(a1));
            assertEquals(5, f.getSampleRows(a1).size());
        }

        {
            RowAnnotation a2 = f.createAnnotation();
            assertNotNull(a2);
            assertFalse(f.hasSampleRows(a2));

            f.annotate(new MockInputRow(idCounter.incrementAndGet()), a2);
            f.annotate(new MockInputRow(idCounter.incrementAndGet()), a2);
            f.annotate(new MockInputRow(idCounter.incrementAndGet()), a2);
            assertTrue(f.hasSampleRows(a2));
            assertEquals(3, f.getSampleRows(a2).size());

            f.annotate(new MockInputRow(idCounter.incrementAndGet()), a2);
            f.annotate(new MockInputRow(idCounter.incrementAndGet()), a2);
            f.annotate(new MockInputRow(idCounter.incrementAndGet()), a2);
            assertTrue(f.hasSampleRows(a2));
            assertEquals(5, f.getSampleRows(a2).size());
        }

        {
            RowAnnotation a3 = f.createAnnotation();
            assertNotNull(a3);

            assertFalse(f.hasSampleRows(a3));

            f.annotate(new MockInputRow(idCounter.incrementAndGet()), a3);
            f.annotate(new MockInputRow(idCounter.incrementAndGet()), a3);
            f.annotate(new MockInputRow(idCounter.incrementAndGet()), a3);
            assertFalse(f.hasSampleRows(a3));
            assertEquals(0, f.getSampleRows(a3).size());
        }
    }
}
