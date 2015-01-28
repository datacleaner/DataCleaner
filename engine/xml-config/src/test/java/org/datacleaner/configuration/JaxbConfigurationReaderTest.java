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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.lang.ArrayUtils;
import org.apache.metamodel.DataContext;
import org.apache.metamodel.csv.CsvConfiguration;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.hbase.HBaseConfiguration;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.ExclusionPredicate;
import org.apache.metamodel.util.Predicate;
import org.apache.metamodel.util.SimpleTableDef;
import org.datacleaner.api.RenderingFormat;
import org.datacleaner.connection.CassandraDatastore;
import org.datacleaner.connection.CouchDbDatastore;
import org.datacleaner.connection.CsvDatastore;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.connection.ElasticSearchDatastore;
import org.datacleaner.connection.FixedWidthDatastore;
import org.datacleaner.connection.HBaseDatastore;
import org.datacleaner.connection.JdbcDatastore;
import org.datacleaner.connection.JsonDatastore;
import org.datacleaner.connection.MongoDbDatastore;
import org.datacleaner.connection.PojoDatastore;
import org.datacleaner.connection.UpdateableDatastoreConnection;
import org.datacleaner.connection.XmlDatastore;
import org.datacleaner.descriptors.ClasspathScanDescriptorProvider;
import org.datacleaner.descriptors.DescriptorProvider;
import org.datacleaner.descriptors.Descriptors;
import org.datacleaner.descriptors.RendererBeanDescriptor;
import org.datacleaner.job.concurrent.SingleThreadedTaskRunner;
import org.datacleaner.lifecycle.LifeCycleHelper;
import org.datacleaner.reference.Dictionary;
import org.datacleaner.reference.ReferenceDataCatalog;
import org.datacleaner.reference.StringPattern;
import org.datacleaner.reference.SynonymCatalog;
import org.datacleaner.result.renderer.HtmlRenderingFormat;
import org.datacleaner.result.renderer.TextRenderingFormat;
import org.datacleaner.storage.BerkeleyDbStorageProvider;
import org.datacleaner.storage.CombinedStorageProvider;
import org.datacleaner.storage.HsqldbStorageProvider;
import org.datacleaner.storage.StorageProvider;
import org.junit.Assert;

public class JaxbConfigurationReaderTest extends TestCase {

    private final JaxbConfigurationReader reader = new JaxbConfigurationReader();
    private DatastoreCatalog _datastoreCatalog;

    public void testReadCsvFilesWithSpecialCharacters() throws Exception {
        AnalyzerBeansConfiguration configuration = reader.create(new File(
                "src/test/resources/example-configuration-csv-with-special-chars.xml"));
        CsvDatastore csv = (CsvDatastore) configuration.getDatastoreCatalog().getDatastore("csv");

        assertTrue("Unexpected separator: " + csv.getSeparatorChar(), '\t' == csv.getSeparatorChar());
        assertTrue("Unexpected escape: " + csv.getEscapeChar(), CsvConfiguration.NOT_A_CHAR == csv.getEscapeChar());

        assertTrue(csv.isMultilineValues());

        csv = (CsvDatastore) configuration.getDatastoreCatalog().getDatastore("csv_quot");

        assertEquals("\"", csv.getQuoteChar().toString());

        assertFalse(csv.isMultilineValues());
    }

    public void testReadClasspathScannerWithExcludedRenderer() throws Exception {
        AnalyzerBeansConfiguration configuration = reader.create(new File(
                "src/test/resources/example-configuration-classpath-scanner-with-exclusions.xml"));

        DescriptorProvider descriptorProvider = configuration.getDescriptorProvider();
        assertTrue(descriptorProvider instanceof ClasspathScanDescriptorProvider);

        ClasspathScanDescriptorProvider scanner = (ClasspathScanDescriptorProvider) descriptorProvider;

        Predicate<Class<? extends RenderingFormat<?>>> predicate = scanner.getRenderingFormatPredicate();
        assertNotNull(predicate);
        assertTrue(predicate instanceof ExclusionPredicate);

        Collection<RendererBeanDescriptor<?>> renderers = descriptorProvider
                .getRendererBeanDescriptorsForRenderingFormat(TextRenderingFormat.class);
        assertTrue(renderers.isEmpty());

        renderers = descriptorProvider.getRendererBeanDescriptorsForRenderingFormat(HtmlRenderingFormat.class);
        assertFalse(renderers.isEmpty());
    }

