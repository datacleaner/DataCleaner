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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.metamodel.util.FileResource;
import org.apache.metamodel.util.ToStringComparator;
import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.OutputDataStream;
import org.datacleaner.beans.CompletenessAnalyzerResult;
import org.datacleaner.beans.StringAnalyzerResult;
import org.datacleaner.beans.dategap.DateGapAnalyzerResult;
import org.datacleaner.beans.dategap.DateGapTextRenderer;
import org.datacleaner.beans.transform.DateMaskMatcherTransformer;
import org.datacleaner.beans.valuedist.ValueDistributionAnalyzerResult;
import org.datacleaner.components.convert.ConvertToDateTransformer;
import org.datacleaner.components.fuse.CoalesceUnit;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.configuration.DataCleanerEnvironment;
import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.configuration.SourceColumnMapping;
import org.datacleaner.connection.CsvDatastore;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.connection.DatastoreCatalogImpl;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.connection.ExcelDatastore;
import org.datacleaner.connection.SchemaNavigator;
import org.datacleaner.data.MetaModelInputColumn;
import org.datacleaner.descriptors.ClasspathScanDescriptorProvider;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.descriptors.DescriptorProvider;
import org.datacleaner.descriptors.Descriptors;
import org.datacleaner.descriptors.SimpleDescriptorProvider;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.job.builder.TransformerComponentBuilder;
import org.datacleaner.job.concurrent.MultiThreadedTaskRunner;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.job.runner.AnalysisRunner;
import org.datacleaner.job.runner.AnalysisRunnerImpl;
import org.datacleaner.reference.ReferenceDataCatalog;
import org.datacleaner.reference.ReferenceDataCatalogImpl;
import org.datacleaner.reference.SimpleSynonymCatalog;
import org.datacleaner.reference.SynonymCatalog;
import org.datacleaner.result.CrosstabResult;
import org.datacleaner.result.renderer.CrosstabTextRenderer;
import org.datacleaner.test.MockAnalyzer;
import org.datacleaner.test.TestHelper;

import junit.framework.TestCase;

public class JaxbJobReaderTest extends TestCase {

    private final DescriptorProvider descriptorProvider =
            new ClasspathScanDescriptorProvider().scanPackage("org.datacleaner", true);
    private final DatastoreCatalog datastoreCatalog =
            new DatastoreCatalogImpl(TestHelper.createSampleDatabaseDatastore("my database"));

    private final DataCleanerConfigurationImpl conf =
            new DataCleanerConfigurationImpl().withDatastoreCatalog(datastoreCatalog)
                    .withEnvironment(new DataCleanerEnvironmentImpl().withDescriptorProvider(descriptorProvider));

    // see #1196 - Synonym lookup changes has broken old jobs
    public void testReadJobWhereOutputColumnsHasBeenAddedToComponent() throws Exception {
        final SynonymCatalog synonymCatalog = new SimpleSynonymCatalog("Job titles");
        final Collection<SynonymCatalog> synonyms = Collections.singletonList(synonymCatalog);
        final ReferenceDataCatalog referenceDataCatalog =
                new ReferenceDataCatalogImpl(Collections.emptyList(), synonyms, Collections.emptyList());
        final DataCleanerConfigurationImpl conf = this.conf.withReferenceDataCatalog(referenceDataCatalog);

        final JobReader<InputStream> reader = new JaxbJobReader(conf);
        final AnalysisJob job = reader.read(
                new FileInputStream(new File("src/test/resources/example-job-job-title-analytics.analysis.xml")));
        assertNotNull(job);
    }

    public void testReadComponentNames() throws Exception {
        final JobReader<InputStream> reader = new JaxbJobReader(conf);
        final AnalysisJob job =
                reader.read(new FileInputStream(new File("src/test/resources/example-job-component-names.xml")));

        assertEquals(1, job.getAnalyzerJobs().size());
        assertEquals("analyzer_1", job.getAnalyzerJobs().iterator().next().getName());

        assertEquals(2, job.getFilterJobs().size());
        assertEquals("single_word_1", job.getFilterJobs().iterator().next().getName());

        assertEquals(1, job.getTransformerJobs().size());
        assertEquals("email_std_1", job.getTransformerJobs().iterator().next().getName());
    }

    public void testReadOnlyColumnNamePaths() throws Exception {
        final JobReader<InputStream> reader = new JaxbJobReader(conf);
        final FileInputStream source =
                new FileInputStream(new File("src/test/resources/example-job-only-columns-names-paths.analysis.xml"));
        final AnalysisJobMetadata metadata = reader.readMetadata(source);
        assertNotNull(metadata);
        assertEquals("UKContactData.csv", metadata.getDatastoreName());
        assertEquals("[RecordId, Company, FirstName, LastName, AddressLine1, AddressLine2, AddressLine3, "
                + "AddressLine4, City, State, Country, Postcode]", metadata.getSourceColumnPaths().toString());
    }

