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

import java.util.List;

import org.apache.commons.lang.SerializationUtils;

import junit.framework.TestCase;

public class XmlDatastoreTest extends TestCase {

    private XmlDatastore ds = new XmlDatastore("foobar", "src/test/resources/example-xml-file.xml");

    public void testGetters() throws Exception {
        assertEquals("foobar", ds.getName());
        assertEquals("src/test/resources/example-xml-file.xml", ds.getFilename());
    }

    public void testGetDatastoreConnection() throws Exception {
        try (DatastoreConnection con = ds.openConnection()) {
            final List<String> tableNames = con.getDataContext().getDefaultSchema().getTableNames();
            assertEquals("[greeting]", tableNames.toString());
        }
    }

    public void testGetPerformanceCharacteristics() throws Exception {
        assertEquals(false, ds.getPerformanceCharacteristics().isQueryOptimizationPreferred());
    }

    public void testCloneAndEquals() throws Exception {
        final Object clone = SerializationUtils.clone(ds);
        assertEquals(ds, clone);
    }
}
