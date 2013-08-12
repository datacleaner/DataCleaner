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

import junit.framework.TestCase;

import org.elasticsearch.common.collect.MapBuilder;
import org.eobjects.analyzer.beans.api.OutputColumns;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;

public class ElasticSearchFullSearchTransformerTest extends TestCase {

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

    public void testTransform() throws Exception {
        final InputColumn<String> col1 = new MockInputColumn<String>("col1");

        final ElasticSearchFullSearchTransformer transformer = new ElasticSearchFullSearchTransformer();
        transformer.searchInput = col1;
        transformer.documentType = ElasticSearchTestServer.DOCUMENT_TYPE;
        transformer.indexName = ElasticSearchTestServer.INDEX_NAME;
        transformer.clusterName = ElasticSearchTestServer.CLUSTER_NAME;
        transformer.clusterHosts = new String[] { "localhost:" + ElasticSearchTestServer.TRANSPORT_PORT };

        OutputColumns out = transformer.getOutputColumns();
        assertEquals("OutputColumns[Document ID, Document]", out.toString());

        try {
            transformer.init();

            _server.truncateIndex();
            assertEquals(0, _server.getDocumentCount());

            _server.addDocument("cph", MapBuilder.newMapBuilder().put("city", "Copenhagen").put("country", "Denmark")
                    .map());
            _server.addDocument("ams", MapBuilder.newMapBuilder().put("city", "Amsterdam")
                    .put("country", "Netherlands").map());
            _server.addDocument("del", MapBuilder.newMapBuilder().put("city", "Delhi").put("country", "India").map());

            Object[] output = transformer.transform(new MockInputRow().put(col1, "Copenhagen"));
            assertEquals("cph", String.valueOf(output[0]));
            assertEquals("{city=Copenhagen, country=Denmark}", String.valueOf(output[1]));
            
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