    public void testReadMetadataFull() throws Exception {
        final JobReader<InputStream> reader = new JaxbJobReader(conf);
        final AnalysisJobMetadata metadata =
                reader.readMetadata(new FileInputStream(new File("src/test/resources/example-job-metadata.xml")));

        assertEquals("Kasper Sørensen", metadata.getAuthor());
        assertEquals("my database", metadata.getDatastoreName());
        assertEquals("Job metadata", metadata.getJobName());
        assertEquals("An example job with complete metadata", metadata.getJobDescription());
        assertEquals("1.1", metadata.getJobVersion());
        assertEquals("[PUBLIC.PERSONS.FIRSTNAME, PUBLIC.PERSONS.LASTNAME]", metadata.getSourceColumnPaths().toString());
        assertEquals("propertyValue", metadata.getProperties().get("propertyName"));

        assertNotNull(metadata.getCreatedDate());
        assertNotNull(metadata.getUpdatedDate());
    }

    public void testReadMetadataNone() throws Exception {
        final JobReader<InputStream> reader = new JaxbJobReader(new DataCleanerConfigurationImpl());
        final AnalysisJobMetadata metadata =
                reader.readMetadata(new FileInputStream(new File("src/test/resources/example-job-valid.xml")));

        assertNull(metadata.getAuthor());
        assertNull(metadata.getJobName());
        assertNull(metadata.getJobDescription());
        assertNull(metadata.getJobVersion());
        assertTrue(metadata.getProperties().isEmpty());
        assertEquals("my database", metadata.getDatastoreName());
        assertEquals("[PUBLIC.EMPLOYEES.FIRSTNAME, PUBLIC.EMPLOYEES.LASTNAME, PUBLIC.EMPLOYEES.EMAIL]",
                metadata.getSourceColumnPaths().toString());

        assertNull(metadata.getCreatedDate());
        assertNull(metadata.getUpdatedDate());
    }

    public void testSimpleFilter() throws Exception {
        final JaxbJobReader reader = new JaxbJobReader(conf);
        final AnalysisJobBuilder jobBuilder =
                reader.create(new File("src/test/resources/example-job-simple-filter.xml"));
        assertEquals(1, jobBuilder.getFilterComponentBuilders().size());
        assertEquals(3, jobBuilder.getAnalyzerComponentBuilders().size());

        final AnalysisJob analysisJob = jobBuilder.toAnalysisJob();

        final AnalysisResultFuture resultFuture = new AnalysisRunnerImpl(conf).run(analysisJob);

        final List<AnalyzerResult> results = resultFuture.getResults();
        assertEquals(3, results.size());

        // sort it to make sure test is deterministic
        Collections.sort(results, ToStringComparator.getComparator());

        // the first result is for the unfiltered String analyzer
        final CrosstabResult res3 = (CrosstabResult) results.get(0);
        assertEquals(1, res3.getCrosstab().where("Column", "FIRSTNAME").where("Measures", "Min words").get());
        assertEquals(2, res3.getCrosstab().where("Column", "FIRSTNAME").where("Measures", "Max words").get());

        // this result represents the single manager (one unique and no repeated
        // values)
        final ValueDistributionAnalyzerResult res1 = (ValueDistributionAnalyzerResult) results.get(1);
        assertEquals("[[<unique>->1]]", res1.getValueCounts().toString());
        assertEquals(1, res1.getUniqueCount().intValue());

        // this result represents all the employees: Two repeated values and 18
        // unique
        final ValueDistributionAnalyzerResult res2 = (ValueDistributionAnalyzerResult) results.get(2);
        assertEquals(18, res2.getUniqueCount().intValue());
        assertEquals("[[<unique>->18], [Gerard->2], [Leslie->2]]", res2.getValueCounts().toString());
    }

