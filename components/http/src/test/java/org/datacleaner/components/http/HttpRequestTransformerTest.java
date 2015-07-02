/**
 * DataCleaner (community edition) Copyright (C) 2014 Neopost - Customer
 * Information Management
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to: Free Software Foundation,
 * Inc. 51 Franklin Street, Fifth Floor Boston, MA 02110-1301 USA
 */
package org.datacleaner.components.http;

import java.util.Arrays;

import junit.framework.TestCase;

import org.apache.http.impl.client.DefaultHttpClient;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.data.MockInputRow;
import org.junit.Ignore;
import org.junit.Test;

public class HttpRequestTransformerTest extends TestCase {

    @Test
    public void testGetOutputColumns() throws Exception {
        HttpRequestTransformer t = new HttpRequestTransformer();
        OutputColumns outputColumns = t.getOutputColumns();
        assertEquals("OutputColumns[Response status, Response body]", outputColumns.toString());
    }

    @Test
    public void testDefaultCharset() throws Exception {
        HttpRequestTransformer t = new HttpRequestTransformer();
        assertEquals("ISO-8859-1", t.charset);
    }

    @Test
    @Ignore
    public void testIntegrationScenarioWithLocalTomcat() throws Exception {
        HttpRequestTransformer t = new HttpRequestTransformer();
        t.setHttpClient(new DefaultHttpClient());
        t.setMethod(HttpMethod.GET);
        t.setRequestBody("");
        t.setUrl("http://localhost:8080");

        t.init();
        Object[] result = t.transform(new MockInputRow());
        t.close();

        assertEquals("[200, Hello world!]", Arrays.toString(result));
    }
}
