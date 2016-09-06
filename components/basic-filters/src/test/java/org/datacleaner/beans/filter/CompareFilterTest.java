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

import org.apache.metamodel.query.Query;
import org.apache.metamodel.schema.Column;
import org.datacleaner.api.InputColumn;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.data.MetaModelInputColumn;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;
import org.datacleaner.test.TestHelper;

import junit.framework.TestCase;

public class CompareFilterTest extends TestCase {

    private Datastore datastore = TestHelper.createSampleDatabaseDatastore("ds");

    public void testCompareStringsWithNumbers() throws Exception {
        final MockInputColumn<String> column = new MockInputColumn<>("col", String.class);
        CompareFilter f = new CompareFilter(column, CompareFilter.Operator.GREATER_THAN, "100");
        assertEquals(CompareFilter.Category.TRUE, f.categorize(new MockInputRow().put(column, "199")));
        assertEquals(CompareFilter.Category.TRUE, f.categorize(new MockInputRow().put(column, "101")));

        assertEquals(CompareFilter.Category.FALSE, f.categorize(new MockInputRow().put(column, "099")));
        assertEquals(CompareFilter.Category.FALSE, f.categorize(new MockInputRow().put(column, "-1243")));

        assertEquals(CompareFilter.Category.FALSE, f.categorize(new MockInputRow().put(column, "100")));

        // unfortunate scenario where lexigraphic comparison doesn't help
        assertEquals(CompareFilter.Category.TRUE, f.categorize(new MockInputRow().put(column, "99")));
    }

    public void testCompareStringsWithLikeOperator() throws Exception {
        final MockInputColumn<String> column = new MockInputColumn<>("col", String.class);
        CompareFilter f = new CompareFilter(column, CompareFilter.Operator.LIKE, "%world%");
        assertEquals(CompareFilter.Category.TRUE, f.categorize(new MockInputRow().put(column, "Hello world")));
        assertEquals(CompareFilter.Category.TRUE,
                f.categorize(new MockInputRow().put(column, "The world is a funny place")));

        assertEquals(CompareFilter.Category.FALSE,
                f.categorize(new MockInputRow().put(column, "I'm outta this place")));
        assertEquals(CompareFilter.Category.FALSE, f.categorize(new MockInputRow().put(column, "")));
        assertEquals(CompareFilter.Category.FALSE, f.categorize(new MockInputRow().put(column, null)));
    }

    public void testCompareNumbers() throws Exception {
        final MockInputColumn<Integer> column = new MockInputColumn<>("col", Integer.class);
        CompareFilter f = new CompareFilter(column, CompareFilter.Operator.GREATER_THAN, "100");
        assertEquals(CompareFilter.Category.TRUE, f.categorize(new MockInputRow().put(column, 199)));
        assertEquals(CompareFilter.Category.TRUE, f.categorize(new MockInputRow().put(column, 101)));

        assertEquals(CompareFilter.Category.FALSE, f.categorize(new MockInputRow().put(column, 99)));
        assertEquals(CompareFilter.Category.FALSE, f.categorize(new MockInputRow().put(column, -1243)));

        assertEquals(CompareFilter.Category.FALSE, f.categorize(new MockInputRow().put(column, 100)));
    }

    public void testOptimizeQueryValuesArray() throws Exception {
        DatastoreConnection con = datastore.openConnection();
        Column firstname = con.getSchemaNavigator().convertToColumn("PUBLIC.EMPLOYEES.FIRSTNAME");
        Column lastname = con.getSchemaNavigator().convertToColumn("PUBLIC.EMPLOYEES.LASTNAME");
        InputColumn<?> firstnameInputColumn = new MetaModelInputColumn(firstname);
        InputColumn<?> lastnameInputColumn = new MetaModelInputColumn(lastname);

        CompareFilter filter = new CompareFilter(firstnameInputColumn, CompareFilter.Operator.DIFFERENT_FROM,
                lastnameInputColumn);
        assertTrue(filter.isOptimizable(CompareFilter.Category.TRUE));
        assertFalse(filter.isOptimizable(CompareFilter.Category.FALSE));

        Query query = con.getDataContext().query().from(firstname.getTable()).select(firstname, lastname).toQuery();
        String originalSql = query.toSql();
        assertEquals("SELECT \"EMPLOYEES\".\"FIRSTNAME\", \"EMPLOYEES\".\"LASTNAME\" FROM PUBLIC.\"EMPLOYEES\"",
                originalSql);

        Query result;
        result = filter.optimizeQuery(query.clone(), CompareFilter.Category.TRUE);
        assertEquals(originalSql + " WHERE \"EMPLOYEES\".\"FIRSTNAME\" <> \"EMPLOYEES\".\"LASTNAME\"", result.toSql());

        con.close();
    }
}
