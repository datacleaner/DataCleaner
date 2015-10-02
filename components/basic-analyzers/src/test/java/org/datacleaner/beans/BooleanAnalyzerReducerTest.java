package org.datacleaner.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.metamodel.util.FileResource;
import org.datacleaner.api.InputColumn;
import org.datacleaner.components.convert.ConvertToBooleanTransformer;
import org.datacleaner.components.maxrows.MaxRowsFilter;
import org.datacleaner.components.maxrows.MaxRowsFilter.Category;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.connection.CsvDatastore;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalogImpl;
import org.datacleaner.data.MutableInputColumn;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.FilterOutcome;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.job.builder.FilterComponentBuilder;
import org.datacleaner.job.builder.TransformerComponentBuilder;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.job.runner.AnalysisRunnerImpl;
import org.datacleaner.result.Crosstab;
import org.datacleaner.result.renderer.CrosstabTextRenderer;
import org.junit.Test;

public class BooleanAnalyzerReducerTest {

    @Test
    public void test() throws Throwable {
        final AnalysisJobBuilder jobBuilder = getAnalysisJobBuilder();
        final AnalysisJobBuilder jobBuilder1 = getAnalysisJobBuilder();
        final AnalysisJobBuilder jobBuilder2 = getAnalysisJobBuilder();
        final AnalysisJobBuilder jobBuilder3 = getAnalysisJobBuilder();

        final BooleanAnalyzerResult fullResult = getPartialResult(jobBuilder, 1, 16);
        final BooleanAnalyzerResult partialResult1 = getPartialResult(jobBuilder1, 1, 5);
        final BooleanAnalyzerResult partialResult2 = getPartialResult(jobBuilder2, 6, 5);
        final BooleanAnalyzerResult partialResult3 = getPartialResult(jobBuilder3, 11, 5);

        { // assert partial result1
            final Crosstab<Number> partialResult1columnStatisticsCrosstab = partialResult1
                    .getColumnStatisticsCrosstab();
            final String[] resultLines = new CrosstabTextRenderer().render(partialResult1columnStatisticsCrosstab)
                    .split("\n");
            assertEquals(5, resultLines.length);
            assertEquals("            b1 (as boolean) b2 (as boolean) ", resultLines[0]);
            assertEquals("Row count                 5               5 ", resultLines[1]);
            assertEquals("Null count                0               0 ", resultLines[2]);
            assertEquals("True count                3               2 ", resultLines[3]);
            assertEquals("False count               2               3 ", resultLines[4]);

            final String[] resultLines1 = new CrosstabTextRenderer().render(
                    partialResult1.getValueCombinationCrosstab()).split("\n");
            assertEquals(3, resultLines1.length);
            assertEquals("               b1 (as boolean) b2 (as boolean)       Frequency ", resultLines1[0]);
            assertEquals("Most frequent                1               0               3 ", resultLines1[1]);
            assertEquals("Least frequent               0               1               2 ", resultLines1[2]);

        }

        { // assert partial result2
            final Crosstab<Number> partialResult2columnStatisticsCrosstab = partialResult2
                    .getColumnStatisticsCrosstab();
            final String[] resultLines = new CrosstabTextRenderer().render(partialResult2columnStatisticsCrosstab)
                    .split("\n");
            assertEquals(5, resultLines.length);
            assertEquals("            b1 (as boolean) b2 (as boolean) ", resultLines[0]);
            assertEquals("Row count                 5               5 ", resultLines[1]);
            assertEquals("Null count                1               0 ", resultLines[2]);
            assertEquals("True count                2               3 ", resultLines[3]);
            assertEquals("False count               2               2 ", resultLines[4]);

            final String[] resultLines1 = new CrosstabTextRenderer().render(
                    partialResult2.getValueCombinationCrosstab()).split("\n");
            assertEquals(5, resultLines1.length);
            assertEquals("               b1 (as boolean) b2 (as boolean)       Frequency ", resultLines1[0]);
            assertEquals("Most frequent                1               1               2 ", resultLines1[1]);
            assertEquals("Combination 1           <null>               0               1 ", resultLines1[2]);
            assertEquals("Combination 2                0               1               1 ", resultLines1[3]);
            assertEquals("Least frequent               0               0               1 ", resultLines1[4]);

        }

        { // assert partial result3
            final Crosstab<Number> partialResult3columnStatisticsCrosstab = partialResult3
                    .getColumnStatisticsCrosstab();
            final String[] resultLines = new CrosstabTextRenderer().render(partialResult3columnStatisticsCrosstab)
                    .split("\n");
            assertEquals(5, resultLines.length);
            assertEquals("            b1 (as boolean) b2 (as boolean) ", resultLines[0]);
            assertEquals("Row count                 5               5 ", resultLines[1]);
            assertEquals("Null count                0               2 ", resultLines[2]);
            assertEquals("True count                2               1 ", resultLines[3]);
            assertEquals("False count               3               2 ", resultLines[4]);

            final String[] resultLines1 = new CrosstabTextRenderer().render(
                    partialResult3.getValueCombinationCrosstab()).split("\n");
            assertEquals(6, resultLines1.length);
            assertEquals("               b1 (as boolean) b2 (as boolean)       Frequency ", resultLines1[0]);
            assertEquals("Most frequent                1          <null>               1 ", resultLines1[1]);
            assertEquals("Combination 1                1               0               1 ", resultLines1[2]);
            assertEquals("Combination 2                0          <null>               1 ", resultLines1[3]);
            assertEquals("Combination 3                0               1               1 ", resultLines1[4]);
            assertEquals("Least frequent               0               0               1 ", resultLines1[5]);

        }

        final List<BooleanAnalyzerResult> partialResults = new ArrayList<BooleanAnalyzerResult>();
        partialResults.add(partialResult1);
        partialResults.add(partialResult2);
        partialResults.add(partialResult3);

        final BooleanAnalyzerReducer booleanAnalyzerReducer = new BooleanAnalyzerReducer();
        final BooleanAnalyzerResult reducedResults = booleanAnalyzerReducer.reduce(partialResults);

        { // assert reduced results
            final Crosstab<Number> reducedResultcolumnStatisticsCrosstab = reducedResults.getColumnStatisticsCrosstab();
            final String[] resultLinesReduced = new CrosstabTextRenderer()
                    .render(reducedResultcolumnStatisticsCrosstab).split("\n");
            assertEquals(5, resultLinesReduced.length);
            assertEquals("            b1 (as boolean) b2 (as boolean) ", resultLinesReduced[0]);
            assertEquals("Row count                15              15 ", resultLinesReduced[1]);
            assertEquals("Null count                1               2 ", resultLinesReduced[2]);
            assertEquals("True count                7               6 ", resultLinesReduced[3]);
            assertEquals("False count               7               7 ", resultLinesReduced[4]);

            final String[] resultLinesReduced1 = new CrosstabTextRenderer().render(
                    reducedResults.getValueCombinationCrosstab()).split("\n");
            assertEquals(8, resultLinesReduced1.length);
            assertEquals("               b1 (as boolean) b2 (as boolean)       Frequency ", resultLinesReduced1[0]);
            assertEquals("Most frequent                1               0               4 ", resultLinesReduced1[1]);
            assertEquals("Combination 1                0               1               4 ", resultLinesReduced1[2]);
            assertEquals("Combination 2                1               1               2 ", resultLinesReduced1[3]);
            assertEquals("Combination 3                0               0               2 ", resultLinesReduced1[4]);
            assertEquals("Combination 4           <null>               0               1 ", resultLinesReduced1[5]);
            assertEquals("Combination 5                1          <null>               1 ", resultLinesReduced1[6]);
            assertEquals("Least frequent               0          <null>               1 ", resultLinesReduced1[7]);

        }

        { // assert full result
            final Crosstab<Number> fullResultcolumnStatisticsCrosstab = fullResult.getColumnStatisticsCrosstab();
            final String[] resultLinesFull = new CrosstabTextRenderer().render(fullResultcolumnStatisticsCrosstab)
                    .split("\n");
            assertEquals(5, resultLinesFull.length);
            assertEquals("            b1 (as boolean) b2 (as boolean) ", resultLinesFull[0]);
            assertEquals("Row count                15              15 ", resultLinesFull[1]);
            assertEquals("Null count                1               2 ", resultLinesFull[2]);
            assertEquals("True count                7               6 ", resultLinesFull[3]);
            assertEquals("False count               7               7 ", resultLinesFull[4]);

            final String[] resultLines = new CrosstabTextRenderer().render(fullResult.getValueCombinationCrosstab())
                    .split("\n");
            assertEquals(8, resultLines.length);
            assertEquals("               b1 (as boolean) b2 (as boolean)       Frequency ", resultLines[0]);
            assertEquals("Most frequent                1               0               4 ", resultLines[1]);
            assertEquals("Combination 1                0               1               4 ", resultLines[2]);
            assertEquals("Combination 2                1               1               2 ", resultLines[3]);
            assertEquals("Combination 3                0               0               2 ", resultLines[4]);
            assertEquals("Combination 4           <null>               0               1 ", resultLines[5]);
            assertEquals("Combination 5                1          <null>               1 ", resultLines[6]);
            assertEquals("Least frequent               0          <null>               1 ", resultLines[7]);
        }

    }

