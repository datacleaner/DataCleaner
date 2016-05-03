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

import org.apache.metamodel.csv.CsvConfiguration;
import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.FileResource;
import org.apache.metamodel.util.HdfsResource;
import org.datacleaner.connection.CsvDatastore;
import org.datacleaner.connection.ExcelDatastore;
import org.datacleaner.connection.JsonDatastore;
import org.datacleaner.connection.Neo4jDatastore;
import org.datacleaner.spark.utils.HadoopJobExecutionUtils;
import org.junit.Test;

import junit.framework.TestCase;

public class HadoopConfigurationUtilsTest extends TestCase{

    @Test
    public void testCsvMultilines(){
        
        final HdfsResource hdfsResource = new  HdfsResource("hdfs://datacleaner/files/test.csv"); 
        final CsvDatastore csvDatastore = new CsvDatastore("MyCsv", hdfsResource);
        assertTrue(csvDatastore.getCsvConfiguration().isMultilineValues());
        assertEquals(FileHelper.UTF_8_ENCODING,csvDatastore.getEncoding()); 
        assertFalse(HadoopJobExecutionUtils.isValidSourceDatastore(csvDatastore)); 
    }
    
    @Test
    public void testCsvSingleLine(){
        final HdfsResource hdfsResource = new  HdfsResource("hdfs://datacleaner/files/test.csv"); 
        final CsvConfiguration csvConfiguration = new CsvConfiguration(0, true, false);
        final CsvDatastore csvDatastore = new CsvDatastore("MyCsv", hdfsResource, csvConfiguration);
        assertFalse(csvDatastore.getCsvConfiguration().isMultilineValues());
        assertEquals(FileHelper.UTF_8_ENCODING,csvDatastore.getEncoding()); 
        assertTrue(HadoopJobExecutionUtils.isValidSourceDatastore(csvDatastore)); 
    }
    
    @Test
    public void testJson(){
        final HdfsResource hdfsResource = new  HdfsResource("hdfs://datacleaner/files/test.csv"); 
        final JsonDatastore jsonDatastore = new JsonDatastore("test", hdfsResource);
        assertTrue(HadoopJobExecutionUtils.isValidSourceDatastore(jsonDatastore)); 
    }
    
    @Test
    public void testInvalidDatastore(){
        final ExcelDatastore excelDatastore = new ExcelDatastore("MyTest", new FileResource("C://test"), "Test");
        assertFalse(HadoopJobExecutionUtils.isValidSourceDatastore(excelDatastore)); 
        final Neo4jDatastore neo4jDatastore = new Neo4jDatastore("neo", "localhost", "me", "password"); 
        assertFalse(HadoopJobExecutionUtils.isValidSourceDatastore(neo4jDatastore));
    }
}
