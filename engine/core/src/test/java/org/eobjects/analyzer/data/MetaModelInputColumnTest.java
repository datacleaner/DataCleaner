/**
 * AnalyzerBeans
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
package org.eobjects.analyzer.data;

import junit.framework.TestCase;

import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.MutableColumn;

public class MetaModelInputColumnTest extends TestCase {

    public void testConstructorArgRequired() throws Exception {
        try {
            new MetaModelInputColumn(null);
            fail("Exception expected");
        } catch (IllegalArgumentException e) {
            assertEquals("column cannot be null", e.getMessage());
        }
    }

    public void testGetDataTypeWhenClob() throws Exception {
        assertEquals(String.class,
                new MetaModelInputColumn(new MutableColumn("foobar", ColumnType.VARCHAR)).getDataType());
        assertEquals(String.class, new MetaModelInputColumn(new MutableColumn("foobar", ColumnType.CLOB)).getDataType());
        assertEquals(byte[].class, new MetaModelInputColumn(new MutableColumn("foobar", ColumnType.BLOB)).getDataType());
    }
}
