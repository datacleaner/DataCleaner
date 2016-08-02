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
package org.datacleaner.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.TableModel;

import org.datacleaner.beans.filter.EqualsFilter;
import org.datacleaner.beans.filter.RangeFilterCategory;
import org.datacleaner.beans.filter.StringLengthRangeFilter;
import org.datacleaner.beans.standardize.EmailStandardizerTransformer;
import org.datacleaner.beans.transform.ConcatenatorTransformer;
import org.datacleaner.beans.transform.TokenizerTransformer;
import org.datacleaner.beans.transform.WhitespaceTrimmerTransformer;
import org.datacleaner.components.convert.ConvertToNumberTransformer;
import org.datacleaner.components.convert.ConvertToStringTransformer;
import org.datacleaner.components.fuse.CoalesceMultipleFieldsTransformer;
import org.datacleaner.components.fuse.CoalesceUnit;
import org.datacleaner.components.fuse.FuseStreamsComponent;
import org.datacleaner.components.maxrows.MaxRowsFilter;
import org.datacleaner.components.maxrows.MaxRowsFilter.Category;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.connection.DatastoreCatalogImpl;
import org.datacleaner.data.ConstantInputColumn;
import org.datacleaner.descriptors.Descriptors;
import org.datacleaner.descriptors.SimpleDescriptorProvider;
import org.datacleaner.extension.output.CreateCsvFileAnalyzer;
import org.datacleaner.job.EmptyJaxbJobMetadataFactory;
import org.datacleaner.job.JaxbJobReader;
import org.datacleaner.job.JaxbJobWriter;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.job.builder.FilterComponentBuilder;
import org.datacleaner.job.builder.TransformerComponentBuilder;
import org.datacleaner.test.MockOutputDataStreamAnalyzer;
import org.datacleaner.test.MockTransformer;
import org.datacleaner.test.TestHelper;
import org.datacleaner.util.PreviewUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.google.common.base.Joiner;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

@SuppressWarnings("deprecation")
public class PreviewTransformedDataActionListenerTest {

    private TransformerComponentBuilder<EmailStandardizerTransformer> emailTransformerBuilder;
    private AnalysisJobBuilder analysisJobBuilder;

    @Rule
    public TestName testName = new TestName();

    @Before
    public void setUp() throws Exception {
        final Datastore datastore = TestHelper.createSampleDatabaseDatastore("orderdb");
        final DatastoreCatalog datastoreCatalog = new DatastoreCatalogImpl(datastore);
        final SimpleDescriptorProvider descriptorProvider = new SimpleDescriptorProvider();

        descriptorProvider.addAnalyzerBeanDescriptor(Descriptors.ofAnalyzer(CreateCsvFileAnalyzer.class));
        descriptorProvider.addTransformerBeanDescriptor(Descriptors.ofTransformer(EmailStandardizerTransformer.class));
        descriptorProvider.addTransformerBeanDescriptor(Descriptors.ofTransformer(ConcatenatorTransformer.class));
        descriptorProvider.addTransformerBeanDescriptor(Descriptors.ofTransformer(TokenizerTransformer.class));
        descriptorProvider.addTransformerBeanDescriptor(Descriptors.ofTransformer(ConvertToNumberTransformer.class));
        descriptorProvider.addTransformerBeanDescriptor(Descriptors.ofTransformer(WhitespaceTrimmerTransformer.class));
        descriptorProvider.addFilterBeanDescriptor(Descriptors.ofFilter(StringLengthRangeFilter.class));
        descriptorProvider.addFilterBeanDescriptor(Descriptors.ofFilter(EqualsFilter.class));



        final DataCleanerConfiguration configuration = new DataCleanerConfigurationImpl().withDatastoreCatalog(
                datastoreCatalog).withEnvironment(new DataCleanerEnvironmentImpl().withDescriptorProvider(
                        descriptorProvider));

        analysisJobBuilder = new AnalysisJobBuilder(configuration);
        analysisJobBuilder.setDatastore("orderdb");
        analysisJobBuilder.addSourceColumns("PUBLIC.EMPLOYEES.EMAIL");

        emailTransformerBuilder = analysisJobBuilder.addTransformer(EmailStandardizerTransformer.class);
        emailTransformerBuilder.addInputColumn(analysisJobBuilder.getSourceColumnByName("EMAIL"));
    }
    
