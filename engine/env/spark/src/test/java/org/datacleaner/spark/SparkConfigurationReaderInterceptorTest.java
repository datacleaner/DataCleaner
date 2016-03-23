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
package org.datacleaner.spark;

import static org.junit.Assert.*;

import java.util.HashMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.metamodel.util.Resource;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.spark.utils.HdfsHelper;
import org.datacleaner.util.HadoopResource;
import org.junit.Before;
import org.junit.Test;

public class SparkConfigurationReaderInterceptorTest {

    @Before
    public void setup() {
        // setup code needed to register the "last known" hadoop configuration
        // in HdfsHelper.
        new HdfsHelper(new Configuration());
    }

    @Test
    public void testRemoveServerFromHdfsUrl() throws Exception {

        final SparkConfigurationReaderInterceptor interceptor = new SparkConfigurationReaderInterceptor(
                new HashMap<>());
        final Resource resource = interceptor.createResource(
                "hdfs://{foo.bar.hadoop.environment}/datacleaner/data/companies_1.csv",
                new DataCleanerConfigurationImpl());

        assertTrue("Found: " + resource.getClass().getName(), resource instanceof HadoopResource);
        assertEquals("hdfs:///datacleaner/data/companies_1.csv", resource.getQualifiedPath());
    }

    @Test
    public void testSimpleHdfsUrl() throws Exception {
        final SparkConfigurationReaderInterceptor interceptor = new SparkConfigurationReaderInterceptor(
                new HashMap<>());
        final Resource resource = interceptor.createResource("hdfs:///datacleaner/data/companies_2.csv",
                new DataCleanerConfigurationImpl());

        assertTrue("Found: " + resource.getClass().getName(), resource instanceof HadoopResource);
        assertEquals("hdfs:///datacleaner/data/companies_2.csv", resource.getQualifiedPath());
    }

    @Test
    public void testSimpleSchemeLessUrl() throws Exception {
        final SparkConfigurationReaderInterceptor interceptor = new SparkConfigurationReaderInterceptor(
                new HashMap<>());
        final Resource resource = interceptor.createResource("/datacleaner/data/companies_3.csv",
                new DataCleanerConfigurationImpl());

        assertTrue("Found: " + resource.getClass().getName(), resource instanceof HadoopResource);
        assertEquals("hdfs:///datacleaner/data/companies_3.csv", resource.getQualifiedPath());
    }
}
