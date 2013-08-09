/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.extension.elasticsearch;

import java.io.File;
import java.util.List;

import junit.framework.TestCase;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.eobjects.analyzer.beans.filter.MaxRowsFilter;
import org.eobjects.analyzer.beans.writers.WriteDataResult;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl;
import org.eobjects.analyzer.connection.CsvDatastore;
import org.eobjects.analyzer.connection.DatastoreCatalogImpl;
import org.eobjects.analyzer.descriptors.Descriptors;
import org.eobjects.analyzer.descriptors.SimpleDescriptorProvider;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.JaxbJobReader;
import org.eobjects.analyzer.job.concurrent.MultiThreadedTaskRunner;
import org.eobjects.analyzer.job.concurrent.TaskRunner;
import org.eobjects.analyzer.job.runner.AnalysisResultFuture;
import org.eobjects.analyzer.job.runner.AnalysisRunnerImpl;
import org.eobjects.metamodel.util.FileResource;
import org.eobjects.metamodel.util.Resource;

public class ElasticSearchIndexAnalyzerIntegrationTest extends TestCase {

    private ElasticSearchTestServer _server;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        _server = new ElasticSearchTestServer();
        _server.startup();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        _server.close();
    }

    public void testIndex() throws Throwable {
        final Resource resource = new FileResource("src/test/resources/AddressAccess.csv");
        final String filename = "AddressAccess.csv";
        final CsvDatastore ds = new CsvDatastore("AddressAccess.csv", resource, filename, '"', ';', '\\', "UTF8", true,
                1);

        final SimpleDescriptorProvider descriptorProvider = new SimpleDescriptorProvider();
        descriptorProvider.addAnalyzerBeanDescriptor(Descriptors.ofAnalyzer(ElasticSearchIndexAnalyzer.class));
        descriptorProvider.addFilterBeanDescriptor(Descriptors.ofFilter(MaxRowsFilter.class));

        final TaskRunner taskRunner = new MultiThreadedTaskRunner(10);
        final AnalyzerBeansConfiguration conf = new AnalyzerBeansConfigurationImpl().replace(taskRunner)
                .replace(new DatastoreCatalogImpl(ds)).replace(descriptorProvider);

        final AnalysisJob job = new JaxbJobReader(conf).create(new File("src/test/resources/es_test.analysis.xml"))
                .toAnalysisJob();
        
        _server.truncateIndex();

        final AnalysisResultFuture resultFuture = new AnalysisRunnerImpl(conf).run(job);

        resultFuture.await();

        if (resultFuture.isErrornous()) {
            List<Throwable> errors = resultFuture.getErrors();
            for (Throwable error : errors) {
                error.printStackTrace();
            }
            throw errors.get(0);
        }

        WriteDataResult result = (WriteDataResult) resultFuture.getResults().get(0);
        assertEquals(8, result.getWrittenRowCount());
        
        assertEquals(8, _server.getDocumentCount());

        final Client client = _server.getClient();
        try {
            SearchResponse searchResponse = new SearchRequestBuilder(client)
                    .setIndices(ElasticSearchTestServer.INDEX_NAME).setTypes(ElasticSearchTestServer.DOCUMENT_TYPE)
                    .setQuery(QueryBuilders.queryString("Allersgade")).execute().actionGet();
            assertEquals(2l, searchResponse.getHits().getTotalHits());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            client.close();
        }
    }
}
