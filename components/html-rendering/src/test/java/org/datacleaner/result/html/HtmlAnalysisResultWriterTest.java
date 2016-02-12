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
package org.datacleaner.result.html;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import org.apache.metamodel.util.FileHelper;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.configuration.DataCleanerEnvironment;
import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.connection.Datastore;
import org.datacleaner.descriptors.Descriptors;
import org.datacleaner.descriptors.SimpleDescriptorProvider;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.runner.AnalysisRunnerImpl;
import org.datacleaner.result.AnalysisResult;
import org.datacleaner.result.SimpleAnalysisResult;
import org.datacleaner.result.renderer.ListResultHtmlRenderer;
import org.datacleaner.test.MockAnalyzer;
import org.datacleaner.test.TestHelper;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

public class HtmlAnalysisResultWriterTest {

    @Rule
    public TestName testName = new TestName();

    @Test
    public void testEmptyResult() throws IOException {
        final AnalysisResult analysisResult = new SimpleAnalysisResult();
        final DataCleanerConfigurationImpl configuration = new DataCleanerConfigurationImpl();

        writeAndCompareWithBenchmark(analysisResult, configuration);
    }

    @Test
    public void testSingleResultElement() throws IOException {
        final Datastore datastore = TestHelper.createSampleDatabaseDatastore("orderdb");
        final SimpleDescriptorProvider descriptorProvider = new SimpleDescriptorProvider();
        descriptorProvider.addRendererBeanDescriptor(Descriptors.ofRenderer(ListResultHtmlRenderer.class));

        final DataCleanerEnvironment environment = new DataCleanerEnvironmentImpl()
                .withDescriptorProvider(descriptorProvider);
        final DataCleanerConfigurationImpl configuration = new DataCleanerConfigurationImpl().withDatastores(datastore)
                .withEnvironment(environment);
        final AnalysisJob job;
        try (AnalysisJobBuilder jobBuilder = new AnalysisJobBuilder(configuration)) {
            jobBuilder.setDatastore(datastore);
            jobBuilder.addSourceColumns("customers.customername");

            jobBuilder.addAnalyzer(MockAnalyzer.class).addInputColumns(jobBuilder.getSourceColumns());

            job = jobBuilder.toAnalysisJob();
        }

        final AnalysisResult analysisResult = new AnalysisRunnerImpl(configuration).run(job);

        writeAndCompareWithBenchmark(analysisResult, configuration);
    }

    private void writeAndCompareWithBenchmark(AnalysisResult analysisResult, DataCleanerConfigurationImpl configuration)
            throws IOException {

        final HtmlAnalysisResultWriter writer = new HtmlAnalysisResultWriter();

        final StringWriter stringWriter = new StringWriter();
        writer.write(analysisResult, configuration, stringWriter);
        final String actual = stringWriter.toString();

        final File benchmarkFile = new File("src/test/resources/benchmark-renderings/" + getClass().getSimpleName()
                + "-" + testName.getMethodName() + ".html");
        if (!benchmarkFile.exists()) {
            Assert.assertEquals("File does not exist: " + benchmarkFile, actual);
        }
        final String expected = FileHelper.readFileAsString(benchmarkFile);

        Assert.assertEquals(expected.replaceAll("\r\n", "\n"), actual.replaceAll("\r\n", "\n"));
    }
}