    public void testNamedInputs() throws Exception {
        final JaxbJobReader factory = new JaxbJobReader(conf);
        final AnalysisJobBuilder jobBuilder =
                factory.create(new File("src/test/resources/example-job-named-inputs.xml"));
        assertEquals(true, jobBuilder.isConfigured());

        assertEquals(2, jobBuilder.getTransformerComponentBuilders().size());

        final List<AnalyzerComponentBuilder<?>> analyzerJobBuilders = jobBuilder.getAnalyzerComponentBuilders();
        assertEquals(1, analyzerJobBuilders.size());

        final AnalyzerComponentBuilder<?> analyzerJobBuilder = analyzerJobBuilders.get(0);
        final AnalyzerJob analyzerJob = analyzerJobBuilder.toAnalyzerJob();
        final ComponentConfiguration configuration = analyzerJob.getConfiguration();

        final InputColumn<?> col1 = (InputColumn<?>) configuration
                .getProperty(analyzerJob.getDescriptor().getConfiguredProperty("From column"));
        assertEquals("date 1", col1.getName());

        final InputColumn<?> col2 = (InputColumn<?>) configuration
                .getProperty(analyzerJob.getDescriptor().getConfiguredProperty("To column"));
        assertEquals("date 2", col2.getName());

        final AnalysisJob analysisJob = jobBuilder.toAnalysisJob();

        final AnalysisResultFuture resultFuture = new AnalysisRunnerImpl(conf).run(analysisJob);
        final List<AnalyzerResult> results = resultFuture.getResults();
        assertEquals(1, results.size());
        final DateGapAnalyzerResult result = (DateGapAnalyzerResult) results.get(0);
        final String[] resultLines = new DateGapTextRenderer().render(result).split("\n");
        assertEquals(58, resultLines.length);
        assertEquals(" - time gap: 2003-01-18 to 2003-01-29", resultLines[0]);
        assertEquals(" - time gap: 2003-02-09 to 2003-02-11", resultLines[1]);
        assertEquals(" - time gap: 2003-05-16 to 2003-05-20", resultLines[2]);
        assertEquals(" - time gap: 2003-07-23 to 2003-07-24", resultLines[3]);
        assertEquals(" - time gap: 2003-08-21 to 2003-08-25", resultLines[4]);
        assertEquals(" - time gap: 2003-09-02 to 2003-09-03", resultLines[5]);
        assertEquals(" - time gap: 2003-11-03 to 2003-11-04", resultLines[6]);
        assertEquals(" - time gap: 2003-12-17 to 2004-01-02", resultLines[7]);
        assertEquals(" - time gap: 2004-05-24 to 2004-05-26", resultLines[8]);
        assertEquals(" - time gap: 2004-09-22 to 2004-09-27", resultLines[9]);
        assertEquals(" - time gap: 2004-12-24 to 2005-01-05", resultLines[10]);
        assertEquals(" - time gap: 2005-05-28 to 2005-05-29", resultLines[11]);
        assertEquals(" - time overlap: 2003-01-09 to 2003-01-18", resultLines[12]);
        assertEquals(" - time overlap: 2003-01-31 to 2003-02-07", resultLines[13]);
        assertEquals(" - time overlap: 2005-05-29 to 2005-06-08", resultLines[57]);
    }

    public void testInvalidRead() throws Exception {
        final JaxbJobReader factory = new JaxbJobReader(new DataCleanerConfigurationImpl());
        try {
            factory.create(new File("src/test/resources/example-job-invalid.xml"));
            fail("Exception expected");
        } catch (final IllegalArgumentException e) {
            final String message = e.getMessage();
            assertTrue(message, message.startsWith("javax.xml.bind.UnmarshalException"));
            assertTrue(message, message.toLowerCase().contains("uri:\"http://eobjects.org/analyzerbeans/job/1.0\""));
            assertTrue(message, message.contains("\"datacontext\""));
        }
    }

    public void testMissingDatastore() throws Exception {
        final JaxbJobReader factory = new JaxbJobReader(new DataCleanerConfigurationImpl());
        try {
            factory.create(new File("src/test/resources/example-job-valid.xml"));
            fail("Exception expected");
        } catch (final NoSuchDatastoreException e) {
            assertEquals("No such datastore: my database", e.getMessage());
        }
    }

    public void testMissingTransformerDescriptor() throws Exception {
        final JaxbJobReader factory = new JaxbJobReader(conf);
        try {
            factory.create(new File("src/test/resources/example-job-missing-descriptor.xml"));
            fail("Exception expected");
        } catch (final NoSuchComponentException e) {
            assertEquals("No such TransformerType descriptor: tokenizerDescriptor", e.getMessage());
        }
    }

