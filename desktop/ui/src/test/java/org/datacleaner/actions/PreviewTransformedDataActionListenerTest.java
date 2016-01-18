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

import javax.swing.table.TableModel;

import junit.framework.TestCase;

import org.datacleaner.beans.filter.RangeFilterCategory;
import org.datacleaner.beans.filter.StringLengthRangeFilter;
import org.datacleaner.beans.standardize.EmailStandardizerTransformer;
import org.datacleaner.beans.transform.ConcatenatorTransformer;
import org.datacleaner.beans.transform.TokenizerTransformer;
import org.datacleaner.components.fuse.CoalesceUnit;
import org.datacleaner.components.fuse.FuseStreamsComponent;
import org.datacleaner.components.maxrows.MaxRowsFilter;
import org.datacleaner.components.maxrows.MaxRowsFilter.Category;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.configuration.DataCleanerEnvironmentImpl;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.connection.DatastoreCatalogImpl;
import org.datacleaner.connection.JdbcDatastore;
import org.datacleaner.data.ConstantInputColumn;
import org.datacleaner.descriptors.Descriptors;
import org.datacleaner.descriptors.SimpleDescriptorProvider;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.job.builder.FilterComponentBuilder;
import org.datacleaner.job.builder.TransformerComponentBuilder;
import org.datacleaner.job.builder.UnconfiguredConfiguredPropertyException;
import org.datacleaner.test.MockOutputDataStreamAnalyzer;
import org.datacleaner.test.MockTransformer;
import org.junit.Test;

@SuppressWarnings("deprecation")
public class PreviewTransformedDataActionListenerTest extends TestCase {

    private DataCleanerConfiguration configuration;
    private TransformerComponentBuilder<EmailStandardizerTransformer> emailTransformerBuilder;
    private AnalysisJobBuilder analysisJobBuilder;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        final JdbcDatastore datastore = new JdbcDatastore("orderdb", "jdbc:hsqldb:res:orderdb;readonly=true",
                "org.hsqldb.jdbcDriver");
        final DatastoreCatalog datastoreCatalog = new DatastoreCatalogImpl(datastore);
        final SimpleDescriptorProvider descriptorProvider = new SimpleDescriptorProvider();
        descriptorProvider.addTransformerBeanDescriptor(Descriptors.ofTransformer(EmailStandardizerTransformer.class));
        descriptorProvider.addTransformerBeanDescriptor(Descriptors.ofTransformer(ConcatenatorTransformer.class));
        descriptorProvider.addFilterBeanDescriptor(Descriptors.ofFilter(StringLengthRangeFilter.class));
        descriptorProvider.addTransformerBeanDescriptor(Descriptors.ofTransformer(TokenizerTransformer.class));
        configuration = new DataCleanerConfigurationImpl().withDatastoreCatalog(datastoreCatalog).withEnvironment(
                new DataCleanerEnvironmentImpl().withDescriptorProvider(descriptorProvider));

        analysisJobBuilder = new AnalysisJobBuilder(configuration);
        analysisJobBuilder.setDatastore("orderdb");
        analysisJobBuilder.addSourceColumns("PUBLIC.EMPLOYEES.EMAIL");
        emailTransformerBuilder = analysisJobBuilder.addTransformer(EmailStandardizerTransformer.class);
        emailTransformerBuilder.addInputColumn(analysisJobBuilder.getSourceColumnByName("EMAIL"));
    }

    @Test
    public void testPreviewTransformationInOutputDataStream() throws Exception {
        final AnalyzerComponentBuilder<MockOutputDataStreamAnalyzer> streamProducer = analysisJobBuilder
                .addAnalyzer(MockOutputDataStreamAnalyzer.class);
        streamProducer.addInputColumn(emailTransformerBuilder.getOutputColumns().get(0));

        final AnalysisJobBuilder streamJobBuilder = streamProducer
                .getOutputDataStreamJobBuilder(MockOutputDataStreamAnalyzer.STREAM_NAME1);
        final TransformerComponentBuilder<MockTransformer> transformer = streamJobBuilder
                .addTransformer(MockTransformer.class);
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

        final TransformerComponentBuilder<FuseStreamsComponent> union = analysisJobBuilder
                .addTransformer(FuseStreamsComponent.class);
        union.addInputColumns(analysisJobBuilder.getSourceColumns());
        union.setConfiguredProperty("Units",
                new CoalesceUnit[] { new CoalesceUnit(analysisJobBuilder.getSourceColumns()) });

        final AnalysisJobBuilder streamJobBuilder = union
                .getOutputDataStreamJobBuilder(FuseStreamsComponent.OUTPUT_DATA_STREAM_NAME);

        final TransformerComponentBuilder<MockTransformer> transformer = streamJobBuilder
                .addTransformer(MockTransformer.class);
        transformer.addInputColumn(streamJobBuilder.getSourceColumns().get(0));

        final PreviewTransformedDataActionListener action = new PreviewTransformedDataActionListener(null, null,
                transformer, 10);
        final TableModel tableModel = action.call();
        assertEquals(10, tableModel.getRowCount());

        // part of it is emails
        assertEquals("dmurphy@classicmodelcars.com", tableModel.getValueAt(0, 0).toString());
        assertEquals("mpatterso@classicmodelcars.com", tableModel.getValueAt(1, 0).toString());

        // another part is phone numbers
        assertEquals("7025551838", tableModel.getValueAt(6, 0).toString());
    }

    public void testJobWithMaxRowsFilter() throws Exception {
        final FilterComponentBuilder<MaxRowsFilter, Category> filter = analysisJobBuilder
                .addFilter(MaxRowsFilter.class);
        filter.addInputColumn(analysisJobBuilder.getSourceColumnByName("email"));
        filter.getComponentInstance().setMaxRows(5);

        emailTransformerBuilder.setRequirement(filter, Category.VALID);

        final PreviewTransformedDataActionListener action = new PreviewTransformedDataActionListener(null, null,
                emailTransformerBuilder, 10);
        final TableModel tableModel = action.call();
        assertEquals(10, tableModel.getRowCount());
    }

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
        final TransformerComponentBuilder<TokenizerTransformer> tokenizer = analysisJobBuilder
                .addTransformer(TokenizerTransformer.class);
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
    
    @Test()
    public void testUnchainedTransformers() throws Exception{
       
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
}