    public void testReadComplexDataInPojoDatastore() throws Exception {
        AnalyzerBeansConfiguration configuration = reader.create(new File(
                "src/test/resources/example-configuration-pojo-datastore-with-complex-data.xml"));
        Datastore datastore = configuration.getDatastoreCatalog().getDatastore("pojo");
        assertNotNull(datastore);

        DatastoreConnection con = datastore.openConnection();
        DataContext dc = con.getDataContext();
        Table table = dc.getDefaultSchema().getTable(0);

        Column[] columns = table.getColumns();
        assertEquals("[Column[name=Foo,columnNumber=0,type=VARCHAR,nullable=true,nativeType=null,columnSize=null], "
                + "Column[name=Bar,columnNumber=1,type=MAP,nullable=true,nativeType=null,columnSize=null], "
                + "Column[name=Baz,columnNumber=2,type=LIST,nullable=true,nativeType=null,columnSize=null], "
                + "Column[name=bytes,columnNumber=3,type=BINARY,nullable=true,nativeType=null,columnSize=null]]",
                Arrays.toString(columns));

        DataSet ds = dc.query().from(table).select(columns).execute();

        assertTrue(ds.next());
        assertEquals("Hello", ds.getRow().getValue(0).toString());
        assertEquals("{greeting=hello, person=world}", ds.getRow().getValue(1).toString());
        assertEquals("[hello, world]", ds.getRow().getValue(2).toString());
        assertEquals("{1,2,3,4,5}", ArrayUtils.toString(ds.getRow().getValue(3)));
        assertTrue(ds.getRow().getValue(1) instanceof Map);
        assertTrue(ds.getRow().getValue(2) instanceof List);
        assertTrue(ds.getRow().getValue(3) instanceof byte[]);

        assertTrue(ds.next());
        assertEquals("There", ds.getRow().getValue(0).toString());
        assertEquals("{greeting=hi, there you!, person={Firstname=Kasper, Lastname=Sørensen}}", ds.getRow().getValue(1)
                .toString());
        assertEquals(null, ds.getRow().getValue(2));
        assertEquals(null, ds.getRow().getValue(3));
        assertTrue(ds.getRow().getValue(1) instanceof Map);

        assertTrue(ds.next());
        assertEquals("World", ds.getRow().getValue(0).toString());
        assertEquals(null, ds.getRow().getValue(1));
        assertEquals("[Sørensen, Kasper]", ds.getRow().getValue(2).toString());
        assertEquals("{-1,-2,-3,-4,-5}", ArrayUtils.toString(ds.getRow().getValue(3)));
        assertTrue(ds.getRow().getValue(2) instanceof List);
        assertTrue(ds.getRow().getValue(3) instanceof byte[]);
    }

    public void testOverrideVariables() throws Exception {
        System.setProperty("datastoreCatalog.myDatabase.username", "foobar");
        System.setProperty("datastoreCatalog.persons_csv.filename", "foo/bar.csv");

        try {
            AnalyzerBeansConfiguration configuration = reader.create(new File(
                    "src/test/resources/example-configuration-valid.xml"));
            Datastore datastore = configuration.getDatastoreCatalog().getDatastore("my database");
            assertTrue(datastore instanceof JdbcDatastore);

            String username = ((JdbcDatastore) datastore).getUsername();
            assertEquals("foobar", username);

            datastore = configuration.getDatastoreCatalog().getDatastore("persons_csv");
            assertTrue(datastore instanceof CsvDatastore);

            String filename = ((CsvDatastore) datastore).getFilename();
            assertEquals("foo/bar.csv", filename);
        } finally {
            System.getProperties().remove("datastoreCatalog.myDatabase.username");
            System.getProperties().remove("datastoreCatalog.persons_csv.filename");
        }
    }

    public void testValidConfiguration() throws Exception {
        AnalyzerBeansConfiguration configuration = reader.create(new File(
                "src/test/resources/example-configuration-valid.xml"));

        DatastoreCatalog datastoreCatalog = getDataStoreCatalog(configuration);
        assertEquals("[composite_datastore, my database, mydb_jndi, persons_csv]",
                Arrays.toString(datastoreCatalog.getDatastoreNames()));

        assertTrue(configuration.getTaskRunner() instanceof SingleThreadedTaskRunner);
    }