    public void testValidJob() throws Exception {
        final JaxbJobReader factory = new JaxbJobReader(conf);
        final AnalysisJobBuilder builder = factory.create(new File("src/test/resources/example-job-valid.xml"));
        assertTrue(builder.isConfigured());

        final List<MetaModelInputColumn> sourceColumns = builder.getSourceColumns();
        assertEquals(3, sourceColumns.size());
        assertEquals("MetaModelInputColumn[PUBLIC.EMPLOYEES.FIRSTNAME]", sourceColumns.get(0).toString());
        assertEquals("MetaModelInputColumn[PUBLIC.EMPLOYEES.LASTNAME]", sourceColumns.get(1).toString());
        assertEquals("MetaModelInputColumn[PUBLIC.EMPLOYEES.EMAIL]", sourceColumns.get(2).toString());

        assertEquals(1, builder.getTransformerComponentBuilders().size());
        assertEquals("[TransformedInputColumn[id=trans-0001-0002,name=username], "
                + "TransformedInputColumn[id=trans-0001-0003,name=domain]]",
                builder.getTransformerComponentBuilders().get(0).getOutputColumns().toString());
        assertEquals("[TransformedInputColumn[id=trans-0001-0002,name=username], "
                        + "TransformedInputColumn[id=trans-0001-0003,name=domain], "
                        + "MetaModelInputColumn[PUBLIC.EMPLOYEES.FIRSTNAME], "
                        + "MetaModelInputColumn[PUBLIC.EMPLOYEES.LASTNAME]]",
                Arrays.toString(builder.getAnalyzerComponentBuilders().get(0).toAnalyzerJob().getInput()));

        final List<AnalyzerResult> results = new AnalysisRunnerImpl(conf).run(builder.toAnalysisJob()).getResults();
        assertEquals(1, results.size());
        final CrosstabResult crosstabResult = (CrosstabResult) results.get(0);

        final String[] resultLines = crosstabResult.toString(-1).split("\n");
        assertEquals(85, resultLines.length);
        assertEquals("Crosstab:", resultLines[0]);
        assertEquals("FIRSTNAME,Avg chars: 5.391304347826087", resultLines[1]);
        assertEquals("FIRSTNAME,Avg white spaces: 0.043478260869565216", resultLines[2]);
        assertEquals("FIRSTNAME,Blank count: 0", resultLines[3]);
        assertEquals("FIRSTNAME,Diacritic chars: 0", resultLines[4]);
        assertEquals("FIRSTNAME,Digit chars: 0", resultLines[5]);
    }

    public void testUsingSourceAlternateDatastore() throws Throwable {
        final Datastore datastore = TestHelper.createSampleDatabaseDatastore("another datastore name");
        final JaxbJobReader reader = new JaxbJobReader(conf);
        final AnalysisJobBuilder analysisJobBuilder =
                reader.create(new FileInputStream(new File("src/test/resources/example-job-valid.xml")), null,
                        datastore);

        final AnalysisJob analysisJob = analysisJobBuilder.toAnalysisJob();
        assertEquals("another datastore name", analysisJob.getDatastore().getName());
    }

    public void testUsingSourceColumnMapping() throws Throwable {
        final Datastore datastore = TestHelper.createSampleDatabaseDatastore("another datastore name");
        final JobReader<InputStream> reader = new JaxbJobReader(conf);

        final AnalysisJobMetadata metadata =
                reader.readMetadata(new FileInputStream(new File("src/test/resources/example-job-valid.xml")));
        final SourceColumnMapping sourceColumnMapping = new SourceColumnMapping(metadata.getSourceColumnPaths());
        assertFalse(sourceColumnMapping.isSatisfied());
        assertEquals("[PUBLIC.EMPLOYEES.EMAIL, PUBLIC.EMPLOYEES.FIRSTNAME, PUBLIC.EMPLOYEES.LASTNAME]",
                sourceColumnMapping.getPaths().toString());

        sourceColumnMapping.setDatastore(datastore);
        final DatastoreConnection con = datastore.openConnection();
        final SchemaNavigator sn = con.getSchemaNavigator();
        sourceColumnMapping.setColumn("PUBLIC.EMPLOYEES.EMAIL", sn.convertToColumn("PUBLIC.CUSTOMERS.PHONE"));
        sourceColumnMapping
                .setColumn("PUBLIC.EMPLOYEES.FIRSTNAME", sn.convertToColumn("PUBLIC.CUSTOMERS.CONTACTFIRSTNAME"));
        sourceColumnMapping
                .setColumn("PUBLIC.EMPLOYEES.LASTNAME", sn.convertToColumn("PUBLIC.CUSTOMERS.CONTACTLASTNAME"));

        assertEquals("[]", sourceColumnMapping.getUnmappedPaths().toString());
        assertTrue(sourceColumnMapping.isSatisfied());

        final AnalysisJob job = reader.read(new FileInputStream(new File("src/test/resources/example-job-valid.xml")),
                sourceColumnMapping);

        assertEquals("another datastore name", job.getDatastore().getName());
        assertEquals("[MetaModelInputColumn[PUBLIC.CUSTOMERS.CONTACTFIRSTNAME], "
                + "MetaModelInputColumn[PUBLIC.CUSTOMERS.CONTACTLASTNAME], "
                + "MetaModelInputColumn[PUBLIC.CUSTOMERS.PHONE]]", job.getSourceColumns().toString());

        final AnalysisRunner runner = new AnalysisRunnerImpl(conf);
        final AnalysisResultFuture resultFuture = runner.run(job);
        if (!resultFuture.isSuccessful()) {
            throw resultFuture.getErrors().get(0);
        }

        final AnalyzerResult res = resultFuture.getResults().get(0);
        assertTrue(res instanceof StringAnalyzerResult);

        final String[] resultLines = new CrosstabTextRenderer().render((CrosstabResult) res).split("\n");
        assertEquals(
                "                                              username           domain CONTACTFIRSTNAME  CONTACTLASTNAME ",
                resultLines[0]);
        assertEquals(
                "Row count                                          214              214              214              214 ",
                resultLines[1]);
        assertEquals(
                "Null count                                         214              214                1                0 ",
                resultLines[2]);
    }

