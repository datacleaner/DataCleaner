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
package org.datacleaner.job;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.datatype.DatatypeFactory;

import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.FileResource;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.OutputDataStream;
import org.datacleaner.beans.CompletenessAnalyzer;
import org.datacleaner.beans.CompletenessAnalyzer.Condition;
import org.datacleaner.beans.NumberAnalyzer;
import org.datacleaner.beans.StringAnalyzer;
import org.datacleaner.beans.dategap.DateGapAnalyzer;
import org.datacleaner.beans.filter.NullCheckFilter;
import org.datacleaner.beans.filter.SingleWordFilter;
import org.datacleaner.beans.filter.ValidationCategory;
import org.datacleaner.beans.standardize.EmailStandardizerTransformer;
import org.datacleaner.beans.stringpattern.PatternFinderAnalyzer;
import org.datacleaner.beans.transform.ConcatenatorTransformer;
import org.datacleaner.beans.valuedist.ValueDistributionAnalyzer;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.connection.CsvDatastore;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalogImpl;
import org.datacleaner.data.MutableInputColumn;
import org.datacleaner.descriptors.ClasspathScanDescriptorProvider;
import org.datacleaner.descriptors.DescriptorProvider;
import org.datacleaner.descriptors.Descriptors;
import org.datacleaner.descriptors.SimpleDescriptorProvider;
import org.datacleaner.extension.output.CreateCsvFileAnalyzer;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.job.builder.FilterComponentBuilder;
import org.datacleaner.job.builder.TransformerComponentBuilder;
import org.datacleaner.job.jaxb.JobMetadataType;
import org.datacleaner.metadata.TemplateMetadata;
import org.datacleaner.test.MockAnalyzer;
import org.datacleaner.test.TestHelper;
import org.easymock.EasyMock;

import junit.framework.TestCase;

@SuppressWarnings("deprecation")
public class JaxbJobWriterTest extends TestCase {

    // mock metadata factory used in this test case because we will otherwise
    // have time-dependent dates in the metadata which will make it difficult to
    // compare results
    private JaxbJobMetadataFactory _metadataFactory;
    private JaxbJobWriter _writer;

    protected void setUp() throws Exception {
        _metadataFactory = new JaxbJobMetadataFactoryImpl() {

            @Override
            protected void buildMainSection(final JobMetadataType jobMetadata, final AnalysisJob analysisJob)
                    throws Exception {
                jobMetadata.setAuthor("John Doe");
                jobMetadata.setJobVersion("2.0");
                jobMetadata.setCreatedDate(
                        DatatypeFactory.newInstance().newXMLGregorianCalendar(2010, 11, 12, 13, 48, 0, 0, 0));
            }

        };
        _writer = new JaxbJobWriter(new DataCleanerConfigurationImpl(), _metadataFactory);
    }