    public void testCombinedStorage() throws Exception {
        AnalyzerBeansConfiguration configuration = reader.create(new File(
                "src/test/resources/example-configuration-combined-storage.xml"));
        StorageProvider storageProvider = configuration.getStorageProvider();

        assertEquals(CombinedStorageProvider.class, storageProvider.getClass());

        CombinedStorageProvider csp = (CombinedStorageProvider) storageProvider;
        assertEquals(BerkeleyDbStorageProvider.class, csp.getCollectionsStorageProvider().getClass());
        assertEquals(HsqldbStorageProvider.class, csp.getRowAnnotationsStorageProvider().getClass());
    }

    public void testAllDatastoreTypes() throws Exception {
        DatastoreCatalog datastoreCatalog = getDataStoreCatalog(getConfiguration());
        String[] datastoreNames = datastoreCatalog.getDatastoreNames();
        assertEquals(
                "[my cassandra db, my couch, my es index, my hbase, my mongo, my_access, my_composite, my_csv, my_custom, my_dbase, my_dom_xml, my_excel_2003, "
                        + "my_fixed_width_1, my_fixed_width_2, my_jdbc_connection, my_jdbc_datasource, my_json, my_odb, my_pojo, "
                        + "my_sas, my_sax_xml, my_sfdc_ds, my_sugarcrm]", Arrays.toString(datastoreNames));

        assertEquals("a mongo db based datastore", datastoreCatalog.getDatastore("my mongo").getDescription());
        assertEquals("jdbc_con", datastoreCatalog.getDatastore("my_jdbc_connection").getDescription());
        assertEquals("jdbc_ds", datastoreCatalog.getDatastore("my_jdbc_datasource").getDescription());
        assertEquals("dbf", datastoreCatalog.getDatastore("my_dbase").getDescription());

        CsvDatastore myCsvDatastore = (CsvDatastore) datastoreCatalog.getDatastore("my_csv");
        assertEquals("csv", myCsvDatastore.getDescription());
        assertTrue(myCsvDatastore.isMultilineValues());
        assertTrue(myCsvDatastore.isFailOnInconsistencies());
        assertEquals('\\', myCsvDatastore.getEscapeChar().charValue());

        CassandraDatastore cassandraDatastore = (CassandraDatastore) datastoreCatalog.getDatastore("my cassandra db");
        assertEquals("localhost", cassandraDatastore.getHostname());
        assertEquals(9042, cassandraDatastore.getPort());
        assertEquals("my_keyspace", cassandraDatastore.getKeyspace());
        assertEquals("foo", cassandraDatastore.getUsername());
        assertEquals("bar", cassandraDatastore.getPassword());
        assertEquals("[SimpleTableDef[name=table,columnNames=[bah, baz],columnTypes=[STRING, STRING]]]",
                Arrays.toString(cassandraDatastore.getTableDefs()));

        ElasticSearchDatastore esDatastore = (ElasticSearchDatastore) datastoreCatalog.getDatastore("my es index");
        assertEquals("localhost", esDatastore.getHostname());
        assertEquals(9300, esDatastore.getPort());
        assertEquals("my_es_cluster", esDatastore.getClusterName());
        assertEquals("my_index", esDatastore.getIndexName());
        assertNull(esDatastore.getTableDefs());

        assertEquals("a SugarCRM instance", datastoreCatalog.getDatastore("my_sugarcrm").getDescription());
        assertEquals("dom xml", datastoreCatalog.getDatastore("my_dom_xml").getDescription());
        assertEquals("sax xml", datastoreCatalog.getDatastore("my_sax_xml").getDescription());
        assertEquals("custom", datastoreCatalog.getDatastore("my_custom").getDescription());
        assertEquals("odb", datastoreCatalog.getDatastore("my_odb").getDescription());
        assertEquals("xls", datastoreCatalog.getDatastore("my_excel_2003").getDescription());
        assertEquals("comp", datastoreCatalog.getDatastore("my_composite").getDescription());
        assertEquals("salesforce.com is an online CRM system", datastoreCatalog.getDatastore("my_sfdc_ds")
                .getDescription());
        assertEquals("mdb", datastoreCatalog.getDatastore("my_access").getDescription());
        assertEquals("folder of sas7bdat files", datastoreCatalog.getDatastore("my_sas").getDescription());
        assertEquals("A datastore based on plain values", datastoreCatalog.getDatastore("my_pojo").getDescription());

        PojoDatastore pojoDatastore = (PojoDatastore) datastoreCatalog.getDatastore("my_pojo");
        {
            try (UpdateableDatastoreConnection con = pojoDatastore.openConnection()) {
                DataContext dc = con.getDataContext();
                Schema schema = dc.getDefaultSchema();
                assertEquals("my_schema", schema.getName());
                assertEquals(2, schema.getTableCount());
                assertEquals("[table1, table2]", Arrays.toString(schema.getTableNames()));

                assertEquals(
                        "[Column[name=Foo,columnNumber=0,type=VARCHAR,nullable=true,nativeType=null,columnSize=null], "
                                + "Column[name=Bar,columnNumber=1,type=INTEGER,nullable=true,nativeType=null,columnSize=null]]",
                        Arrays.toString(schema.getTable(0).getColumns()));
                assertEquals(
                        "[Column[name=Baz,columnNumber=0,type=BOOLEAN,nullable=true,nativeType=null,columnSize=null]]",
                        Arrays.toString(schema.getTable(1).getColumns()));

                try (DataSet ds = dc.query().from("table1").select("Foo", "Bar").execute()) {
                    assertTrue(ds.next());
                    assertEquals("Row[values=[Hello, 1]]", ds.getRow().toString());
                    assertEquals(String.class, ds.getRow().getValue(0).getClass());
                    assertEquals(Integer.class, ds.getRow().getValue(1).getClass());

                    assertTrue(ds.next());
                    assertEquals("Row[values=[There, null]]", ds.getRow().toString());
                    assertNull(ds.getRow().getValue(1));
                }

                try (DataSet ds = dc.query().from("table2").select("Baz").execute()) {
                    assertTrue(ds.next());
                    assertEquals("Row[values=[true]]", ds.getRow().toString());
                    assertEquals(Boolean.class, ds.getRow().getValue(0).getClass());
                }
            }
        }

        CouchDbDatastore couchDbDatastore = (CouchDbDatastore) datastoreCatalog.getDatastore("my couch");
        assertEquals("localhost", couchDbDatastore.getHostname());
        assertEquals("user", couchDbDatastore.getUsername());
        assertEquals("pass", couchDbDatastore.getPassword());
        assertEquals(true, couchDbDatastore.isSslEnabled());
        assertEquals(1, couchDbDatastore.getTableDefs().length);
        assertEquals("SimpleTableDef[name=foobar,columnNames=[foo, bar, baz],columnTypes=[MAP, INTEGER, VARCHAR]]",
                couchDbDatastore.getTableDefs()[0].toString());

        MongoDbDatastore mongoDbDatastore = (MongoDbDatastore) datastoreCatalog.getDatastore("my mongo");
        assertEquals("analyzerbeans_test", mongoDbDatastore.getDatabaseName());
        assertEquals("localhost", mongoDbDatastore.getHostname());
        assertEquals(27017, mongoDbDatastore.getPort());
        SimpleTableDef[] tableDefs = mongoDbDatastore.getTableDefs();
        assertEquals(
                "[SimpleTableDef[name=my_col_1,columnNames=[foo, bar, baz],columnTypes=[VARCHAR, INTEGER, DATE]]]",
                Arrays.toString(tableDefs));

        XmlDatastore xmlDatastore = (XmlDatastore) datastoreCatalog.getDatastore("my_sax_xml");
        assertEquals("../core/src/test/resources/example-xml-file.xml", xmlDatastore.getFilename());
        assertEquals("[XmlSaxTableDef[rowXpath=/greetings/greeting,"
                + "valueXpaths=[/greetings/greeting/how, /greetings/greeting/what]]]",
                Arrays.toString(xmlDatastore.getTableDefs()));

        FixedWidthDatastore ds = (FixedWidthDatastore) datastoreCatalog.getDatastore("my_fixed_width_1");
        assertEquals(19, ds.getFixedValueWidth());
        assertEquals("[]", Arrays.toString(ds.getValueWidths()));
        assertEquals(0, ds.getHeaderLineNumber());

        ds = (FixedWidthDatastore) datastoreCatalog.getDatastore("my_fixed_width_2");
        assertEquals(-1, ds.getFixedValueWidth());
        assertEquals("[4, 17, 19]", Arrays.toString(ds.getValueWidths()));
        assertEquals(1, ds.getHeaderLineNumber());

        HBaseDatastore hbaseDatastore = (HBaseDatastore) datastoreCatalog.getDatastore("my hbase");
        assertEquals("HBaseDatastore[name=my hbase]", hbaseDatastore.toString());
        assertEquals("localhost", hbaseDatastore.getZookeeperHostname());
        assertEquals(HBaseConfiguration.DEFAULT_ZOOKEEPER_PORT, hbaseDatastore.getZookeeperPort());
        tableDefs = hbaseDatastore.getTableDefs();
        assertNotNull(tableDefs);
        assertEquals(2, tableDefs.length);
        assertEquals(
                "SimpleTableDef[name=table1,columnNames=[fam1:foo, fam1:bar, fam2:baz],columnTypes=[STRING, STRING, INTEGER]]",
                tableDefs[0].toString());
        assertEquals("SimpleTableDef[name=table2,columnNames=[fam3:hello, fam3:world],columnTypes=[STRING, VARCHAR]]",
                tableDefs[1].toString());

        JsonDatastore jsonDatastore = (JsonDatastore) datastoreCatalog.getDatastore("my_json");
        assertEquals("JsonDatastore[name=my_json]", jsonDatastore.toString());

        for (String name : datastoreNames) {
            // test that all connections, except the JNDI-, MongoDB- and
            // CouchDB-based on will work
            if (!"my_jdbc_datasource".equals(name) && !"my mongo".equals(name) && !"my couch".equals(name)
                    && !"my hbase".equals(name) && !"my_sfdc_ds".equals(name) && !"my_sugarcrm".equals(name)
                    && !"my es index".equals(name)) {
                Datastore datastore = datastoreCatalog.getDatastore(name);
                DataContext dc;
                try {
                    DatastoreConnection connection = datastore.openConnection();
                    dc = connection.getDataContext();
                    assertNotNull(dc);
                } catch (RuntimeException e) {
                    throw new RuntimeException("Failed to read from datastore: " + name, e);
                }
            }
        }

        Datastore compositeDatastore = datastoreCatalog.getDatastore("my_composite");
        {
            try (DatastoreConnection con = compositeDatastore.openConnection();) {
                DataContext dataContext = con.getDataContext();
                String[] schemaNames = dataContext.getSchemaNames();
                assertEquals("[PUBLIC, Spreadsheet2003.xls, developers.mdb, resources]", Arrays.toString(schemaNames));
            }
        }
    }