    @Test
    public void testPreviewTransformationAfterQueryOptimizedFilteredTransformation() throws Exception {
        analysisJobBuilder.addSourceColumns("employees.lastname");

        // add a "lastname=Patterson" filter on the email transformer - only 3
        // records will pass through that filter
        final FilterComponentBuilder<EqualsFilter, EqualsFilter.Category> filter = analysisJobBuilder.addFilter(
                EqualsFilter.class);
        filter.getComponentInstance().setValues(new String[] { "Patterson" });
        filter.addInputColumn(analysisJobBuilder.getSourceColumnByName("lastname"));
        emailTransformerBuilder.setRequirement(filter, EqualsFilter.Category.EQUALS);

        // add a transformer that consumes the email output
        final TransformerComponentBuilder<ConcatenatorTransformer> concatenator = analysisJobBuilder.addTransformer(
                ConcatenatorTransformer.class);
        concatenator.addInputColumns(emailTransformerBuilder.getOutputColumns());

        final PreviewTransformedDataActionListener action = new PreviewTransformedDataActionListener(null,
                concatenator);
        final TableModel tableModel = action.call();

        assertEquals(3, tableModel.getRowCount());
        assertEquals("mpatterso", tableModel.getValueAt(0, 0));
    }

    @Test
    public void testPreviewTransformationInOutputDataStream() throws Exception {
        final AnalyzerComponentBuilder<MockOutputDataStreamAnalyzer> streamProducer = analysisJobBuilder.addAnalyzer(
                MockOutputDataStreamAnalyzer.class);
        streamProducer.addInputColumn(emailTransformerBuilder.getOutputColumns().get(0));

        final AnalysisJobBuilder streamJobBuilder = streamProducer.getOutputDataStreamJobBuilder(
                MockOutputDataStreamAnalyzer.STREAM_NAME1);
        final TransformerComponentBuilder<MockTransformer> transformer = streamJobBuilder.addTransformer(
                MockTransformer.class);
        transformer.addInputColumn(streamJobBuilder.getSourceColumns().get(0));

        final PreviewTransformedDataActionListener action = new PreviewTransformedDataActionListener(null, transformer);
        final TableModel tableModel = action.call();
        assertEquals(17, tableModel.getRowCount());

        assertEquals("bar", tableModel.getValueAt(0, 0));
        assertEquals("mocked: bar", tableModel.getValueAt(0, 1));
    }

    @Test
    public void testPreviewTransformationInMultiStreamGeneratedOutputDataStream() throws Exception {
        analysisJobBuilder.addSourceColumns("PUBLIC.CUSTOMERS.PHONE");

        final TransformerComponentBuilder<FuseStreamsComponent> union = analysisJobBuilder.addTransformer(
                FuseStreamsComponent.class);
        union.addInputColumns(analysisJobBuilder.getSourceColumns());
        union.setConfiguredProperty("Units", new CoalesceUnit[] { new CoalesceUnit(analysisJobBuilder
                .getSourceColumns()) });

        final AnalysisJobBuilder streamJobBuilder = union.getOutputDataStreamJobBuilder(
                FuseStreamsComponent.OUTPUT_DATA_STREAM_NAME);

        final TransformerComponentBuilder<MockTransformer> transformer = streamJobBuilder.addTransformer(
                MockTransformer.class);
        transformer.addInputColumn(streamJobBuilder.getSourceColumns().get(0));

        final PreviewTransformedDataActionListener action = new PreviewTransformedDataActionListener(null, null,
                transformer, 10);
        
        compareWithBenchmark(action);
        
        final TableModel tableModel = action.call();
        assertEquals(10, tableModel.getRowCount());

        // part of it is emails
        assertEquals("dmurphy@classicmodelcars.com", tableModel.getValueAt(0, 0).toString());
        assertEquals("mpatterso@classicmodelcars.com", tableModel.getValueAt(1, 0).toString());

        // another part is phone numbers
        assertEquals("7025551838", tableModel.getValueAt(6, 0).toString());
    }

