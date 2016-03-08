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
import java.net.URI;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.hadoop.conf.Configuration;
import org.apache.metamodel.schema.TableType;
import org.apache.metamodel.util.ClasspathResource;
import org.apache.metamodel.util.FileHelper;
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
import org.datacleaner.reference.DatastoreDictionary;
import org.datacleaner.reference.DatastoreSynonymCatalog;
import org.datacleaner.reference.RegexStringPattern;
import org.datacleaner.reference.SimpleDictionary;
import org.datacleaner.reference.SimpleStringPattern;
import org.datacleaner.reference.TextFileDictionary;
import org.datacleaner.reference.TextFileSynonymCatalog;
import org.datacleaner.server.DirectConnectionHadoopClusterInformation;
import org.datacleaner.server.DirectoryBasedHadoopClusterInformation;
import org.datacleaner.server.EnvironmentBasedHadoopClusterInformation;
import org.datacleaner.test.MockHadoopConfigHelper;
import org.datacleaner.util.HadoopResource;
import org.datacleaner.util.xml.XmlUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static org.junit.Assert.*;

public class DomConfigurationWriterTest {

    @Rule
    public TemporaryFolder _temporaryFolder = new TemporaryFolder();

    private DomConfigurationWriter configurationWriter;

    private static final String PASSWORD_ENCODED = "enc:00em6E9KEO9FG42CH0yrVQ==";

    @Rule
    public TestName testName = new TestName();

    @Before
    public void setUp() throws Exception {
        configurationWriter = new DomConfigurationWriter();
    }

    @Test
    public void testExternalizeCsvDatastore() throws Exception {
        CsvDatastore ds = new CsvDatastore("foo", "foo.txt");
        ds.setDescription("bar");

        Element elem = configurationWriter.toElement(ds, "baz.txt");

        String str = transform(elem);

        assertEquals("<csv-datastore description=\"bar\" name=\"foo\">\n" + "  <filename>baz.txt</filename>\n"
                + "  <quote-char>\"</quote-char>\n" + "  <separator-char>,</separator-char>\n"
                + "  <escape-char>\\</escape-char>\n" + "  <encoding>UTF-8</encoding>\n"
                + "  <fail-on-inconsistencies>true</fail-on-inconsistencies>\n"
                + "  <multiline-values>true</multiline-values>\n" + "  <header-line-number>1</header-line-number>\n"
                + "</csv-datastore>\n", str);
    }

    @Test
    public void testExternalizeExcelDatastore() throws Exception {
        ExcelDatastore ds = new ExcelDatastore("foo", new FileResource("foo.txt"), "foo.txt");
        ds.setDescription("bar");

        Element elem = configurationWriter.toElement(ds, "baz.txt");

        String str = transform(elem);

        assertEquals(
                "<excel-datastore description=\"bar\" name=\"foo\">\n  <filename>baz.txt</filename>\n</excel-datastore>\n",
                str);
    }

    @Test
    public void testIsExternalizableAndExternalize() throws Exception {
        final Resource resource = new ClasspathResource("foo.txt");
        final CsvDatastore unsupportedDatastore = new CsvDatastore("foo", resource);
        assertFalse(configurationWriter.isExternalizable(unsupportedDatastore));

        try {
            configurationWriter.externalize(unsupportedDatastore);
            fail("Exception expected");
        } catch (UnsupportedOperationException e) {
            assertEquals("Unsupported resource type: ClasspathResource[foo.txt]", e.getMessage());
        }

        final CsvDatastore datastore1 = new CsvDatastore("foo", "src/test/resources/example-dates.csv");

        assertTrue(configurationWriter.isExternalizable(datastore1));

        final Element elem = configurationWriter.externalize(datastore1);
        final String str = transform(elem);

        final char sep = File.separatorChar;
        assertEquals("<csv-datastore name=\"foo\">\n" + "  <filename>src" + sep + "test" + sep + "resources" + sep
                + "example-dates.csv</filename>\n  <quote-char>\"</quote-char>\n"
                + "  <separator-char>,</separator-char>\n  <escape-char>\\</escape-char>\n  <encoding>UTF-8</encoding>\n"
                + "  <fail-on-inconsistencies>true</fail-on-inconsistencies>\n"
                + "  <multiline-values>true</multiline-values>\n" + "  <header-line-number>1</header-line-number>\n"
                + "</csv-datastore>\n", str);
    }

