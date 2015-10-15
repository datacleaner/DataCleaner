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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

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

import org.apache.metamodel.schema.TableType;
import org.apache.metamodel.util.ClasspathResource;
import org.apache.metamodel.util.FileResource;
import org.apache.metamodel.util.Resource;
import org.datacleaner.connection.CouchDbDatastore;
import org.datacleaner.connection.CsvDatastore;
import org.datacleaner.connection.DataHubDatastore;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.ExcelDatastore;
import org.datacleaner.connection.JdbcDatastore;
import org.datacleaner.connection.MongoDbDatastore;
import org.datacleaner.connection.SalesforceDatastore;
import org.datacleaner.metamodel.datahub.DataHubSecurityMode;
import org.w3c.dom.Element;

public class DatastoreXmlExternalizerTest extends TestCase {

    private DatastoreXmlExternalizer externalizer;

    private static final String PASSWORD_ENCODED = "00em6E9KEO9FG42CH0yrVQ==";

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

    public void testExternalizeJdbcDatastoreWithPassword() throws Exception {
        Datastore ds1 = new JdbcDatastore("name", "jdbcUrl", "driverClass", "username", "password", true,
                new TableType[] { TableType.ALIAS }, "catalogName");

        Element externalized = externalizer.externalize(ds1);
        assertEquals(
                "<jdbc-datastore name=\"name\"><url>jdbcUrl</url><driver>driverClass</driver><username>username</username><password>enc:"
                        + PASSWORD_ENCODED
                        + "</password>"
                        + "<multiple-connections>true</multiple-connections><table-types><table-type>ALIAS</table-type></table-types><catalog-name>catalogName</catalog-name></jdbc-datastore>",
                transform(externalized));
    }

    public void testExternalizeMongoDbDatastoreWithPassword() throws Exception {
        Datastore ds1 = new MongoDbDatastore("name", "hostname", 1234, "database", "user", "password");

        Element externalized = externalizer.externalize(ds1);
        assertEquals("<mongodb-datastore name=\"name\"><hostname>hostname</hostname><port>1234</port>"
                + "<database-name>database</database-name><username>user</username>" + "<password>enc:"
                + PASSWORD_ENCODED + "</password></mongodb-datastore>", transform(externalized));
    }

    public void testExternalizeCouchDbDatastoreWithPassword() throws Exception {
        Datastore ds1 = new CouchDbDatastore("name", "hostname", 1234, "user", "password", true, null);

        Element externalized = externalizer.externalize(ds1);
        assertEquals("<couchdb-datastore name=\"name\"><hostname>hostname</hostname><port>1234</port>"
                + "<username>user</username><password>enc:" + PASSWORD_ENCODED + "</password>"
                + "<ssl>true</ssl></couchdb-datastore>", transform(externalized));
    }

    public void testExternalizeSalesforceDatastoreWithPassword() throws Exception {
        Datastore ds1 = new SalesforceDatastore("name", "username", "password", "securityToken");

        Element externalized = externalizer.externalize(ds1);
        assertEquals("<salesforce-datastore name=\"name\"><username>username</username>" + "<password>enc:"
                + PASSWORD_ENCODED + "</password>"
                + "<security-token>securityToken</security-token></salesforce-datastore>", transform(externalized));
    }
    
    public void testExternalizeDataHubDatastoreWithPassword() throws Exception {
        Datastore datastore = new DataHubDatastore("name", "hostname", 1234, "user", "password", "tenant", false, false,
                DataHubSecurityMode.DEFAULT);

        Element externalized = externalizer.externalize(datastore);
        StringBuilder expectedConfiguration = new StringBuilder();
        expectedConfiguration.append("<datahub-datastore name=\"name\">")
                .append("<host>hostname</host>").append("<port>1234</port>")
                .append("<username>user</username>").append("<password>" + "enc:" + PASSWORD_ENCODED + "</password>")
                .append("<tenantname>tenant</tenantname>").append("<https>false</https>")
                .append("<acceptunverifiedsslpeers>false</acceptunverifiedsslpeers>")
                .append("<datahubsecuritymode>DEFAULT</datahubsecuritymode>").append("</datahub-datastore>");

        assertThat(transform(externalized), is(expectedConfiguration.toString()));
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