    @Test
    public void testJobWithMaxRowsFilter() throws Exception {
        final FilterComponentBuilder<MaxRowsFilter, Category> filter = analysisJobBuilder.addFilter(
                MaxRowsFilter.class);
        filter.addInputColumn(analysisJobBuilder.getSourceColumnByName("email"));
        filter.getComponentInstance().setMaxRows(5);

        emailTransformerBuilder.setRequirement(filter, Category.VALID);

        final PreviewTransformedDataActionListener action = new PreviewTransformedDataActionListener(null, null,
                emailTransformerBuilder, 10);
        
        final TableModel tableModel = action.call();
        assertEquals(5, tableModel.getRowCount());
    }

    @Test
    public void testSingleTransformer() throws Exception {
        final PreviewTransformedDataActionListener action = new PreviewTransformedDataActionListener(null, null,
                emailTransformerBuilder);
        final TableModel tableModel = action.call();

        assertEquals(3, tableModel.getColumnCount());
        assertEquals("EMAIL", tableModel.getColumnName(0));
        assertEquals("Username", tableModel.getColumnName(1));
        assertEquals("Domain", tableModel.getColumnName(2));

        assertEquals(23, tableModel.getRowCount());

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            assertTrue(tableModel.getValueAt(i, 0).toString().indexOf('@') > 1);
            assertNotNull(tableModel.getValueAt(i, 1).toString());
            assertNotNull(tableModel.getValueAt(i, 2).toString());
        }

        assertEquals("dmurphy@classicmodelcars.com", tableModel.getValueAt(0, 0).toString());
        assertEquals("dmurphy", tableModel.getValueAt(0, 1).toString());
        assertEquals("classicmodelcars.com", tableModel.getValueAt(0, 2).toString());

