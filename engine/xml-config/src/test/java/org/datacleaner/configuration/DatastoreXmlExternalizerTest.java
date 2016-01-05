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
import org.datacleaner.util.xml.XmlUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import junit.framework.TestCase;

public class DatastoreXmlExternalizerTest extends TestCase {

    private DatastoreXmlExternalizer externalizer;

    private static final String PASSWORD_ENCODED = "enc:00em6E9KEO9FG42CH0yrVQ==";

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

        assertEquals("<csv-datastore description=\"bar\" name=\"foo\">\n" + "  <filename>baz.txt</filename>\n"
                + "  <quote-char>\"</quote-char>\n" + "  <separator-char>,</separator-char>\n"
                + "  <escape-char>\\</escape-char>\n" + "  <encoding>UTF-8</encoding>\n"
                + "  <fail-on-inconsistencies>true</fail-on-inconsistencies>\n"
                + "  <multiline-values>true</multiline-values>\n" + "  <header-line-number>1</header-line-number>\n"
                + "</csv-datastore>\n", str);
    }

    public void testExternalizeExcelDatastore() throws Exception {
        ExcelDatastore ds = new ExcelDatastore("foo", new FileResource("foo.txt"), "foo.txt");
        ds.setDescription("bar");

        Element elem = externalizer.toElement(ds, "baz.txt");

        String str = transform(elem);

        assertEquals(
                "<excel-datastore description=\"bar\" name=\"foo\">\n  <filename>baz.txt</filename>\n</excel-datastore>\n",
                str);
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
        assertEquals("<csv-datastore name=\"foo\">\n" + "  <filename>src" + sep + "test" + sep + "resources" + sep
                + "example-dates.csv</filename>\n  <quote-char>\"</quote-char>\n"
                + "  <separator-char>,</separator-char>\n  <escape-char>\\</escape-char>\n  <encoding>UTF-8</encoding>\n"
                + "  <fail-on-inconsistencies>true</fail-on-inconsistencies>\n"
                + "  <multiline-values>true</multiline-values>\n" + "  <header-line-number>1</header-line-number>\n"
                + "</csv-datastore>\n", str);
    }

    public void testExternalizeJdbcDatastore() throws Exception {
        final JdbcDatastore datastore1 = new JdbcDatastore("foo ds 1", "jdbc:foo//bar", "foo.bar.Baz");

        assertTrue(externalizer.isExternalizable(datastore1));
        final String str1 = transform(externalizer.externalize(datastore1));
        assertEquals("<jdbc-datastore name=\"foo ds 1\">\n" + "  <url>jdbc:foo//bar</url>\n"
                + "  <driver>foo.bar.Baz</driver>\n"
                + "  <multiple-connections>true</multiple-connections>\n"
                + "</jdbc-datastore>\n", str1);

        final JdbcDatastore datastore2 = new JdbcDatastore("foo ds 2", "JNDI_URL",
                new TableType[] { TableType.VIEW, TableType.ALIAS }, "mycatalog");
        assertTrue(externalizer.isExternalizable(datastore2));
        final String str2 = transform(externalizer.externalize(datastore2));
        assertEquals("<jdbc-datastore name=\"foo ds 2\">\n" + "  <datasource-jndi-url>JNDI_URL</datasource-jndi-url>\n"
                + "  <table-types>\n    <table-type>VIEW</table-type>\n    <table-type>ALIAS</table-type>\n  </table-types>\n"
                + "  <catalog-name>mycatalog</catalog-name>\n</jdbc-datastore>\n", str2);

        final Element documentElement = externalizer.getDocument().getDocumentElement();
        final NodeList jdbcDatastoreElements1 = documentElement.getElementsByTagName("jdbc-datastore");
        assertEquals(2, jdbcDatastoreElements1.getLength());
        
        boolean removed = externalizer.removeDatastore("foo ds");
        assertFalse(removed);

        removed = externalizer.removeDatastore("foo ds 1");
        assertTrue(removed);

        final NodeList jdbcDatastoreElements2 = documentElement.getElementsByTagName("jdbc-datastore");
        assertEquals(1, jdbcDatastoreElements2.getLength());
    }

    public void testExternalizeJdbcDatastoreWithPassword() throws Exception {
        Datastore ds1 = new JdbcDatastore("name", "jdbcUrl", "driverClass", "username", "password", true,
                new TableType[] { TableType.ALIAS }, "catalogName");

        Element externalized = externalizer.externalize(ds1);
        assertEquals(
                "<jdbc-datastore name=\"name\">\n"
                + "  <url>jdbcUrl</url>\n"
                + "  <driver>driverClass</driver>\n"
                + "  <username>username</username>\n"
                + "  <password>"
                        + PASSWORD_ENCODED + "</password>\n"
                        + "  <multiple-connections>true</multiple-connections>\n"
                        + "  <table-types>\n"
                        + "    <table-type>ALIAS</table-type>\n"
                        + "  </table-types>\n"
                        + "  <catalog-name>catalogName</catalog-name>\n"
                        + "</jdbc-datastore>\n",
                transform(externalized));
    }

    public void testExternalizeMongoDbDatastoreWithPassword() throws Exception {
        Datastore ds1 = new MongoDbDatastore("name", "hostname", 1234, "database", "user", "password");

        Element externalized = externalizer.externalize(ds1);
        assertEquals(
                "<mongodb-datastore name=\"name\">\n" + "  <hostname>hostname</hostname>\n  <port>1234</port>\n"
                        + "  <database-name>database</database-name>\n" + "  <username>user</username>\n"
                        + "  <password>" + PASSWORD_ENCODED + "</password>\n</mongodb-datastore>\n",
                transform(externalized));
    }

    public void testExternalizeCouchDbDatastoreWithPassword() throws Exception {
        Datastore ds1 = new CouchDbDatastore("name", "hostname", 1234, "user", "password", true, null);

        Element externalized = externalizer.externalize(ds1);
        assertEquals("<couchdb-datastore name=\"name\">\n"
                + "  <hostname>hostname</hostname>\n"
                + "  <port>1234</port>\n"
                + "  <username>user</username>\n"
                + "  <password>" + PASSWORD_ENCODED + "</password>\n"
                + "  <ssl>true</ssl>\n"
                + "</couchdb-datastore>\n", transform(externalized));
    }

    public void testExternalizeSalesforceDatastoreWithPassword() throws Exception {
        Datastore ds1 = new SalesforceDatastore("name", "username", "password", "securityToken");

        Element externalized = externalizer.externalize(ds1);
        assertEquals(
                "<salesforce-datastore name=\"name\">\n" + "  <username>username</username>\n" + "  <password>"
                        + PASSWORD_ENCODED + "</password>\n"
                        + "  <security-token>securityToken</security-token>\n</salesforce-datastore>\n",
                transform(externalized));
    }

    public void testExternalizeDataHubDatastoreWithPassword() throws Exception {
        Datastore datastore = new DataHubDatastore("name", "hostname", 1234, "user", "password", false, false,
                DataHubSecurityMode.DEFAULT);

        Element externalized = externalizer.externalize(datastore);
        StringBuilder expectedConfiguration = new StringBuilder();
        expectedConfiguration//
                .append("<datahub-datastore name=\"name\">\n")//
                .append("  <host>hostname</host>\n")//
                .append("  <port>1234</port>\n")//
                .append("  <username>user</username>\n")//
                .append("  <password>" + PASSWORD_ENCODED + "</password>\n")//
                .append("  <https>false</https>\n")//
                .append("  <acceptunverifiedsslpeers>false</acceptunverifiedsslpeers>\n")//
                .append("  <datahubsecuritymode>DEFAULT</datahubsecuritymode>\n")//
                .append("</datahub-datastore>\n");//

        assertEquals(expectedConfiguration.toString(), transform(externalized));
    }

    private String transform(Element elem) throws Exception {
        return XmlUtils.writeDocumentToString(elem, false).replace("\r", "");
    }
}
