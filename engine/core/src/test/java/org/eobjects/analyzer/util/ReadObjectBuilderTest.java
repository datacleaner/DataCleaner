/**
 * AnalyzerBeans
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
package org.eobjects.analyzer.util;

import java.io.FileInputStream;
import java.util.List;

import junit.framework.TestCase;

import org.eobjects.analyzer.connection.Datastore;

public class ReadObjectBuilderTest extends TestCase {

    public void testDeserializeLegacyDatastores() throws Exception {
        Object deserializedObject;
        try (ChangeAwareObjectInputStream objectInputStream = new ChangeAwareObjectInputStream(new FileInputStream(
                "src/test/resources/analyzerbeans-0.4-datastores.dat"))) {
            deserializedObject = objectInputStream.readObject();
        }
        
        assertTrue(deserializedObject instanceof List);

        @SuppressWarnings("unchecked")
        List<Datastore> list = (List<Datastore>) deserializedObject;
        assertEquals(8, list.size());

        assertEquals("JdbcDatastore[name=my_jdbc_connection,url=jdbc:hsqldb:res:metamodel]", list.get(0).toString());
        assertEquals("DbaseDatastore[name=my_dbase]", list.get(1).toString());
        assertEquals(
                "CsvDatastore[name=my_csv, filename=src/test/resources/employees.csv, quoteChar='\"', separatorChar=',', encoding=null, headerLineNumber=0]",
                list.get(2).toString());
        assertEquals("ExcelDatastore[name=my_xml]", list.get(3).toString());
        assertEquals("OdbDatastore[name=my_odb]", list.get(4).toString());
        assertEquals("ExcelDatastore[name=my_excel_2003]", list.get(5).toString());
        assertEquals("CompositeDatastore[name=my_composite]", list.get(6).toString());
        assertEquals("AccessDatastore[name=my_access]", list.get(7).toString());
    }

}
