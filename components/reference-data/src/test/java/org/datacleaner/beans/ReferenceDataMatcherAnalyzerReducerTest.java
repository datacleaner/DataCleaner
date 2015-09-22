package org.datacleaner.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.datacleaner.api.InputColumn;
import org.datacleaner.components.maxrows.MaxRowsFilter;
import org.datacleaner.components.maxrows.MaxRowsFilter.Category;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalogImpl;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.job.builder.FilterComponentBuilder;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.job.runner.AnalysisRunnerImpl;
import org.datacleaner.reference.SynonymCatalog;
import org.datacleaner.reference.TextFileSynonymCatalog;
import org.datacleaner.result.Crosstab;
import org.datacleaner.result.renderer.CrosstabTextRenderer;
import org.datacleaner.test.TestHelper;
import org.junit.Test;

public class ReferenceDataMatcherAnalyzerReducerTest {

    @Test
    public void test() throws Throwable {

        final AnalysisJobBuilder jobBuilder = getAnalysisJobBuilder();
        final AnalysisJobBuilder jobBuilder1 = getAnalysisJobBuilder();
        final AnalysisJobBuilder jobBuilder2 = getAnalysisJobBuilder();
        final AnalysisJobBuilder jobBuilder3 = getAnalysisJobBuilder();

        final BooleanAnalyzerResult fullResult = getPartialResult(jobBuilder, 1, 23);
        final BooleanAnalyzerResult partialResult1 = getPartialResult(jobBuilder1, 1, 12);
        final BooleanAnalyzerResult partialResult2 = getPartialResult(jobBuilder2, 13, 23);
        final BooleanAnalyzerResult partialResult3 = getPartialResult(jobBuilder3, 24, null);

        // assert first job

        {
            final Crosstab<Number> partialResult1columnStatisticsCrosstab = partialResult1
                    .getColumnStatisticsCrosstab();
            final String[] resultLines1 = new CrosstabTextRenderer().render(partialResult1columnStatisticsCrosstab)
                    .split("\n");
            assertEquals(5, resultLines1.length);
            assertEquals("            JOBTITLE in Job Titles ", resultLines1[0]);
            assertEquals("Row count                       12 ", resultLines1[1]);
            assertEquals("Null count                       0 ", resultLines1[2]);
            assertEquals("True count                       9 ", resultLines1[3]);
            assertEquals("False count                      3 ", resultLines1[4]);

            // assert the value combination crosstab
            assertNull(partialResult1.getValueCombinationCrosstab());

        }

        // assert second job

        {
            final Crosstab<Number> partialResult2columnStatisticsCrosstab = partialResult2
                    .getColumnStatisticsCrosstab();
            final String[] resultLines2 = new CrosstabTextRenderer().render(partialResult2columnStatisticsCrosstab)
                    .split("\n");
            assertEquals(5, resultLines2.length);
            assertEquals("            JOBTITLE in Job Titles ", resultLines2[0]);
            assertEquals("Row count                       11 ", resultLines2[1]);
            assertEquals("Null count                       0 ", resultLines2[2]);
            assertEquals("True count                      11 ", resultLines2[3]);
            assertEquals("False count                      0 ", resultLines2[4]);

            // assert the value combination crosstab
            assertNull(partialResult2.getValueCombinationCrosstab());
        }

        // assert third job

        {
            final Crosstab<Number> partialResult3columnStatisticsCrosstab = partialResult3
                    .getColumnStatisticsCrosstab();
            final String[] resultLines3 = new CrosstabTextRenderer().render(partialResult3columnStatisticsCrosstab)
                    .split("\n");
            assertEquals(5, resultLines3.length);
            assertEquals("            JOBTITLE in Job Titles ", resultLines3[0]);
            assertEquals("Row count                        0 ", resultLines3[1]);
            assertEquals("Null count                       0 ", resultLines3[2]);
            assertEquals("True count                       0 ", resultLines3[3]);
            assertEquals("False count                      0 ", resultLines3[4]);

            // assert the value combination crosstab
            assertNull(partialResult3.getValueCombinationCrosstab());
        }

        // assert full job result

        final Crosstab<Number> fullResultcolumnStatisticsCrosstab = fullResult.getColumnStatisticsCrosstab();
        final String[] resultLinesFull = new CrosstabTextRenderer().render(fullResultcolumnStatisticsCrosstab).split(
                "\n");
        assertEquals(5, resultLinesFull.length);
        assertEquals("            JOBTITLE in Job Titles ", resultLinesFull[0]);
        assertEquals("Row count                       23 ", resultLinesFull[1]);
        assertEquals("Null count                       0 ", resultLinesFull[2]);
        assertEquals("True count                      20 ", resultLinesFull[3]);
        assertEquals("False count                      3 ", resultLinesFull[4]);

        // assert the value combination crosstab
        assertNull(fullResult.getValueCombinationCrosstab());

        assertEquals(0, fullResult.getNullCount().getValue(BooleanAnalyzer.MEASURE_NULL_COUNT).intValue());
        assertEquals(0, fullResult.getFalseCount().getValue(BooleanAnalyzer.MEASURE_FALSE_COUNT).intValue());
        assertEquals(23, fullResult.getRowCount().intValue());
        assertEquals(0, fullResult.getTrueCount().getValue(BooleanAnalyzer.MEASURE_TRUE_COUNT).intValue());

        final Collection<BooleanAnalyzerResult> partialsResults = new ArrayList<>();
        partialsResults.add(partialResult1);
        partialsResults.add(partialResult2);
        partialsResults.add(partialResult3);

        final ReferenceDataMatcherAnalyzerReducer reducer = new ReferenceDataMatcherAnalyzerReducer();
        final BooleanAnalyzerResult reducedResult = reducer.reduce(partialsResults);

        // Assert the reduced values

        final Crosstab<Number> columnStatisticsCrosstab = reducedResult.getColumnStatisticsCrosstab();
        final String[] reducerLinesResults = new CrosstabTextRenderer().render(columnStatisticsCrosstab).split("\n");

        assertEquals(5, reducerLinesResults.length);
        assertEquals("            JOBTITLE in Job Titles ", reducerLinesResults[0]);
        assertEquals("Row count                       23 ", reducerLinesResults[1]);
        assertEquals("Null count                       0 ", reducerLinesResults[2]);
        assertEquals("True count                      20 ", reducerLinesResults[3]);
        assertEquals("False count                      3 ", reducerLinesResults[4]);

        assertEquals(0, reducedResult.getNullCount().getValue(BooleanAnalyzer.MEASURE_NULL_COUNT).intValue());
        assertEquals(0, reducedResult.getFalseCount().getValue(BooleanAnalyzer.MEASURE_FALSE_COUNT).intValue());
        assertEquals(23, reducedResult.getRowCount().intValue());
        assertEquals(0, reducedResult.getTrueCount().getValue(BooleanAnalyzer.MEASURE_TRUE_COUNT).intValue());

        // compare reduced result with full results

        {
            assertEquals(resultLinesFull.length, reducerLinesResults.length);
            assertEquals(resultLinesFull[0], reducerLinesResults[0]);
            assertEquals(resultLinesFull[1], reducerLinesResults[1]);
            assertEquals(resultLinesFull[2], reducerLinesResults[2]);
            assertEquals(resultLinesFull[3], reducerLinesResults[3]);
            assertEquals(resultLinesFull[4], reducerLinesResults[4]);
        }

    }

