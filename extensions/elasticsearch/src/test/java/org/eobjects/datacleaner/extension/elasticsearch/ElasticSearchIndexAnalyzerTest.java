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

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.data.MockInputRow;

public class ElasticSearchIndexAnalyzerTest extends TestCase {

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

    public void testIndex() throws Exception {
        final InputColumn<String> col1 = new MockInputColumn<String>("col1");
        final InputColumn<String> col2 = new MockInputColumn<String>("col2");
        final InputColumn<String> idCol = new MockInputColumn<String>("id");

        final ElasticSearchIndexAnalyzer analyzer = new ElasticSearchIndexAnalyzer();
        analyzer.idColumn = idCol;
        analyzer.fields = new String[] { "col1", "col2" };
        analyzer.values = new InputColumn[] { col1, col2 };
        analyzer.documentType = ElasticSearchTestServer.DOCUMENT_TYPE;
        analyzer.indexName = ElasticSearchTestServer.INDEX_NAME;
        analyzer.clusterName = ElasticSearchTestServer.CLUSTER_NAME;
        analyzer.clusterHosts = new String[] { "localhost:" + ElasticSearchTestServer.TRANSPORT_PORT };

        try {
            analyzer.init();

            _server.truncateIndex();
            assertEquals(0, _server.getDocumentCount());

            for (int i = 0; i < 200; i++) {
                analyzer.run(new MockInputRow().put(col1, "foo" + i).put(col2, "bar").put(idCol, "id_" + i), 1);
                analyzer.run(new MockInputRow().put(col1, "foobar" + i).put(col2, "baz").put(idCol, "key_" + i), 1);
            }

            int writtenRowCount = analyzer.getResult().getWrittenRowCount();
            assertEquals(400, writtenRowCount);

            assertEquals(400, _server.getDocumentCount());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
