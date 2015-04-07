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
package org.datacleaner.job.runner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;

import org.datacleaner.api.InputColumn;
import org.datacleaner.beans.BooleanAnalyzer;
import org.datacleaner.beans.standardize.EmailStandardizerTransformer;
import org.datacleaner.beans.transform.DictionaryMatcherTransformer;
import org.datacleaner.configuration.AnalyzerBeansConfigurationImpl;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalogImpl;
import org.datacleaner.data.MutableInputColumn;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.TransformerComponentBuilder;
import org.datacleaner.reference.Dictionary;
import org.datacleaner.test.TestHelper;

public class ReferenceDataActivationManagerTest extends TestCase {

    public void testInvocationThroughAnalysisRunner() throws Throwable {
        MockMonitoredDictionary dict1 = new MockMonitoredDictionary();
        MockMonitoredDictionary dict2 = new MockMonitoredDictionary();
        MockMonitoredDictionary dict3 = new MockMonitoredDictionary();
        assertEquals(0, dict1.getInitCount());
        assertEquals(0, dict1.getCloseCount());
        assertEquals(0, dict2.getInitCount());
        assertEquals(0, dict2.getCloseCount());
        assertEquals(0, dict3.getInitCount());
        assertEquals(0, dict3.getCloseCount());

        Collection<Dictionary> dictionaries = new ArrayList<Dictionary>();
        dictionaries.add(dict1);
        dictionaries.add(dict2);
        dictionaries.add(dict3);

        Datastore datastore = TestHelper.createSampleDatabaseDatastore("db");

        DataCleanerConfiguration configuration = new AnalyzerBeansConfigurationImpl()
                .replace(new DatastoreCatalogImpl(datastore));

        AnalysisRunner runner = new AnalysisRunnerImpl(configuration);

        // build a job
        AnalysisJobBuilder ajb = new AnalysisJobBuilder(configuration);
        ajb.setDatastore(datastore);
        ajb.addSourceColumn(datastore.openConnection().getSchemaNavigator().convertToColumn("PUBLIC.EMPLOYEES.EMAIL"));

        InputColumn<?> emailColumn = ajb.getSourceColumnByName("email");
        assertNotNull(emailColumn);

        MutableInputColumn<?> usernameColumn = ajb.addTransformer(EmailStandardizerTransformer.class)
                .addInputColumn(emailColumn).getOutputColumnByName("Username");
        assertNotNull(usernameColumn);

        TransformerComponentBuilder<DictionaryMatcherTransformer> tjb = ajb
                .addTransformer(DictionaryMatcherTransformer.class);
        DictionaryMatcherTransformer transformer = tjb.getComponentInstance();
        transformer.setDictionaries(new Dictionary[] { dict1, dict2, dict3 });
        tjb.addInputColumn(usernameColumn);
        List<MutableInputColumn<?>> outputColumns = tjb.getOutputColumns();

        ajb.addAnalyzer(BooleanAnalyzer.class).addInputColumns(outputColumns);
        AnalysisJob job = ajb.toAnalysisJob();
        ajb.close();

        AnalysisResultFuture result = runner.run(job);

        if (!result.isSuccessful()) {
            throw result.getErrors().get(0);
        }

        // sleep to ensure that close is invoked (happens after results are
        // returned)
        Thread.sleep(700);

        assertEquals(1, dict1.getInitCount());
        assertEquals(1, dict1.getCloseCount());
        assertEquals(1, dict2.getInitCount());
        assertEquals(1, dict2.getCloseCount());
        assertEquals(1, dict3.getInitCount());
        assertEquals(1, dict3.getCloseCount());

        result = runner.run(job);

        if (!result.isSuccessful()) {
            throw result.getErrors().get(0);
        }

        // sleep to ensure that close is invoked (happens after results are
        // returned)
        Thread.sleep(700);

        assertEquals(2, dict1.getInitCount());
        assertEquals(2, dict1.getCloseCount());
        assertEquals(2, dict2.getInitCount());
        assertEquals(2, dict2.getCloseCount());
        assertEquals(2, dict3.getInitCount());
        assertEquals(2, dict3.getCloseCount());
    }
}
