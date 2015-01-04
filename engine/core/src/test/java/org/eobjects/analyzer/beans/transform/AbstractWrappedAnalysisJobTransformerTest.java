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
package org.eobjects.analyzer.beans.transform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.eobjects.analyzer.beans.api.Configured;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.connection.DatastoreCatalogImpl;
import org.eobjects.analyzer.connection.PojoDatastore;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.runner.AnalysisResultFuture;
import org.eobjects.analyzer.job.runner.AnalysisRunnerImpl;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.ListResult;
import org.eobjects.analyzer.test.MockAnalyzer;
import org.eobjects.analyzer.test.MockTransformer;
import org.apache.metamodel.pojo.ArrayTableDataProvider;
import org.apache.metamodel.pojo.TableDataProvider;
import org.apache.metamodel.util.SimpleTableDef;

public class AbstractWrappedAnalysisJobTransformerTest extends TestCase {

    private AnalyzerBeansConfigurationImpl analyzerBeansConfiguration;

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
        analyzerBeansConfiguration = new AnalyzerBeansConfigurationImpl().replace(datastoreCatalog);
    }

    public void testGetOutputColumns() throws Exception {
        MockWrappedAnalysisJobTransformer transformer = new MockWrappedAnalysisJobTransformer();
        transformer._analyzerBeansConfiguration = analyzerBeansConfiguration;
        transformer.input = new InputColumn[] { new MockInputColumn<String>("foo"), new MockInputColumn<String>("bar"),
                new MockInputColumn<String>("baz") };
        OutputColumns outputColumns = transformer.getOutputColumns();
        assertEquals("OutputColumns[mock output]", outputColumns.toString());
    }

    public void testWrappedExecution() throws Throwable {
        final AnalysisJob job;
        try (AnalysisJobBuilder builder = new AnalysisJobBuilder(analyzerBeansConfiguration)) {
            builder.setDatastore("actual_input");
            builder.addSourceColumns("table.name");
            builder.addTransformer(MockWrappedAnalysisJobTransformer.class).addInputColumn(
                    builder.getSourceColumnByName("name"));
            builder.addAnalyzer(MockAnalyzer.class).addInputColumns(builder.getAvailableInputColumns(Object.class));

            job = builder.toAnalysisJob();
        }

        AnalysisResultFuture resultFuture = new AnalysisRunnerImpl(analyzerBeansConfiguration).run(job);
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

    @TransformerBean("MockWrappedAnalysisJobTransformer")
    public static class MockWrappedAnalysisJobTransformer extends AbstractWrappedAnalysisJobTransformer {

        @Configured
        InputColumn<?>[] input;

        @Override
        protected AnalysisJob createWrappedAnalysisJob() {
            try (AnalysisJobBuilder builder = new AnalysisJobBuilder(_analyzerBeansConfiguration)) {
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
