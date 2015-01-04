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
package org.eobjects.analyzer.configuration;

import java.io.ByteArrayOutputStream;
import java.io.File;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import junit.framework.TestCase;

import org.eobjects.analyzer.connection.CsvDatastore;
import org.eobjects.analyzer.connection.ExcelDatastore;
import org.eobjects.analyzer.connection.JdbcDatastore;
import org.apache.metamodel.schema.TableType;
import org.apache.metamodel.util.ClasspathResource;
import org.apache.metamodel.util.FileResource;
import org.apache.metamodel.util.Resource;
import org.w3c.dom.Element;

public class DatastoreXmlExternalizerTest extends TestCase {

    private DatastoreXmlExternalizer externalizer;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        externalizer = new DatastoreXmlExternalizer();
    }

    public void testExternalizeCsvDatastore() throws Exception {
        CsvDatastore ds = new CsvDatastore("foo", "foo.txt");
        ds.setDescription("bar");

        Element elem = externalizer.toElement(ds, "baz.txt");

        String str = transform(elem);

        assertEquals("<csv-datastore description=\"bar\" name=\"foo\"><filename>baz.txt</filename>"
                + "<quote-char>\"</quote-char><separator-char>,</separator-char>"
                + "<escape-char>\\</escape-char><encoding>UTF-8</encoding>"
                + "<fail-on-inconsistencies>true</fail-on-inconsistencies><multiline-values>true</multiline-values>"
                + "<header-line-number>1</header-line-number></csv-datastore>", str);
    }

    public void testExternalizeExcelDatastore() throws Exception {
        ExcelDatastore ds = new ExcelDatastore("foo", new FileResource("foo.txt"), "foo.txt");
        ds.setDescription("bar");

        Element elem = externalizer.toElement(ds, "baz.txt");

        String str = transform(elem);

        assertEquals(
                "<excel-datastore description=\"bar\" name=\"foo\"><filename>baz.txt</filename></excel-datastore>", str);
    }

    public void testIsExternalizableAndExternalize() throws Exception {
        final Resource resource = new ClasspathResource("foo.txt");
        final CsvDatastore unsupportedDatastore = new CsvDatastore("foo", resource);
        assertFalse(externalizer.isExternalizable(unsupportedDatastore));

        try {
            externalizer.externalize(unsupportedDatastore);
            fail("Exception expected");
        } catch (UnsupportedOperationException e) {
            assertEquals("Unsupported resource type: ClasspathResource[foo.txt]", e.getMessage());
        }

        final CsvDatastore datastore1 = new CsvDatastore("foo", "src/test/resources/example-dates.csv");

        assertTrue(externalizer.isExternalizable(datastore1));

        final Element elem = externalizer.externalize(datastore1);
        final String str = transform(elem);

        final char sep = File.separatorChar;
        assertEquals("<csv-datastore name=\"foo\">" + "<filename>src" + sep + "test" + sep + "resources" + sep
                + "example-dates.csv</filename><quote-char>\"</quote-char>"
                + "<separator-char>,</separator-char><escape-char>\\</escape-char><encoding>UTF-8</encoding>"
                + "<fail-on-inconsistencies>true</fail-on-inconsistencies><multiline-values>true</multiline-values>"
                + "<header-line-number>1</header-line-number></csv-datastore>", str);
    }

    public void testExternalizeJdbcDatastore() throws Exception {
        final JdbcDatastore datastore1 = new JdbcDatastore("foo ds 1", "jdbc:foo//bar", "foo.bar.Baz");

        assertTrue(externalizer.isExternalizable(datastore1));
        final String str1 = transform(externalizer.externalize(datastore1));
        assertEquals("<jdbc-datastore name=\"foo ds 1\">" + "<url>jdbc:foo//bar</url><driver>foo.bar.Baz</driver>"
                + "<multiple-connections>true</multiple-connections></jdbc-datastore>", str1);

        final JdbcDatastore datastore2 = new JdbcDatastore("foo ds 2", "JNDI_URL", new TableType[] { TableType.VIEW,
                TableType.ALIAS }, "mycatalog");
        assertTrue(externalizer.isExternalizable(datastore2));
        final String str2 = transform(externalizer.externalize(datastore2));
        assertEquals("<jdbc-datastore name=\"foo ds 2\">" + "<datasource-jndi-url>JNDI_URL</datasource-jndi-url>"
                + "<table-types><table-type>VIEW</table-type><table-type>ALIAS</table-type></table-types>"
                + "<catalog-name>mycatalog</catalog-name></jdbc-datastore>", str2);

        final Element datastoreCatalogElement = externalizer.getDocument().getDocumentElement();
        assertEquals("<configuration><datastore-catalog>" + str1 + str2 + "</datastore-catalog></configuration>",
                transform(datastoreCatalogElement));
        
        boolean removed = externalizer.removeDatastore("foo ds");
        assertFalse(removed);
        
        removed = externalizer.removeDatastore("foo ds 1");
        assertTrue(removed);
        
        assertEquals("<configuration><datastore-catalog>" + str2 + "</datastore-catalog></configuration>",
                transform(datastoreCatalogElement));
    }

    private String transform(Element elem) throws Exception {
        Source source = new DOMSource(elem);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Result outputTarget = new StreamResult(baos);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.transform(source, outputTarget);

        String str = new String(baos.toByteArray());
        return str;
    }
}
