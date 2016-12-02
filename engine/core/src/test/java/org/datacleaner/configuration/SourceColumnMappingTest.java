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
package org.datacleaner.configuration;

import org.apache.metamodel.schema.MutableColumn;
import org.datacleaner.connection.Datastore;
import org.datacleaner.test.TestHelper;
import org.datacleaner.test.mock.MockDatastore;

import junit.framework.TestCase;

public class SourceColumnMappingTest extends TestCase {

    public void testIsSatisfied() throws Exception {
        final SourceColumnMapping columnMapping = new SourceColumnMapping("foo.bar.col1", "foo.bar.col2");
        assertFalse(columnMapping.isSatisfied());

        columnMapping.setDatastore(new MockDatastore());
        assertFalse(columnMapping.isSatisfied());

        columnMapping.setColumn("foo.bar.col1", new MutableColumn("col1"));
        assertFalse(columnMapping.isSatisfied());

        columnMapping.setColumn("foo.bar.col2", new MutableColumn("col2"));
        assertTrue(columnMapping.isSatisfied());


    }

    public void testAutoMapAllMatches() throws Exception {
        final Datastore datastore = TestHelper.createSampleDatabaseDatastore("testdb");

        final SourceColumnMapping columnMapping =
                new SourceColumnMapping("PUBLIC.EMPLOYEES.FIRSTNAME", "PUBLIC.EMPLOYEES.LASTNAME");
        assertFalse(columnMapping.isSatisfied());
        columnMapping.autoMap(datastore);

        assertTrue(columnMapping.isSatisfied());
    }

    public void testAutoMapPartialMatches() throws Exception {
        final Datastore datastore = TestHelper.createSampleDatabaseDatastore("testdb");

        final SourceColumnMapping columnMapping =
                new SourceColumnMapping("PUBLIC.EMPLOYEES.FIRSTNAME", "foo.bar.col1", "PUBLIC.EMPLOYEES.LASTNAME",
                        "foo.bar.col2");

        columnMapping.setColumn("foo.bar.col1", new MutableColumn("col1"));
        assertFalse(columnMapping.isSatisfied());
        assertEquals("[PUBLIC.EMPLOYEES.FIRSTNAME, PUBLIC.EMPLOYEES.LASTNAME, foo.bar.col2]",
                columnMapping.getUnmappedPaths().toString());

        columnMapping.autoMap(datastore);
        assertFalse(columnMapping.isSatisfied());
        assertEquals("[foo.bar.col2]", columnMapping.getUnmappedPaths().toString());

        columnMapping.setColumn("foo.bar.col2", new MutableColumn("col2"));
        assertTrue(columnMapping.isSatisfied());
    }
}