    public void testReadVariables() throws Exception {
        final CsvDatastore datastore = new CsvDatastore("date-datastore", "src/test/resources/example-dates.csv");
        final DataCleanerConfiguration configuration = new DataCleanerConfigurationImpl().withDatastores(datastore)
                .withEnvironment(new DataCleanerEnvironmentImpl().withDescriptorProvider(descriptorProvider));
        final JaxbJobReader reader = new JaxbJobReader(configuration);
        final File file = new File("src/test/resources/example-job-variables-ods.analysis.xml");
        assertTrue(file.exists());
        final AnalysisJobBuilder ajb = reader.create(file);

        final AnalysisJobBuilder odsjb =
                ajb.getAnalyzerComponentBuilders().get(0).getOutputDataStreamJobBuilder("Complete rows");

        final List<TransformerComponentBuilder<?>> tjbs = odsjb.getTransformerComponentBuilders();

        final DateMaskMatcherTransformer dateMaskMatcherTransformer1 =
                (DateMaskMatcherTransformer) tjbs.get(0).getComponentInstance();
        assertEquals("[yyyy-MM-dd]", Arrays.toString(dateMaskMatcherTransformer1.getDateMasks()));

        final DateMaskMatcherTransformer dateMaskMatcherTransformer2 =
                (DateMaskMatcherTransformer) tjbs.get(1).getComponentInstance();
        assertEquals("[yy-dd-MM]", Arrays.toString(dateMaskMatcherTransformer2.getDateMasks()));

        final ConvertToDateTransformer convertToDateTransformer =
                (ConvertToDateTransformer) tjbs.get(2).getComponentInstance();
        assertEquals("[yyyy-MM-dd]", Arrays.toString(convertToDateTransformer.getDateMasks()));
        assertEquals("2000-01-01",
                new SimpleDateFormat("yyyy-MM-dd").format(convertToDateTransformer.getNullReplacement()));
    }

    public void testReadChainOfFilters() throws Exception {
        final JaxbJobReader reader = new JaxbJobReader(conf);
        final AnalysisJobBuilder jobBuilder =
                reader.create(new File("src/test/resources/example-job-chain-of-filters.xml"));
        assertNotNull(jobBuilder);

        assertEquals(3, jobBuilder.getFilterComponentBuilders().size());
        assertEquals(1, jobBuilder.getAnalyzerComponentBuilders().size());
        assertEquals(0, jobBuilder.getTransformerComponentBuilders().size());
    }

