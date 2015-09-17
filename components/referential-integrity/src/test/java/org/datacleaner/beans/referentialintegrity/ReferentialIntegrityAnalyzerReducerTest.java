package org.datacleaner.beans.referentialintegrity;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
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
import org.datacleaner.test.TestHelper;
import org.junit.Test;

public class ReferentialIntegrityAnalyzerReducerTest {

    @Test
    public void testVanilla() throws Throwable {
        AnalysisJobBuilder jobBuilder1 = getAnalysisJobBuilder();
        AnalysisJobBuilder jobBuilder2 = getAnalysisJobBuilder();
        
        ReferentialIntegrityAnalyzerResult partialResult1 = getPartialResult(jobBuilder1, 1, 22);
        ReferentialIntegrityAnalyzerResult partialResult2 = getPartialResult(jobBuilder2, 23, null);
        
        // Assert what we have in the first partial result
        {
            InputColumn<?> salesRepEmployeeNumber = jobBuilder1.getSourceColumnByName("SALESREPEMPLOYEENUMBER");
            int annotatedRowCount = partialResult1.getAnnotatedRowCount();
            assertEquals(1, annotatedRowCount);
            
            List<InputRow> rows = partialResult1.getSampleRows();
            assertEquals(1, rows.size());
            assertEquals(-1000, rows.get(0).getValue(salesRepEmployeeNumber));
        }
        // Assert what we have in the second partial result
        {
            InputColumn<?> salesRepEmployeeNumber = jobBuilder2.getSourceColumnByName("SALESREPEMPLOYEENUMBER");
            int annotatedRowCount = partialResult2.getAnnotatedRowCount();
            assertEquals(2, annotatedRowCount);
            
            List<InputRow> rows = partialResult2.getSampleRows();
            assertEquals(2, rows.size());
            assertEquals(-1, rows.get(0).getValue(salesRepEmployeeNumber));
            assertEquals(-1, rows.get(1).getValue(salesRepEmployeeNumber));
        }
        
        Collection<ReferentialIntegrityAnalyzerResult> partialResults = new ArrayList<>();
        partialResults.add(partialResult1);
        partialResults.add(partialResult2);
        
        ReferentialIntegrityAnalyzerReducer reducer = new ReferentialIntegrityAnalyzerReducer();
        ReferentialIntegrityAnalyzerResult reducedResult = reducer.reduce(partialResults);
        
        // Assert what we have in the reduced result
        {
            InputColumn<?> salesRepEmployeeNumber = jobBuilder1.getSourceColumnByName("SALESREPEMPLOYEENUMBER");
            int annotatedRowCount = reducedResult.getAnnotatedRowCount();
            assertEquals(3, annotatedRowCount);
            
            List<InputRow> rows = reducedResult.getSampleRows();
            assertEquals(3, rows.size());
            assertEquals(-1000, rows.get(0).getValue(salesRepEmployeeNumber));
            assertEquals(-1, rows.get(1).getValue(salesRepEmployeeNumber));
            assertEquals(-1, rows.get(2).getValue(salesRepEmployeeNumber));
        }
    }

    private AnalysisJobBuilder getAnalysisJobBuilder() {
        Datastore datastore = TestHelper.createSampleDatabaseDatastore("orderdb");
        
        DataCleanerConfigurationImpl configuration = new DataCleanerConfigurationImpl()
                .withDatastoreCatalog(new DatastoreCatalogImpl(datastore));
        AnalysisJobBuilder jobBuilder = new AnalysisJobBuilder(configuration);

        jobBuilder.setDatastore(datastore);
        jobBuilder.addSourceColumns("customers.SALESREPEMPLOYEENUMBER");
        
        return jobBuilder;
    }

    private ReferentialIntegrityAnalyzerResult getPartialResult(AnalysisJobBuilder jobBuilder, Integer firstRow, Integer maxRows) throws Throwable {
        InputColumn<?> salesRepEmployeeNumber = jobBuilder.getSourceColumnByName("SALESREPEMPLOYEENUMBER");
        FilterComponentBuilder<MaxRowsFilter, Category> maxRowsFilter = jobBuilder.addFilter(MaxRowsFilter.class);
        maxRowsFilter.addInputColumn(salesRepEmployeeNumber);
        if (firstRow != null) {
            maxRowsFilter.setConfiguredProperty("First row", firstRow);
        }
        if (maxRows != null) {
            maxRowsFilter.setConfiguredProperty("Max rows", maxRows);
        }

        AnalyzerComponentBuilder<ReferentialIntegrityAnalyzer> referentialIntegrityAnalyzer = jobBuilder
                .addAnalyzer(ReferentialIntegrityAnalyzer.class);
        referentialIntegrityAnalyzer.setRequirement(maxRowsFilter.getFilterOutcome(MaxRowsFilter.Category.VALID));
        ReferentialIntegrityAnalyzer referentialIntegrity = referentialIntegrityAnalyzer.getComponentInstance();
        referentialIntegrity.foreignKey = salesRepEmployeeNumber;
        referentialIntegrity.cacheLookups = true;
        referentialIntegrity.datastore = jobBuilder.getDatastore();
        referentialIntegrity.schemaName = "PUBLIC";
        referentialIntegrity.tableName = "employees";
        referentialIntegrity.columnName = "EMPLOYEENUMBER";

        AnalysisJob analysisJob = jobBuilder.toAnalysisJob();

        jobBuilder.close();

        AnalysisResultFuture resultFuture = new AnalysisRunnerImpl(jobBuilder.getConfiguration()).run(analysisJob);

        resultFuture.await();

        if (resultFuture.isErrornous()) {
            throw resultFuture.getErrors().get(0);
        }

        ReferentialIntegrityAnalyzerResult result = resultFuture.getResults(ReferentialIntegrityAnalyzerResult.class)
                .get(0);

        return result;
    }

}
