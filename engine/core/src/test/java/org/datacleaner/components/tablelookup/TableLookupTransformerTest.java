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
package org.datacleaner.components.tablelookup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.datacleaner.api.InputColumn;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.components.tablelookup.TableLookupTransformer.JoinSemantic;
import org.datacleaner.connection.CsvDatastore;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;
import org.datacleaner.job.AbstractOutputRowCollector;
import org.datacleaner.storage.InMemoryRowAnnotationFactory;
import org.datacleaner.storage.RowAnnotationFactory;

public class TableLookupTransformerTest extends TestCase {

    private final RowAnnotationFactory annotationFactory = new InMemoryRowAnnotationFactory();

    public void testScenario() throws Exception {
        TableLookupTransformer trans = createTransformer();
        trans.datastore = new CsvDatastore("my ds", "src/test/resources/employees.csv");
        trans.outputColumns = new String[] { "name" };
        trans.conditionColumns = new String[] { "email" };
        InputColumn<String> col1 = new MockInputColumn<String>("my email col", String.class);
        trans.conditionValues = new InputColumn[] { col1 };

        OutputColumns outputColumns = trans.getOutputColumns();
        assertEquals("OutputColumns[name (lookup)]", outputColumns.toString());
        assertEquals(String.class, outputColumns.getColumnType(0));

        trans.validate();
        trans.init();

        assertEquals("[Jane Doe]",
                Arrays.toString(trans.transform(new MockInputRow().put(col1, "jane.doe@company.com"))));
        assertEquals("[null]", Arrays.toString(trans.transform(new MockInputRow().put(col1, "foo bar"))));

        trans.close();
    }

    private TableLookupTransformer createTransformer() {
        TableLookupTransformer t = new TableLookupTransformer();
        t._annotationFactory = annotationFactory;
        t._cached = annotationFactory.createAnnotation();
        t._matches = annotationFactory.createAnnotation();
        t._misses = annotationFactory.createAnnotation();
        return t;
    }

    public void testGetOutputColumnsClearCache() throws Exception {
        TableLookupTransformer trans = createTransformer();
        trans.datastore = new CsvDatastore("my ds", "src/test/resources/employees.csv");
        trans.outputColumns = new String[] { "name", "email" };
        trans.conditionColumns = new String[] { "email" };

        assertEquals("OutputColumns[name (lookup), email (lookup)]", trans.getOutputColumns().toString());

        trans.outputColumns = new String[] { "name" };

        assertEquals("OutputColumns[name (lookup)]", trans.getOutputColumns().toString());

        // check cache reuse by removing the datastore (that would be used when
        // re-fetching the columns)
        trans.datastore = null;
        assertEquals("OutputColumns[name (lookup)]", trans.getOutputColumns().toString());

        // confirm the above assumption by creating a NPE when no datastore is
        // set.
        trans.outputColumns = new String[] { "name", "email" };
        try {
            trans.getOutputColumns();
            fail("Exception expected");
        } catch (NullPointerException e) {
            // OK!
        }
    }

    public void testInnerJoinMinOneRecordSemantics() throws Exception {
        final List<Object[]> result = new ArrayList<Object[]>();

        final TableLookupTransformer trans = createTransformer();
        trans.datastore = new CsvDatastore("my ds", "src/test/resources/employees.csv");
        trans.outputColumns = new String[] { "name" };
        trans.outputRowCollector = new AbstractOutputRowCollector() {
            @Override
            public void putValues(Object... values) {
                result.add(values);
            }
        };
        trans.joinSemantic = JoinSemantic.LEFT_JOIN;
        trans.conditionColumns = new String[] { "email" };
        InputColumn<String> col1 = new MockInputColumn<String>("my email col", String.class);
        trans.conditionValues = new InputColumn[] { col1 };

        OutputColumns outputColumns = trans.getOutputColumns();
        assertEquals("OutputColumns[name (lookup)]", outputColumns.toString());
        assertEquals(String.class, outputColumns.getColumnType(0));

        trans.validate();
        trans.init();

        assertEquals("[null]", Arrays.toString(trans.transform(new MockInputRow().put(col1, "foo bar"))));
        assertEquals(0, result.size());

        assertNull(trans.transform(new MockInputRow().put(col1, "jane.doe@company.com")));
        assertEquals(2, result.size());
        assertEquals("[Jane Doe]", Arrays.toString(result.get(0)));
        assertEquals("[Jane doe]", Arrays.toString(result.get(1)));

        trans.close();
    }