    public void testReadAndExecuteOutputDataStreams() throws Throwable {
        final JobReader<InputStream> reader = new JaxbJobReader(conf);
        final AnalysisJob job = reader.read(
                new FileInputStream(new File("src/test/resources/example-job-output-dataset.analysis.xml")));

        assertEquals(1, job.getAnalyzerJobs().size());
        final AnalyzerJob analyzerJob = job.getAnalyzerJobs().get(0);
        assertEquals("Completeness analyzer", analyzerJob.getDescriptor().getDisplayName());

        assertEquals(2, analyzerJob.getOutputDataStreamJobs().length);
        final OutputDataStreamJob completeOutputDataStreamJob = analyzerJob.getOutputDataStreamJobs()[0];
        assertEquals("Complete rows", completeOutputDataStreamJob.getOutputDataStream().getName());
        assertEquals(2, completeOutputDataStreamJob.getJob().getAnalyzerJobs().size());
        final AnalyzerJob completeStringAnalyzer = completeOutputDataStreamJob.getJob().getAnalyzerJobs().get(0);
        assertEquals("String analyzer", completeStringAnalyzer.getDescriptor().getDisplayName());
        assertEquals(1, completeStringAnalyzer.getInput().length);
        assertEquals("Concat of FIRSTNAME,LASTNAME", completeStringAnalyzer.getInput()[0].getName());

        final AnalyzerJob completeNumberAnalyzer = completeOutputDataStreamJob.getJob().getAnalyzerJobs().get(1);
        assertEquals("Number analyzer", completeNumberAnalyzer.getDescriptor().getDisplayName());
        assertEquals(1, completeNumberAnalyzer.getInput().length);
        assertEquals("REPORTSTO", completeNumberAnalyzer.getInput()[0].getName());

        assertEquals(2, analyzerJob.getOutputDataStreamJobs().length);
        final OutputDataStreamJob incompleteOutputDataStreamJob = analyzerJob.getOutputDataStreamJobs()[1];
        assertEquals("Incomplete rows", incompleteOutputDataStreamJob.getOutputDataStream().getName());
        assertEquals(2, incompleteOutputDataStreamJob.getJob().getAnalyzerJobs().size());
        final AnalyzerJob incompleteStringAnalyzer = incompleteOutputDataStreamJob.getJob().getAnalyzerJobs().get(0);
        assertEquals("String analyzer", incompleteStringAnalyzer.getDescriptor().getDisplayName());
        assertEquals(1, incompleteStringAnalyzer.getInput().length);
        assertEquals("Concat of FIRSTNAME,LASTNAME", incompleteStringAnalyzer.getInput()[0].getName());

        final AnalyzerJob incompleteNumberAnalyzer = incompleteOutputDataStreamJob.getJob().getAnalyzerJobs().get(1);
        assertEquals("Number analyzer", incompleteNumberAnalyzer.getDescriptor().getDisplayName());
        assertEquals(1, incompleteNumberAnalyzer.getInput().length);
        assertEquals("REPORTSTO", incompleteNumberAnalyzer.getInput()[0].getName());

        final MultiThreadedTaskRunner taskRunner = new MultiThreadedTaskRunner(16);
        final DataCleanerEnvironment environment = new DataCleanerEnvironmentImpl().withTaskRunner(taskRunner);
        final Datastore datastore = TestHelper.createSampleDatabaseDatastore("testoutputdatastream");
        final DataCleanerConfiguration configuration =
                new DataCleanerConfigurationImpl().withDatastores(datastore).withEnvironment(environment);

        final OutputDataStreamJob[] outputDataStreamJobs = analyzerJob.getOutputDataStreamJobs();
        final AnalyzerJob analyzerJob2 = outputDataStreamJobs[0].getJob().getAnalyzerJobs().get(0);
        final AnalyzerJob analyzerJob3 = outputDataStreamJobs[1].getJob().getAnalyzerJobs().get(0);

        // now run the job(s)
        final AnalysisRunnerImpl runner = new AnalysisRunnerImpl(configuration);
        final AnalysisResultFuture resultFuture = runner.run(job);
        resultFuture.await();

        if (resultFuture.isErrornous()) {
            throw resultFuture.getErrors().get(0);
        }

        assertEquals(5, resultFuture.getResults().size());

        final CompletenessAnalyzerResult result1 = (CompletenessAnalyzerResult) resultFuture.getResult(analyzerJob);
        assertNotNull(result1);
        assertEquals(23, result1.getValidRowCount());
        assertEquals(0, result1.getInvalidRowCount());
        final StringAnalyzerResult result2 = (StringAnalyzerResult) resultFuture.getResult(analyzerJob2);
        assertNotNull(result2);
        assertEquals(23, result2.getRowCount(result2.getColumns()[0]));
        assertEquals(0, result2.getNullCount(result2.getColumns()[0]));

        final StringAnalyzerResult result3 = (StringAnalyzerResult) resultFuture.getResult(analyzerJob3);
        assertNotNull(result3);
        assertEquals(0, result3.getRowCount(result3.getColumns()[0]));
    }

    public void testPlainSearchReplaceJobUpgrade() throws Exception {
        final JaxbJobReader reader = new JaxbJobReader(conf);
        final AnalysisJobBuilder jobBuilder =
                reader.create(new File("src/test/resources/version_4_5_3_plain_search_replace.analysis.xml"));
        assertTrue(jobBuilder.isConfigured());
    }

    public void testCoalesceJobWithTranformerColumns() throws Exception {
        final JaxbJobReader reader = new JaxbJobReader(conf);
        final AnalysisJobBuilder jobBuilder =
                reader.create(new File("src/test/resources/example-job-coalesce-issue.analysis.xml"));
        final List<ComponentBuilder> componentBuilders = new ArrayList<>(jobBuilder.getComponentBuilders());
        assertEquals(5, componentBuilders.size());
        final ComponentBuilder componentBuilder = componentBuilders.get(3);
        final ComponentDescriptor<?> descriptor = componentBuilder.getDescriptor();

        assertEquals("Fuse / Coalesce fields", descriptor.getDisplayName());
        assertTrue(componentBuilder.isConfigured());
        final ConfiguredPropertyDescriptor configuredPropertyDescriptor =
                componentBuilder.getDescriptor().getConfiguredProperty("Units");
        final CoalesceUnit[] units =
                (CoalesceUnit[]) componentBuilder.getConfiguredProperty(configuredPropertyDescriptor);
        assertEquals("EQ name", units[0].getInputColumnNames()[0]);
        assertEquals("NEQ name", units[0].getInputColumnNames()[1]);
    }

