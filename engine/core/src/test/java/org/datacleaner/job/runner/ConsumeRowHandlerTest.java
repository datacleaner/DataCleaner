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
package org.datacleaner.job.runner;

import java.util.ArrayList;
import java.util.List;

import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.configuration.AnalyzerBeansConfiguration;
import org.datacleaner.configuration.AnalyzerBeansConfigurationImpl;
import org.datacleaner.connection.PojoDatastore;
import org.datacleaner.data.MetaModelInputColumn;
import org.datacleaner.data.MockInputRow;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.job.builder.TransformerComponentBuilder;
import org.datacleaner.job.runner.ConsumeRowHandler.Configuration;
import org.datacleaner.job.tasks.MockMultiRowTransformer;
import org.datacleaner.test.MockAnalyzer;
import org.datacleaner.test.MockTransformer;
import org.apache.metamodel.pojo.ArrayTableDataProvider;
import org.apache.metamodel.pojo.TableDataProvider;
import org.apache.metamodel.util.SimpleTableDef;

import junit.framework.TestCase;

public class ConsumeRowHandlerTest extends TestCase {

    final AnalyzerBeansConfiguration analyzerBeansConfiguration = new AnalyzerBeansConfigurationImpl();
    private AnalysisJobBuilder ajb;
    private MetaModelInputColumn nameColumn;
    private MetaModelInputColumn ageColumn;
    private MetaModelInputColumn countryColumn;
    private List<MetaModelInputColumn> sourceColumns;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        List<TableDataProvider<?>> tableDataProviders = new ArrayList<TableDataProvider<?>>();
        tableDataProviders.add(new ArrayTableDataProvider(new SimpleTableDef("table", new String[] { "name", "age",
                "country" }), new ArrayList<Object[]>()));

        ajb = new AnalysisJobBuilder(analyzerBeansConfiguration);
        ajb.setDatastore(new PojoDatastore("ds", "sch", tableDataProviders));
        ajb.addSourceColumns("name", "age", "country");
        sourceColumns = ajb.getSourceColumns();

        nameColumn = sourceColumns.get(0);
        ageColumn = sourceColumns.get(1);
        countryColumn = sourceColumns.get(2);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        ajb.close();
    }

    public void testSingleRecordOutputScenario() throws Exception {
        final TransformerComponentBuilder<MockTransformer> tr1 = ajb.addTransformer(MockTransformer.class);
        tr1.addInputColumn(ajb.getSourceColumnByName("name"));

        final TransformerComponentBuilder<MockTransformer> tr2 = ajb.addTransformer(MockTransformer.class);
        tr2.addInputColumn(tr1.getOutputColumns().get(0));

        final AnalyzerComponentBuilder<MockAnalyzer> analyzer = ajb.addAnalyzer(MockAnalyzer.class);
        analyzer.addInputColumns(sourceColumns);

        final AnalysisJob job = ajb.toAnalysisJob(true);

        final Configuration configuration = new Configuration();
        configuration.includeAnalyzers = false;

        final ConsumeRowHandler handler = new ConsumeRowHandler(job, analyzerBeansConfiguration, configuration);

        List<InputRow> result;

        MockInputRow inputRow = new MockInputRow().put(nameColumn, "Kasper").put(ageColumn, null)
                .put(countryColumn, null);
        result = handler.consumeRow(inputRow).getRows();

        assertEquals(1, result.size());
        InputRow outputRow = result.get(0);
        assertEquals("TransformedInputRow[values={"
                + "TransformedInputColumn[id=trans-0001-0002,name=mock output]=mocked: Kasper, "
                + "TransformedInputColumn[id=trans-0003-0004,name=mock output]=mocked: mocked: Kasper}," + "delegate="
                + inputRow.toString() + "]", outputRow.toString());

        List<InputColumn<?>> outputColumns = outputRow.getInputColumns();
        assertEquals(5, outputColumns.size());

        assertEquals("mocked: Kasper", outputRow.getValue(outputColumns.get(3)).toString());
        assertEquals("mocked: mocked: Kasper", outputRow.getValue(outputColumns.get(4)).toString());
    }

    public void testMultiRecordOutputScenario() throws Exception {
        final TransformerComponentBuilder<MockMultiRowTransformer> tr1 = ajb.addTransformer(MockMultiRowTransformer.class);
        tr1.setConfiguredProperty("Count to what?", ajb.getSourceColumnByName("age"));

        final TransformerComponentBuilder<MockTransformer> tr2 = ajb.addTransformer(MockTransformer.class);
        tr2.addInputColumn(tr1.getOutputColumns().get(0));

        final AnalyzerComponentBuilder<MockAnalyzer> analyzer = ajb.addAnalyzer(MockAnalyzer.class);
        analyzer.addInputColumns(sourceColumns);

        final AnalysisJob job = ajb.toAnalysisJob(true);

        final Configuration configuration = new Configuration();
        configuration.includeAnalyzers = true;

        final ConsumeRowHandler handler = new ConsumeRowHandler(job, analyzerBeansConfiguration, configuration);

        List<InputRow> result;

        MockInputRow inputRow = new MockInputRow(100).put(nameColumn, "Vera").put(ageColumn, 3)
                .put(countryColumn, "DK");
        result = handler.consumeRow(inputRow).getRows();

        assertEquals(3, result.size());
        InputRow outputRow = result.get(0);
        assertEquals("TransformedInputRow[values={"
                + "TransformedInputColumn[id=trans-0001-0002,name=Mock multi row transformer (1)]=1, "
                + "TransformedInputColumn[id=trans-0001-0003,name=Mock multi row transformer (2)]=42, "
                + "TransformedInputColumn[id=trans-0004-0005,name=mock output]=mocked: 1}," + "delegate=" + inputRow
                + "]", outputRow.toString());
        assertEquals(100, outputRow.getId());

        outputRow = result.get(1);
        assertEquals("TransformedInputRow[values={"
                + "TransformedInputColumn[id=trans-0001-0002,name=Mock multi row transformer (1)]=2, "
                + "TransformedInputColumn[id=trans-0001-0003,name=Mock multi row transformer (2)]=42, "
                + "TransformedInputColumn[id=trans-0004-0005,name=mock output]=mocked: 2}," + "delegate=" + inputRow
                + "]", outputRow.toString());
        assertEquals(2147383649, outputRow.getId());

        outputRow = result.get(2);
        assertEquals("TransformedInputRow[values={"
                + "TransformedInputColumn[id=trans-0001-0002,name=Mock multi row transformer (1)]=3, "
                + "TransformedInputColumn[id=trans-0001-0003,name=Mock multi row transformer (2)]=42, "
                + "TransformedInputColumn[id=trans-0004-0005,name=mock output]=mocked: 3}," + "delegate=" + inputRow
                + "]", outputRow.toString());
        assertEquals(2147383650, outputRow.getId());

        List<InputColumn<?>> outputColumns = outputRow.getInputColumns();
        assertEquals(6, outputColumns.size());
    }
}
