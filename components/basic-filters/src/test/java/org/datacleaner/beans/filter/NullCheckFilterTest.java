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
import org.datacleaner.api.InputColumn;
import org.datacleaner.beans.filter.NullCheckFilter.EvaluationMode;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.connection.SchemaNavigator;
import org.datacleaner.data.MetaModelInputColumn;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;
import org.datacleaner.descriptors.Descriptors;
import org.datacleaner.descriptors.FilterDescriptor;
import org.datacleaner.descriptors.SimpleDescriptorProvider;
import org.datacleaner.test.TestHelper;

import junit.framework.TestCase;

public class NullCheckFilterTest extends TestCase {

    public void testAliases() throws Exception {
        final FilterDescriptor<?, ?> desc1 = Descriptors.ofFilterUnbound(NullCheckFilter.class);

        final SimpleDescriptorProvider descriptorProvider = new SimpleDescriptorProvider();
        descriptorProvider.addFilterBeanDescriptor(desc1);

        final FilterDescriptor<?, ?> desc2 = descriptorProvider.getFilterDescriptorByDisplayName("Not null");
        final FilterDescriptor<?, ?> desc3 = descriptorProvider.getFilterDescriptorByDisplayName("Null check");

        assertSame(desc1, desc2);
        assertSame(desc1, desc3);

        final Enum<?> notNullOutcome1 = desc1.getOutcomeCategoryByName("VALID");
        final Enum<?> notNullOutcome2 = desc1.getOutcomeCategoryByName("NOT_NULL");
        assertSame(notNullOutcome1, notNullOutcome2);

        final Enum<?> nullOutcome1 = desc1.getOutcomeCategoryByName("INVALID");
        final Enum<?> nullOutcome2 = desc1.getOutcomeCategoryByName("NULL");
        assertSame(nullOutcome1, nullOutcome2);
    }

    public void testCategorize() throws Exception {
        final InputColumn<Integer> col1 = new MockInputColumn<>("col1", Integer.class);
        final InputColumn<Boolean> col2 = new MockInputColumn<>("col2", Boolean.class);
        final InputColumn<String> col3 = new MockInputColumn<>("col3", String.class);
        final InputColumn<?>[] columns = new InputColumn[] { col1, col2, col3 };

        final NullCheckFilter filter = new NullCheckFilter(columns, true);
        assertEquals(NullCheckFilter.NullCheckCategory.NOT_NULL,
                filter.categorize(new MockInputRow().put(col1, 1).put(col2, true).put(col3, "foo")));

        assertEquals(NullCheckFilter.NullCheckCategory.NULL,
                filter.categorize(new MockInputRow().put(col1, 1).put(col2, null).put(col3, "foo")));

        assertEquals(NullCheckFilter.NullCheckCategory.NULL,
                filter.categorize(new MockInputRow().put(col1, 1).put(col2, true).put(col3, "")));

        assertEquals(NullCheckFilter.NullCheckCategory.NULL,
                filter.categorize(new MockInputRow().put(col1, 1).put(col2, true).put(col3, null)));

        assertEquals(NullCheckFilter.NullCheckCategory.NULL,
                filter.categorize(new MockInputRow().put(col1, null).put(col2, null).put(col3, null)));
    }

    public void testCategorizeAllFieldsMode() throws Exception {
        final InputColumn<Integer> col1 = new MockInputColumn<>("col1", Integer.class);
        final InputColumn<Boolean> col2 = new MockInputColumn<>("col2", Boolean.class);
        final InputColumn<String> col3 = new MockInputColumn<>("col3", String.class);
        final InputColumn<?>[] columns = new InputColumn[] { col1, col2, col3 };

        final NullCheckFilter filter = new NullCheckFilter(columns, true, EvaluationMode.ALL_FIELDS);
        assertEquals(NullCheckFilter.NullCheckCategory.NOT_NULL,
                filter.categorize(new MockInputRow().put(col1, 1).put(col2, true).put(col3, "foo")));

        assertEquals(NullCheckFilter.NullCheckCategory.NOT_NULL,
                filter.categorize(new MockInputRow().put(col1, 1).put(col2, null).put(col3, "foo")));

        assertEquals(NullCheckFilter.NullCheckCategory.NOT_NULL,
                filter.categorize(new MockInputRow().put(col1, 1).put(col2, true).put(col3, "")));

        assertEquals(NullCheckFilter.NullCheckCategory.NOT_NULL,
                filter.categorize(new MockInputRow().put(col1, 1).put(col2, true).put(col3, null)));

        assertEquals(NullCheckFilter.NullCheckCategory.NULL,
                filter.categorize(new MockInputRow().put(col1, null).put(col2, null).put(col3, null)));

        assertEquals(NullCheckFilter.NullCheckCategory.NULL,
                filter.categorize(new MockInputRow().put(col1, "").put(col2, "").put(col3, "")));

        assertEquals(NullCheckFilter.NullCheckCategory.NULL,
                filter.categorize(new MockInputRow().put(col1, null).put(col2, "").put(col3, null)));
    }

    public void testDescriptor() throws Exception {
        final FilterDescriptor<NullCheckFilter, NullCheckFilter.NullCheckCategory> desc =
                Descriptors.ofFilter(NullCheckFilter.class);
        final Class<NullCheckFilter.NullCheckCategory> categoryEnum = desc.getOutcomeCategoryEnum();
        assertEquals(NullCheckFilter.NullCheckCategory.class, categoryEnum);
    }

    public void testOptimizeQuery() throws Exception {
        final Datastore datastore = TestHelper.createSampleDatabaseDatastore("mydb");
        final DatastoreConnection con = datastore.openConnection();
        final SchemaNavigator nav = con.getSchemaNavigator();

        final MetaModelInputColumn col1 = new MetaModelInputColumn(nav.convertToColumn("EMPLOYEES.EMAIL"));
        final MetaModelInputColumn col2 = new MetaModelInputColumn(nav.convertToColumn("EMPLOYEES.EMPLOYEENUMBER"));
        final InputColumn<?>[] columns = new InputColumn[] { col1, col2 };

        final NullCheckFilter filter = new NullCheckFilter(columns, true);

        final Query baseQuery =
                con.getDataContext().query().from("EMPLOYEES").select("EMAIL").and("EMPLOYEENUMBER").toQuery();
        Query optimizedQuery = filter.optimizeQuery(baseQuery.clone(), NullCheckFilter.NullCheckCategory.NOT_NULL);

        assertEquals("SELECT \"EMPLOYEES\".\"EMAIL\", \"EMPLOYEES\".\"EMPLOYEENUMBER\" FROM "
                + "PUBLIC.\"EMPLOYEES\" WHERE \"EMPLOYEES\".\"EMAIL\" IS NOT NULL AND \"EMPLOYEES\".\"EMAIL\" <> '' AND "
                + "\"EMPLOYEES\".\"EMPLOYEENUMBER\" IS NOT NULL", optimizedQuery.toSql());

        optimizedQuery = filter.optimizeQuery(baseQuery.clone(), NullCheckFilter.NullCheckCategory.NULL);

        assertEquals("SELECT \"EMPLOYEES\".\"EMAIL\", \"EMPLOYEES\".\"EMPLOYEENUMBER\" FROM "
                + "PUBLIC.\"EMPLOYEES\" WHERE (\"EMPLOYEES\".\"EMAIL\" IS NULL OR \"EMPLOYEES\".\"EMAIL\" = '' OR "
                + "\"EMPLOYEES\".\"EMPLOYEENUMBER\" IS NULL)", optimizedQuery.toSql());

        con.close();
    }
}
