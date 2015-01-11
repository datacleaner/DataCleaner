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

import junit.framework.TestCase;

import org.datacleaner.api.InputColumn;
import org.datacleaner.beans.StringAnalyzerResult;
import org.datacleaner.configuration.AnalyzerBeansConfiguration;
import org.datacleaner.configuration.AnalyzerBeansConfigurationImpl;
import org.datacleaner.connection.CsvDatastore;
import org.datacleaner.connection.DatastoreCatalogImpl;
import org.datacleaner.descriptors.ClasspathScanDescriptorProvider;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.JaxbJobReader;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.job.runner.AnalysisRunner;
import org.datacleaner.job.runner.AnalysisRunnerImpl;

public class ComponentsWithoutInputColumnsTest extends TestCase {

	public void testScenario() throws Throwable {
		CsvDatastore datastore = new CsvDatastore("my database",
				"../core/src/test/resources/example-name-lengths.csv");
		ClasspathScanDescriptorProvider descriptorProvider = new ClasspathScanDescriptorProvider().scanPackage("org.datacleaner", true);
		AnalyzerBeansConfiguration configuration = new AnalyzerBeansConfigurationImpl()
				.replace(new DatastoreCatalogImpl(datastore)).replace(descriptorProvider);
		AnalysisJob job = new JaxbJobReader(configuration)
				.read(new FileInputStream(
						"src/test/resources/example-job-components-without-inputcolumns.xml"));

		AnalysisRunner runner = new AnalysisRunnerImpl(configuration);
		AnalysisResultFuture resultFuture = runner.run(job);

		if (!resultFuture.isSuccessful()) {
			throw resultFuture.getErrors().get(0);
		}

		InputColumn<?>[] input = job.getAnalyzerJobs().iterator().next()
				.getInput();
		assertEquals(4, input.length);

		StringAnalyzerResult result = (StringAnalyzerResult) resultFuture
				.getResults().get(0);
		for (int i = 0; i < input.length; i++) {
			assertEquals(5, result.getRowCount(input[i]));
		}
	}
}
