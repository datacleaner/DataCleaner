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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.beans.valuedist.ValueDistributionAnalyzerResult;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.connection.DatastoreCatalogImpl;
import org.datacleaner.descriptors.ClasspathScanDescriptorProvider;
import org.datacleaner.descriptors.DescriptorProvider;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.JaxbJobReader;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.job.runner.AnalysisRunner;
import org.datacleaner.job.runner.AnalysisRunnerImpl;
import org.datacleaner.result.CrosstabResult;
import org.datacleaner.result.renderer.CrosstabTextRenderer;
import org.datacleaner.test.TestHelper;

import junit.framework.TestCase;

public class AnalyzerJobPartitioningTest extends TestCase {

    public void testScenario() throws Exception {
        final DatastoreCatalog datastoreCatalog =
                new DatastoreCatalogImpl(TestHelper.createSampleDatabaseDatastore("my database"));
        final DescriptorProvider descriptorProvider =
                new ClasspathScanDescriptorProvider().scanPackage("org.datacleaner.beans", true);
        final DataCleanerConfiguration conf = new DataCleanerConfigurationImpl().withDatastoreCatalog(datastoreCatalog)
                .withEnvironment(new DataCleanerEnvironmentImpl().withDescriptorProvider(descriptorProvider));

        final AnalysisRunner runner = new AnalysisRunnerImpl(conf);

        final AnalysisJobBuilder jobBuilder =
                new JaxbJobReader(conf).create(new File("src/test/resources/example-job-partitioning.xml"));

        final AnalysisJob analysisJob = jobBuilder.toAnalysisJob();
        assertEquals(6, analysisJob.getAnalyzerJobs().size());

        final AnalysisResultFuture resultFuture = runner.run(analysisJob);
        assertTrue(resultFuture.isSuccessful());

        final List<AnalyzerResult> results = resultFuture.getResults();

        int vdResults = 0;
        final List<CrosstabResult> saResults = new ArrayList<>();

        for (final AnalyzerResult analyzerResult : results) {
            if (analyzerResult instanceof ValueDistributionAnalyzerResult) {
                vdResults++;
            } else if (analyzerResult instanceof CrosstabResult) {
                saResults.add((CrosstabResult) analyzerResult);
            } else {
                fail("Unexpected result: " + analyzerResult);
            }
        }

        assertEquals(4, vdResults);
        assertEquals(2, saResults.size());

        final int dimensionIndex = saResults.get(0).getCrosstab().getDimensionIndex("Column");

        Collections.sort(saResults, (o1, o2) -> {
            final int count1 = o1.getCrosstab().getDimension(dimensionIndex).getCategoryCount();
            final int count2 = o2.getCrosstab().getDimension(dimensionIndex).getCategoryCount();
            return count1 - count2;
        });

        final String[] resultLines1 = new CrosstabTextRenderer().render(saResults.get(0)).split("\n");
        assertEquals("                                      CUSTOMERNAME ", resultLines1[0]);
        assertEquals("Row count                                      214 ", resultLines1[1]);

        final String[] resultLines2 = new CrosstabTextRenderer().render(saResults.get(1)).split("\n");
        assertEquals("                                      FIRSTNAME  LASTNAME     EMAIL ", resultLines2[0]);
        assertEquals("Row count                                    23        23        23 ", resultLines2[1]);
    }
}