    public void testColumnPathWhenColumnNameIsBlank() throws Exception {
        final CsvDatastore ds =
                new CsvDatastore("input", "src/test/resources/csv_with_blank_column_name.txt", null, ';', "UTF8");

        final SimpleDescriptorProvider descriptorProvider = new SimpleDescriptorProvider();
        descriptorProvider.addAnalyzerBeanDescriptor(Descriptors.ofAnalyzer(MockAnalyzer.class));

        final DatastoreCatalogImpl datastoreCatalog = new DatastoreCatalogImpl(ds);

        final DataCleanerConfiguration conf = new DataCleanerConfigurationImpl().withDatastoreCatalog(datastoreCatalog)
                .withEnvironment(new DataCleanerEnvironmentImpl().withDescriptorProvider(descriptorProvider));

        final AnalysisJob builtJob;
        try (AnalysisJobBuilder jobBuilder = new AnalysisJobBuilder(conf)) {
            jobBuilder.setDatastore(ds);
            final Table table = jobBuilder.getDatastoreConnection().getDataContext().getDefaultSchema().getTable(0);
            assertEquals("[foo, bar, baz, A]", table.getColumnNames().toString());
            assertEquals(4, table.getColumnCount());
            jobBuilder.addSourceColumns(table.getColumns());

            final AnalyzerComponentBuilder<MockAnalyzer> analyzer = jobBuilder.addAnalyzer(MockAnalyzer.class);
            analyzer.addInputColumns(jobBuilder.getSourceColumns());

            builtJob = jobBuilder.toAnalysisJob();
        }

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        _writer.write(builtJob, out);

        final byte[] bytes = out.toByteArray();
        final String str = new String(bytes);
        assertTrue(str, str.indexOf("<column id=\"col_a\" path=\"A\" type=\"STRING\"/>") != -1);

        final AnalysisJob readJob = new JaxbJobReader(conf).read(new ByteArrayInputStream(bytes));

        final List<InputColumn<?>> sourceColumns = readJob.getSourceColumns();
        assertEquals("[MetaModelInputColumn[resources.csv_with_blank_column_name.txt.foo], "
                + "MetaModelInputColumn[resources.csv_with_blank_column_name.txt.bar], "
                + "MetaModelInputColumn[resources.csv_with_blank_column_name.txt.baz], "
                + "MetaModelInputColumn[resources.csv_with_blank_column_name.txt.A]]", sourceColumns.toString());
    }

    public void testReadAndWriteAnyComponentRequirementJob() throws Exception {
        final Datastore ds = TestHelper.createSampleDatabaseDatastore("my database");
        final SimpleDescriptorProvider descriptorProvider = new SimpleDescriptorProvider();
        descriptorProvider.addFilterBeanDescriptor(Descriptors.ofFilter(NullCheckFilter.class));
        descriptorProvider.addTransformerBeanDescriptor(Descriptors.ofTransformer(ConcatenatorTransformer.class));
        descriptorProvider.addAnalyzerBeanDescriptor(Descriptors.ofAnalyzer(StringAnalyzer.class));

        final DataCleanerConfiguration conf = new DataCleanerConfigurationImpl().withDatastores(ds)
                .withEnvironment(new DataCleanerEnvironmentImpl().withDescriptorProvider(descriptorProvider));

        final JaxbJobReader reader = new JaxbJobReader(conf);
        final AnalysisJob job;
        try (AnalysisJobBuilder jobBuilder = reader
                .create(new File("src/test/resources/example-job-any-component-requirement.xml"))) {
            job = jobBuilder.toAnalysisJob();
        }

        final ComponentRequirement requirement = job.getAnalyzerJobs().get(0).getComponentRequirement();
        assertEquals("AnyComponentRequirement[]", requirement.toString());

        assertMatchesBenchmark(job, "JaxbJobWriterTest-testReadAndWriteAnyComponentRequirementJob.xml");
    }

    public void testReadAndWriteCompoundComponentRequirementJob() throws Exception {
        final Datastore ds = TestHelper.createSampleDatabaseDatastore("my database");
        final SimpleDescriptorProvider descriptorProvider = new SimpleDescriptorProvider();
        descriptorProvider.addFilterBeanDescriptor(Descriptors.ofFilter(NullCheckFilter.class));
        descriptorProvider.addTransformerBeanDescriptor(Descriptors.ofTransformer(ConcatenatorTransformer.class));
        descriptorProvider.addAnalyzerBeanDescriptor(Descriptors.ofAnalyzer(StringAnalyzer.class));
        final DataCleanerConfiguration conf = new DataCleanerConfigurationImpl().withDatastores(ds)
                .withEnvironment(new DataCleanerEnvironmentImpl().withDescriptorProvider(descriptorProvider));

        final JaxbJobReader reader = new JaxbJobReader(conf);
        final AnalysisJob job;
        try (AnalysisJobBuilder jobBuilder = reader
                .create(new File("src/test/resources/example-job-compound-component-requirement.xml"))) {
            job = jobBuilder.toAnalysisJob();
        }

        final ComponentRequirement requirement = job.getAnalyzerJobs().get(0).getComponentRequirement();
        assertEquals("FilterOutcome[category=NOT_NULL] OR FilterOutcome[category=NULL]", requirement.toString());

        assertMatchesBenchmark(job, "JaxbJobWriterTest-testReadAndWriteCompoundComponentRequirementJob.xml");
    }

