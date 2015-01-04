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
package org.eobjects.analyzer.test.full.scenarios;

import java.util.Arrays;

import junit.framework.TestCase;

import org.eobjects.analyzer.beans.StringAnalyzer;
import org.eobjects.analyzer.beans.standardize.EmailStandardizerTransformer;
import org.eobjects.analyzer.beans.stringpattern.PatternFinderAnalyzer;
import org.eobjects.analyzer.beans.stringpattern.PatternFinderResult;
import org.eobjects.analyzer.beans.stringpattern.PatternFinderResultTextRenderer;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreConnection;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.AnalyzerJobBuilder;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.eobjects.analyzer.job.concurrent.MultiThreadedTaskRunner;
import org.eobjects.analyzer.job.concurrent.TaskRunner;
import org.eobjects.analyzer.job.runner.AnalysisResultFuture;
import org.eobjects.analyzer.job.runner.AnalysisRunnerImpl;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.AnnotatedRowsResult;
import org.eobjects.analyzer.result.CrosstabResult;
import org.eobjects.analyzer.result.DefaultResultProducer;
import org.eobjects.analyzer.result.ResultProducer;
import org.eobjects.analyzer.result.renderer.CrosstabTextRenderer;
import org.eobjects.analyzer.test.TestHelper;
import org.apache.metamodel.DataContext;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;

public class PatternFinderAndStringAnalyzerDrillToDetailTest extends TestCase {

    public void testScenario() throws Throwable {
        TaskRunner taskRunner = new MultiThreadedTaskRunner(5);

        AnalyzerBeansConfiguration configuration = new AnalyzerBeansConfigurationImpl().replace(taskRunner);

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
            TransformerJobBuilder<EmailStandardizerTransformer> emailStd1 = ajb.addTransformer(
                    EmailStandardizerTransformer.class).addInputColumn(emailInputColumn);

            AnalyzerJobBuilder<PatternFinderAnalyzer> pf = ajb.addAnalyzer(PatternFinderAnalyzer.class);
            InputColumn<?> jobtitleInputColumn = ajb.getSourceColumnByName("JOBTITLE");
            pf.addInputColumn(jobtitleInputColumn);
            pf.getComponentInstance().setDiscriminateTextCase(false);

            AnalyzerJobBuilder<StringAnalyzer> sa = ajb.addAnalyzer(StringAnalyzer.class);
            sa.addInputColumns(emailInputColumn, emailStd1.getOutputColumnByName("Username"),
                    emailStd1.getOutputColumnByName("Domain"));

            AnalysisResultFuture resultFuture = new AnalysisRunnerImpl(configuration).run(ajb.toAnalysisJob());
            if (!resultFuture.isSuccessful()) {
                throw resultFuture.getErrors().iterator().next();
            }

            // pattern finder result tests
            {
                PatternFinderResult result = (PatternFinderResult) resultFuture.getResult(pf.toAnalyzerJob());
                String[] resultLines = new PatternFinderResultTextRenderer().render(result).split("\n");

                assertEquals(5, resultLines.length);

                assertEquals("                            Match count Sample      ", resultLines[0]);
                assertTrue(resultLines[1].startsWith("aaaaa aaaaaaaaa                      19"));

                ResultProducer resultProducer = result.getSingleCrosstab().where("Pattern", "aaaaa aaaaaaaaa")
                        .where("Measures", "Match count").explore();
                assertEquals(DefaultResultProducer.class, resultProducer.getClass());
                AnalyzerResult result2 = resultProducer.getResult();
                assertEquals(AnnotatedRowsResult.class, result2.getClass());

                AnnotatedRowsResult annotatedRowsResult = (AnnotatedRowsResult) result2;
                assertEquals(19, annotatedRowsResult.getAnnotatedRowCount());
                InputRow[] rows = annotatedRowsResult.getRows();
                assertEquals(19, rows.length);

                String[] values = new String[19];
                for (int i = 0; i < values.length; i++) {
                    values[i] = (String) rows[i].getValue(jobtitleInputColumn);
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
                InputRow[] rows = arr.getRows();
                assertEquals(1, rows.length);
                assertEquals("wpatterson@classicmodelcars.com", rows[0].getValue(emailInputColumn).toString());
            }

        } finally {
            ajb.close();
        }

        con.close();
        taskRunner.shutdown();
    }
}