    @Test
    public void testExternalizeJdbcDatastore() throws Exception {
        final JdbcDatastore datastore1 = new JdbcDatastore("foo ds 1", "jdbc:foo//bar", "foo.bar.Baz");

        assertTrue(configurationWriter.isExternalizable(datastore1));
        final String str1 = transform(configurationWriter.externalize(datastore1));
        assertEquals("<jdbc-datastore name=\"foo ds 1\">\n" + "  <url>jdbc:foo//bar</url>\n"
                + "  <driver>foo.bar.Baz</driver>\n" + "  <multiple-connections>true</multiple-connections>\n"
                + "</jdbc-datastore>\n", str1);

        final JdbcDatastore datastore2 = new JdbcDatastore("foo ds 2", "JNDI_URL", new TableType[] { TableType.VIEW,
                TableType.ALIAS }, "mycatalog");
        assertTrue(configurationWriter.isExternalizable(datastore2));
        final String str2 = transform(configurationWriter.externalize(datastore2));
        assertEquals("<jdbc-datastore name=\"foo ds 2\">\n" + "  <datasource-jndi-url>JNDI_URL</datasource-jndi-url>\n"
                + "  <table-types>\n    <table-type>VIEW</table-type>\n    <table-type>ALIAS</table-type>\n  </table-types>\n"
                + "  <catalog-name>mycatalog</catalog-name>\n</jdbc-datastore>\n", str2);

        final Element documentElement = configurationWriter.getDocument().getDocumentElement();
        final NodeList jdbcDatastoreElements1 = documentElement.getElementsByTagName("jdbc-datastore");
        assertEquals(2, jdbcDatastoreElements1.getLength());

        boolean removed = configurationWriter.removeDatastore("foo ds");
        assertFalse(removed);

        removed = configurationWriter.removeDatastore("foo ds 1");
        assertTrue(removed);

        final NodeList jdbcDatastoreElements2 = documentElement.getElementsByTagName("jdbc-datastore");
        assertEquals(1, jdbcDatastoreElements2.getLength());
    }

    @Test
    public void testExternalizeJdbcDatastoreWithPassword() throws Exception {
        Datastore ds1 = new JdbcDatastore("name", "jdbcUrl", "driverClass", "username", "password", true,
                new TableType[] { TableType.ALIAS }, "catalogName");

        Element externalized = configurationWriter.externalize(ds1);
        assertEquals("<jdbc-datastore name=\"name\">\n" + "  <url>jdbcUrl</url>\n" + "  <driver>driverClass</driver>\n"
                + "  <username>username</username>\n" + "  <password>" + PASSWORD_ENCODED + "</password>\n"
                + "  <multiple-connections>true</multiple-connections>\n" + "  <table-types>\n"
                + "    <table-type>ALIAS</table-type>\n" + "  </table-types>\n"
                + "  <catalog-name>catalogName</catalog-name>\n" + "</jdbc-datastore>\n", transform(externalized));
    }

    @Test
    public void testExternalizeMongoDbDatastoreWithPassword() throws Exception {
        Datastore ds1 = new MongoDbDatastore("name", "hostname", 1234, "database", "user", "password");

        Element externalized = configurationWriter.externalize(ds1);
        assertEquals("<mongodb-datastore name=\"name\">\n" + "  <hostname>hostname</hostname>\n  <port>1234</port>\n"
                + "  <database-name>database</database-name>\n" + "  <username>user</username>\n" + "  <password>"
                + PASSWORD_ENCODED + "</password>\n</mongodb-datastore>\n", transform(externalized));
    }

    @Test
    public void testExternalizeCouchDbDatastoreWithPassword() throws Exception {
        Datastore ds1 = new CouchDbDatastore("name", "hostname", 1234, "user", "password", true, null);

        Element externalized = configurationWriter.externalize(ds1);
        assertEquals("<couchdb-datastore name=\"name\">\n" + "  <hostname>hostname</hostname>\n"
                + "  <port>1234</port>\n" + "  <username>user</username>\n" + "  <password>" + PASSWORD_ENCODED
                + "</password>\n" + "  <ssl>true</ssl>\n" + "</couchdb-datastore>\n", transform(externalized));
    }

    @Test
    public void testExternalizeSalesforceDatastoreWithPassword() throws Exception {
        Datastore ds1 = new SalesforceDatastore("name", "username", "password", "securityToken");

        Element externalized = configurationWriter.externalize(ds1);
        assertEquals("<salesforce-datastore name=\"name\">\n" + "  <username>username</username>\n" + "  <password>"
                + PASSWORD_ENCODED + "</password>\n"
                + "  <security-token>securityToken</security-token>\n</salesforce-datastore>\n", transform(
                        externalized));
    }

