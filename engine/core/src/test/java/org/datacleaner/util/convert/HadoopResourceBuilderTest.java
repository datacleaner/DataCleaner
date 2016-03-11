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
package org.datacleaner.util.convert;

import static org.junit.Assert.*;

import java.util.regex.Matcher;

import org.junit.Test;

public class HadoopResourceBuilderTest {

    @Test
    public void testPatternGroupsVanilla() throws Exception {
        final Matcher matcher = HadoopResourceBuilder.RESOURCE_SCHEME_PATTERN.matcher("hdfs://{myserver}/foo/bar.txt");
        assertTrue(matcher.find());
        assertEquals("hdfs", matcher.group(1));
        assertEquals("myserver", matcher.group(2));
        assertEquals("/foo/bar.txt", matcher.group(3));
    }

    @Test
    public void testPatternGroupsNoScheme() throws Exception {
        final Matcher matcher = HadoopResourceBuilder.RESOURCE_SCHEME_PATTERN.matcher("/foo/bar.txt");
        assertFalse(matcher.find());
    }

    @Test
    public void testPatternGroupsNoServer() throws Exception {
        final Matcher matcher = HadoopResourceBuilder.RESOURCE_SCHEME_PATTERN.matcher("hdfs:///foo/bar.txt");
        assertFalse(matcher.find());
    }

}
