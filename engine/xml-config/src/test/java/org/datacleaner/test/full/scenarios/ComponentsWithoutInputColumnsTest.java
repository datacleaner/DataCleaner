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

import java.io.FileInputStream;

import org.datacleaner.api.InputColumn;
import org.datacleaner.beans.StringAnalyzerResult;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.connection.CsvDatastore;
import org.datacleaner.descriptors.ClasspathScanDescriptorProvider;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.JaxbJobReader;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.job.runner.AnalysisRunner;
import org.datacleaner.job.runner.AnalysisRunnerImpl;

import junit.framework.TestCase;

public class ComponentsWithoutInputColumnsTest extends TestCase {

    public void testScenario() throws Throwable {
        final CsvDatastore datastore =
                new CsvDatastore("my database", "../core/src/test/resources/example-name-lengths.csv");
        final ClasspathScanDescriptorProvider descriptorProvider =
                new ClasspathScanDescriptorProvider().scanPackage("org.datacleaner", true);
        final DataCleanerConfiguration configuration = new DataCleanerConfigurationImpl().withDatastores(datastore)
                .withEnvironment(new DataCleanerEnvironmentImpl().withDescriptorProvider(descriptorProvider));
        final AnalysisJob job = new JaxbJobReader(configuration)
                .read(new FileInputStream("src/test/resources/example-job-components-without-inputcolumns.xml"));

        final AnalysisRunner runner = new AnalysisRunnerImpl(configuration);
        final AnalysisResultFuture resultFuture = runner.run(job);

        if (!resultFuture.isSuccessful()) {
            throw resultFuture.getErrors().get(0);
        }

        final InputColumn<?>[] input = job.getAnalyzerJobs().iterator().next().getInput();
        assertEquals(4, input.length);

        final StringAnalyzerResult result = (StringAnalyzerResult) resultFuture.getResults().get(0);
        for (int i = 0; i < input.length; i++) {
            assertEquals(5, result.getRowCount(input[i]));
        }
    }
}
