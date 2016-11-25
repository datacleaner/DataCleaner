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
package org.datacleaner.beans;

import java.util.Date;
import java.util.List;

import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.configuration.DataCleanerEnvironment;
import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.connection.DatastoreCatalogImpl;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.job.runner.AnalysisRunnerImpl;
import org.datacleaner.result.AnnotatedRowsResult;
import org.datacleaner.result.CrosstabNavigator;
import org.datacleaner.result.ResultProducer;
import org.datacleaner.result.renderer.CrosstabTextRenderer;
import org.datacleaner.storage.InMemoryStorageProvider;
import org.datacleaner.storage.StorageProvider;
import org.datacleaner.test.TestHelper;

import junit.framework.TestCase;

public class DateAndTimeAnalyzerTest extends TestCase {
    public void testOrderFactTable() throws Throwable {
        final StorageProvider storageProvider = new InMemoryStorageProvider(20, 1000);
        final DataCleanerEnvironment environment =
                new DataCleanerEnvironmentImpl().withStorageProvider(storageProvider);
        final DataCleanerConfiguration conf = new DataCleanerConfigurationImpl().withEnvironment(environment)
                .withDatastoreCatalog(new DatastoreCatalogImpl(TestHelper.createSampleDatabaseDatastore("orderdb")));

        try (AnalysisJobBuilder ajb = new AnalysisJobBuilder(conf)) {
            ajb.setDatastore("orderdb");

            ajb.addSourceColumns("ORDERFACT.ORDERDATE", "ORDERFACT.REQUIREDDATE", "ORDERFACT.SHIPPEDDATE");

            final AnalyzerComponentBuilder<DateAndTimeAnalyzer> analyzer = ajb.addAnalyzer(DateAndTimeAnalyzer.class);
            analyzer.addInputColumns(ajb.getSourceColumns());

            final AnalysisJob job = ajb.toAnalysisJob();

            final AnalysisResultFuture resultFuture = new AnalysisRunnerImpl(conf).run(job);
            resultFuture.await();

            if (!resultFuture.isSuccessful()) {
                throw resultFuture.getErrors().get(0);
            }
            assertTrue(resultFuture.isSuccessful());

            final List<AnalyzerResult> results = resultFuture.getResults();
            assertEquals(1, results.size());

            final DateAndTimeAnalyzerResult result = (DateAndTimeAnalyzerResult) results.get(0);

            final String[] resultLines = new CrosstabTextRenderer().render(result).split("\n");
            assertEquals(8, resultLines.length);

            assertEquals("             ORDERDATE    REQUIREDDATE SHIPPEDDATE  ", resultLines[0]);
            assertEquals("Row count            2996         2996         2996 ", resultLines[1]);
            assertEquals("Null count              0            0          141 ", resultLines[2]);
            assertEquals("Highest date 2005-05-31   2005-06-11   2005-05-20   ", resultLines[3]);
            assertEquals("Lowest date  2003-01-06   2003-01-13   2003-01-10   ", resultLines[4]);
            assertEquals("Highest time 00:00:00.000 00:00:00.000 00:00:00.000 ", resultLines[5]);
            assertEquals("Lowest time  00:00:00.000 00:00:00.000 00:00:00.000 ", resultLines[6]);

            final String meanResultLine = resultLines[7];
            // due to timezone diffs, this line will have slight variants on
            // different machines
            assertTrue(meanResultLine.startsWith("Mean         2004-05-14"));

            final CrosstabNavigator<?> nav = result.getCrosstab().where("Column", "ORDERDATE");
            final InputColumn<?> column = ajb.getSourceColumnByName("ORDERDATE");

            ResultProducer resultProducer = nav.where("Measure", "Highest date").explore();
            assertAnnotatedRowResult(resultProducer.getResult(), column, 19, 19);

            resultProducer = nav.where("Measure", "Lowest date").explore();
            assertAnnotatedRowResult(resultProducer.getResult(), column, 4, 4);

            resultProducer = nav.where("Measure", "Highest time").explore();
            assertAnnotatedRowResult(resultProducer.getResult(), column, 2996, 1000);

            resultProducer = nav.where("Measure", "Lowest time").explore();
            assertAnnotatedRowResult(resultProducer.getResult(), column, 2996, 1000);

            assertEquals(2996, result.getRowCount(new MockInputColumn<Date>("ORDERDATE")));
            assertEquals(0, result.getNullCount(new MockInputColumn<Date>("ORDERDATE")));
            assertEquals(12934, result.getHighestDate(new MockInputColumn<Date>("ORDERDATE")));
            assertEquals(12058, result.getLowestDate(new MockInputColumn<Date>("ORDERDATE")));
        }
    }

    public void testResultConvertToDaysFromEpoch() throws Exception {
        assertEquals(0, DateAndTimeAnalyzerResult.convertToDaysSinceEpoch("1970-01-01"));
        assertEquals(1, DateAndTimeAnalyzerResult.convertToDaysSinceEpoch("1970-01-02"));
        assertEquals(31, DateAndTimeAnalyzerResult.convertToDaysSinceEpoch("1970-02-01"));
        assertEquals(12934, DateAndTimeAnalyzerResult.convertToDaysSinceEpoch("2005-05-31"));
    }

    private void assertAnnotatedRowResult(final AnalyzerResult result, final InputColumn<?> col, final int rowCount,
            final int distinctRowCount) {
        assertTrue("Unexpected result type: " + result.getClass(), result instanceof AnnotatedRowsResult);
        final AnnotatedRowsResult res = (AnnotatedRowsResult) result;
        final InputColumn<?>[] highlightedColumns = res.getHighlightedColumns();
        assertEquals(1, highlightedColumns.length);
        assertEquals(col, highlightedColumns[0]);

        assertEquals(rowCount, res.getAnnotatedRowCount());

        final List<InputRow> sampleRows = res.getSampleRows();
        assertEquals(distinctRowCount, sampleRows.size());
    }
}
