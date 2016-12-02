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
package org.datacleaner.extension.networktools;

import static org.junit.Assert.assertEquals;

import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;
import org.datacleaner.test.TestHelper;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

public class ResolveHostnameTransformerTest {

    private final ResolveHostnameTransformer _transformer = new ResolveHostnameTransformer();
    private final MockInputColumn<String> _col = new MockInputColumn<>("host", String.class);

    @Before
    public void setUp() {
        _transformer.hostnameColumn = _col;
    }

    @Test
    public void testTransformLocal() throws Exception {
        assertEquals("127.0.0.1", _transformer.transform(new MockInputRow().put(_col, "localhost"))[0]);

        assertEquals("127.0.0.1", _transformer.transform(new MockInputRow().put(_col, "127.0.0.1"))[0]);

        assertEquals(null, _transformer.transform(new MockInputRow().put(_col, ""))[0]);

        assertEquals(null, _transformer.transform(new MockInputRow().put(_col, null))[0]);

        assertEquals(null, _transformer.transform(new MockInputRow().put(_col,
                "lmdslfsm flskmf lskmfls kmslf kdmlfsk mflsk fmsl kfdmsl"))[0]);
    }

    @Test
    public void testTransformInternet() throws Exception {
        Assume.assumeTrue(TestHelper.isInternetConnected());

        assertEquals("94.142.215.39", _transformer.transform(new MockInputRow().put(_col, "eobjects.org"))[0]);
    }

    @Test
    public void testGetOutputColumns() throws Exception {
        assertEquals("OutputColumns[host (ip address)]", _transformer.getOutputColumns().toString());
    }
}
