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
package org.datacleaner.beans.filter;

import junit.framework.TestCase;

import org.apache.metamodel.query.Query;
import org.apache.metamodel.schema.Column;
import org.datacleaner.api.InputColumn;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.data.MetaModelInputColumn;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.test.TestHelper;

public class EqualsFilterTest extends TestCase {

    private Datastore datastore = TestHelper.createSampleDatabaseDatastore("ds");

    public void testCompareToEnum() throws Exception {
        EqualsFilter f = new EqualsFilter(new String[] { "EQUALS" }, new MockInputColumn<String>("col", String.class));
        assertEquals(EqualsFilter.Category.EQUALS, f.filter(EqualsFilter.Category.EQUALS));
        assertEquals(EqualsFilter.Category.NOT_EQUALS, f.filter(EqualsFilter.Category.NOT_EQUALS));
    }

    public void testSingleString() throws Exception {
        EqualsFilter f = new EqualsFilter(new String[] { "hello" }, new MockInputColumn<String>("col", String.class));
        assertEquals(EqualsFilter.Category.EQUALS, f.filter("hello"));
        assertEquals(EqualsFilter.Category.NOT_EQUALS, f.filter("Hello"));
        assertEquals(EqualsFilter.Category.NOT_EQUALS, f.filter(""));
        assertEquals(EqualsFilter.Category.NOT_EQUALS, f.filter(null));
    }

    public void testSingleNumber() throws Exception {
        EqualsFilter f = new EqualsFilter(new String[] { "1234" }, new MockInputColumn<Number>("col", Number.class));
        assertEquals(EqualsFilter.Category.EQUALS, f.filter(1234));
        assertEquals(EqualsFilter.Category.EQUALS, f.filter(1234.0));
        assertEquals(EqualsFilter.Category.NOT_EQUALS, f.filter(2));
        assertEquals(EqualsFilter.Category.NOT_EQUALS, f.filter(-2));
    }

    public void testMultipleStrings() throws Exception {
        EqualsFilter f = new EqualsFilter(new String[] { "hello", "Hello", "World" },
                new MockInputColumn<String>("col", String.class));
        assertEquals(EqualsFilter.Category.EQUALS, f.filter("hello"));
        assertEquals(EqualsFilter.Category.EQUALS, f.filter("Hello"));
        assertEquals(EqualsFilter.Category.NOT_EQUALS, f.filter(""));
        assertEquals(EqualsFilter.Category.NOT_EQUALS, f.filter("world"));
        assertEquals(EqualsFilter.Category.NOT_EQUALS, f.filter(null));
    }

    public void testCompareValueColumnNumbers() throws Exception {
        EqualsFilter f = new EqualsFilter(new MockInputColumn<Object>("col1", Number.class),
                new MockInputColumn<Object>("col2", Object.class));

        assertEquals(EqualsFilter.Category.EQUALS, f.filter(1234, 1234));
        assertEquals(EqualsFilter.Category.EQUALS, f.filter(1234.0, 1234));
        assertEquals(EqualsFilter.Category.EQUALS, f.filter(1235, "1235"));
        assertEquals(EqualsFilter.Category.EQUALS, f.filter(1235, "1235.0"));

        assertEquals(EqualsFilter.Category.NOT_EQUALS, f.filter(2, 3));
        assertEquals(EqualsFilter.Category.NOT_EQUALS, f.filter(-2, "2000"));
    }

    public void testCompareValueColumnStrings() throws Exception {
        EqualsFilter f = new EqualsFilter(new MockInputColumn<Object>("col1", String.class),
                new MockInputColumn<Object>("col2", Object.class));

        assertEquals(EqualsFilter.Category.EQUALS, f.filter("foo", "foo"));
        assertEquals(EqualsFilter.Category.NOT_EQUALS, f.filter("foo", "bar"));
    }

