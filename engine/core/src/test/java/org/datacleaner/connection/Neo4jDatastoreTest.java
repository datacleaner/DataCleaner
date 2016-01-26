package org.datacleaner.connection;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class Neo4jDatastoreTest {

    @Test
    public void testDatastore() {
        final Neo4jDatastore neo4jDatastore = new Neo4jDatastore("TheNeo4j", "localhost", "neo4j", "neo4j");
        assertEquals("TheNeo4j", neo4jDatastore.getName()); 
        assertEquals("localhost", neo4jDatastore.getHostname());  
        assertEquals("neo4j", neo4jDatastore.getUsername());
        assertEquals("neo4j", neo4jDatastore.getPassword()); 
        assertEquals("Neo4jDatastore[name=TheNeo4j, hostname=localhost, port=7474, _username=neo4j]", neo4jDatastore.toString()); 
        
    }
    
    @Test
    public void testDatastoreWithPort(){
        
        final Neo4jDatastore neo4jDatastore = new Neo4jDatastore("TheNeo4j", "localhost",8080, "neo4j", "neo4j");
        assertEquals("TheNeo4j", neo4jDatastore.getName()); 
        assertEquals("localhost", neo4jDatastore.getHostname());  
        assertEquals(8080, neo4jDatastore.getPort()); 
        assertEquals("neo4j", neo4jDatastore.getUsername());
        assertEquals("neo4j", neo4jDatastore.getPassword()); 
        assertEquals("Neo4jDatastore[name=TheNeo4j, hostname=localhost, port=8080, _username=neo4j]", neo4jDatastore.toString());
    }
    
}