    @Test
    public void testExternalizeDataHubDatastoreWithPassword() throws Exception {
        Datastore datastore = new DataHubDatastore("name", "hostname", 1234, "user", "password", false, false,
                DataHubSecurityMode.DEFAULT);

        Element externalized = configurationWriter.externalize(datastore);
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

    @Test
    public void testWriteAndReadAllDictionaries() throws Exception {
        configurationWriter.externalize(new SimpleDictionary("simple dict", false, "foo", "bar", "baz"));
        configurationWriter.externalize(new TextFileDictionary("textfile dict", "/foo/bar.txt", "UTF8", false));
        configurationWriter.externalize(new DatastoreDictionary("ds dict", "orderdb", "products.productname", false));

        final String str = transform(configurationWriter.getDocument());
        final File file = new File("target/" + getClass().getSimpleName() + "-" + testName.getMethodName() + ".xml");
        FileHelper.writeStringAsFile(file, str);

        final DataCleanerConfiguration configuration = new JaxbConfigurationReader().create(file);
        assertEquals("[ds dict, simple dict, textfile dict]", Arrays.toString(configuration.getReferenceDataCatalog()
                .getDictionaryNames()));

        final SimpleDictionary simpleDictionary = (SimpleDictionary) configuration.getReferenceDataCatalog()
                .getDictionary("simple dict");
        assertEquals(false, simpleDictionary.isCaseSensitive());
        assertEquals("[bar, baz, foo]", simpleDictionary.getValueSet().stream().sorted().collect(Collectors.toList())
                .toString());

        final TextFileDictionary textFileDictionary = (TextFileDictionary) configuration.getReferenceDataCatalog()
                .getDictionary("textfile dict");
        assertEquals(false, textFileDictionary.isCaseSensitive());
        assertEquals("UTF8", textFileDictionary.getEncoding());
        assertTrue(textFileDictionary.getFilename().endsWith("bar.txt"));

        final DatastoreDictionary datastoreDictionary = (DatastoreDictionary) configuration.getReferenceDataCatalog()
                .getDictionary("ds dict");
        assertEquals(false, datastoreDictionary.isLoadIntoMemory());
        assertEquals("orderdb", datastoreDictionary.getDatastoreName());
        assertEquals("products.productname", datastoreDictionary.getQualifiedColumnName());
    }

    @Test
    public void testWriteAndReadAllSynonymCatalogs() throws Exception {
        configurationWriter.externalize(new TextFileSynonymCatalog("textfile sc", "/foo/bar.txt", false, "UTF8"));
        configurationWriter.externalize(new DatastoreSynonymCatalog("ds sc", "orderdb", "products.productname",
                new String[] { "products.productline", "product.producttype" }, false));

        final String str = transform(configurationWriter.getDocument());
        final File file = new File("target/" + getClass().getSimpleName() + "-" + testName.getMethodName() + ".xml");
        FileHelper.writeStringAsFile(file, str);

        final DataCleanerConfiguration configuration = new JaxbConfigurationReader().create(file);
        assertEquals("[ds sc, textfile sc]", Arrays.toString(configuration.getReferenceDataCatalog()
                .getSynonymCatalogNames()));

        final TextFileSynonymCatalog textFileSynonymCatalog = (TextFileSynonymCatalog) configuration
                .getReferenceDataCatalog().getSynonymCatalog("textfile sc");
        assertEquals("UTF8", textFileSynonymCatalog.getEncoding());
        assertTrue(textFileSynonymCatalog.getFilename().endsWith("bar.txt"));

        final DatastoreSynonymCatalog datastoreSynonymCatalog = (DatastoreSynonymCatalog) configuration
                .getReferenceDataCatalog().getSynonymCatalog("ds sc");
        assertEquals(false, datastoreSynonymCatalog.isLoadIntoMemory());
        assertEquals("orderdb", datastoreSynonymCatalog.getDatastoreName());
        assertEquals("products.productname", datastoreSynonymCatalog.getMasterTermColumnPath());
        assertEquals("[products.productline, product.producttype]", Arrays.toString(datastoreSynonymCatalog
                .getSynonymColumnPaths()));
    }

    @Test
    public void testWriteAndReadAllServers() throws Exception {
        configurationWriter
                .externalize(new EnvironmentBasedHadoopClusterInformation("environment", "Environment-based cluster"));
        configurationWriter.externalize(
                new DirectoryBasedHadoopClusterInformation("directory", "Directory-based cluster", "C:\\Users\\Test",
                        "file:///C:/Users/Test2"));
        configurationWriter
                .externalize(new DirectConnectionHadoopClusterInformation("namenode", "Namenode-based cluster",
                        URI.create("hdfs://localhost:8020/")));

        final String str = transform(configurationWriter.getDocument());

        // "Default" hadoop cluster reference should never be written
        assertFalse(str.contains(HadoopResource.DEFAULT_CLUSTERREFERENCE));

        final File file = new File("target/" + getClass().getSimpleName() + "-" + testName.getMethodName() + ".xml");
        FileHelper.writeStringAsFile(file, str);

        final DataCleanerConfiguration configuration = new JaxbConfigurationReader().create(file);
        ServerInformationCatalog serverInformationCatalog = configuration.getServerInformationCatalog();
        assertEquals("[directory, environment, namenode, org.datacleaner.hadoop.environment]",
                Arrays.toString(serverInformationCatalog.getServerNames()));

        assertNotNull(serverInformationCatalog.getServer("environment"));

        DirectoryBasedHadoopClusterInformation directoryBasedHadoopClusterInformation =
                (DirectoryBasedHadoopClusterInformation) serverInformationCatalog.getServer("directory");
        assertArrayEquals(new String[] { "C:\\Users\\Test", "file:///C:/Users/Test2" },
                directoryBasedHadoopClusterInformation.getDirectories());

        DirectConnectionHadoopClusterInformation directConnectionHadoopClusterInformation =
                (DirectConnectionHadoopClusterInformation) serverInformationCatalog.getServer("namenode");
        assertEquals(URI.create("hdfs://localhost:8020/"), directConnectionHadoopClusterInformation.getNameNodeUri());
    }

    @Test
    public void testWriteAndReadHadoopResourceDatastore() throws Exception {
        final MockHadoopConfigHelper helper = new MockHadoopConfigHelper(_temporaryFolder);

        helper.generateCoreFile();
        // Prepare "environment"
        try {
            System.setProperty(EnvironmentBasedHadoopClusterInformation.HADOOP_CONF_DIR, helper.getConfFolder().getAbsolutePath());

            final HadoopResource hadoopResource = new HadoopResource(URI.create("example-dates.csv"), new Configuration(),
                    HadoopResource.DEFAULT_CLUSTERREFERENCE);
            configurationWriter.externalize(new CsvDatastore("csvDatastore", hadoopResource));
            final String str = transform(configurationWriter.getDocument());
            final File file = new File("target/" + getClass().getSimpleName() + "-" + testName.getMethodName() + ".xml");
            FileHelper.writeStringAsFile(file, str);

            final DataCleanerConfiguration configuration = new JaxbConfigurationReader().create(file);

            final CsvDatastore csvDatastore = (CsvDatastore) configuration.getDatastoreCatalog().getDatastore("csvDatastore");
            final HadoopResource resource = (HadoopResource) csvDatastore.getResource();
            assertNotNull(resource);
            assertEquals("example-dates.csv", resource.getFilepath());
            assertEquals(helper.getPath(), resource.getHadoopConfiguration().get("fs.defaultFS"));
        } finally {
            System.clearProperty(EnvironmentBasedHadoopClusterInformation.HADOOP_CONF_DIR);
        }
    }

    @Test
    public void testWriteAndReadAllStringPatterns() throws Exception {
        configurationWriter.externalize(new SimpleStringPattern("simple sp", "aaaa@aaaa.aaa"));
        configurationWriter.externalize(new RegexStringPattern("regex pattern", ".*", false));

        final String str = transform(configurationWriter.getDocument());
        final File file = new File("target/" + getClass().getSimpleName() + "-" + testName.getMethodName() + ".xml");
        FileHelper.writeStringAsFile(file, str);

        final DataCleanerConfiguration configuration = new JaxbConfigurationReader().create(file);
        assertEquals("[regex pattern, simple sp]", Arrays.toString(configuration.getReferenceDataCatalog()
                .getStringPatternNames()));

        final SimpleStringPattern simpleStringPattern = (SimpleStringPattern) configuration.getReferenceDataCatalog()
                .getStringPattern("simple sp");
        assertEquals("aaaa@aaaa.aaa", simpleStringPattern.getExpression());

        final RegexStringPattern regexStringPattern = (RegexStringPattern) configuration.getReferenceDataCatalog()
                .getStringPattern("regex pattern");
        assertEquals(".*", regexStringPattern.getExpression());
        assertEquals(false, regexStringPattern.isMatchEntireString());
    }

    private String transform(Node elem) throws Exception {
        return XmlUtils.writeDocumentToString(elem, false).replace("\r", "");
    }
}
