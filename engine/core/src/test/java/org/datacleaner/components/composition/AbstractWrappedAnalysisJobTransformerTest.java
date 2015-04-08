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
package org.datacleaner.components.composition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Named;

import junit.framework.TestCase;

import org.apache.metamodel.pojo.ArrayTableDataProvider;
import org.apache.metamodel.pojo.TableDataProvider;
import org.apache.metamodel.util.SimpleTableDef;
import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.Configured;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.connection.DatastoreCatalogImpl;
import org.datacleaner.connection.PojoDatastore;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.job.runner.AnalysisRunnerImpl;
import org.datacleaner.result.ListResult;
import org.datacleaner.test.MockAnalyzer;
import org.datacleaner.test.MockTransformer;

public class AbstractWrappedAnalysisJobTransformerTest extends TestCase {

    private DataCleanerConfiguration _configuration;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // define the datastore that the wrapped job will refer to
        TableDataProvider<?> origTableDataProvider = new ArrayTableDataProvider(new SimpleTableDef("table",
                new String[] { "foo", "bar", "baz" }), new ArrayList<Object[]>(0));
        Datastore origInput = new PojoDatastore("orig_input", origTableDataProvider);

        // define the datastore that our actual job refers to
        ArrayList<Object[]> rows = new ArrayList<Object[]>();
        TableDataProvider<?> actualTableDataProvider = new ArrayTableDataProvider(new SimpleTableDef("table",
                new String[] { "name" }), rows);
        rows.add(new Object[] { "Tomasz" });
        rows.add(new Object[] { "Kasper" });
        rows.add(new Object[] { "Claudia" });
        rows.add(new Object[] { "Anders" });
        Datastore actualInput = new PojoDatastore("actual_input", actualTableDataProvider);

        DatastoreCatalog datastoreCatalog = new DatastoreCatalogImpl(origInput, actualInput);
        _configuration = new DataCleanerConfigurationImpl().withDatastoreCatalog(datastoreCatalog);
    }

    public void testGetOutputColumns() throws Exception {
        MockWrappedAnalysisJobTransformer transformer = new MockWrappedAnalysisJobTransformer();
        transformer._configuration = _configuration;
        transformer.input = new InputColumn[] { new MockInputColumn<String>("foo"), new MockInputColumn<String>("bar"),
                new MockInputColumn<String>("baz") };
        OutputColumns outputColumns = transformer.getOutputColumns();
        assertEquals("OutputColumns[mock output]", outputColumns.toString());
    }

    public void testWrappedExecution() throws Throwable {
        final AnalysisJob job;
        try (AnalysisJobBuilder builder = new AnalysisJobBuilder(_configuration)) {
            builder.setDatastore("actual_input");
            builder.addSourceColumns("table.name");
            builder.addTransformer(MockWrappedAnalysisJobTransformer.class).addInputColumn(
                    builder.getSourceColumnByName("name"));
            builder.addAnalyzer(MockAnalyzer.class).addInputColumns(builder.getAvailableInputColumns(Object.class));

            job = builder.toAnalysisJob();
        }

        AnalysisResultFuture resultFuture = new AnalysisRunnerImpl(_configuration).run(job);
        resultFuture.await();

        if (resultFuture.isErrornous()) {
            throw resultFuture.getErrors().get(0);
        }

        List<AnalyzerResult> results = resultFuture.getResults();
        assertEquals(1, results.size());

        @SuppressWarnings("unchecked")
        ListResult<InputRow> analyzerResult = (ListResult<InputRow>) results.get(0);

        List<InputRow> values = analyzerResult.getValues();
        assertEquals(4, values.size());

        assertEquals("TransformedInputRow[values={"
                + "TransformedInputColumn[id=trans-0001-0002,name=mock output]=mocked: Tomasz},"
                + "delegate=MetaModelInputRow[Row[values=[Tomasz]]]]", values.get(0).toString());
        assertEquals("TransformedInputRow[values={"
                + "TransformedInputColumn[id=trans-0001-0002,name=mock output]=mocked: Kasper},"
                + "delegate=MetaModelInputRow[Row[values=[Kasper]]]]", values.get(1).toString());
    }

    @Named("MockWrappedAnalysisJobTransformer")
    public static class MockWrappedAnalysisJobTransformer extends AbstractWrappedAnalysisJobTransformer {

        @Configured
        InputColumn<?>[] input;

        @Override
        protected AnalysisJob createWrappedAnalysisJob() {
            try (AnalysisJobBuilder builder = new AnalysisJobBuilder(_configuration)) {
                builder.setDatastore("orig_input");
                builder.addSourceColumns("table.foo");
                builder.addTransformer(MockTransformer.class).addInputColumns(builder.getSourceColumns());
                builder.addAnalyzer(MockAnalyzer.class).addInputColumns(builder.getAvailableInputColumns(Object.class));
                AnalysisJob job = builder.toAnalysisJob();
                return job;
            }
        }

        @Override
        protected Map<InputColumn<?>, InputColumn<?>> getInputColumnConversion(AnalysisJob wrappedAnalysisJob) {
            final Map<InputColumn<?>, InputColumn<?>> map = new HashMap<InputColumn<?>, InputColumn<?>>();
            final Iterator<InputColumn<?>> sourceColumns = wrappedAnalysisJob.getSourceColumns().iterator();
            int i = 0;
            while (i < input.length && sourceColumns.hasNext()) {
                InputColumn<?> next = sourceColumns.next();
                map.put(input[i], next);
                i++;
            }
            return map;
        }

    }
}
