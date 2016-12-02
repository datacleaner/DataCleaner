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
package org.datacleaner.beans.valuedist;

import org.datacleaner.result.SingleValueFrequency;
import org.datacleaner.result.ValueFrequency;

import junit.framework.TestCase;

public class ValueCountTest extends TestCase {

    public void testEqualsAndHashCode() throws Exception {
        final ValueFrequency vc1 = new SingleValueFrequency("foo", 1337);
        final ValueFrequency vc2 = new SingleValueFrequency("foo", 1337);
        final ValueFrequency vc3 = new SingleValueFrequency(null, 1337);
        final ValueFrequency vc4 = new SingleValueFrequency("foo", 1338);

        assertEquals(vc1, vc2);
        assertEquals(vc1.hashCode(), vc2.hashCode());
        assertTrue(!vc1.equals(vc3));
        assertTrue(!vc1.equals(vc4));
    }
}
