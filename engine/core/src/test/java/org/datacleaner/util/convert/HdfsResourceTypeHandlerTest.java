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
package org.datacleaner.util.convert;

import static org.junit.Assert.*;

import org.apache.metamodel.util.HdfsResource;
import org.junit.Test;

public class HdfsResourceTypeHandlerTest {

    @Test
    public void testToAndFromString() throws Exception {
        final HdfsResourceTypeHandler typeHandler = new HdfsResourceTypeHandler();

        final HdfsResource resource1 = new HdfsResource("localhost", 9000, "/foo.bar.txt");
        assertTrue(typeHandler.isParserFor(resource1.getClass()));
        
        final String path = typeHandler.createPath(resource1);
        assertEquals("hdfs://localhost:9000/foo.bar.txt", path);
        
        final HdfsResource resource2 = typeHandler.parsePath(path);
        
        assertEquals(resource2, resource1);
    }
}
