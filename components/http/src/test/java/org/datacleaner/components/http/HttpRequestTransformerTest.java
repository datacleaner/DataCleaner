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

import static org.junit.Assert.*;

import java.util.Arrays;

import org.apache.http.impl.client.HttpClients;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;
import org.junit.Ignore;
import org.junit.Test;

public class HttpRequestTransformerTest {

    @Test
    public void testGetOutputColumns() throws Exception {
        HttpRequestTransformer t = new HttpRequestTransformer();
        OutputColumns outputColumns = t.getOutputColumns();
        assertEquals("OutputColumns[Response status code, Response body]", outputColumns.toString());
    }

    @Test
    public void testDefaultCharset() throws Exception {
        HttpRequestTransformer t = new HttpRequestTransformer();
        assertEquals("ISO-8859-1", t.charset);
    }

    @Test
    public void testApplyVariablesToString() throws Exception {
        HttpRequestTransformer t = new HttpRequestTransformer();
        t.setHttpClient(HttpClients.createSystem());
        t.setMethod(HttpMethod.GET);
        t.setRequestBody("Hello ${name}! Dear ${name} would you like some ${product}?");
        final InputColumn<?> col1 = new MockInputColumn<>("foo");
        t.setInputAndVariables(new InputColumn[] { col1 }, new String[] { "${name}" });

        t.init();

        assertEquals("Hello world! Dear world would you like some ${product}?",
                t.applyVariablesToString(t.requestBody, new MockInputRow().put(col1, "world")));
        assertEquals("Hello ! Dear  would you like some ${product}?",
                t.applyVariablesToString(t.requestBody, new MockInputRow().put(col1, null)));

        final InputColumn<?> col2 = new MockInputColumn<>("bar");
        t.setInputAndVariables(new InputColumn[] { col1, col2 }, new String[] { "${name}", "${product}" });
        assertEquals("Hello customer! Dear customer would you like some tea?",
                t.applyVariablesToString(t.requestBody, new MockInputRow().put(col1, "customer").put(col2, "tea")));

        t.close();
    }

    @Test
    @Ignore
    public void testIntegrationScenarioWithLocalTomcat() throws Exception {
        HttpRequestTransformer t = new HttpRequestTransformer();
        t.setHttpClient(HttpClients.createSystem());
        t.setMethod(HttpMethod.GET);
        t.setRequestBody("Hello ${name}");
        t.setUrl("http://localhost:8080");
        final InputColumn<?> col1 = new MockInputColumn<>("foo");
        t.setInputAndVariables(new InputColumn[] { col1 }, new String[] { "${name}" });

        t.init();

        Object[] result = t.transform(new MockInputRow().put(col1, "world"));
        t.close();

        assertEquals("[200, Hello world!]", Arrays.toString(result));
    }
}
