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
package org.datacleaner.beans.standardize;

import java.util.Arrays;

import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.data.MockInputRow;

import junit.framework.TestCase;

public class UrlStandardizerTransformerTest extends TestCase {

    UrlStandardizerTransformer transformer;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        transformer = new UrlStandardizerTransformer();
    }

    public void testGetOutputColumns() throws Exception {
        final OutputColumns outputColumns = transformer.getOutputColumns();
        assertEquals(5, outputColumns.getColumnCount());
        assertEquals("Protocol", outputColumns.getColumnName(0));
        assertEquals("Domain", outputColumns.getColumnName(1));
        assertEquals("Port", outputColumns.getColumnName(2));
        assertEquals("Path", outputColumns.getColumnName(3));
        assertEquals("Querystring", outputColumns.getColumnName(4));
    }

    public void testTransformValidUrls() throws Exception {
        Object[] result;

        result = transformer.transform("http://www.google.com/search?q=eobjects");
        assertEquals(5, result.length);
        assertEquals("[http, www.google.com, null, /search, q=eobjects]", Arrays.toString(result));

        result = transformer.transform("https://localhost");
        assertEquals("[https, localhost, null, null, null]", Arrays.toString(result));

        result = transformer.transform("http://localhost:8080");
        assertEquals("[http, localhost, 8080, null, null]", Arrays.toString(result));

        result = transformer.transform("http://www.yahoo.com/");
        assertEquals("[http, www.yahoo.com, null, /, null]", Arrays.toString(result));

        result = transformer.transform("http://www.rethe.com/ref=bleh/234-2565344-2354454");
        assertEquals("[http, www.rethe.com, null, /ref=bleh/234-2565344-2354454, null]", Arrays.toString(result));

        result = transformer.transform("https://www.ghzsffs.com/gswdp/nav/redir.html/ref=some-page");
        assertEquals("[https, www.ghzsffs.com, null, /gswdp/nav/redir.html/ref=some-page, null]",
                Arrays.toString(result));

        result = transformer.transform("http://localhost:8080/trac");
        assertEquals("[http, localhost, 8080, /trac, null]", Arrays.toString(result));

        result = transformer.transform("http://eobjects.org/trac/ticket/395#comment:1");
        assertEquals("[http, eobjects.org, null, /trac/ticket/395, null]", Arrays.toString(result));

        result = transformer.transform("http://localhost?string=hello%20world");
        assertEquals("[http, localhost, null, null, string=hello%20world]", Arrays.toString(result));

        result = transformer.transform("https://foo.bar.foobar.w00p:1234/hello/world?who=eobjects");
        assertEquals("[https, foo.bar.foobar.w00p, 1234, /hello/world, who=eobjects]", Arrays.toString(result));

        result = transformer.transform("ftp://username@hostname/path");
        assertEquals("[ftp, hostname, null, /path, null]", Arrays.toString(result));
    }

    // Best effort
    public void testInvalidUrls() throws Exception {
        Object[] result;

        // white space is not allowed
        result = transformer.transform("http://www.google com/search?q=eobjects");
        assertEquals(5, result.length);
        assertEquals("[null, null, null, null, null]", Arrays.toString(result));

        // semicolon is not a valid port delim
        result = transformer.transform("http://www.google.com;8080/search?q=eobjects");
        assertEquals(5, result.length);
        assertEquals("[http, null, null, /search, q=eobjects]", Arrays.toString(result));
    }

    public void testTransformNull() throws Exception {
        final Object[] result = transformer.transform((InputRow) new MockInputRow());
        assertEquals(5, result.length);
        assertEquals("[null, null, null, null, null]", Arrays.toString(result));
    }
}
