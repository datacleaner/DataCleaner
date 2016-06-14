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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class VersionComparatorTest {

    @Test
    public void testSameMajors() {
        VersionComparator comparator = new VersionComparator();
        
        List<String> versions = Arrays.asList("4.0.2", "4.0.4", "4.0.6");
        
        String max = Collections.max(versions, comparator);
        assertEquals("4.0.6", max);
    }
    
    @Test
    public void testDifferentMajors() {
        VersionComparator comparator = new VersionComparator();
        
        List<String> versions = Arrays.asList("3.7.2", "4.0.4", "4.0.6", "5.0");
        
        String max = Collections.max(versions, comparator);
        assertEquals("5.0", max);
    }
    
    @Test
    public void testSnapshotAndRelease() {
        VersionComparator comparator = new VersionComparator();
        
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
        VersionComparator comparator = new VersionComparator();

        List<String> versions1 = Arrays.asList("5.0-RC1", "5.0", "5.0.1.5","4.0", "5.0-SNAPSHOT", "4.0.1-SNAPSHOT","5.0.1-beta","5.0.1-alfa", "5.0.1-RC2", "5.0.0-SNAPSHOT", "5.0.1-SNAPSHOT", "5.0.1-RC1");
        List<String> versions2 = new ArrayList<>(versions1);

        Collections.sort(versions1, comparator);
        assertEquals("[4.0, 4.0.1-SNAPSHOT, 5.0-RC1, 5.0-SNAPSHOT, 5.0, 5.0.0-SNAPSHOT, 5.0.1-alfa, 5.0.1-beta, 5.0.1-RC1, 5.0.1-RC2, 5.0.1-SNAPSHOT, 5.0.1.5]", versions1.toString());

        // Reversed order
        Collections.reverse(versions2);

        Collections.sort(versions2, comparator);
        assertEquals("[4.0, 4.0.1-SNAPSHOT, 5.0-RC1, 5.0-SNAPSHOT, 5.0, 5.0.0-SNAPSHOT, 5.0.1-alfa, 5.0.1-beta, 5.0.1-RC1, 5.0.1-RC2, 5.0.1-SNAPSHOT, 5.0.1.5]", versions2.toString());
    }

}
