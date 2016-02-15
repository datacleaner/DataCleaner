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