    public void testCoalesceJobWithCombinedTranformerColumns() throws Exception {
        final JaxbJobReader reader = new JaxbJobReader(conf);
        final AnalysisJobBuilder jobBuilder =
                reader.create(new File("src/test/resources/example-job-coalesce-combined-columns.analysis.xml"));
        final List<ComponentBuilder> componentBuilders = new ArrayList<>(jobBuilder.getComponentBuilders());
        assertEquals(3, componentBuilders.size());
        final ComponentBuilder componentBuilder = componentBuilders.get(1);
        final ComponentDescriptor<?> descriptor = componentBuilder.getDescriptor();

        assertEquals("Fuse / Coalesce fields", descriptor.getDisplayName());
        assertTrue(componentBuilder.isConfigured());

        final ConfiguredPropertyDescriptor configuredPropertyDescriptor =
                componentBuilder.getDescriptor().getConfiguredProperty("Units");
        final CoalesceUnit[] units =
                (CoalesceUnit[]) componentBuilder.getConfiguredProperty(configuredPropertyDescriptor);
        assertEquals("PUBLIC.CUSTOMERS.CONTACTLASTNAME", units[0].getInputColumnNames()[0]);
        assertEquals("CONTACTLASTNAME (Upper case)", units[0].getInputColumnNames()[1]);
    }

    public void testCoalesceJobWithInputColumns() throws Exception {
        final JaxbJobReader reader = new JaxbJobReader(conf);
        final AnalysisJobBuilder jobBuilder =
                reader.create(new File("src/test/resources/example-job-coalesce-inputcolumns.analysis.xml"));
        final List<ComponentBuilder> componentBuilders = new ArrayList<>(jobBuilder.getComponentBuilders());
        assertEquals(2, componentBuilders.size());
        final ComponentBuilder componentBuilder = componentBuilders.get(0);
        final ComponentDescriptor<?> descriptor = componentBuilder.getDescriptor();

        assertEquals("Fuse / Coalesce fields", descriptor.getDisplayName());
        assertTrue(componentBuilder.isConfigured());

        final ConfiguredPropertyDescriptor configuredPropertyDescriptor =
                componentBuilder.getDescriptor().getConfiguredProperty("Units");
        final CoalesceUnit[] units =
                (CoalesceUnit[]) componentBuilder.getConfiguredProperty(configuredPropertyDescriptor);
        assertEquals("PUBLIC.CUSTOMERS.CONTACTLASTNAME", units[0].getInputColumnNames()[0]);
        assertEquals("PUBLIC.CUSTOMERS.CONTACTFIRSTNAME", units[0].getInputColumnNames()[1]);
        assertEquals("PUBLIC.CUSTOMERS.PHONE", units[1].getInputColumnNames()[0]);
        assertEquals("PUBLIC.CUSTOMERS.CITY", units[1].getInputColumnNames()[1]);
    }

    public void testUnionJob() throws Exception {

        final JaxbJobReader reader = new JaxbJobReader(conf);
        final AnalysisJobBuilder jobBuilder =
                reader.create(new File("src/test/resources/example-job-union.analysis.xml"));
        final List<ComponentBuilder> componentBuilders = new ArrayList<>(jobBuilder.getComponentBuilders());
        assertEquals(1, componentBuilders.size());
        final ComponentBuilder componentBuilder = componentBuilders.get(0);
        final ComponentDescriptor<?> descriptor = componentBuilder.getDescriptor();

        assertEquals("Union", descriptor.getDisplayName());
        assertTrue(componentBuilder.isConfigured());

        final ConfiguredPropertyDescriptor configuredPropertyDescriptor =
                componentBuilder.getDescriptor().getConfiguredProperty("Units");
        final CoalesceUnit[] units =
                (CoalesceUnit[]) componentBuilder.getConfiguredProperty(configuredPropertyDescriptor);
        assertEquals("PUBLIC.CUSTOMERS.CONTACTLASTNAME", units[0].getInputColumnNames()[0]);
        assertEquals("PUBLIC.EMPLOYEES.LASTNAME", units[0].getInputColumnNames()[1]);
        assertEquals("CONTACTLASTNAME", units[0].getSuggestedOutputColumnName());
        assertEquals("PUBLIC.CUSTOMERS.CONTACTFIRSTNAME", units[1].getInputColumnNames()[0]);
        assertEquals("PUBLIC.EMPLOYEES.FIRSTNAME", units[1].getInputColumnNames()[1]);
        assertEquals("CONTACTFIRSTNAME", units[1].getSuggestedOutputColumnName());

        final List<OutputDataStream> outputDataStreams = componentBuilder.getOutputDataStreams();
        assertEquals(1, outputDataStreams.size());
        final OutputDataStream outputDataStream = outputDataStreams.get(0);
        assertEquals("output", outputDataStream.getName());
    }

