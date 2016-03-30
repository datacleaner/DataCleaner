package org.datacleaner.spark;

import org.apache.metamodel.csv.CsvConfiguration;
import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.FileResource;
import org.apache.metamodel.util.HdfsResource;
import org.datacleaner.connection.CsvDatastore;
import org.datacleaner.connection.ExcelDatastore;
import org.datacleaner.connection.JsonDatastore;
import org.datacleaner.connection.Neo4jDatastore;
import org.datacleaner.spark.utils.HadoopConfigurationUtils;
import org.junit.Test;

import junit.framework.TestCase;

public class HadoopConfigurationUtilsTest extends TestCase{

    @Test
    public void testCsvMultilines(){
        
        final HdfsResource hdfsResource = new  HdfsResource("hdfs://datacleaner/files/test.csv"); 
        final CsvDatastore csvDatastore = new CsvDatastore("MyCsv", hdfsResource);
        assertTrue(csvDatastore.getCsvConfiguration().isMultilineValues());
        assertEquals(FileHelper.UTF_8_ENCODING,csvDatastore.getEncoding()); 
        assertFalse(HadoopConfigurationUtils.isValidConfiguration(csvDatastore)); 
    }
    
    @Test
    public void testCsvSingleLine(){
        final HdfsResource hdfsResource = new  HdfsResource("hdfs://datacleaner/files/test.csv"); 
        final CsvConfiguration csvConfiguration = new CsvConfiguration(0, true, false);
        final CsvDatastore csvDatastore = new CsvDatastore("MyCsv", hdfsResource, csvConfiguration);
        assertFalse(csvDatastore.getCsvConfiguration().isMultilineValues());
        assertEquals(FileHelper.UTF_8_ENCODING,csvDatastore.getEncoding()); 
        assertTrue(HadoopConfigurationUtils.isValidConfiguration(csvDatastore)); 
    }
    
    @Test
    public void testJson(){
        final HdfsResource hdfsResource = new  HdfsResource("hdfs://datacleaner/files/test.csv"); 
        final JsonDatastore jsonDatastore = new JsonDatastore("test", hdfsResource);
        assertTrue(HadoopConfigurationUtils.isValidConfiguration(jsonDatastore)); 
    }
    
    @Test
    public void testInvalidDatastore(){
        final ExcelDatastore excelDatastore = new ExcelDatastore("MyTest", new FileResource("C://test"), "Test");
        assertFalse(HadoopConfigurationUtils.isValidConfiguration(excelDatastore)); 
        final Neo4jDatastore neo4jDatastore = new Neo4jDatastore("neo", "localhost", "me", "password"); 
        assertFalse(HadoopConfigurationUtils.isValidConfiguration(neo4jDatastore));
    }
}