        assertEquals("mpatterso@classicmodelcars.com", tableModel.getValueAt(1, 0).toString());
        assertEquals("mpatterso", tableModel.getValueAt(1, 1).toString());
        assertEquals("classicmodelcars.com", tableModel.getValueAt(1, 2).toString());
    }

    @Test
    public void testChainedTransformers() throws Exception {
        final TransformerComponentBuilder<ConcatenatorTransformer> lengthTransformerBuilder = analysisJobBuilder
                .addTransformer(ConcatenatorTransformer.class);
        lengthTransformerBuilder.addInputColumn(emailTransformerBuilder.getOutputColumnByName("Username"));
        lengthTransformerBuilder.addInputColumn(new ConstantInputColumn("foo"));

        // first simple run
        {
            PreviewTransformedDataActionListener action = new PreviewTransformedDataActionListener(null, null,
                    lengthTransformerBuilder);
            TableModel tableModel = action.call();

            assertEquals(3, tableModel.getColumnCount());
            assertEquals("Username", tableModel.getColumnName(0));
            assertEquals("\"foo\"", tableModel.getColumnName(1));
            assertEquals("Concat of Username,\"foo\"", tableModel.getColumnName(2));

            assertEquals(23, tableModel.getRowCount());

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                assertTrue(tableModel.getValueAt(i, 0).toString().indexOf('@') == -1);
                Object concatValue = tableModel.getValueAt(i, 1);
                assertNotNull(concatValue);
                assertTrue(concatValue instanceof String);
            }

            assertEquals("dmurphy", tableModel.getValueAt(0, 0).toString());
            assertEquals("dmurphyfoo", tableModel.getValueAt(0, 2).toString());

            assertEquals("mpatterso", tableModel.getValueAt(1, 0).toString());
            assertEquals("mpattersofoo", tableModel.getValueAt(1, 2).toString());
        }

        // add a filter
        final FilterComponentBuilder<StringLengthRangeFilter, RangeFilterCategory> rangeFilter = analysisJobBuilder
                .addFilter(StringLengthRangeFilter.class);
        rangeFilter.addInputColumn(lengthTransformerBuilder.getOutputColumnByName("Concat of Username,\"foo\""));
        rangeFilter.setConfiguredProperty("Minimum length", 5);
        rangeFilter.setConfiguredProperty("Maximum length", 20);

        // add a multi-row transformer
        final TransformerComponentBuilder<TokenizerTransformer> tokenizer = analysisJobBuilder.addTransformer(
                TokenizerTransformer.class);
        tokenizer.addInputColumn(emailTransformerBuilder.getOutputColumnByName("Username"));
        tokenizer.setRequirement(rangeFilter.getFilterOutcome(RangeFilterCategory.VALID));
        tokenizer.setConfiguredProperty("Token target", TokenizerTransformer.TokenTarget.ROWS);
        tokenizer.setConfiguredProperty("Number of tokens", 50);
        tokenizer.setConfiguredProperty("Delimiters", new char[] { 'p' });
        assertTrue(tokenizer.isConfigured());

        // run advanced
        {
            final PreviewTransformedDataActionListener action = new PreviewTransformedDataActionListener(null, null,
                    tokenizer);
            final TableModel tableModel = action.call();

            assertEquals(29, tableModel.getRowCount());

            assertEquals("dmurphy", tableModel.getValueAt(0, 0).toString());
            assertEquals("dmur", tableModel.getValueAt(0, 1).toString());

            assertEquals("dmurphy", tableModel.getValueAt(1, 0).toString());
            assertEquals("hy", tableModel.getValueAt(1, 1).toString());

            assertEquals("mpatterso", tableModel.getValueAt(2, 0).toString());
            assertEquals("m", tableModel.getValueAt(2, 1).toString());
        }
    }

    @Test
    public void testChainedFilters() throws Exception {
        analysisJobBuilder.removeAllComponents();
        analysisJobBuilder.removeAllSourceColumns();
        analysisJobBuilder.addSourceColumns("CUSTOMERS.CUSTOMERNUMBER");
        analysisJobBuilder.addSourceColumns("CUSTOMERS.COUNTRY");
        analysisJobBuilder.addSourceColumns("CUSTOMERS.SALESREPEMPLOYEENUMBER");

        final FilterComponentBuilder<EqualsFilter, EqualsFilter.Category> countryEqualsFilter =
                analysisJobBuilder.addFilter(
                        EqualsFilter.class);
        countryEqualsFilter.getComponentInstance().setValues(new String[] { "France" });
        countryEqualsFilter.addInputColumn(analysisJobBuilder.getSourceColumnByName("COUNTRY"));
        assertTrue(countryEqualsFilter.isConfigured());

        final FilterComponentBuilder<EqualsFilter, EqualsFilter.Category> salesEmployeeNumberEqualsFilter =
                analysisJobBuilder.addFilter(
                        EqualsFilter.class);
        salesEmployeeNumberEqualsFilter.getComponentInstance().setValues(new String[] { "1370" });
        salesEmployeeNumberEqualsFilter.addInputColumn(analysisJobBuilder.getSourceColumnByName("SALESREPEMPLOYEENUMBER"));
        salesEmployeeNumberEqualsFilter
                .setRequirement(countryEqualsFilter.getFilterOutcome(EqualsFilter.Category.EQUALS));
        assertTrue(salesEmployeeNumberEqualsFilter.isConfigured());

        final TransformerComponentBuilder<ConvertToStringTransformer> convertToStringBuilder = analysisJobBuilder
                .addTransformer(ConvertToStringTransformer.class);
        convertToStringBuilder.addInputColumn(analysisJobBuilder.getSourceColumnByName("CUSTOMERNUMBER"));
        convertToStringBuilder.getComponentInstance().setNullReplacement("<null>");
        convertToStringBuilder
                .setRequirement(salesEmployeeNumberEqualsFilter.getFilterOutcome(EqualsFilter.Category.EQUALS));
        convertToStringBuilder.getOutputColumns().get(0).setName("CUSTOMERNUMBER (English)");
        assertTrue(convertToStringBuilder.isConfigured());

        final PreviewTransformedDataActionListener action = new PreviewTransformedDataActionListener(null, null,
                convertToStringBuilder);

        final TableModel tableModel = action.call();
        assertEquals(6, tableModel.getRowCount());
    }

    @Test
    public void testFilterWithCoalesce() throws Exception {
        analysisJobBuilder.removeAllComponents();
        analysisJobBuilder.removeAllSourceColumns();
        analysisJobBuilder.addSourceColumns("CUSTOMERS.CUSTOMERNUMBER");
        analysisJobBuilder.addSourceColumns("CUSTOMERS.COUNTRY");
        final FilterComponentBuilder<EqualsFilter, EqualsFilter.Category> equalsFilter = analysisJobBuilder.addFilter(
                EqualsFilter.class);
        equalsFilter.getComponentInstance().setValues(new String[] { "US", "UK", "GB", "USA" });
        equalsFilter.addInputColumn(analysisJobBuilder.getSourceColumnByName("COUNTRY"));
        assertTrue(equalsFilter.isConfigured());

        final TransformerComponentBuilder<ConvertToStringTransformer> englishConvertToStringBuilder = analysisJobBuilder
                .addTransformer(ConvertToStringTransformer.class);
        englishConvertToStringBuilder.setName("English customers");
        englishConvertToStringBuilder.addInputColumn(analysisJobBuilder.getSourceColumnByName("CUSTOMERNUMBER"));
        englishConvertToStringBuilder.addInputColumn(new ConstantInputColumn("foo"));
        englishConvertToStringBuilder.getComponentInstance().setNullReplacement("<null>");
        englishConvertToStringBuilder.setRequirement(equalsFilter.getFilterOutcome(EqualsFilter.Category.EQUALS));
        englishConvertToStringBuilder.getOutputColumns().get(0).setName("CUSTOMERNUMBER (English)");
        assertTrue(englishConvertToStringBuilder.isConfigured());

        final TransformerComponentBuilder<ConvertToStringTransformer> nonEnglishConvertToStringBuilder = analysisJobBuilder
                .addTransformer(ConvertToStringTransformer.class);
        nonEnglishConvertToStringBuilder.setName("Non-English customers");
        nonEnglishConvertToStringBuilder.addInputColumn(analysisJobBuilder.getSourceColumnByName("CUSTOMERNUMBER"));
        nonEnglishConvertToStringBuilder.addInputColumn(new ConstantInputColumn("foo"));
        nonEnglishConvertToStringBuilder.getComponentInstance().setNullReplacement("<null>");
        nonEnglishConvertToStringBuilder.setRequirement(equalsFilter.getFilterOutcome(EqualsFilter.Category.NOT_EQUALS));
        nonEnglishConvertToStringBuilder.getOutputColumns().get(0).setName("CUSTOMERNUMBER (Non-English)");
        assertTrue(nonEnglishConvertToStringBuilder.isConfigured());

        final TransformerComponentBuilder<CoalesceMultipleFieldsTransformer> coalesceMultipleFieldsTransformerComponentBuilder = analysisJobBuilder
                .addTransformer(CoalesceMultipleFieldsTransformer.class);

        coalesceMultipleFieldsTransformerComponentBuilder.addInputColumns(englishConvertToStringBuilder
                .getOutputColumns().get(0), nonEnglishConvertToStringBuilder.getOutputColumns().get(0));
        coalesceMultipleFieldsTransformerComponentBuilder.setConfiguredProperty("Units", new CoalesceUnit[] {
                new CoalesceUnit(englishConvertToStringBuilder.getOutputColumns().get(0),
                        nonEnglishConvertToStringBuilder.getOutputColumns().get(0)) });
        assertTrue(coalesceMultipleFieldsTransformerComponentBuilder.isConfigured());

        final PreviewTransformedDataActionListener action = new PreviewTransformedDataActionListener(null, null,
                coalesceMultipleFieldsTransformerComponentBuilder, 500);

        compareWithBenchmark(action);

        final TableModel tableModel = action.call();

        assertEquals(214, tableModel.getRowCount());

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            assertTrue(printRow(tableModel, i), tableModel.getValueAt(i, 0) == null || tableModel.getValueAt(i,
                    1) == null);
        }
    }

    private String printRow(TableModel tableModel, int row) {
        List<String> values = new ArrayList<>(tableModel.getColumnCount());
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            Object value = tableModel.getValueAt(row, i);
            values.add(value == null ? "<null>" : value.toString());
        }
        return Joiner.on(',').join(values);
    }

    @Test()
    public void testUnchainedTransformers() throws Exception {

        @SuppressWarnings("unused")
        final TransformerComponentBuilder<ConcatenatorTransformer> lengthTransformerBuilder = analysisJobBuilder
                .addTransformer(ConcatenatorTransformer.class);

        final PreviewTransformedDataActionListener action = new PreviewTransformedDataActionListener(null, null,
                emailTransformerBuilder);

        final TableModel tableModel = action.call();

        assertEquals(3, tableModel.getColumnCount());
        assertEquals("EMAIL", tableModel.getColumnName(0));
        assertEquals("Username", tableModel.getColumnName(1));
        assertEquals("Domain", tableModel.getColumnName(2));

        assertEquals(23, tableModel.getRowCount());

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            assertTrue(tableModel.getValueAt(i, 0).toString().indexOf('@') > 1);
            assertNotNull(tableModel.getValueAt(i, 1).toString());
            assertNotNull(tableModel.getValueAt(i, 2).toString());
        }

        assertEquals("dmurphy@classicmodelcars.com", tableModel.getValueAt(0, 0).toString());
        assertEquals("dmurphy", tableModel.getValueAt(0, 1).toString());
        assertEquals("classicmodelcars.com", tableModel.getValueAt(0, 2).toString());

        assertEquals("mpatterso@classicmodelcars.com", tableModel.getValueAt(1, 0).toString());
        assertEquals("mpatterso", tableModel.getValueAt(1, 1).toString());
        assertEquals("classicmodelcars.com", tableModel.getValueAt(1, 2).toString());

    }

    @Test
    public void testPreviewTransformationAfterNonOptimizedFilteredTransformation() throws Exception {
        final String baseFilename = getClass().getSimpleName() + "-" + testName
                .getMethodName() + ".analysis.xml";
        final File analysisFile = new File("src/test/resources/previewfiles/" + baseFilename);

        final JaxbJobReader reader = new JaxbJobReader(analysisJobBuilder.getConfiguration());

        final AnalysisJobBuilder readAnalysisJobBuilder = reader.create(new FileInputStream(analysisFile));
        final List<TransformerComponentBuilder<?>> transformerComponentBuilders =
                readAnalysisJobBuilder.getTransformerComponentBuilders();

        final TransformerComponentBuilder<?> whitespaceTrimmer = transformerComponentBuilders.get(2);
        
        // verify that we got the right transformer - the trimmer
        assertEquals("Whitespace trimmer", whitespaceTrimmer.getDescriptor().getDisplayName());

        final PreviewTransformedDataActionListener action = new PreviewTransformedDataActionListener(null, null,
                whitespaceTrimmer, 500);
        
        final TableModel tableModel = action.call();

        assertEquals(1, tableModel.getRowCount());
        
        assertTrue(tableModel.getValueAt(0, 1).toString().contains("5307.98"));
    }

    private void compareWithBenchmark(PreviewTransformedDataActionListener action) throws IOException {
        final String baseFilename = getClass().getSimpleName() + "-" + testName
                        .getMethodName() + ".analysis.xml";
        final File benchmarkFile = new File("src/test/resources/benchmark/" + baseFilename);
        final File outputFile = new File("target/" + baseFilename);

        final PreviewTransformedDataActionListener.PreviewJob previewJob = action.createPreviewJob();
        assertNotNull(previewJob);
        final AnalysisJobBuilder ajb = previewJob.analysisJobBuilder;
        ajb.getAnalysisJobMetadata().getProperties().put(PreviewUtils.METADATA_PROPERTY_MARKER, "test");
        
        final JaxbJobWriter writer = new JaxbJobWriter(ajb.getConfiguration(), new EmptyJaxbJobMetadataFactory());
        
        try (FileOutputStream out = new FileOutputStream(outputFile)) {
            writer.write(ajb.toAnalysisJob(), out);
        }
        
        TestHelper.assertXmlFilesEquals(benchmarkFile, outputFile);
    }
}
