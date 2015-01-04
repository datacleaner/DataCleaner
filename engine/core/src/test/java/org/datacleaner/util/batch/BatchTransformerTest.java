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
package org.datacleaner.util.batch;

import java.util.List;

import junit.framework.TestCase;

import org.datacleaner.configuration.AnalyzerBeansConfigurationImpl;
import org.datacleaner.connection.CsvDatastore;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.connection.DatastoreCatalogImpl;
import org.datacleaner.data.InputRow;
import org.datacleaner.data.MetaModelInputColumn;
import org.datacleaner.data.MutableInputColumn;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerJobBuilder;
import org.datacleaner.job.builder.TransformerJobBuilder;
import org.datacleaner.job.concurrent.MultiThreadedTaskRunner;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.job.runner.AnalysisRunnerImpl;
import org.datacleaner.result.ListResult;
import org.datacleaner.test.MockAnalyzer;

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
