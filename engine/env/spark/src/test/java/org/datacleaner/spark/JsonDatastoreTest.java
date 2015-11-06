package org.datacleaner.spark;

import java.io.File;

import junit.framework.TestCase;

import org.apache.metamodel.data.Row;
import org.apache.metamodel.json.JsonDataContext;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonDatastoreTest  extends TestCase {

    private final Logger logger= LoggerFactory.getLogger(JsonDatastoreTest.class);
    
    @Test
    public void test() {
        final JsonDataContext dc = new JsonDataContext(new File("src/test/resources/generated_json.json"));
        final Schema defaultSchema = dc.getDefaultSchema();

        final Table table = defaultSchema.getTable(0);
        final Column[] columns = table.getColumns();
        for (int i= 0; i<columns.length; i++){
            logger.info("Column" + columns[i]);
        }
        
       
        final org.apache.metamodel.data.DataSet executeQuery = dc.executeQuery("Select * from table"); 
        while (executeQuery.next()){
            final Row row = executeQuery.getRow();
            logger.info("Row {}" + row.toString());
        }
       
    }

}
