/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.actions;

import javax.swing.table.TableModel;

import junit.framework.TestCase;

import org.eobjects.analyzer.beans.standardize.EmailStandardizerTransformer;
import org.eobjects.analyzer.beans.standardize.TokenizerTransformer;
import org.eobjects.analyzer.beans.transform.StringLengthTransformer;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.connection.DatastoreCatalogImpl;
import org.eobjects.analyzer.connection.JdbcDatastore;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;

public class PreviewTransformedDataActionListenerTest extends TestCase {

	private AnalyzerBeansConfiguration configuration;
	private TransformerJobBuilder<EmailStandardizerTransformer> emailTransformerBuilder;
	private AnalysisJobBuilder analysisJobBuilder;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		final JdbcDatastore datastore = new JdbcDatastore("orderdb", "jdbc:hsqldb:res:orderdb;readonly=true", "org.hsqldb.jdbcDriver");
		final DatastoreCatalog datastoreCatalog = new DatastoreCatalogImpl(datastore);
		configuration = new AnalyzerBeansConfigurationImpl().replace(datastoreCatalog);

		analysisJobBuilder = new AnalysisJobBuilder(configuration);
		analysisJobBuilder.setDatastore("orderdb");
		analysisJobBuilder.addSourceColumns("PUBLIC.EMPLOYEES.EMAIL");
		emailTransformerBuilder = analysisJobBuilder.addTransformer(EmailStandardizerTransformer.class);
		emailTransformerBuilder.addInputColumn(analysisJobBuilder.getSourceColumnByName("EMAIL"));
	}

	public void testSingleTransformer() throws Exception {
		PreviewTransformedDataActionListener action = new PreviewTransformedDataActionListener(null, null,
				analysisJobBuilder, emailTransformerBuilder, configuration);
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
		TransformerJobBuilder<StringLengthTransformer> lengthTransformerBuilder = analysisJobBuilder
				.addTransformer(StringLengthTransformer.class);
		lengthTransformerBuilder.addInputColumn(emailTransformerBuilder.getOutputColumnByName("Username"));

		PreviewTransformedDataActionListener action = new PreviewTransformedDataActionListener(null, null,
				analysisJobBuilder, lengthTransformerBuilder, configuration);
		TableModel tableModel = action.call();

		assertEquals(2, tableModel.getColumnCount());
		assertEquals("Username", tableModel.getColumnName(0));
		assertEquals("Username length", tableModel.getColumnName(1));

		assertEquals(23, tableModel.getRowCount());

		for (int i = 0; i < tableModel.getRowCount(); i++) {
			assertTrue(tableModel.getValueAt(i, 0).toString().indexOf('@') == -1);
			Object lengthValue = tableModel.getValueAt(i, 1);
			assertNotNull(lengthValue);
			assertTrue(lengthValue instanceof Number);
		}

		assertEquals("dmurphy", tableModel.getValueAt(0, 0).toString());
		assertEquals("7", tableModel.getValueAt(0, 1).toString());

		assertEquals("mpatterso", tableModel.getValueAt(1, 0).toString());
		assertEquals("9", tableModel.getValueAt(1, 1).toString());
		
		// add a multi-row transformer
		TransformerJobBuilder<TokenizerTransformer> tokenizer = analysisJobBuilder.addTransformer(TokenizerTransformer.class);
		tokenizer.addInputColumn(emailTransformerBuilder.getOutputColumnByName("Username"));
		tokenizer.setConfiguredProperty("Token target", TokenizerTransformer.TokenTarget.ROWS);
		tokenizer.setConfiguredProperty("Number of tokens", 50);
		tokenizer.setConfiguredProperty("Delimiters", new char[] {'p'});
		assertTrue(tokenizer.isConfigured());
		
		action = new PreviewTransformedDataActionListener(null, null,
				analysisJobBuilder, tokenizer, configuration);
		tableModel = action.call();
		
		// rows changed from 23 -> 29
		assertEquals(29, tableModel.getRowCount());

		assertEquals("dmurphy", tableModel.getValueAt(0, 0).toString());
		assertEquals("dmur", tableModel.getValueAt(0, 1).toString());
		
		assertEquals("dmurphy", tableModel.getValueAt(1, 0).toString());
		assertEquals("hy", tableModel.getValueAt(1, 1).toString());

		assertEquals("mpatterso", tableModel.getValueAt(2, 0).toString());
		assertEquals("m", tableModel.getValueAt(2, 1).toString());
		
		assertEquals("mpatterso", tableModel.getValueAt(3, 0).toString());
		assertEquals("atterso", tableModel.getValueAt(3, 1).toString());
	}
}
