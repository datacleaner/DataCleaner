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
package org.datacleaner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class VersionComparatorTest {
    
    @Test
    public void testIsComparable() {
        final VersionComparator comparator = new VersionComparator();
        assertTrue(comparator.isComparable("1.2.3"));
        assertTrue(comparator.isComparable("423423.43252.3432"));
        assertTrue(comparator.isComparable("1.2"));
        assertTrue(comparator.isComparable("1.2.3-FOO"));
        assertTrue(comparator.isComparable("311.242-FOO"));
        assertTrue(comparator.isComparable("5.2-RC1"));
        
        assertFalse(comparator.isComparable("UNKNOWN"));
        assertFalse(comparator.isComparable("a.b.c"));
        assertFalse(comparator.isComparable("5.2RC1"));
    }

    @Test
    public void testSameMajors() {
        final VersionComparator comparator = new VersionComparator();

        final List<String> versions = Arrays.asList("4.0.2", "4.0.4", "4.0.6");

        final String max = Collections.max(versions, comparator);
        assertEquals("4.0.6", max);
    }

    @Test
    public void testDifferentMajors() {
        final VersionComparator comparator = new VersionComparator();

        final List<String> versions = Arrays.asList("3.7.2", "4.0.4", "4.0.6", "5.0");

        final String max = Collections.max(versions, comparator);
        assertEquals("5.0", max);
    }

    @Test
    public void testSnapshotAndRelease() {
        final VersionComparator comparator = new VersionComparator();

        List<String> versions = Arrays.asList("4.1", "4.1-SNAPSHOT");

        String max = Collections.max(versions, comparator);
        assertEquals("4.1", max);

        // Reversed order
        versions = Arrays.asList("4.1-SNAPSHOT", "4.1");

        max = Collections.max(versions, comparator);
        assertEquals("4.1", max);
    }

    @Test
    public void testRC() {
        final VersionComparator comparator = new VersionComparator();

        final List<String> versions1 =
                Arrays.asList("5.0-RC1", "5.0", "5.0.1.5", "4.0", "5.0-SNAPSHOT", "4.0.1-SNAPSHOT", "5.0.1-beta",
                        "5.0.1-alfa", "5.0.1-RC2", "5.0.0-SNAPSHOT", "5.0.1-SNAPSHOT", "5.0.1-RC1");
        final List<String> versions2 = new ArrayList<>(versions1);

        Collections.sort(versions1, comparator);
        assertEquals("[4.0, 4.0.1-SNAPSHOT, 5.0-RC1, 5.0-SNAPSHOT, 5.0, 5.0.0-SNAPSHOT, 5.0.1-alfa, 5.0.1-beta, "
                + "5.0.1-RC1, 5.0.1-RC2, 5.0.1-SNAPSHOT, 5.0.1.5]", versions1.toString());

        // Reversed order
        Collections.reverse(versions2);

        Collections.sort(versions2, comparator);
        assertEquals("[4.0, 4.0.1-SNAPSHOT, 5.0-RC1, 5.0-SNAPSHOT, 5.0, 5.0.0-SNAPSHOT, 5.0.1-alfa, "
                + "5.0.1-beta, 5.0.1-RC1, 5.0.1-RC2, 5.0.1-SNAPSHOT, 5.0.1.5]", versions2.toString());
    }

}