    @SuppressWarnings("unchecked")
    public void testNullColumnProperty() throws Exception {
        final Datastore ds = TestHelper.createSampleDatabaseDatastore("db");
        final DataCleanerConfiguration conf = new DataCleanerConfigurationImpl().withDatastores(ds);
        try (AnalysisJobBuilder ajb = new AnalysisJobBuilder(conf)) {
            ajb.setDatastore(ds);

            final DateGapAnalyzer dga = ajb.addAnalyzer(DateGapAnalyzer.class).getComponentInstance();
            final Column orderDateColumn =
                    ds.openConnection().getSchemaNavigator().convertToColumn("PUBLIC.ORDERS.ORDERDATE");
            final Column shippedDateColumn =
                    ds.openConnection().getSchemaNavigator().convertToColumn("PUBLIC.ORDERS.SHIPPEDDATE");

            ajb.addSourceColumns(orderDateColumn, shippedDateColumn);

            dga.setFromColumn((InputColumn<Date>) ajb.getSourceColumnByName("ORDERDATE"));
            dga.setToColumn((InputColumn<Date>) ajb.getSourceColumnByName("SHIPPEDDATE"));
            dga.setSingleDateOverlaps(true);

            final ByteArrayOutputStream baos = new ByteArrayOutputStream();

            _writer.write(ajb.toAnalysisJob(), baos);

            String str = new String(baos.toByteArray());
            str = str.replaceAll("\"", "_");

            final String[] lines = str.split("\n");
            assertEquals(27, lines.length);

            assertEquals("<?xml version=_1.0_ encoding=_UTF-8_ standalone=_yes_?>", lines[0]);
            assertEquals("<job xmlns=_http://eobjects.org/analyzerbeans/job/1.0_>", lines[1]);
            assertEquals("    <job-metadata>", lines[2]);
            assertEquals("        <job-version>2.0</job-version>", lines[3]);
            assertEquals("        <author>John Doe</author>", lines[4]);
            assertEquals("        <created-date>2010-11-12Z</created-date>", lines[5]);
            assertEquals("    </job-metadata>", lines[6]);
            assertEquals("    <source>", lines[7]);
            assertEquals("        <data-context ref=_db_/>", lines[8]);
            assertEquals("        <columns>", lines[9]);
            assertEquals("            <column id=_col_orderdate_ path=_ORDERS.ORDERDATE_ type=_TIMESTAMP_/>",
                    lines[10]);
            assertEquals("            <column id=_col_shippeddate_ path=_ORDERS.SHIPPEDDATE_ type=_TIMESTAMP_/>",
                    lines[11]);
            assertEquals("        </columns>", lines[12]);
            assertEquals("    </source>", lines[13]);
            assertEquals("    <transformation/>", lines[14]);
            assertEquals("    <analysis>", lines[15]);
            assertEquals("        <analyzer>", lines[16]);
            assertEquals("            <descriptor ref=_Date gap analyzer_/>", lines[17]);
            assertEquals("            <properties>", lines[18]);
            assertEquals(
                    "                <property name=_Count intersecting from and to dates as overlaps_ value=_true_/>",
                    lines[19]);
            assertEquals("                <property name=_Fault tolerant switch from/to dates_ value=_true_/>",
                    lines[20]);
            assertEquals("            </properties>", lines[21]);
            assertEquals("            <input ref=_col_orderdate_ name=_From column_/>", lines[22]);
            assertEquals("            <input ref=_col_shippeddate_ name=_To column_/>", lines[23]);
            assertEquals("        </analyzer>", lines[24]);
            assertEquals("    </analysis>", lines[25]);
            assertEquals("</job>", lines[26]);
        }
    }

