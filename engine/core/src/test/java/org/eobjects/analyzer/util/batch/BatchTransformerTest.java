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
package org.eobjects.analyzer.util.batch;

import java.util.List;

import junit.framework.TestCase;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl;
import org.eobjects.analyzer.connection.CsvDatastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.connection.DatastoreCatalogImpl;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.data.MetaModelInputColumn;
import org.eobjects.analyzer.data.MutableInputColumn;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.AnalyzerJobBuilder;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.eobjects.analyzer.job.concurrent.MultiThreadedTaskRunner;
import org.eobjects.analyzer.job.runner.AnalysisResultFuture;
import org.eobjects.analyzer.job.runner.AnalysisRunnerImpl;
import org.eobjects.analyzer.result.ListResult;
import org.eobjects.analyzer.test.MockAnalyzer;

public class BatchTransformerTest extends TestCase {

    private AnalysisJob job;
    private AnalyzerBeansConfigurationImpl configuration;
    private MetaModelInputColumn sourceColumn;
    private MutableInputColumn<?> sortedColumn;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        DatastoreCatalog datastoreCatalog = new DatastoreCatalogImpl(new CsvDatastore("foo",
                "src/test/resources/employees.csv"));
        configuration = new AnalyzerBeansConfigurationImpl().replace(new MultiThreadedTaskRunner(10)).replace(
                datastoreCatalog);

        try (AnalysisJobBuilder jobBuilder = new AnalysisJobBuilder(configuration)) {
            jobBuilder.setDatastore("foo");
            jobBuilder.addSourceColumns("name");

            TransformerJobBuilder<MockBatchTransformer> transformerBuilder = jobBuilder
                    .addTransformer(MockBatchTransformer.class);
            sourceColumn = jobBuilder.getSourceColumns().get(0);
            transformerBuilder.addInputColumns(sourceColumn);

            AnalyzerJobBuilder<MockAnalyzer> analyzer = jobBuilder.addAnalyzer(MockAnalyzer.class);
            analyzer.addInputColumns(sourceColumn);
            sortedColumn = transformerBuilder.getOutputColumns().get(0);
            analyzer.addInputColumns(sortedColumn);

            job = jobBuilder.toAnalysisJob();
        }
    }

    public void testScenario() throws Exception {
        AnalysisResultFuture resultFuture = new AnalysisRunnerImpl(configuration).run(job);

        @SuppressWarnings("unchecked")
        ListResult<InputRow> result = (ListResult<InputRow>) resultFuture.getResults().get(0);

        List<InputRow> values = result.getValues();
        assertEquals(7, values.size());

        boolean foundRemixedFields = false;
        for (InputRow inputRow : values) {
            Object sourceValue = inputRow.getValue(sourceColumn);
            Object sortedValue = inputRow.getValue(sortedColumn);
            if (!sourceValue.equals(sortedValue)) {
                foundRemixedFields = true;
                break;
            }
        }
        assertTrue(foundRemixedFields);
    }
}
