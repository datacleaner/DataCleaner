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
package org.datacleaner.beans.api;

import org.datacleaner.api.OutputColumns;

import junit.framework.TestCase;

public class OutputColumnsTest extends TestCase {

    public void testConstructWithTypes() throws Exception {
        final OutputColumns oc =
                new OutputColumns(new String[] { "foo", "bar" }, new Class[] { String.class, Number.class });
        assertEquals(2, oc.getColumnCount());
        assertEquals("foo", oc.getColumnName(0));
        assertEquals("bar", oc.getColumnName(1));
        assertEquals(String.class, oc.getColumnType(0));
        assertEquals(Number.class, oc.getColumnType(1));

        assertEquals("OutputColumns[foo, bar]", oc.toString());
    }

    public void testConstructVarArgNames() throws Exception {
        final OutputColumns oc = new OutputColumns(Object.class, "first", "second", "third");
        assertEquals(3, oc.getColumnCount());
        assertEquals("first", oc.getColumnName(0));
        assertEquals("second", oc.getColumnName(1));
        assertEquals("third", oc.getColumnName(2));
        assertEquals(Object.class, oc.getColumnType(0));
        assertEquals(Object.class, oc.getColumnType(1));
        assertEquals(Object.class, oc.getColumnType(2));
    }

    public void testConstructNullTypes() throws Exception {
        final OutputColumns oc = new OutputColumns(new String[] { "foo", "bar" }, null);
        assertEquals(2, oc.getColumnCount());
        assertEquals("foo", oc.getColumnName(0));
        assertEquals("bar", oc.getColumnName(1));
        assertEquals(Object.class, oc.getColumnType(0));
        assertEquals(Object.class, oc.getColumnType(1));
    }

    public void testConstructUnequalLength() throws Exception {
        try {
            new OutputColumns(new String[] { "foo", "bar" }, new Class[] { String.class });
            fail("Exception expected");
        } catch (final Exception e) {
            assertEquals("Column names and column types must have equal length", e.getMessage());
        }
    }

    public void testSetColumnType() throws Exception {
        final OutputColumns oc = new OutputColumns(Object.class, "first", "second", "third");
        oc.setColumnType(1, String.class);
        assertEquals(Object.class, oc.getColumnType(0));
        assertEquals(String.class, oc.getColumnType(1));
        assertEquals(Object.class, oc.getColumnType(2));
    }

    public void testSetColumnName() throws Exception {
        final OutputColumns oc = new OutputColumns(1, String.class);
        oc.setColumnName(0, "foo");
        assertEquals("foo", oc.getColumnName(0));
        assertEquals(String.class, oc.getColumnType(0));
    }
}
