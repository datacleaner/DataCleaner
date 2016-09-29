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
package org.datacleaner.job;

import junit.framework.TestCase;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.OutputDataStream;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.connection.CsvDatastore;
import org.datacleaner.connection.Datastore;
import org.datacleaner.data.MetaModelInputColumn;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.test.MockAnalyzer;
import org.datacleaner.test.MockOutputDataStreamAnalyzer;

import java.util.List;

public class AnalyzerJobHelperTest extends TestCase {

    private AnalysisJobBuilder ajb;
    private Datastore datastore = new CsvDatastore("ds", "src/test/resources/employees.csv");

    @Override
    protected void setUp() throws Exception {
        ajb = new AnalysisJobBuilder(new DataCleanerConfigurationImpl());
        ajb.setDatastore(datastore);
        ajb.addSourceColumns("name", "email");
    }

    public void testGetAnalyzerJob() throws Exception {
        final AnalyzerComponentBuilder<MockAnalyzer> analyzer = ajb.addAnalyzer(MockAnalyzer.class);
        analyzer.addInputColumns(ajb.getSourceColumns());

        final AnalysisJob job1 = ajb.toAnalysisJob();
        final AnalyzerJob analyzer1 = job1.getAnalyzerJobs().iterator().next();

        // create a copy
        final AnalysisJob job2;
        final Datastore datastore2 = new CsvDatastore("ds", "src/test/resources/employees.csv");
        try (final AnalysisJobBuilder ajb2 = new AnalysisJobBuilder(new DataCleanerConfigurationImpl())) {

            ajb2.setDatastore(datastore2);
            ajb2.addSourceColumns("name", "email");
            ajb2.addAnalyzer(MockAnalyzer.class).addInputColumns(new MockInputColumn<String>("name"));

            job2 = ajb2.toAnalysisJob();
        }
        final AnalyzerJob analyzer2 = job2.getAnalyzerJobs().iterator().next();

        assertNotSame(job1, job2);
        assertNotSame(analyzer1, analyzer2);

        final AnalyzerJobHelper helper = new AnalyzerJobHelper(job2);
        final AnalyzerJob result = helper.getAnalyzerJob(analyzer1);
        assertSame(analyzer2, result);

        assertSame(analyzer2, helper.getAnalyzerJob("Mock analyzer", null, null));

        assertEquals(1, helper.getAnalyzerJobs().size());
    }

    public void testGetAnalyzerJobFromChildScope() {
        final Datastore datastore = new CsvDatastore("ds", "src/test/resources/employees.csv");
        final AnalysisJob aj;
        try (final AnalysisJobBuilder ajb = new AnalysisJobBuilder(new DataCleanerConfigurationImpl())) {
            ajb.setDatastore(datastore);
            ajb.addSourceColumns("name", "email");

            final AnalyzerComponentBuilder<MockOutputDataStreamAnalyzer> analyzer1 = ajb
                    .addAnalyzer(MockOutputDataStreamAnalyzer.class);
            final List<MetaModelInputColumn> sourceColumns = ajb.getSourceColumns();
            analyzer1.setName("analyzer1");
            analyzer1.addInputColumn(sourceColumns.get(0));

            final OutputDataStream outputDataStream = analyzer1.getOutputDataStreams().get(0);
            final AnalysisJobBuilder outputDataStreamJobBuilder =
                    analyzer1.getOutputDataStreamJobBuilder(outputDataStream);
            final List<MetaModelInputColumn> outputDataStreamColumns = outputDataStreamJobBuilder.getSourceColumns();

            final AnalyzerComponentBuilder<MockAnalyzer> analyzer2 = outputDataStreamJobBuilder
                    .addAnalyzer(MockAnalyzer.class);
            analyzer2.addInputColumns(outputDataStreamColumns);
            analyzer2.setName("analyzer2");

            aj = ajb.toAnalysisJob();
        }

        AnalyzerJobHelper analyzerJobHelper = new AnalyzerJobHelper(aj);
        assertEquals(2,analyzerJobHelper.getAnalyzerJobs().size());
    }

    public void testGetIdentifyingInputColumn() throws Exception {
        AnalyzerComponentBuilder<MockAnalyzer> analyzer = ajb.addAnalyzer(MockAnalyzer.class);
        analyzer.addInputColumns(ajb.getSourceColumns());

        AnalysisJob job = ajb.toAnalysisJob();

        InputColumn<?> column = AnalyzerJobHelper.getIdentifyingInputColumn(job.getAnalyzerJobs().iterator().next());
        assertNull(column);

        analyzer.clearInputColumns();
        analyzer.addInputColumn(ajb.getSourceColumnByName("name"));

        job = ajb.toAnalysisJob();
        column = AnalyzerJobHelper.getIdentifyingInputColumn(job.getAnalyzerJobs().iterator().next());
        assertNotNull(column);
        assertEquals("MetaModelInputColumn[resources.employees.csv.name]", column.toString());

        ajb.close();
    }
}