    @SuppressWarnings("unchecked")
    private BooleanAnalyzerResult getPartialResult(AnalysisJobBuilder jobBuilder, Integer firstRow, Integer maxRows)
            throws Throwable {

        final InputColumn<?>[] inputColumns = jobBuilder.getSourceColumns().toArray(new InputColumn[2]);
        final FilterComponentBuilder<MaxRowsFilter, Category> maxRowsFilter = jobBuilder.addFilter(MaxRowsFilter.class);
        maxRowsFilter.addInputColumn(inputColumns[0]);
        if (firstRow != null) {
            maxRowsFilter.setConfiguredProperty("First row", firstRow);
        }
        if (maxRows != null) {
            maxRowsFilter.setConfiguredProperty("Max rows", maxRows);
        }
        maxRowsFilter.setConfiguredProperty("Order column", null);
        final TransformerComponentBuilder<ConvertToBooleanTransformer> convertToBoolean = jobBuilder
                .addTransformer(ConvertToBooleanTransformer.class);
        convertToBoolean.addInputColumns(inputColumns);
        final FilterOutcome filterOutcome = maxRowsFilter.getFilterOutcome(MaxRowsFilter.Category.VALID);
        convertToBoolean.setRequirement(filterOutcome);
        final AnalyzerComponentBuilder<BooleanAnalyzer> booleanAnalyzerBuilder = jobBuilder
                .addAnalyzer(BooleanAnalyzer.class);
        final BooleanAnalyzer booleanAnalyzer = booleanAnalyzerBuilder.getComponentInstance();
        booleanAnalyzer._columns = convertToBoolean.getOutputColumns().toArray(new MutableInputColumn[2]);
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

    private AnalysisJobBuilder getAnalysisJobBuilder() {
        final File file = new File("src/test/resources/testBooleanAnalyzer.txt");
        assertTrue(file.exists());
        final Datastore datastore = new CsvDatastore("test", new FileResource(file));
        final DataCleanerConfigurationImpl configuration = new DataCleanerConfigurationImpl()
                .withDatastoreCatalog(new DatastoreCatalogImpl(datastore));
        final AnalysisJobBuilder jobBuilder = new AnalysisJobBuilder(configuration);
        jobBuilder.setDatastore(datastore);
        jobBuilder.addSourceColumns("b1");
        jobBuilder.addSourceColumns("b2");
        return jobBuilder;
    }
}
