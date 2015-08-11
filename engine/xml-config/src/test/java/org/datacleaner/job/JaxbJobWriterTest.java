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
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.xml.datatype.DatatypeFactory;

import junit.framework.TestCase;

import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.FileHelper;
import org.datacleaner.api.InputColumn;
import org.datacleaner.beans.CharacterSetDistributionAnalyzer;
import org.datacleaner.beans.NumberAnalyzer;
import org.datacleaner.beans.StringAnalyzer;
import org.datacleaner.beans.dategap.DateGapAnalyzer;
import org.datacleaner.beans.filter.NullCheckFilter;
import org.datacleaner.beans.filter.SingleWordFilter;
import org.datacleaner.beans.filter.ValidationCategory;
import org.datacleaner.beans.standardize.EmailStandardizerTransformer;
import org.datacleaner.beans.stringpattern.PatternFinderAnalyzer;
import org.datacleaner.beans.transform.ConcatenatorTransformer;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.connection.CsvDatastore;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalogImpl;
import org.datacleaner.data.MutableInputColumn;
import org.datacleaner.descriptors.Descriptors;
import org.datacleaner.descriptors.SimpleDescriptorProvider;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.job.builder.FilterComponentBuilder;
import org.datacleaner.job.builder.TransformerComponentBuilder;
import org.datacleaner.job.jaxb.JobMetadataType;
import org.datacleaner.test.MockAnalyzer;
import org.datacleaner.test.TestHelper;
import org.easymock.EasyMock;

public class JaxbJobWriterTest extends TestCase {

    // mock metadata factory used in this test case because we will otherwise
    // have time-dependent dates in the metadata which will make it difficult to
    // compare results
    private JaxbJobMetadataFactory _metadataFactory;
    private JaxbJobWriter _writer;