    public void testInnerJoinSemantics() throws Exception {
        final List<Object[]> result = new ArrayList<Object[]>();

        final TableLookupTransformer trans = createTransformer();
        trans.datastore = new CsvDatastore("my ds", "src/test/resources/employees.csv");
        trans.outputColumns = new String[] { "name" };
        trans.outputRowCollector = new AbstractOutputRowCollector() {
            @Override
            public void putValues(Object... values) {
                result.add(values);
            }
        };
        trans.joinSemantic = JoinSemantic.INNER_JOIN;
        trans.conditionColumns = new String[] { "email" };
        InputColumn<String> col1 = new MockInputColumn<String>("my email col", String.class);
        trans.conditionValues = new InputColumn[] { col1 };

        OutputColumns outputColumns = trans.getOutputColumns();
        assertEquals("OutputColumns[name (lookup)]", outputColumns.toString());
        assertEquals(String.class, outputColumns.getColumnType(0));

        trans.validate();
        trans.init();

        assertNull(trans.transform(new MockInputRow().put(col1, "foo bar")));
        assertEquals(0, result.size());

        assertNull(trans.transform(new MockInputRow().put(col1, "jane.doe@company.com")));
        assertEquals(2, result.size());
        assertEquals("[Jane Doe]", Arrays.toString(result.get(0)));
        assertEquals("[Jane doe]", Arrays.toString(result.get(1)));

        trans.close();
    }

    public void testCarthesianJoin() throws Exception {
        final List<Object[]> result = new ArrayList<Object[]>();

        final TableLookupTransformer trans = createTransformer();
        trans.datastore = new CsvDatastore("my ds", "src/test/resources/employees.csv");
        trans.outputColumns = new String[] { "name" };
        trans.outputRowCollector = new AbstractOutputRowCollector() {
            @Override
            public void putValues(Object... values) {
                result.add(values);
            }
        };
        trans.joinSemantic = JoinSemantic.INNER_JOIN;
        InputColumn<String> col1 = new MockInputColumn<String>("my email col", String.class);

        OutputColumns outputColumns = trans.getOutputColumns();
        assertEquals("OutputColumns[name (lookup)]", outputColumns.toString());
        assertEquals(String.class, outputColumns.getColumnType(0));

        trans.validate();
        trans.init();

        assertNull(trans.transform(new MockInputRow().put(col1, "foo bar")));
        assertEquals(7, result.size());
        assertEquals("[John Doe]", Arrays.toString(result.get(0)));
        assertEquals("[Jane Doe]", Arrays.toString(result.get(1)));
        assertEquals("[Jane doe]", Arrays.toString(result.get(2)));

        assertNull(trans.transform(new MockInputRow().put(col1, "jane.doe@company.com")));
        assertEquals(14, result.size());
        assertEquals("[John Doe]", Arrays.toString(result.get(0)));
        assertEquals("[Jane Doe]", Arrays.toString(result.get(1)));
        assertEquals("[Jane doe]", Arrays.toString(result.get(2)));
        assertEquals("[John Doe]", Arrays.toString(result.get(7 + 0)));
        assertEquals("[Jane Doe]", Arrays.toString(result.get(7 + 1)));
        assertEquals("[Jane doe]", Arrays.toString(result.get(7 + 2)));

        trans.close();
    }
}
