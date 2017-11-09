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
package org.datacleaner.test.full.scenarios;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.metamodel.DataContext;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;
import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.beans.StringAnalyzer;
import org.datacleaner.beans.valuedist.ValueDistributionAnalyzer;
import org.datacleaner.beans.valuedist.ValueDistributionAnalyzerResult;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.configuration.DataCleanerEnvironment;
import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.job.concurrent.MultiThreadedTaskRunner;
import org.datacleaner.job.concurrent.TaskRunner;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.job.runner.AnalysisRunner;
import org.datacleaner.job.runner.AnalysisRunnerImpl;
import org.datacleaner.result.AnnotatedRowsResult;
import org.datacleaner.result.Crosstab;
import org.datacleaner.result.CrosstabNavigator;
import org.datacleaner.result.CrosstabResult;
import org.datacleaner.result.ResultProducer;
import org.datacleaner.result.ValueFrequency;
import org.datacleaner.result.renderer.CrosstabTextRenderer;
import org.datacleaner.test.TestHelper;

import junit.framework.TestCase;

public class ValueDistributionAndStringAnalysisTest extends TestCase {

    public void testScenario() throws Exception {
        final TaskRunner taskRunner = new MultiThreadedTaskRunner(5);
        final DataCleanerEnvironment environment = new DataCleanerEnvironmentImpl().withTaskRunner(taskRunner);

        final DataCleanerConfiguration configuration = new DataCleanerConfigurationImpl().withEnvironment(environment);

        final AnalysisRunner runner = new AnalysisRunnerImpl(configuration);

        final Datastore datastore = TestHelper.createSampleDatabaseDatastore("ds");
        final DatastoreConnection con = datastore.openConnection();
        final DataContext dc = con.getDataContext();

        final AnalysisJobBuilder analysisJobBuilder = new AnalysisJobBuilder(configuration);
        analysisJobBuilder.setDatastoreConnection(con);

        final Table table = dc.getDefaultSchema().getTableByName("EMPLOYEES");
        assertNotNull(table);

        final List<Column> columns = table.getColumns();

        analysisJobBuilder.addSourceColumns(columns);

        for (final InputColumn<?> inputColumn : analysisJobBuilder.getSourceColumns()) {
            final AnalyzerComponentBuilder<ValueDistributionAnalyzer> valueDistribuitionJobBuilder =
                    analysisJobBuilder.addAnalyzer(ValueDistributionAnalyzer.class);
            valueDistribuitionJobBuilder.addInputColumn(inputColumn);
            valueDistribuitionJobBuilder.setConfiguredProperty("Record unique values", false);
            valueDistribuitionJobBuilder.setConfiguredProperty("Top n most frequent values", null);
            valueDistribuitionJobBuilder.setConfiguredProperty("Bottom n most frequent values", null);
        }

        final AnalyzerComponentBuilder<StringAnalyzer> stringAnalyzerJob =
                analysisJobBuilder.addAnalyzer(StringAnalyzer.class);
        stringAnalyzerJob.addInputColumns(analysisJobBuilder.getAvailableInputColumns(String.class));

        final AnalysisJob analysisJob = analysisJobBuilder.toAnalysisJob();
        analysisJobBuilder.close();

        final AnalysisResultFuture resultFuture = runner.run(analysisJob);

        assertFalse(resultFuture.isDone());

        final List<AnalyzerResult> results = resultFuture.getResults();

        assertTrue(resultFuture.isDone());

        // expect 1 result for each column (the value distributions) and 1
        // result for the string analyzer
        assertEquals(table.getColumnCount() + 1, results.size());

        int stringAnalyzerResults = 0;
        int valueDistributionResults = 0;

        final CrosstabResult stringAnalyzerResult =
                (CrosstabResult) resultFuture.getResult(stringAnalyzerJob.toAnalyzerJob());

        for (final AnalyzerResult result : results) {
            if (result instanceof CrosstabResult) {
                stringAnalyzerResults++;

                assertTrue(result instanceof CrosstabResult);
                final CrosstabResult cr = (CrosstabResult) result;
                final Crosstab<?> crosstab = cr.getCrosstab();
                assertEquals("[Column, Measures]", Arrays.toString(crosstab.getDimensionNames()));
                assertEquals("[LASTNAME, FIRSTNAME, EXTENSION, EMAIL, OFFICECODE, JOBTITLE]",
                        crosstab.getDimension(0).getCategories().toString());
                assertEquals("[Row count, Null count, Blank count, Entirely uppercase count, Entirely lowercase count, "
                                + "Total char count, Max chars, Min chars, Avg chars, Max white spaces, "
                                + "Min white spaces, Avg white spaces, Uppercase chars, "
                                + "Uppercase chars (excl. first letters), Lowercase chars, Digit chars, "
                                + "Diacritic chars, Non-letter chars, Word count, Max words, Min words]",
                        crosstab.getDimension(1).getCategories().toString());
                final CrosstabNavigator<?> nav = crosstab.navigate();
                nav.where("Column", "EMAIL");
                nav.where("Measures", "Total char count");
                assertEquals("655", nav.get().toString());
            } else {
                assertTrue(result instanceof ValueDistributionAnalyzerResult);

                valueDistributionResults++;
            }
        }

        assertEquals(1, stringAnalyzerResults);
        assertEquals(8, valueDistributionResults);

        ValueDistributionAnalyzerResult jobTitleResult = null;
        ValueDistributionAnalyzerResult lastnameResult = null;
        for (final AnalyzerResult result : results) {
            if (result instanceof ValueDistributionAnalyzerResult) {
                final ValueDistributionAnalyzerResult vdResult = (ValueDistributionAnalyzerResult) result;
                if ("JOBTITLE".equals(vdResult.getName())) {
                    jobTitleResult = vdResult;
                } else if ("LASTNAME".equals(vdResult.getName())) {
                    lastnameResult = vdResult;
                }
            }
        }

        assertNotNull(jobTitleResult);
        assertNotNull(lastnameResult);

        final Iterator<ValueFrequency> it = lastnameResult.getValueCounts().iterator();
        assertEquals("<unique>", it.next().getName());
        assertEquals("Patterson", it.next().getName());
        assertEquals(2, it.next().getCount());
        assertEquals(16, lastnameResult.getUniqueCount().intValue());
        assertEquals(0, lastnameResult.getNullCount());

        assertEquals("Sales Rep", jobTitleResult.getValueCounts().iterator().next().getValue());

        final String[] resultLines = new CrosstabTextRenderer().render(stringAnalyzerResult).split("\n");
        assertEquals(
                "                                        LASTNAME  FIRSTNAME  EXTENSION      EMAIL OFFICECODE   JOBTITLE ",
                resultLines[0]);
        assertEquals(
                "Uppercase chars (excl. first letters)          0          1          0          0          0         39 ",
                resultLines[14]);
        assertEquals(
                "Diacritic chars                                0          0          0          0          0          0 ",
                resultLines[17]);

        // do some drill-to-detail on the StringAnalyzerResult
        final Crosstab<?> crosstab = stringAnalyzerResult.getCrosstab();

        ResultProducer resultProducer =
                crosstab.where("Column", "FIRSTNAME").where("Measures", "Uppercase chars (excl. first letters)")
                        .explore();
        assertNotNull(resultProducer);

        final AnnotatedRowsResult arr = (AnnotatedRowsResult) resultProducer.getResult();
        final List<InputRow> rows = arr.getSampleRows();
        assertEquals(1, rows.size());
        assertEquals("Foon Yue",
                rows.get(0).getValue(analysisJobBuilder.getSourceColumnByName("FIRSTNAME")).toString());

        resultProducer = crosstab.where("Column", "FIRSTNAME").where("Measures", "Diacritic chars").explore();
        assertNull(resultProducer);

        con.close();
        taskRunner.shutdown();
    }
}