    /**
     * Validates whether the values of datastore column names of a job which is written to disk using the
     * {@link JaxbJobWriter} and then read as a new job using the {@link JaxbJobReader} are the same before
     * and after when the datastore column names contains line feeds.
     */
    public void testReadJobWithMultipleLinedColumnNames() throws Exception {
        final Datastore datastore = new ExcelDatastore("doubles", null, "src/test/resources/double.xlsx");

        final SimpleDescriptorProvider descriptorProvider = new SimpleDescriptorProvider();
        descriptorProvider.addAnalyzerBeanDescriptor(Descriptors.ofAnalyzer(MockAnalyzer.class));

        final DataCleanerConfiguration configuration =
                new DataCleanerConfigurationImpl().withDatastoreCatalog(new DatastoreCatalogImpl(datastore))
                        .withEnvironment(new DataCleanerEnvironmentImpl().withDescriptorProvider(descriptorProvider));

        final AnalysisJobBuilder jobBuilder = new AnalysisJobBuilder(configuration);

        jobBuilder.setDatastore(datastore);
        jobBuilder.addSourceColumns(
                jobBuilder.getDatastoreConnection().getDataContext().getDefaultSchema().getTable(0).getColumns());
        jobBuilder.addAnalyzer(MockAnalyzer.class).addInputColumns(jobBuilder.getSourceColumns());

        final AnalysisJob originalJob = jobBuilder.toAnalysisJob();

        jobBuilder.close();

        final JaxbJobWriter writer =
                new JaxbJobWriter(new DataCleanerConfigurationImpl(), new JaxbJobMetadataFactoryImpl());

        final File jobFile = File.createTempFile("double", ".analysis.xml");

        writer.write(originalJob, new FileOutputStream(jobFile));

        final AnalysisJob readJob = new JaxbJobReader(configuration).read(new FileInputStream(jobFile));

        assertEquals(originalJob.getSourceColumns().stream().map(InputColumn::getName).collect(Collectors.toList()),
                readJob.getSourceColumns().stream().map(InputColumn::getName).collect(Collectors.toList()));
    }
    public void testJobWithTemplateProperties() throws IOException {
        final JaxbJobReader reader = new JaxbJobReader(conf);
        final AnalysisJobBuilder jobBuilder = reader.create(new File("src/test/resources/example-job-template.xml"));

        final List<ComponentBuilder> componentBuilders = new ArrayList<>(jobBuilder.getComponentBuilders());
        assertEquals(1, componentBuilders.size());
        final ComponentBuilder componentBuilder = componentBuilders.get(0);
        final ComponentDescriptor<?> descriptor = componentBuilder.getDescriptor();
        assertEquals("Completeness analyzer", descriptor.getDisplayName());
        final AnalysisJobBuilder outputDataStreamJobBuilder = componentBuilder.getOutputDataStreamJobBuilder(
                "Complete rows");
        final List<ComponentBuilder> componentBuilders2 = new ArrayList<>(outputDataStreamJobBuilder
                .getComponentBuilders());
        assertEquals(1, componentBuilders2.size());
        final ComponentBuilder createCsvComponentBuilder = componentBuilders2.get(0);
        assertEquals("Create CSV file", createCsvComponentBuilder.getDescriptor().getDisplayName());
        final LinkedList<Object> linkedList = new LinkedList<>(createCsvComponentBuilder.getConfiguredProperties()
                .values());
        final FileResource propertyFile = (FileResource) linkedList
                .get(3);
        String absolutePath = propertyFile.getFile().getAbsolutePath();
        absolutePath = absolutePath.replace("\\", "/"); 
        absolutePath = absolutePath.replace("C:", "");
        assertEquals("/tmp/ignite/hotfolder/dc_input - 2016-12-12 14:14:56 - samples.csv", absolutePath);
    }
    
    public void testJobWithTemplateProperties2() throws IOException {
        final DescriptorProvider descriptorProvider = new ClasspathScanDescriptorProvider().scanPackage(
                "org.datacleaner", true);
        final Datastore datastore = TestHelper.createSampleDatabaseDatastore("my db");
        final DataCleanerConfiguration configuration = new DataCleanerConfigurationImpl().withDatastores(datastore)
                .withEnvironment(new DataCleanerEnvironmentImpl().withDescriptorProvider(descriptorProvider));
        ;
        final JaxbJobReader reader = new JaxbJobReader(configuration);
        final AnalysisJobBuilder jobBuilder = reader.create(new File(
                "src/test/resources/JaxbJobWriterTest-testWriteCsvTemplate.xml"));

        final List<ComponentBuilder> componentBuilders = new ArrayList<>(jobBuilder.getComponentBuilders());
        assertEquals(1, componentBuilders.size());
        final ComponentBuilder componentBuilder = componentBuilders.get(0);
        final ComponentDescriptor<?> descriptor = componentBuilder.getDescriptor();
        assertEquals("Create CSV file", descriptor.getDisplayName());
        final LinkedList<Object> linkedList = new LinkedList<>(componentBuilder.getConfiguredProperties().values());
        final FileResource propertyFile = (FileResource) linkedList.get(3);
        String absolutePath = propertyFile.getFile().getAbsolutePath();
        absolutePath = absolutePath.replace("\\", "/");
        absolutePath = absolutePath.replace("C:", "");
        assertEquals("/Users/claudiap/Documents/OutgoingHotFolder/myFile/1482244133378-samples.csv", absolutePath);
    }

}