    public void testNonOptimizeableQuery() throws Exception {
        DatastoreConnection con = datastore.openConnection();
        Column column1 = con.getSchemaNavigator().convertToColumn("PUBLIC.EMPLOYEES.FIRSTNAME");
        Column column2 = con.getSchemaNavigator().convertToColumn("PUBLIC.EMPLOYEES.LASTNAME");
        InputColumn<?> inputColumn1 = new MetaModelInputColumn(column1);
        InputColumn<?> inputColumn2 = new MetaModelInputColumn(column2);

        EqualsFilter filter = new EqualsFilter(inputColumn1, inputColumn2);
        filter.setValues(new String[] { "foobar" });

        assertFalse(filter.isOptimizable(EqualsFilter.Category.EQUALS));
        assertFalse(filter.isOptimizable(EqualsFilter.Category.NOT_EQUALS));
    }

    public void testOptimizeQueryValueColumn() throws Exception {
        DatastoreConnection con = datastore.openConnection();
        Column column1 = con.getSchemaNavigator().convertToColumn("PUBLIC.EMPLOYEES.FIRSTNAME");
        Column column2 = con.getSchemaNavigator().convertToColumn("PUBLIC.EMPLOYEES.LASTNAME");
        InputColumn<?> inputColumn1 = new MetaModelInputColumn(column1);
        InputColumn<?> inputColumn2 = new MetaModelInputColumn(column2);

        EqualsFilter filter = new EqualsFilter(inputColumn1, inputColumn2);
        assertTrue(filter.isOptimizable(EqualsFilter.Category.EQUALS));
        assertTrue(filter.isOptimizable(EqualsFilter.Category.NOT_EQUALS));

        Query query = con.getDataContext().query().from(column1.getTable()).select(column1, column2).toQuery();
        String originalSql = query.toSql();
        assertEquals("SELECT \"EMPLOYEES\".\"FIRSTNAME\", \"EMPLOYEES\".\"LASTNAME\" FROM PUBLIC.\"EMPLOYEES\"",
                originalSql);

        Query result;
        result = filter.optimizeQuery(query.clone(), EqualsFilter.Category.EQUALS);
        assertEquals(originalSql + " WHERE \"EMPLOYEES\".\"FIRSTNAME\" = \"EMPLOYEES\".\"LASTNAME\"", result.toSql());

        result = filter.optimizeQuery(query.clone(), EqualsFilter.Category.NOT_EQUALS);
        assertEquals(originalSql + " WHERE \"EMPLOYEES\".\"FIRSTNAME\" <> \"EMPLOYEES\".\"LASTNAME\"", result.toSql());

        con.close();
    }

    public void testOptimizeQueryValuesArray() throws Exception {
        DatastoreConnection con = datastore.openConnection();
        Column column = con.getSchemaNavigator().convertToColumn("PUBLIC.EMPLOYEES.FIRSTNAME");
        InputColumn<?> inputColumn = new MetaModelInputColumn(column);

        EqualsFilter filter = new EqualsFilter(new String[] { "foobar" }, inputColumn);
        assertTrue(filter.isOptimizable(EqualsFilter.Category.EQUALS));
        assertTrue(filter.isOptimizable(EqualsFilter.Category.NOT_EQUALS));

        Query query = con.getDataContext().query().from(column.getTable()).select(column).toQuery();
        String originalSql = query.toSql();
        assertEquals("SELECT \"EMPLOYEES\".\"FIRSTNAME\" FROM PUBLIC.\"EMPLOYEES\"", originalSql);

        Query result;
        result = filter.optimizeQuery(query.clone(), EqualsFilter.Category.EQUALS);
        assertEquals(originalSql + " WHERE \"EMPLOYEES\".\"FIRSTNAME\" = 'foobar'", result.toSql());

        result = filter.optimizeQuery(query.clone(), EqualsFilter.Category.NOT_EQUALS);
        assertEquals(originalSql + " WHERE \"EMPLOYEES\".\"FIRSTNAME\" <> 'foobar'", result.toSql());

        con.close();
    }
}