    private AnalysisJobBuilder getAnalysisJobBuilder() {
        final Datastore datastore = TestHelper.createSampleDatabaseDatastore("orderdb");
        final DataCleanerConfigurationImpl configuration = new DataCleanerConfigurationImpl()
                .withDatastoreCatalog(new DatastoreCatalogImpl(datastore));
        final AnalysisJobBuilder jobBuilder = new AnalysisJobBuilder(configuration);
        jobBuilder.setDatastore(datastore);
        jobBuilder.addSourceColumns("employees.JOBTITLE");
        return jobBuilder;
    }

    private BooleanAnalyzerResult getPartialResult(AnalysisJobBuilder jobBuilder, Integer firstRow, Integer maxRows)
            throws Throwable {
        final InputColumn<?> jobTitleColumn = jobBuilder.getSourceColumnByName("JOBTITLE");
        final FilterComponentBuilder<MaxRowsFilter, Category> maxRowsFilter = jobBuilder.addFilter(MaxRowsFilter.class);
        maxRowsFilter.addInputColumn(jobTitleColumn);
        if (firstRow != null) {
            maxRowsFilter.setConfiguredProperty("First row", firstRow);
        }
        if (maxRows != null) {
            maxRowsFilter.setConfiguredProperty("Max rows", maxRows);
        }

        final AnalyzerComponentBuilder<ReferenceDataMatcherAnalyzer> referenceDataMatcherAnalyzer = jobBuilder
                .addAnalyzer(ReferenceDataMatcherAnalyzer.class);
        referenceDataMatcherAnalyzer.setRequirement(maxRowsFilter.getFilterOutcome(MaxRowsFilter.Category.VALID));
        final ReferenceDataMatcherAnalyzer referenceDataMatcher = referenceDataMatcherAnalyzer.getComponentInstance();
        final InputColumn<?>[] inputColumns = { jobTitleColumn };
        referenceDataMatcher.columns = inputColumns;
        final File file = new File("src/test/resources/synonym_titles_test.txt");
        assertTrue(file.exists());
        final TextFileSynonymCatalog jobTitlesCatalog = new TextFileSynonymCatalog("Job Titles", file, true, "UTF-8");
        final SynonymCatalog[] synonymCatalogs = { jobTitlesCatalog };
        referenceDataMatcher.synonymCatalogs = synonymCatalogs;
        final AnalysisJob analysisJob = jobBuilder.toAnalysisJob();
        jobBuilder.close();
        final AnalysisResultFuture resultFuture = new AnalysisRunnerImpl(jobBuilder.getConfiguration())
                .run(analysisJob);
        resultFuture.await();
        if (resultFuture.isErrornous()) {
            throw resultFuture.getErrors().get(0);
        }
        final BooleanAnalyzerResult result = resultFuture.getResults(BooleanAnalyzerResult.class).get(0);

        return result;
    }

}
