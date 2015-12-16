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
        final AnalysisJobBuilder jobBuilder1 = getAnalysisJobBuilder();
        final AnalysisJobBuilder jobBuilder2 = getAnalysisJobBuilder();
        final AnalysisJobBuilder jobBuilder3 = getAnalysisJobBuilder();
        
        final ReferentialIntegrityAnalyzerResult partialResult1 = getPartialResult(jobBuilder1, 1, 22);
        final ReferentialIntegrityAnalyzerResult partialResult2 = getPartialResult(jobBuilder2, 23, 1);
        final ReferentialIntegrityAnalyzerResult partialResult3 = getPartialResult(jobBuilder3, 24, null);
        
        // Assert what we have in the first partial result
        {
            final InputColumn<?> salesRepEmployeeNumber = jobBuilder1.getSourceColumnByName("SALESREPEMPLOYEENUMBER");
            final int annotatedRowCount = partialResult1.getAnnotatedRowCount();
            assertEquals(1, annotatedRowCount);
            
            final List<InputRow> rows = partialResult1.getSampleRows();
            assertEquals(1, rows.size());
            assertEquals(-1000, rows.get(0).getValue(salesRepEmployeeNumber));
        }
        // Assert what we have in the second partial result
        {
            final InputColumn<?> salesRepEmployeeNumber = jobBuilder2.getSourceColumnByName("SALESREPEMPLOYEENUMBER");
            final int annotatedRowCount = partialResult2.getAnnotatedRowCount();
            assertEquals(1, annotatedRowCount);
            
            final List<InputRow> rows = partialResult2.getSampleRows();
            assertEquals(1, rows.size());
            assertEquals(-1, rows.get(0).getValue(salesRepEmployeeNumber));
        }
        // Assert what we have in the thrird partial result
        {
            final InputColumn<?> salesRepEmployeeNumber = jobBuilder3.getSourceColumnByName("SALESREPEMPLOYEENUMBER");
            int annotatedRowCount = partialResult3.getAnnotatedRowCount();
            assertEquals(1, annotatedRowCount);
            
            final List<InputRow> rows = partialResult3.getSampleRows();
            assertEquals(1, rows.size());
            assertEquals(-1, rows.get(0).getValue(salesRepEmployeeNumber));
        }
        
        final Collection<ReferentialIntegrityAnalyzerResult> partialResults = new ArrayList<>();
        partialResults.add(partialResult1);
        partialResults.add(partialResult2);
        partialResults.add(partialResult3);
        
        final ReferentialIntegrityAnalyzerReducer reducer = new ReferentialIntegrityAnalyzerReducer();
        final ReferentialIntegrityAnalyzerResult reducedResult = reducer.reduce(partialResults);
        
        // Assert what we have in the reduced result
        {
            final InputColumn<?> salesRepEmployeeNumber = jobBuilder1.getSourceColumnByName("SALESREPEMPLOYEENUMBER");
            final int annotatedRowCount = reducedResult.getAnnotatedRowCount();
            assertEquals(3, annotatedRowCount);
            
            final List<InputRow> rows = reducedResult.getSampleRows();
            assertEquals(3, rows.size());
            assertEquals(-1000, rows.get(0).getValue(salesRepEmployeeNumber));
            assertEquals(-1, rows.get(1).getValue(salesRepEmployeeNumber));
            assertEquals(-1, rows.get(2).getValue(salesRepEmployeeNumber));
        }
    }
    
    private AnalysisJobBuilder getAnalysisJobBuilder() {
        final Datastore datastore = TestHelper.createSampleDatabaseDatastore("orderdb");
        
        final DataCleanerConfigurationImpl configuration = new DataCleanerConfigurationImpl()
                .withDatastoreCatalog(new DatastoreCatalogImpl(datastore));
        final AnalysisJobBuilder jobBuilder = new AnalysisJobBuilder(configuration);

        jobBuilder.setDatastore(datastore);
        jobBuilder.addSourceColumns("customers.CUSTOMERNUMBER");
        jobBuilder.addSourceColumns("customers.SALESREPEMPLOYEENUMBER");
        
        return jobBuilder;
    }

    private ReferentialIntegrityAnalyzerResult getPartialResult(AnalysisJobBuilder jobBuilder, Integer firstRow, Integer maxRows) throws Throwable {
        final InputColumn<?> salesRepEmployeeNumber = jobBuilder.getSourceColumnByName("SALESREPEMPLOYEENUMBER");
        final FilterComponentBuilder<MaxRowsFilter, Category> maxRowsFilter = jobBuilder.addFilter(MaxRowsFilter.class);
        maxRowsFilter.addInputColumn(salesRepEmployeeNumber);
        if (firstRow != null) {
            maxRowsFilter.setConfiguredProperty("First row", firstRow);
        }
        if (maxRows != null) {
            maxRowsFilter.setConfiguredProperty("Max rows", maxRows);
        }

        final AnalyzerComponentBuilder<ReferentialIntegrityAnalyzer> referentialIntegrityAnalyzer = jobBuilder
                .addAnalyzer(ReferentialIntegrityAnalyzer.class);
        referentialIntegrityAnalyzer.setRequirement(maxRowsFilter.getFilterOutcome(MaxRowsFilter.Category.VALID));
        final ReferentialIntegrityAnalyzer referentialIntegrity = referentialIntegrityAnalyzer.getComponentInstance();
        referentialIntegrity.foreignKey = salesRepEmployeeNumber;
        referentialIntegrity.cacheLookups = true;
        referentialIntegrity.datastore = jobBuilder.getDatastore();
        referentialIntegrity.schemaName = "PUBLIC";
        referentialIntegrity.tableName = "employees";
        referentialIntegrity.columnName = "EMPLOYEENUMBER";

        final AnalysisJob analysisJob = jobBuilder.toAnalysisJob();

        jobBuilder.close();

        final AnalysisResultFuture resultFuture = new AnalysisRunnerImpl(jobBuilder.getConfiguration()).run(analysisJob);

        resultFuture.await();

        if (resultFuture.isErrornous()) {
            throw resultFuture.getErrors().get(0);
        }

        final ReferentialIntegrityAnalyzerResult result = resultFuture.getResults(ReferentialIntegrityAnalyzerResult.class)
                .get(0);

        return result;
    }

}
