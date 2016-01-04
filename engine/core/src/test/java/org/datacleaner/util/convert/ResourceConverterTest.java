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

import java.io.File;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.apache.metamodel.util.FileResource;
import org.apache.metamodel.util.HdfsResource;
import org.apache.metamodel.util.Resource;
import org.apache.metamodel.util.UrlResource;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.util.VFSUtils;
import org.datacleaner.util.VfsResource;
import org.datacleaner.util.convert.ResourceConverter.ResourceTypeHandler;
import org.junit.Test;

public class ResourceConverterTest extends TestCase {

    private final DataCleanerConfiguration configuration = new DataCleanerConfigurationImpl();

    public void testParse() throws Exception {
        ResourceConverter resourceConverter = new ResourceConverter(configuration);
        assertEquals("url", resourceConverter.parseStructure("url://foobar").getScheme());
        assertEquals("foobar", resourceConverter.parseStructure("url://foobar").getPath());
        assertEquals("file", resourceConverter.parseStructure("file://c:/blabla").getScheme());
        assertEquals("c:/blabla", resourceConverter.parseStructure("file://c:/blabla").getPath());
        assertEquals("/c:/blabla", resourceConverter.parseStructure("file:///c:/blabla").getPath());
    }

    public void testConvertFileResource() throws Exception {
        List<? extends ResourceTypeHandler<?>> handlers = Arrays.asList(new FileResourceTypeHandler(configuration));
        ResourceConverter converter = new ResourceConverter(handlers, "foo");

        FileResource resource1 = new FileResource("foo/bar.txt");

        String str = converter.toString(resource1);

        assertEquals("file://foo/bar.txt", str);

        Resource resource2 = converter.fromString(Resource.class, str);

        assertTrue(resource2 instanceof FileResource);
        assertEquals("foo/bar.txt", ((FileResource) resource2).getFile().getPath().replace('\\', '/'));
    }

    public void testConvertUrlResource() throws Exception {
        List<? extends ResourceTypeHandler<?>> handlers = Arrays.asList(new UrlResourceTypeHandler());
        ResourceConverter converter = new ResourceConverter(handlers, "foo");

        UrlResource resource1 = new UrlResource("http://localhost");

        String str = converter.toString(resource1);

        assertEquals("url://http://localhost", str);

        Resource resource2 = converter.fromString(Resource.class, str);

        assertTrue(resource2 instanceof UrlResource);
        assertEquals("localhost", resource2.getName());
    }

    public void testConvertVfsResource() throws Exception {
        List<? extends ResourceTypeHandler<?>> handlers = Arrays.asList(new VfsResourceTypeHandler());
        ResourceConverter converter = new ResourceConverter(handlers, "foo");

        VfsResource resource1 = new VfsResource(VFSUtils.getFileSystemManager().resolveFile("target"));

        String str = converter.toString(resource1);

        String absoluteFilePath = new File("target").getAbsolutePath().replaceAll("\\\\", "/");
        if (absoluteFilePath.startsWith("/")) {
            assertEquals("vfs://file://" + absoluteFilePath, str);
        } else {
            assertEquals("vfs://file:///" + absoluteFilePath, str);
        }

        Resource resource2 = converter.fromString(Resource.class, str);

        assertTrue(resource2 instanceof VfsResource);
        assertEquals("target", resource2.getName());
    }
    @Test
    public void testConvertHdfsResource() throws Exception {
        List<? extends ResourceTypeHandler<?>> handlers = Arrays.asList(new HdfsResourceTypeHandler("hdfs"));
        ResourceConverter converter = new ResourceConverter(handlers, "foo");

        HdfsResource resource1 = new  HdfsResource("hdfs://localhost:9000/user/vagrant/file.csv");

        String str = converter.toString(resource1);

        assertEquals("hdfs://localhost:9000/user/vagrant/file.csv", str);

        Resource resource2 = converter.fromString(Resource.class, str);

        assertTrue(resource2 instanceof  HdfsResource);
        assertEquals("file.csv", resource2.getName());
    }
}
