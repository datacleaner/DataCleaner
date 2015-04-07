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

import junit.framework.TestCase;

import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.configuration.DataCleanerEnvironment;
import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.descriptors.ClasspathScanDescriptorProvider;
import org.datacleaner.descriptors.DescriptorProvider;
import org.datacleaner.job.JaxbJobReader;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.runner.AnalysisRunner;
import org.datacleaner.job.runner.AnalysisRunnerImpl;
import org.datacleaner.test.TestHelper;

/**
 * Ticket #383: Error handling when a job has been errornously configured - the
 * input columns of a transformer originate from different tables
 */
public class InputColumnsFromDifferentTablesTest extends TestCase {

    public void testScenario() throws Exception {
        DescriptorProvider descriptorProvider = new ClasspathScanDescriptorProvider().scanPackage(
                "org.datacleaner.beans", true);
        DataCleanerEnvironment environment = new DataCleanerEnvironmentImpl()
                .withDescriptorProvider(descriptorProvider);
        DataCleanerConfiguration conf = new DataCleanerConfigurationImpl().withDatastores(
                TestHelper.createSampleDatabaseDatastore("my database")).withEnvironment(environment);

        AnalysisRunner runner = new AnalysisRunnerImpl(conf);

        AnalysisJobBuilder jobBuilder = new JaxbJobReader(conf).create(new File(
                "src/test/resources/example-job-input-columns-from-different-tables.xml"));

        try {
            runner.run(jobBuilder.toAnalysisJob());
            fail("exception expected");
        } catch (IllegalStateException e) {
            assertEquals(
                    "Multiple originating tables (CUSTOMERS, EMPLOYEES) found for source: TransformerComponentBuilder[transformer=Concatenator,inputColumns=[MetaModelInputColumn[PUBLIC.EMPLOYEES.LASTNAME], MetaModelInputColumn[PUBLIC.CUSTOMERS.CONTACTLASTNAME]]]",
                    e.getMessage());
        }

    }
}
