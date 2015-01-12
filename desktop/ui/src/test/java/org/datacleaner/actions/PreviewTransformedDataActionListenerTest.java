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
import org.datacleaner.configuration.AnalyzerBeansConfiguration;
import org.datacleaner.configuration.AnalyzerBeansConfigurationImpl;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.connection.DatastoreCatalogImpl;
import org.datacleaner.connection.JdbcDatastore;
import org.datacleaner.data.ConstantInputColumn;
import org.datacleaner.descriptors.Descriptors;
import org.datacleaner.descriptors.SimpleDescriptorProvider;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.FilterJobBuilder;
import org.datacleaner.job.builder.TransformerJobBuilder;

public class PreviewTransformedDataActionListenerTest extends TestCase {

    private AnalyzerBeansConfiguration configuration;
    private TransformerJobBuilder<EmailStandardizerTransformer> emailTransformerBuilder;
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
        configuration = new AnalyzerBeansConfigurationImpl().replace(datastoreCatalog).replace(descriptorProvider);

        analysisJobBuilder = new AnalysisJobBuilder(configuration);
        analysisJobBuilder.setDatastore("orderdb");
        analysisJobBuilder.addSourceColumns("PUBLIC.EMPLOYEES.EMAIL");
        emailTransformerBuilder = analysisJobBuilder.addTransformer(EmailStandardizerTransformer.class);
        emailTransformerBuilder.addInputColumn(analysisJobBuilder.getSourceColumnByName("EMAIL"));
    }

    public void testSingleTransformer() throws Exception {
        PreviewTransformedDataActionListener action = new PreviewTransformedDataActionListener(null, null,
                emailTransformerBuilder);
        TableModel tableModel = action.call();

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
        TransformerJobBuilder<ConcatenatorTransformer> lengthTransformerBuilder = analysisJobBuilder
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
        FilterJobBuilder<StringLengthRangeFilter, RangeFilterCategory> rangeFilter = analysisJobBuilder
                .addFilter(StringLengthRangeFilter.class);
        rangeFilter.addInputColumn(lengthTransformerBuilder.getOutputColumnByName("Concat of Username,\"foo\""));
        rangeFilter.setConfiguredProperty("Minimum length", 5);
        rangeFilter.setConfiguredProperty("Maximum length", 20);

        // add a multi-row transformer
        TransformerJobBuilder<TokenizerTransformer> tokenizer = analysisJobBuilder
                .addTransformer(TokenizerTransformer.class);
        tokenizer.addInputColumn(emailTransformerBuilder.getOutputColumnByName("Username"));
        tokenizer.setRequirement(rangeFilter.getFilterOutcome(RangeFilterCategory.VALID));
        tokenizer.setConfiguredProperty("Token target", TokenizerTransformer.TokenTarget.ROWS);
        tokenizer.setConfiguredProperty("Number of tokens", 50);
        tokenizer.setConfiguredProperty("Delimiters", new char[] { 'p' });
        assertTrue(tokenizer.isConfigured());

        // run advanced
        {
            PreviewTransformedDataActionListener action = new PreviewTransformedDataActionListener(null, null,
                    tokenizer);
            TableModel tableModel = action.call();

            assertEquals(29, tableModel.getRowCount());

            assertEquals("dmurphy", tableModel.getValueAt(0, 0).toString());
            assertEquals("dmur", tableModel.getValueAt(0, 1).toString());

            assertEquals("dmurphy", tableModel.getValueAt(1, 0).toString());
            assertEquals("hy", tableModel.getValueAt(1, 1).toString());

            assertEquals("mpatterso", tableModel.getValueAt(2, 0).toString());
            assertEquals("m", tableModel.getValueAt(2, 1).toString());
        }
    }
}
