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

import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.FileResource;

import junit.framework.TestCase;

public class FixedWidthDatastoreTest extends TestCase {

    public void testSimpleGetters() throws Exception {
        final FixedWidthDatastore ds = new FixedWidthDatastore("name", "filename", "encoding", 5);
        assertEquals("name", ds.getName());
        assertEquals("filename", ds.getResource().getName());
        assertEquals("encoding", ds.getEncoding());
        assertEquals(5, ds.getFixedValueWidth());

        assertFalse(ds.getPerformanceCharacteristics().isQueryOptimizationPreferred());
    }

    public void testToString() throws Exception {
        final FixedWidthDatastore ds = new FixedWidthDatastore("name", "filename", "encoding", 5);
        assertEquals( "FixedWidthDatastore[name=name, filename=filename, encoding=encoding, headerLineNumber=1, "
                + "valueWidths=[], fixedValueWidth=5]", ds.toString());
    }

    public void testGetDatastoreConnection() throws Exception {
        final FixedWidthDatastore ds = new FixedWidthDatastore("example datastore",
                "src/test/resources/employees-fixed-width.txt", "UTF-8", 19, false, false, false);

        try (DatastoreConnection con = ds.openConnection()) {
            final Schema schema = con.getDataContext().getDefaultSchema();
            assertEquals("resources", schema.getName());

            final Table table = schema.getTable(0);
            assertEquals("employees-fixed-width.txt", table.getName());

            assertEquals("[name, email]", table.getColumnNames().toString());
        }
    }

    public void testResource() throws Exception {
        final FileResource resource = new FileResource("mytest.txt");
        final FixedWidthDatastore fixedWidthDatastore =
                new FixedWidthDatastore("name", resource, resource.getName(), "UTF-8", 20, false, false, false, 0);
        assertEquals( "FixedWidthDatastore[name=name, filename=mytest.txt, encoding=UTF-8, headerLineNumber=0, "
                + "valueWidths=[], fixedValueWidth=20]", fixedWidthDatastore.toString());
    }
}
