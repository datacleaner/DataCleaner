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
import org.datacleaner.server.EnvironmentBasedHadoopClusterInformation;
import org.datacleaner.test.MockHadoopConfigHelper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class HdfsResourceTypeHandlerTest {
    @Rule
    public TemporaryFolder _temporaryFolder = new TemporaryFolder();

    @Test
    public void testToAndFromString() throws Exception {
        MockHadoopConfigHelper helper = new MockHadoopConfigHelper(_temporaryFolder);
        helper.generateCoreFile();
        try {
            System.setProperty(EnvironmentBasedHadoopClusterInformation.HADOOP_CONF_DIR,
                    helper.getConfFolder().getAbsolutePath());

            final HdfsResourceTypeHandler typeHandler = new HdfsResourceTypeHandler("hdfs");

            final HdfsResource resource1 = new HdfsResource("hdfs", "localhost", 9000, "/foo.bar.txt", null);
            assertTrue(typeHandler.isParserFor(resource1.getClass()));

            final String path = typeHandler.createPath(resource1);
            assertEquals("localhost:9000/foo.bar.txt", path);

            final HdfsResource resource2 = typeHandler.parsePath(path);

            // they should now be equal, but not the same instance
            assertEquals(resource2, resource1);
            assertNotSame(resource2, resource1);
        } finally {
            System.clearProperty(EnvironmentBasedHadoopClusterInformation.HADOOP_CONF_DIR);
        }
    }
}