    private AnalyzerBeansConfiguration getConfiguration() {
        AnalyzerBeansConfiguration configuration = reader.create(new File(
                "src/test/resources/example-configuration-all-datastore-types.xml"));
        return configuration;
    }

    private DatastoreCatalog getDataStoreCatalog(AnalyzerBeansConfiguration configuration) {
        _datastoreCatalog = configuration.getDatastoreCatalog();
        return _datastoreCatalog;
    }

    public void testReferenceDataCatalog() throws Exception {
        AnalyzerBeansConfiguration conf = getConfigurationFromXMLFile();
        ReferenceDataCatalog referenceDataCatalog = conf.getReferenceDataCatalog();
        String[] dictionaryNames = referenceDataCatalog.getDictionaryNames();
        assertEquals("[custom_dict, datastore_dict, textfile_dict, valuelist_dict]", Arrays.toString(dictionaryNames));

        LifeCycleHelper lifeCycleHelper = new LifeCycleHelper(conf.getInjectionManager(null), null, true);

        Dictionary d = referenceDataCatalog.getDictionary("datastore_dict");
        assertEquals("dict_ds", d.getDescription());
        lifeCycleHelper.assignProvidedProperties(Descriptors.ofComponent(d.getClass()), d);
        lifeCycleHelper.initialize(Descriptors.ofComponent(d.getClass()), d);
        assertTrue(d.containsValue("Patterson"));
        assertTrue(d.containsValue("Murphy"));
        assertFalse(d.containsValue("Gates"));

        d = referenceDataCatalog.getDictionary("textfile_dict");
        assertEquals("dict_txt", d.getDescription());
        lifeCycleHelper.initialize(Descriptors.ofComponent(d.getClass()), d);
        assertTrue(d.containsValue("Patterson"));
        assertFalse(d.containsValue("Murphy"));
        assertTrue(d.containsValue("Gates"));

        d = referenceDataCatalog.getDictionary("valuelist_dict");
        assertEquals("dict_simple", d.getDescription());
        lifeCycleHelper.initialize(Descriptors.ofComponent(d.getClass()), d);
        assertFalse(d.containsValue("Patterson"));
        assertFalse(d.containsValue("Murphy"));
        assertTrue(d.containsValue("greetings"));

        d = referenceDataCatalog.getDictionary("custom_dict");
        assertEquals("dict_custom", d.getDescription());
        lifeCycleHelper.initialize(Descriptors.ofComponent(d.getClass()), d);
        assertFalse(d.containsValue("Patterson"));
        assertFalse(d.containsValue("Murphy"));
        assertFalse(d.containsValue("Gates"));
        assertTrue(d.containsValue("value0"));
        assertTrue(d.containsValue("value1"));
        assertTrue(d.containsValue("value2"));
        assertTrue(d.containsValue("value3"));
        assertTrue(d.containsValue("value4"));
        assertFalse(d.containsValue("value5"));

        String[] synonymCatalogNames = referenceDataCatalog.getSynonymCatalogNames();
        assertEquals("[custom_syn, datastore_syn, textfile_syn]", Arrays.toString(synonymCatalogNames));

        SynonymCatalog s = referenceDataCatalog.getSynonymCatalog("textfile_syn");
        assertEquals("syn_txt", s.getDescription());
        lifeCycleHelper.initialize(Descriptors.ofComponent(s.getClass()), s);
        assertEquals("DNK", s.getMasterTerm("Denmark"));
        assertEquals("DNK", s.getMasterTerm("Danmark"));
        assertEquals("DNK", s.getMasterTerm("DK"));
        assertEquals("ALB", s.getMasterTerm("Albania"));
        assertEquals(null, s.getMasterTerm("Netherlands"));

        s = referenceDataCatalog.getSynonymCatalog("datastore_syn");
        assertEquals("syn_ds", s.getDescription());
        lifeCycleHelper.assignProvidedProperties(Descriptors.ofComponent(s.getClass()), s);
        lifeCycleHelper.initialize(Descriptors.ofComponent(s.getClass()), s);

        // lookup by id
        assertEquals("La Rochelle Gifts", s.getMasterTerm("119"));
        // lookup by phone number (string)
        assertEquals("Danish Wholesale Imports", s.getMasterTerm("31 12 3555"));
        assertEquals(null, s.getMasterTerm("foobar"));

        s = referenceDataCatalog.getSynonymCatalog("custom_syn");
        assertEquals("syn_custom", s.getDescription());
        lifeCycleHelper.initialize(Descriptors.ofComponent(s.getClass()), s);
        assertEquals("DNK", s.getMasterTerm("Denmark"));
        assertEquals("DNK", s.getMasterTerm("Danmark"));
        assertEquals(null, s.getMasterTerm("DK"));
        assertEquals(null, s.getMasterTerm("Albania"));
        assertEquals("NLD", s.getMasterTerm("Netherlands"));

        String[] stringPatternNames = referenceDataCatalog.getStringPatternNames();
        assertEquals("[regex danish email, simple email]", Arrays.toString(stringPatternNames));

        StringPattern pattern = referenceDataCatalog.getStringPattern("regex danish email");
        assertEquals("pattern_reg", pattern.getDescription());
        lifeCycleHelper.initialize(Descriptors.ofComponent(pattern.getClass()), pattern);
        assertEquals(
                "RegexStringPattern[name=regex danish email, expression=[a-z]+@[a-z]+\\.dk, matchEntireString=true]",
                pattern.toString());
        assertTrue(pattern.matches("kasper@eobjects.dk"));
        assertFalse(pattern.matches("kasper@eobjects.org"));
        assertFalse(pattern.matches(" kasper@eobjects.dk"));

        pattern = referenceDataCatalog.getStringPattern("simple email");
        assertEquals("pattern_simple", pattern.getDescription());
        lifeCycleHelper.initialize(Descriptors.ofComponent(pattern.getClass()), pattern);
        assertEquals("SimpleStringPattern[name=simple email, expression=aaaa@aaaaa.aa]", pattern.toString());
        assertTrue(pattern.matches("kasper@eobjects.dk"));
        assertTrue(pattern.matches("kasper@eobjects.org"));
        assertFalse(pattern.matches(" kasper@eobjects.dk"));
    }

    public void testCustomDictionaryWithInjectedDatastore() {
        AnalyzerBeansConfiguration configuration = getConfigurationFromXMLFile();
        ReferenceDataCatalog referenceDataCatalog = configuration.getReferenceDataCatalog();
        SampleCustomDictionary sampleCustomDictionary = (SampleCustomDictionary) referenceDataCatalog
                .getDictionary("custom_dict");
        Assert.assertEquals("my_jdbc_connection", sampleCustomDictionary.datastore.getName());
    }

    private AnalyzerBeansConfiguration getConfigurationFromXMLFile() {
        AnalyzerBeansConfiguration configuration = reader.create(new File(
                "src/test/resources/example-configuration-all-reference-data-types.xml"));
        return configuration;
    }
}
