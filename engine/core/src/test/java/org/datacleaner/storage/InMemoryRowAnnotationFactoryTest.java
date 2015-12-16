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

import junit.framework.TestCase;

import org.datacleaner.data.MockInputRow;

public class InMemoryRowAnnotationFactoryTest extends TestCase {

    public void testCountingAboveThreshold() throws Exception {
        RowAnnotationFactory f = RowAnnotations.getDefaultFactory();
        RowAnnotation a = f.createAnnotation();

        f.annotate(new MockInputRow(), 1, a);
        f.annotate(new MockInputRow(), 1, a);
        f.annotate(new MockInputRow(), 1, a);
        f.annotate(new MockInputRow(), 1, a);

        assertEquals(4, a.getRowCount());

        f.annotate(new MockInputRow(), 1, a);

        assertEquals(5, a.getRowCount());

        f.annotate(new MockInputRow(), 1, a);

        assertEquals(6, a.getRowCount());

        f.annotate(new MockInputRow(), 1, a);

        assertEquals(7, a.getRowCount());
    }
}