    public void testEmptyJobEnvelope() throws Exception {
        final AnalysisJob job = EasyMock.createMock(AnalysisJob.class);
        EasyMock.expect(job.getMetadata()).andReturn(AnalysisJobMetadata.EMPTY_METADATA).anyTimes();
        final Datastore ds = EasyMock.createMock(Datastore.class);

        EasyMock.expect(job.getDatastore()).andReturn(ds).atLeastOnce();

        EasyMock.expect(ds.getName()).andReturn("myds");

        EasyMock.expect(job.getSourceColumns()).andReturn(new ArrayList<>());
        EasyMock.expect(job.getTransformerJobs()).andReturn(new ArrayList<>());
        EasyMock.expect(job.getFilterJobs()).andReturn(new ArrayList<>());
        EasyMock.expect(job.getAnalyzerJobs()).andReturn(new ArrayList<>());

        EasyMock.replay(job, ds);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        _writer.write(job, baos);

        String str = new String(baos.toByteArray());
        str = str.replaceAll("\"", "_");
        final String[] lines = str.split("\n");
        assertEquals(14, lines.length);

        assertEquals("<?xml version=_1.0_ encoding=_UTF-8_ standalone=_yes_?>", lines[0]);
        assertEquals("<job xmlns=_http://eobjects.org/analyzerbeans/job/1.0_>", lines[1]);
        assertEquals("    <job-metadata>", lines[2]);
        assertEquals("        <job-version>2.0</job-version>", lines[3]);
        assertEquals("        <author>John Doe</author>", lines[4]);
        assertEquals("        <created-date>2010-11-12Z</created-date>", lines[5]);
        assertEquals("    </job-metadata>", lines[6]);
        assertEquals("    <source>", lines[7]);
        assertEquals("        <data-context ref=_myds_/>", lines[8]);
        assertEquals("        <columns/>", lines[9]);
        assertEquals("    </source>", lines[10]);
        assertEquals("    <transformation/>", lines[11]);
        assertEquals("    <analysis/>", lines[12]);
        assertEquals("</job>", lines[13]);

        EasyMock.verify(job, ds);
    }

