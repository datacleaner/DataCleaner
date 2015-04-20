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
package org.datacleaner.configuration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;

import junit.framework.TestCase;

import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.pojo.ArrayTableDataProvider;
import org.apache.metamodel.pojo.TableDataProvider;
import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.util.SimpleTableDef;
import org.datacleaner.configuration.jaxb.AbstractDatastoreType;
import org.datacleaner.configuration.jaxb.Configuration;
import org.datacleaner.configuration.jaxb.DatastoreCatalogType;
import org.datacleaner.configuration.jaxb.ObjectFactory;
import org.datacleaner.configuration.jaxb.PojoDatastoreType;
import org.datacleaner.connection.PojoDatastore;
import org.datacleaner.connection.UpdateableDatastoreConnection;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JaxbPojoDatastoreAdaptorTest extends TestCase {

    public void testSerializeAndDeserialize() throws Exception {
        final Object map1 = buildMap("{'some_number':1234, 'gender':'M','address':{'city':'Copenhagen','country':'DK','additional_info':null}}");
        final Object map2 = buildMap("{'some_number':5678,'gender':'M','address':{'city':'Amsterdam','countries':['NL','IN']}}");

        SimpleTableDef tableDef = new SimpleTableDef("bar", new String[] { "id", "name", "details", "bytes" },
                new ColumnType[] { ColumnType.INTEGER, ColumnType.VARCHAR, ColumnType.MAP, ColumnType.BINARY });
        Collection<Object[]> arrays = new ArrayList<Object[]>();
        arrays.add(new Object[] { 1, "Kasper Sørensen", map1, new byte[] { (byte) -40, (byte) -2 } });
        arrays.add(new Object[] { 2, "Ankit Kumar", map2, new byte[] { (byte) 1, (byte) 3, (byte) 3, (byte) 7 } });
        TableDataProvider<?> tableProvider = new ArrayTableDataProvider(tableDef, arrays);
        List<TableDataProvider<?>> tableProviders = new ArrayList<TableDataProvider<?>>();
        tableProviders.add(tableProvider);

        PojoDatastore datastore;
        datastore = new PojoDatastore("foo", tableProviders);

        final JaxbPojoDatastoreAdaptor adaptor = new JaxbPojoDatastoreAdaptor(new DataCleanerConfigurationImpl());
        AbstractDatastoreType serializedDatastore = adaptor.createPojoDatastore(datastore, null, 20);

        DatastoreCatalogType serializedDatastoreCatalogType = new DatastoreCatalogType();
        serializedDatastoreCatalogType.getJdbcDatastoreOrAccessDatastoreOrCsvDatastore().add(serializedDatastore);
        Configuration serializedConfiguration = new Configuration();
        serializedConfiguration.setDatastoreCatalog(serializedDatastoreCatalogType);

        // serialize and deserialize
        JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName(),
                ObjectFactory.class.getClassLoader());
        File file = new File("target/JaxbPojoDatastoreAdaptorTest_serialize_and_deserialize.xml");
        jaxbContext.createMarshaller().marshal(serializedConfiguration, file);
        serializedConfiguration = (Configuration) jaxbContext.createUnmarshaller().unmarshal(file);

        serializedDatastore = serializedConfiguration.getDatastoreCatalog()
                .getJdbcDatastoreOrAccessDatastoreOrCsvDatastore().get(0);
        datastore = adaptor.read((PojoDatastoreType) serializedDatastore);

        UpdateableDatastoreConnection con = datastore.openConnection();
        DataSet ds = con.getDataContext().query().from("bar").select("id", "name", "details").execute();
        assertTrue(ds.next());
        assertTrue(ds.getRow().getValue(0) instanceof Integer);
        assertEquals(1, ds.getRow().getValue(0));
        assertTrue(ds.getRow().getValue(1) instanceof String);
        assertEquals("Kasper Sørensen", ds.getRow().getValue(1));
        assertTrue(ds.getRow().getValue(2) instanceof Map);

        @SuppressWarnings("unchecked")
        final Map<String, ?> map3 = (Map<String, ?>) ds.getRow().getValue(2);
        assertEquals("{some_number=1234, gender=M, address={city=Copenhagen, country=DK, additional_info=null}}",
                map3.toString());
        assertEquals(Integer.class, map3.get("some_number").getClass());

        assertTrue(ds.next());
        assertTrue(ds.getRow().getValue(0) instanceof Integer);
        assertEquals(2, ds.getRow().getValue(0));
        assertTrue(ds.getRow().getValue(1) instanceof String);
        assertEquals("Ankit Kumar", ds.getRow().getValue(1));
        assertTrue(ds.getRow().getValue(2) instanceof Map);

        @SuppressWarnings("unchecked")
        final Map<String, ?> map4 = (Map<String, ?>) ds.getRow().getValue(2);
        assertEquals("{some_number=5678, gender=M, address={city=Amsterdam, countries=[NL, IN]}}", map4.toString());
        assertEquals(Integer.class, map3.get("some_number").getClass());

        assertFalse(ds.next());

        assertEquals(map1, map3);
        assertEquals(map2, map4);
    }

    private Object buildMap(String string) throws JsonParseException, JsonMappingException, IOException {
        string = string.replaceAll("'", "\"");
        return new ObjectMapper().readValue(string, Map.class);
    }
}