    protected void setUp() throws Exception {
        _metadataFactory = new JaxbJobMetadataFactoryImpl() {

            @Override
            protected void buildMainSection(JobMetadataType jobMetadata, AnalysisJob analysisJob) throws Exception {
                jobMetadata.setAuthor("John Doe");
                jobMetadata.setJobVersion("2.0");
                jobMetadata.setCreatedDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(2010, 11, 12, 13, 48,
                        0, 0, 0));
            }

        };
        _writer = new JaxbJobWriter(new DataCleanerConfigurationImpl(), _metadataFactory);
    };

    public void testColumnPathWhenColumnNameIsBlank() throws Exception {
        final CsvDatastore ds = new CsvDatastore("input", "src/test/resources/csv_with_blank_column_name.txt", null,
                ';', "UTF8");

        final SimpleDescriptorProvider descriptorProvider = new SimpleDescriptorProvider();
        descriptorProvider.addAnalyzerBeanDescriptor(Descriptors.ofAnalyzer(MockAnalyzer.class));

        final DatastoreCatalogImpl datastoreCatalog = new DatastoreCatalogImpl(ds);

        final DataCleanerConfiguration conf = new DataCleanerConfigurationImpl().withDatastoreCatalog(datastoreCatalog)
                .withEnvironment(new DataCleanerEnvironmentImpl().withDescriptorProvider(descriptorProvider));

        final AnalysisJob builtJob;
        try (final AnalysisJobBuilder jobBuilder = new AnalysisJobBuilder(conf)) {
            jobBuilder.setDatastore(ds);
            final Table table = jobBuilder.getDatastoreConnection().getDataContext().getDefaultSchema().getTable(0);
            assertEquals("[foo, bar, baz, ]", Arrays.toString(table.getColumnNames()));
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
        assertTrue(str,
                str.indexOf("<column id=\"col_\" path=\"csv_with_blank_column_name.txt.\" type=\"STRING\"/>") != -1);

        final AnalysisJob readJob = new JaxbJobReader(conf).read(new ByteArrayInputStream(bytes));

        List<InputColumn<?>> sourceColumns = readJob.getSourceColumns();
        assertEquals("[MetaModelInputColumn[resources.csv_with_blank_column_name.txt.foo], "
                + "MetaModelInputColumn[resources.csv_with_blank_column_name.txt.bar], "
                + "MetaModelInputColumn[resources.csv_with_blank_column_name.txt.baz], "
                + "MetaModelInputColumn[resources.csv_with_blank_column_name.txt.]]", sourceColumns.toString());
    }

    public void testReadAndWriteAnyComponentRequirementJob() throws Exception {
        Datastore ds = TestHelper.createSampleDatabaseDatastore("my database");
        SimpleDescriptorProvider descriptorProvider = new SimpleDescriptorProvider();
        descriptorProvider.addFilterBeanDescriptor(Descriptors.ofFilter(NullCheckFilter.class));
        descriptorProvider.addTransformerBeanDescriptor(Descriptors.ofTransformer(ConcatenatorTransformer.class));
        descriptorProvider.addAnalyzerBeanDescriptor(Descriptors.ofAnalyzer(StringAnalyzer.class));

        DataCleanerConfiguration conf = new DataCleanerConfigurationImpl().withDatastores(ds).withEnvironment(
                new DataCleanerEnvironmentImpl().withDescriptorProvider(descriptorProvider));

        JaxbJobReader reader = new JaxbJobReader(conf);
        AnalysisJob job;
        try (AnalysisJobBuilder jobBuilder = reader.create(new File(
                "src/test/resources/example-job-any-component-requirement.xml"))) {
            job = jobBuilder.toAnalysisJob();
        }

        ComponentRequirement requirement = job.getAnalyzerJobs().get(0).getComponentRequirement();
        assertEquals("AnyComponentRequirement[]", requirement.toString());

        assertMatchesBenchmark(job, "JaxbJobWriterTest-testReadAndWriteAnyComponentRequirementJob.xml");
    }

    public void testReadAndWriteCompoundComponentRequirementJob() throws Exception {
        Datastore ds = TestHelper.createSampleDatabaseDatastore("my database");
        SimpleDescriptorProvider descriptorProvider = new SimpleDescriptorProvider();
        descriptorProvider.addFilterBeanDescriptor(Descriptors.ofFilter(NullCheckFilter.class));
        descriptorProvider.addTransformerBeanDescriptor(Descriptors.ofTransformer(ConcatenatorTransformer.class));
        descriptorProvider.addAnalyzerBeanDescriptor(Descriptors.ofAnalyzer(StringAnalyzer.class));
        DataCleanerConfiguration conf = new DataCleanerConfigurationImpl().withDatastores(ds).withEnvironment(
                new DataCleanerEnvironmentImpl().withDescriptorProvider(descriptorProvider));

        JaxbJobReader reader = new JaxbJobReader(conf);
        AnalysisJob job;
        try (AnalysisJobBuilder jobBuilder = reader.create(new File(
                "src/test/resources/example-job-compound-component-requirement.xml"))) {
            job = jobBuilder.toAnalysisJob();
        }

        ComponentRequirement requirement = job.getAnalyzerJobs().get(0).getComponentRequirement();
        assertEquals("FilterOutcome[category=NOT_NULL] OR FilterOutcome[category=NULL]", requirement.toString());

        assertMatchesBenchmark(job, "JaxbJobWriterTest-testReadAndWriteCompoundComponentRequirementJob.xml");
    }

    @SuppressWarnings("unchecked")
    public void testNullColumnProperty() throws Exception {
        Datastore ds = TestHelper.createSampleDatabaseDatastore("db");
        DataCleanerConfiguration conf = new DataCleanerConfigurationImpl().withDatastores(ds);
        try (AnalysisJobBuilder ajb = new AnalysisJobBuilder(conf)) {
            ajb.setDatastore(ds);

            DateGapAnalyzer dga = ajb.addAnalyzer(DateGapAnalyzer.class).getComponentInstance();
            Column orderDateColumn = ds.openConnection().getSchemaNavigator()
                    .convertToColumn("PUBLIC.ORDERS.ORDERDATE");
            Column shippedDateColumn = ds.openConnection().getSchemaNavigator()
                    .convertToColumn("PUBLIC.ORDERS.SHIPPEDDATE");

            ajb.addSourceColumns(orderDateColumn, shippedDateColumn);

            dga.setFromColumn((InputColumn<Date>) ajb.getSourceColumnByName("ORDERDATE"));
            dga.setToColumn((InputColumn<Date>) ajb.getSourceColumnByName("SHIPPEDDATE"));
            dga.setSingleDateOverlaps(true);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            _writer.write(ajb.toAnalysisJob(), baos);

            String str = new String(baos.toByteArray());
            str = str.replaceAll("\"", "_");

            String[] lines = str.split("\n");
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
            assertEquals("            <column id=_col_orderdate_ path=_ORDERS.ORDERDATE_ type=_TIMESTAMP_/>", lines[10]);
            assertEquals("            <column id=_col_shippeddate_ path=_ORDERS.SHIPPEDDATE_ type=_TIMESTAMP_/>", lines[11]);
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
        AnalysisJob job = EasyMock.createMock(AnalysisJob.class);
        EasyMock.expect(job.getMetadata()).andReturn(AnalysisJobMetadata.EMPTY_METADATA).anyTimes();
        Datastore ds = EasyMock.createMock(Datastore.class);

        EasyMock.expect(job.getDatastore()).andReturn(ds);

        EasyMock.expect(ds.getName()).andReturn("myds");

        EasyMock.expect(job.getSourceColumns()).andReturn(new ArrayList<InputColumn<?>>());
        EasyMock.expect(job.getTransformerJobs()).andReturn(new ArrayList<TransformerJob>());
        EasyMock.expect(job.getFilterJobs()).andReturn(new ArrayList<FilterJob>());
        EasyMock.expect(job.getAnalyzerJobs()).andReturn(new ArrayList<AnalyzerJob>());

        EasyMock.replay(job, ds);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        _writer.write(job, baos);

        String str = new String(baos.toByteArray());
        str = str.replaceAll("\"", "_");
        String[] lines = str.split("\n");
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
        Datastore datastore = TestHelper.createSampleDatabaseDatastore("my db");
        DataCleanerConfiguration configuration = new DataCleanerConfigurationImpl().withDatastores(datastore);
        try (AnalysisJobBuilder ajb = new AnalysisJobBuilder(configuration)) {

            ajb.setDatastore("my db");

            ajb.addSourceColumns("PUBLIC.EMPLOYEES.FIRSTNAME", "PUBLIC.EMPLOYEES.LASTNAME", "PUBLIC.EMPLOYEES.EMAIL");

            InputColumn<?> fnCol = ajb.getSourceColumnByName("FIRSTNAME");
            InputColumn<?> lnCol = ajb.getSourceColumnByName("LASTNAME");
            InputColumn<?> emailCol = ajb.getSourceColumnByName("EMAIL");

            AnalyzerComponentBuilder<StringAnalyzer> strAnalyzer = ajb.addAnalyzer(StringAnalyzer.class);
            strAnalyzer.addInputColumns(fnCol, lnCol);

            assertMatchesBenchmark(ajb.toAnalysisJob(), "JaxbJobWriterTest-file1.xml");

            TransformerComponentBuilder<EmailStandardizerTransformer> tjb = ajb
                    .addTransformer(EmailStandardizerTransformer.class);
            tjb.addInputColumn(emailCol);
            strAnalyzer.addInputColumns(tjb.getOutputColumns());

            assertMatchesBenchmark(ajb.toAnalysisJob(), "JaxbJobWriterTest-file2.xml");

            FilterComponentBuilder<NullCheckFilter, NullCheckFilter.NullCheckCategory> fjb1 = ajb
                    .addFilter(NullCheckFilter.class);
            fjb1.addInputColumn(fnCol);
            strAnalyzer.setRequirement(fjb1, "NOT_NULL");

            assertMatchesBenchmark(ajb.toAnalysisJob(), "JaxbJobWriterTest-file3.xml");

            AnalyzerComponentBuilder<PatternFinderAnalyzer> patternFinder1 = ajb
                    .addAnalyzer(PatternFinderAnalyzer.class);
            makeCrossPlatformCompatible(patternFinder1);
            MutableInputColumn<?> usernameColumn = tjb.getOutputColumnByName("Username");
            patternFinder1.addInputColumn(fnCol).addInputColumn(usernameColumn).getComponentInstance()
                    .setEnableMixedTokens(false);

            assertMatchesBenchmark(ajb.toAnalysisJob(), "JaxbJobWriterTest-file4.xml");

            FilterComponentBuilder<SingleWordFilter, ValidationCategory> fjb2 = ajb.addFilter(SingleWordFilter.class);
            fjb2.addInputColumn(usernameColumn);

            AnalyzerComponentBuilder<PatternFinderAnalyzer> patternFinder2 = ajb
                    .addAnalyzer(PatternFinderAnalyzer.class);
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
        Datastore ds = TestHelper.createSampleDatabaseDatastore("my database");
        SimpleDescriptorProvider descriptorProvider = new SimpleDescriptorProvider();
        descriptorProvider.addTransformerBeanDescriptor(Descriptors.ofTransformer(ConcatenatorTransformer.class));
        descriptorProvider.addAnalyzerBeanDescriptor(Descriptors.ofAnalyzer(CharacterSetDistributionAnalyzer.class));
        descriptorProvider.addAnalyzerBeanDescriptor(Descriptors.ofAnalyzer(StringAnalyzer.class));
        descriptorProvider.addAnalyzerBeanDescriptor(Descriptors.ofAnalyzer(NumberAnalyzer.class));

        DataCleanerConfiguration conf = new DataCleanerConfigurationImpl().withDatastores(ds).withEnvironment(
                new DataCleanerEnvironmentImpl().withDescriptorProvider(descriptorProvider));

        JaxbJobReader reader = new JaxbJobReader(conf);
        AnalysisJob job;
        try (AnalysisJobBuilder jobBuilder = reader.create(new File(
                "src/test/resources/example-job-output-dataset.analysis.xml"))) {
            job = jobBuilder.toAnalysisJob();
        }

        assertMatchesBenchmark(job, "example-job-output-dataset.analysis.xml");
    }

    /**
     * Helper method to make sure that some of the locale-dependent settings of
     * the pattern finder are standardized in order to make the test
     * cross-platform compatible.
     * 
     * @param pfb
     */
    private void makeCrossPlatformCompatible(AnalyzerComponentBuilder<PatternFinderAnalyzer> pfb) {
        PatternFinderAnalyzer pf = pfb.getComponentInstance();
        pf.setDecimalSeparator('.');
        pf.setMinusSign('-');
        pf.setThousandsSeparator(',');
    }

    private void assertMatchesBenchmark(AnalysisJob analysisJob, String filename) throws Exception {
        final File outputFolder = new File("target/test-output/");
        if (!outputFolder.exists()) {
            assertTrue("Could not create output folder!", outputFolder.mkdirs());
        }

        final File benchmarkFolder = new File("src/test/resources/");

        File outputFile = new File(outputFolder, filename);

        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile))) {
            _writer.write(analysisJob, bos);
            bos.flush();
        }
        String output = FileHelper.readFileAsString(outputFile);

        File benchmarkFile = new File(benchmarkFolder, filename);
        if (!benchmarkFile.exists()) {
            assertEquals("No benchmark file '" + filename + "' exists!", output);
        }
        String benchmark = FileHelper.readFileAsString(benchmarkFile);
        assertEquals(benchmark, output);
    }
}