    public void testCompareWithBenchmarkFiles() throws Exception {
        final Datastore datastore = TestHelper.createSampleDatabaseDatastore("my db");
        final DataCleanerConfiguration configuration = new DataCleanerConfigurationImpl().withDatastores(datastore);
        try (AnalysisJobBuilder ajb = new AnalysisJobBuilder(configuration)) {

            ajb.setDatastore("my db");

            ajb.addSourceColumns("PUBLIC.EMPLOYEES.FIRSTNAME", "PUBLIC.EMPLOYEES.LASTNAME", "PUBLIC.EMPLOYEES.EMAIL");

            final InputColumn<?> fnCol = ajb.getSourceColumnByName("FIRSTNAME");
            final InputColumn<?> lnCol = ajb.getSourceColumnByName("LASTNAME");
            final InputColumn<?> emailCol = ajb.getSourceColumnByName("EMAIL");

            final AnalyzerComponentBuilder<StringAnalyzer> strAnalyzer = ajb.addAnalyzer(StringAnalyzer.class);
            strAnalyzer.addInputColumns(fnCol, lnCol);

            assertMatchesBenchmark(ajb.toAnalysisJob(), "JaxbJobWriterTest-file1.xml");

            final TransformerComponentBuilder<EmailStandardizerTransformer> tjb =
                    ajb.addTransformer(EmailStandardizerTransformer.class);
            tjb.addInputColumn(emailCol);
            strAnalyzer.addInputColumns(tjb.getOutputColumns());

            assertMatchesBenchmark(ajb.toAnalysisJob(), "JaxbJobWriterTest-file2.xml");

            final FilterComponentBuilder<NullCheckFilter, NullCheckFilter.NullCheckCategory> fjb1 =
                    ajb.addFilter(NullCheckFilter.class);
            fjb1.addInputColumn(fnCol);
            strAnalyzer.setRequirement(fjb1, "NOT_NULL");

            assertMatchesBenchmark(ajb.toAnalysisJob(), "JaxbJobWriterTest-file3.xml");

            final AnalyzerComponentBuilder<PatternFinderAnalyzer> patternFinder1 =
                    ajb.addAnalyzer(PatternFinderAnalyzer.class);
            makeCrossPlatformCompatible(patternFinder1);
            final MutableInputColumn<?> usernameColumn = tjb.getOutputColumnByName("Username");
            patternFinder1.addInputColumn(fnCol).addInputColumn(usernameColumn).getComponentInstance()
                    .setEnableMixedTokens(false);

            assertMatchesBenchmark(ajb.toAnalysisJob(), "JaxbJobWriterTest-file4.xml");

            final FilterComponentBuilder<SingleWordFilter, ValidationCategory> fjb2 =
                    ajb.addFilter(SingleWordFilter.class);
            fjb2.addInputColumn(usernameColumn);

            final AnalyzerComponentBuilder<PatternFinderAnalyzer> patternFinder2 =
                    ajb.addAnalyzer(PatternFinderAnalyzer.class);
            patternFinder2.addInputColumn(tjb.getOutputColumns().get(1));
            patternFinder2.setRequirement(fjb2, ValidationCategory.INVALID);
            makeCrossPlatformCompatible(patternFinder2);

            assertMatchesBenchmark(ajb.toAnalysisJob(), "JaxbJobWriterTest-file5.xml");

            tjb.setName("trans1");
            fjb1.setName("fjb1");
            fjb2.setName("fjb2");
            patternFinder1.setName("pf 1");
            patternFinder2.setName("pf 2");

            assertMatchesBenchmark(ajb.toAnalysisJob(), "JaxbJobWriterTest-file6.xml");

        }
    }

    public void testReadAndWriteOutputDataStreamsJob() throws Exception {
        final Datastore ds = TestHelper.createSampleDatabaseDatastore("my database");
        final SimpleDescriptorProvider descriptorProvider = new SimpleDescriptorProvider();
        descriptorProvider.addTransformerBeanDescriptor(Descriptors.ofTransformer(ConcatenatorTransformer.class));
        descriptorProvider.addAnalyzerBeanDescriptor(Descriptors.ofAnalyzer(CompletenessAnalyzer.class));
        descriptorProvider.addAnalyzerBeanDescriptor(Descriptors.ofAnalyzer(StringAnalyzer.class));
        descriptorProvider.addAnalyzerBeanDescriptor(Descriptors.ofAnalyzer(NumberAnalyzer.class));

        final DataCleanerConfiguration conf = new DataCleanerConfigurationImpl().withDatastores(ds)
                .withEnvironment(new DataCleanerEnvironmentImpl().withDescriptorProvider(descriptorProvider));

        final JaxbJobReader reader = new JaxbJobReader(conf);
        final AnalysisJob job;
        try (AnalysisJobBuilder jobBuilder = reader
                .create(new File("src/test/resources/example-job-output-dataset.analysis.xml"))) {
            job = jobBuilder.toAnalysisJob();
        }

        assertMatchesBenchmark(job, "JaxbJobWriterTest-testReadAndWriteOutputDataStreamsJob.xml");
    }

