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
package org.datacleaner.monitor.configuration;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.apache.metamodel.util.FileHelper;

import junit.framework.TestCase;

public class WriteDefaultTenantConfigurationActionTest extends TestCase {

    public void testWrite() throws Exception {
        WriteDefaultTenantConfigurationAction action = new WriteDefaultTenantConfigurationAction();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        action.run(out);

        String result = FileHelper.readInputStreamAsString(new ByteArrayInputStream(out.toByteArray()), "UTF8");
        assertNotNull(result);
        assertTrue(result, result.replaceAll("\r\n", "\n").startsWith(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<configuration"));
    }
}
