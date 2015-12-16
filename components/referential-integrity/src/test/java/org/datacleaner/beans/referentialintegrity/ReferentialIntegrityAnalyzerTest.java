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

import java.util.List;

import junit.framework.TestCase;

import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalogImpl;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.job.runner.AnalysisRunnerImpl;
import org.datacleaner.test.TestHelper;

public class ReferentialIntegrityAnalyzerTest extends TestCase {

    public void testSimpleScenario() throws Throwable {
        Datastore datastore = TestHelper.createSampleDatabaseDatastore("orderdb");

        DataCleanerConfigurationImpl configuration = new DataCleanerConfigurationImpl()
                .withDatastoreCatalog(new DatastoreCatalogImpl(datastore));
        AnalysisJobBuilder jobBuilder = new AnalysisJobBuilder(configuration);

        jobBuilder.setDatastore(datastore);
        jobBuilder.addSourceColumns("customers.SALESREPEMPLOYEENUMBER");

        AnalyzerComponentBuilder<ReferentialIntegrityAnalyzer> analyzer = jobBuilder
                .addAnalyzer(ReferentialIntegrityAnalyzer.class);
        ReferentialIntegrityAnalyzer referentialIntegrity = analyzer.getComponentInstance();
        InputColumn<?> salesRepEmployeeNumber = jobBuilder.getSourceColumnByName("SALESREPEMPLOYEENUMBER");
        referentialIntegrity.foreignKey = salesRepEmployeeNumber;
        referentialIntegrity.cacheLookups = true;
        referentialIntegrity.datastore = datastore;
        referentialIntegrity.schemaName = "PUBLIC";
        referentialIntegrity.tableName = "employees";
        referentialIntegrity.columnName = "EMPLOYEENUMBER";

        AnalysisJob analysisJob = jobBuilder.toAnalysisJob();

        jobBuilder.close();

        AnalysisResultFuture resultFuture = new AnalysisRunnerImpl(configuration).run(analysisJob);

        resultFuture.await();

        if (resultFuture.isErrornous()) {
            throw resultFuture.getErrors().get(0);
        }

        ReferentialIntegrityAnalyzerResult result = resultFuture.getResults(ReferentialIntegrityAnalyzerResult.class)
                .get(0);

        int annotatedRowCount = result.getAnnotatedRowCount();
        assertEquals(3, annotatedRowCount);

        List<InputRow> rows = result.getSampleRows();
        assertEquals(-1, rows.get(0).getValue(salesRepEmployeeNumber));
        assertEquals(-1000, rows.get(1).getValue(salesRepEmployeeNumber));
        assertEquals(-1, rows.get(2).getValue(salesRepEmployeeNumber));
    }

}