    public void testWriteVariable() throws Exception {
        final DescriptorProvider descriptorProvider =
                new ClasspathScanDescriptorProvider().scanPackage("org.datacleaner", true);
        final CsvDatastore datastore = new CsvDatastore("date-datastore", "src/test/resources/example-dates.csv");
        final DataCleanerConfiguration configuration = new DataCleanerConfigurationImpl().withDatastores(datastore)
                .withEnvironment(new DataCleanerEnvironmentImpl().withDescriptorProvider(descriptorProvider));
        final JaxbJobReader reader = new JaxbJobReader(configuration);
        final File file = new File("src/test/resources/example-job-variables.xml");
        final AnalysisJobBuilder ajb = reader.create(file);

        assertMatchesBenchmark(ajb.toAnalysisJob(), "JaxbJobWriterTest-testWriteVariable.xml");
    }

    public void testNameClashInMelonAndDefaultScope() throws RuntimeException, Exception {
        final Datastore ds = TestHelper.createSampleDatabaseDatastore("db");
        final DataCleanerConfiguration conf = new DataCleanerConfigurationImpl().withDatastores(ds);
        try (AnalysisJobBuilder ajb = new AnalysisJobBuilder(conf)) {
            ajb.setDatastore(ds);
            ajb.addSourceColumns("PUBLIC.EMPLOYEES.FIRSTNAME");
            final InputColumn<?> inputFirstNameColumn = ajb.getSourceColumnByName("FIRSTNAME");

            final AnalyzerComponentBuilder<CompletenessAnalyzer> completenessAnalyzerBuilder =
                    ajb.addAnalyzer(CompletenessAnalyzer.class);
            completenessAnalyzerBuilder.addInputColumn(inputFirstNameColumn);
            final Condition[] conditions = new CompletenessAnalyzer.Condition[1];
            conditions[0] = Condition.NOT_BLANK_OR_NULL;
            completenessAnalyzerBuilder.setConfiguredProperty("Conditions", conditions);

            final OutputDataStream completeRecordsOutputDataStream =
                    completenessAnalyzerBuilder.getOutputDataStream("Complete rows");
            final AnalysisJobBuilder completeRecordsJobBuilder =
                    completenessAnalyzerBuilder.getOutputDataStreamJobBuilder(completeRecordsOutputDataStream);

            final AnalyzerComponentBuilder<ValueDistributionAnalyzer> valueDistBuilder =
                    completeRecordsJobBuilder.addAnalyzer(ValueDistributionAnalyzer.class);
            valueDistBuilder.addInputColumn(completeRecordsJobBuilder.getSourceColumnByName("Complete rows.FIRSTNAME"));

            // The benchmark expects the id of the source column in the melon to
            // be different than "col_lastname" which is the ID in the default
            // scope
            assertMatchesBenchmark(ajb.toAnalysisJob(), "JaxbJobWriterTest-testNameClashInMelonAndDefaultScope.xml");
        }
    }
    
