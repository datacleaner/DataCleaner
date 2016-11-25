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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.metamodel.util.FileResource;
import org.apache.metamodel.util.HdfsResource;
import org.apache.metamodel.util.Resource;
import org.apache.metamodel.util.UrlResource;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.server.EnvironmentBasedHadoopClusterInformation;
import org.datacleaner.test.MockHadoopConfigHelper;
import org.datacleaner.util.VFSUtils;
import org.datacleaner.util.VfsResource;
import org.datacleaner.util.convert.ResourceConverter.ResourceTypeHandler;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ResourceConverterTest {

    private final DataCleanerConfiguration configuration = new DataCleanerConfigurationImpl();

    @Rule
    public TemporaryFolder _temporaryFolder = new TemporaryFolder();

    @Test
    public void testParse() throws Exception {
        final ResourceConverter resourceConverter = new ResourceConverter(configuration);
        assertEquals("url", resourceConverter.parseStructure("url://foobar").getScheme());
        assertEquals("foobar", resourceConverter.parseStructure("url://foobar").getPath());
        assertEquals("file", resourceConverter.parseStructure("file://c:/blabla").getScheme());
        assertEquals("c:/blabla", resourceConverter.parseStructure("file://c:/blabla").getPath());
        assertEquals("/c:/blabla", resourceConverter.parseStructure("file:///c:/blabla").getPath());
    }

    @Test
    public void testConvertFileResource() throws Exception {
        final List<? extends ResourceTypeHandler<?>> handlers =
                Arrays.asList(new FileResourceTypeHandler(configuration));
        final ResourceConverter converter = new ResourceConverter(handlers, "foo");

        final FileResource resource1 = new FileResource("foo/bar.txt");

        final String str = converter.toString(resource1);

        assertEquals("file://foo/bar.txt", str);

        final Resource resource2 = converter.fromString(Resource.class, str);

        assertTrue(resource2 instanceof FileResource);
        assertEquals("foo/bar.txt", ((FileResource) resource2).getFile().getPath().replace('\\', '/'));
    }

    @Test
    public void testConvertUrlResource() throws Exception {
        final List<? extends ResourceTypeHandler<?>> handlers = Arrays.asList(new UrlResourceTypeHandler());
        final ResourceConverter converter = new ResourceConverter(handlers, "foo");

        final UrlResource resource1 = new UrlResource("http://localhost");

        final String str = converter.toString(resource1);

        assertEquals("url://http://localhost", str);

        final Resource resource2 = converter.fromString(Resource.class, str);

        assertTrue(resource2 instanceof UrlResource);
        assertEquals("localhost", resource2.getName());
    }

    @Test
    public void testConvertVfsResource() throws Exception {
        final List<? extends ResourceTypeHandler<?>> handlers = Arrays.asList(new VfsResourceTypeHandler());
        final ResourceConverter converter = new ResourceConverter(handlers, "foo");

        final VfsResource resource1 = new VfsResource(VFSUtils.getFileSystemManager().resolveFile("target"));

        final String str = converter.toString(resource1);

        final String absoluteFilePath = new File("target").getAbsolutePath().replaceAll("\\\\", "/");
        if (absoluteFilePath.startsWith("/")) {
            assertEquals("vfs://file://" + absoluteFilePath, str);
        } else {
            assertEquals("vfs://file:///" + absoluteFilePath, str);
        }

        final Resource resource2 = converter.fromString(Resource.class, str);

        assertTrue(resource2 instanceof VfsResource);
        assertEquals("target", resource2.getName());
    }

    @Test
    public void testConvertHdfsResource() throws Exception {
        final MockHadoopConfigHelper helper = new MockHadoopConfigHelper(_temporaryFolder);
        helper.generateCoreFile();
        try {
            System.setProperty(EnvironmentBasedHadoopClusterInformation.HADOOP_CONF_DIR,
                    helper.getConfFolder().getAbsolutePath());

            final List<? extends ResourceTypeHandler<?>> handlers = Arrays.asList(new HdfsResourceTypeHandler("hdfs"));
            final ResourceConverter converter = new ResourceConverter(handlers, "foo");

            final HdfsResource resource1 = new HdfsResource("hdfs://localhost:9000/user/vagrant/file.csv");

            final String str = converter.toString(resource1);

            assertEquals("hdfs://localhost:9000/user/vagrant/file.csv", str);

            final Resource resource2 = converter.fromString(Resource.class, str);

            assertTrue(resource2 instanceof HdfsResource);
            assertEquals("file.csv", resource2.getName());
        } finally {
            System.clearProperty(EnvironmentBasedHadoopClusterInformation.HADOOP_CONF_DIR);
        }
    }
}
