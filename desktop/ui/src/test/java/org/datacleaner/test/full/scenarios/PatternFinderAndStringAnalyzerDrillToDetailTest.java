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
import java.util.List;

import junit.framework.TestCase;

import org.apache.metamodel.DataContext;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;
import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.beans.StringAnalyzer;
import org.datacleaner.beans.standardize.EmailStandardizerTransformer;
import org.datacleaner.beans.stringpattern.PatternFinderAnalyzer;
import org.datacleaner.beans.stringpattern.PatternFinderResult;
import org.datacleaner.beans.stringpattern.PatternFinderResultTextRenderer;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.configuration.DataCleanerEnvironment;
import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.job.builder.TransformerComponentBuilder;
import org.datacleaner.job.concurrent.MultiThreadedTaskRunner;
import org.datacleaner.job.concurrent.TaskRunner;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.job.runner.AnalysisRunnerImpl;
import org.datacleaner.result.AnnotatedRowsResult;
import org.datacleaner.result.CrosstabResult;
import org.datacleaner.result.DefaultResultProducer;
import org.datacleaner.result.ResultProducer;
import org.datacleaner.result.renderer.CrosstabTextRenderer;
import org.datacleaner.test.TestHelper;

@SuppressWarnings("deprecation")
public class PatternFinderAndStringAnalyzerDrillToDetailTest extends TestCase {

    public void testScenario() throws Throwable {
        final TaskRunner taskRunner = new MultiThreadedTaskRunner(5);
        final DataCleanerEnvironment environment = new DataCleanerEnvironmentImpl().withTaskRunner(taskRunner);

        final DataCleanerConfiguration configuration = new DataCleanerConfigurationImpl().withEnvironment(environment);

        Datastore datastore = TestHelper.createSampleDatabaseDatastore("ds");
        DatastoreConnection con = datastore.openConnection();
        DataContext dc = con.getDataContext();

        AnalysisJobBuilder ajb = new AnalysisJobBuilder(configuration);
        try {
            ajb.setDatastoreConnection(con);

            Table table = dc.getDefaultSchema().getTableByName("EMPLOYEES");
            assertNotNull(table);

            Column jobTitleColumn = table.getColumnByName("JOBTITLE");
            assertNotNull(jobTitleColumn);

            Column emailColumn = table.getColumnByName("EMAIL");
            assertNotNull(emailColumn);

            ajb.addSourceColumns(jobTitleColumn, emailColumn);

            InputColumn<?> emailInputColumn = ajb.getSourceColumnByName("EMAIL");
            TransformerComponentBuilder<EmailStandardizerTransformer> emailStd1 = ajb.addTransformer(
                    EmailStandardizerTransformer.class).addInputColumn(emailInputColumn);

            AnalyzerComponentBuilder<PatternFinderAnalyzer> pf = ajb.addAnalyzer(PatternFinderAnalyzer.class);
            InputColumn<?> jobtitleInputColumn = ajb.getSourceColumnByName("JOBTITLE");
            pf.addInputColumn(jobtitleInputColumn);
            pf.getComponentInstance().setDiscriminateTextCase(false);

            AnalyzerComponentBuilder<StringAnalyzer> sa = ajb.addAnalyzer(StringAnalyzer.class);
            sa.addInputColumns(emailInputColumn, emailStd1.getOutputColumnByName("Username"),
                    emailStd1.getOutputColumnByName("Domain"));

            AnalysisResultFuture resultFuture = new AnalysisRunnerImpl(configuration).run(ajb.toAnalysisJob());
            if (!resultFuture.isSuccessful()) {
                throw resultFuture.getErrors().iterator().next();
            }

            // pattern finder result tests
            {
                final PatternFinderResult result = (PatternFinderResult) resultFuture.getResult(pf.toAnalyzerJob());
                final String resultString = new PatternFinderResultTextRenderer().render(result);
                final String[] resultLines = resultString.split("\n");

                assertEquals(resultString, 5, resultLines.length);

                assertEquals(resultString, "                            Match count Sample      ", resultLines[0]);
                assertTrue(resultString, resultLines[1].startsWith("aaaaa aaaaaaaaa                      19"));

                ResultProducer resultProducer = result.getSingleCrosstab().where("Pattern", "aaaaa aaaaaaaaa")
                        .where("Measures", "Match count").explore();
                assertEquals(DefaultResultProducer.class, resultProducer.getClass());
                AnalyzerResult result2 = resultProducer.getResult();
                assertEquals(AnnotatedRowsResult.class, result2.getClass());

                AnnotatedRowsResult annotatedRowsResult = (AnnotatedRowsResult) result2;
                assertEquals(19, annotatedRowsResult.getAnnotatedRowCount());
                List<InputRow> rows = annotatedRowsResult.getSampleRows();
                assertEquals(19, rows.size());

                String[] values = new String[19];
                for (int i = 0; i < values.length; i++) {
                    values[i] = (String) rows.get(i).getValue(jobtitleInputColumn);
                }

                Arrays.sort(values);

                assertEquals(
                        "[Sales Rep, Sales Rep, Sales Rep, Sales Rep, Sales Rep, Sales Rep, Sales Rep, Sales Rep, Sales Rep, Sales Rep, Sales Rep, Sales Rep, Sales Rep, Sales Rep, Sales Rep, Sales Rep, Sales Rep, VP Marketing, VP Sales]",
                        Arrays.toString(values));
            }

            // string analyzer tests
            {
                CrosstabResult result = (CrosstabResult) resultFuture.getResult(sa.toAnalyzerJob());
                String[] resultLines = new CrosstabTextRenderer().render(result).split("\n");

                assertEquals("                                         EMAIL Username   Domain ", resultLines[0]);
                assertEquals("Total char count                           655      172      460 ", resultLines[6]);
                assertEquals("Max chars                                   31       10       20 ", resultLines[7]);
                assertEquals("Min chars                                   26        5       20 ", resultLines[8]);

                // username is a virtual columns, but because of the
                // row-annotation
                // system it is still possible to drill to detail on it.
                ResultProducer resultProducer = result.getCrosstab().where("Column", "Username")
                        .where("Measures", "Max chars").explore();
                assertNotNull(resultProducer);
                assertEquals(AnnotatedRowsResult.class, resultProducer.getResult().getClass());

                // email is a physical column so it IS queryable
                resultProducer = result.getCrosstab().where("Column", "EMAIL").where("Measures", "Max chars").explore();
                assertNotNull(resultProducer);

                AnalyzerResult result2 = resultProducer.getResult();
                assertEquals(AnnotatedRowsResult.class, result2.getClass());

                AnnotatedRowsResult arr = (AnnotatedRowsResult) result2;
                List<InputRow> rows = arr.getSampleRows();
                assertEquals(1, rows.size());
                assertEquals("wpatterson@classicmodelcars.com", rows.get(0).getValue(emailInputColumn).toString());
            }

        } finally {
            ajb.close();
        }

        con.close();
        taskRunner.shutdown();
    }
}