    public void testWriteCsvTemplate() throws Exception {

        final String VARIABLE_FOLDER_OUTGOING = "psp.output.path.final";
        final String VARIABLE_FILENAME_OUTGOING = "hotfolder.input.filename";
        final String VARIABLE_TIMESTAMP_OUTGOING = "datacleaner.run.timestamp";
        final Datastore datastore = TestHelper.createSampleDatabaseDatastore("my db");
        final DataCleanerConfiguration configuration = new DataCleanerConfigurationImpl().withDatastores(datastore);
        try (AnalysisJobBuilder ajb = new AnalysisJobBuilder(configuration)) {

            ajb.setDatastore("my db");

            ajb.addSourceColumns("PUBLIC.EMPLOYEES.FIRSTNAME", "PUBLIC.EMPLOYEES.LASTNAME", "PUBLIC.EMPLOYEES.EMAIL");

            final InputColumn<?> fnCol = ajb.getSourceColumnByName("FIRSTNAME");
            final InputColumn<?> lnCol = ajb.getSourceColumnByName("LASTNAME");
            final InputColumn<?> emailCol = ajb.getSourceColumnByName("EMAIL");
            ajb.getAnalysisJobMetadata().getVariables().put(VARIABLE_FOLDER_OUTGOING,
                    "/Users/claudiap/Documents/OutgoingHotFolder");
            ajb.getAnalysisJobMetadata().getVariables().put(VARIABLE_FILENAME_OUTGOING, "myFile");
            ajb.getAnalysisJobMetadata().getVariables().put(VARIABLE_TIMESTAMP_OUTGOING, "1482244133378");

            final String value = "${" + VARIABLE_FOLDER_OUTGOING + "}" + "/" + "${" + VARIABLE_FILENAME_OUTGOING  + "}" + "/"
                    + "${" + VARIABLE_TIMESTAMP_OUTGOING + "}" + "-samples.csv";
         
            final AnalyzerComponentBuilder<CreateCsvFileAnalyzer> csvAnalyzer = ajb.addAnalyzer(
                    CreateCsvFileAnalyzer.class);
            csvAnalyzer.addInputColumns(fnCol, lnCol, emailCol);
            csvAnalyzer.setMetadataProperties(TemplateMetadata.createMetadataProperty("File", value));
            csvAnalyzer.setConfiguredProperty("File", new FileResource(value));
            assertMatchesBenchmark(ajb.toAnalysisJob(),"JaxbJobWriterTest-testWriteCsvTemplate.xml");
        }
    }
    
    public void testWriteCsv() throws Exception {

        final Datastore datastore = TestHelper.createSampleDatabaseDatastore("my db");
        final DataCleanerConfiguration configuration = new DataCleanerConfigurationImpl().withDatastores(datastore);
        try (AnalysisJobBuilder ajb = new AnalysisJobBuilder(configuration)) {
            ajb.setDatastore("my db");
            ajb.addSourceColumns("PUBLIC.EMPLOYEES.FIRSTNAME", "PUBLIC.EMPLOYEES.LASTNAME", "PUBLIC.EMPLOYEES.EMAIL");

            final InputColumn<?> fnCol = ajb.getSourceColumnByName("FIRSTNAME");
            final InputColumn<?> lnCol = ajb.getSourceColumnByName("LASTNAME");
            final InputColumn<?> emailCol = ajb.getSourceColumnByName("EMAIL");

            final FileResource file = new FileResource("tmp/myFile.csv");
            final AnalyzerComponentBuilder<CreateCsvFileAnalyzer> csvAnalyzer = ajb.addAnalyzer(
                    CreateCsvFileAnalyzer.class);
            csvAnalyzer.addInputColumns(fnCol, lnCol, emailCol);
            
            csvAnalyzer.setConfiguredProperty("File", file);

            assertMatchesBenchmark(ajb.toAnalysisJob(),"JaxbJobWriterTest-testWriteCsv.xml");
        }
    }

    /**
     * Helper method to make sure that some of the locale-dependent settings of
     * the pattern finder are standardized in order to make the test
     * cross-platform compatible.
     *
     * @param pfb
     */
    private void makeCrossPlatformCompatible(final AnalyzerComponentBuilder<PatternFinderAnalyzer> pfb) {
        final PatternFinderAnalyzer pf = pfb.getComponentInstance();
        pf.setDecimalSeparator('.');
        pf.setMinusSign('-');
        pf.setThousandsSeparator(',');
    }

    private void assertMatchesBenchmark(final AnalysisJob analysisJob, final String filename) throws Exception {
        final File outputFolder = new File("target/test-output/");

        if (!outputFolder.exists()) {
            assertTrue("Could not create output folder!", outputFolder.mkdirs());
        }

        final File benchmarkFolder = new File("src/test/resources/");
        final File outputFile = new File(outputFolder, filename);

        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile))) {
            _writer.write(analysisJob, bos);
            bos.flush();
        }

        final File benchmarkFile = new File(benchmarkFolder, filename);
        TestHelper.assertXmlFilesEquals(benchmarkFile, outputFile);
    }
}
