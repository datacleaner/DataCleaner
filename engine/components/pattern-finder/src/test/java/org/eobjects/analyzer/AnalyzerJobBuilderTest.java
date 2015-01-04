/**
 * AnalyzerBeans
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
package org.eobjects.analyzer;

import junit.framework.TestCase;

import org.eobjects.analyzer.beans.stringpattern.PatternFinderAnalyzer;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl;
import org.eobjects.analyzer.data.MetaModelInputColumn;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.AnalyzerJobBuilder;
import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.MutableColumn;
import org.apache.metamodel.schema.MutableTable;
import org.apache.metamodel.schema.Table;

public class AnalyzerJobBuilderTest extends TestCase {

	private AnalysisJobBuilder ajb;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ajb = new AnalysisJobBuilder(new AnalyzerBeansConfigurationImpl());
	}

	public void testBuildMultipleJobsForSingleInputAnalyzer() throws Exception {
		AnalyzerJobBuilder<PatternFinderAnalyzer> jobBuilder = ajb.addAnalyzer(PatternFinderAnalyzer.class);

		assertFalse(jobBuilder.isConfigured());

		Table table = new MutableTable("table");
		jobBuilder.addInputColumn(new MetaModelInputColumn(new MutableColumn("foo", ColumnType.VARCHAR, table, 0, true)));
		jobBuilder.addInputColumn(new MetaModelInputColumn(new MutableColumn("bar", ColumnType.VARCHAR, table, 1, true)));

		// change a property
		ConfiguredPropertyDescriptor property = jobBuilder.getDescriptor().getConfiguredProperty(
				"Discriminate negative numbers");
		jobBuilder.setConfiguredProperty(property, false);

		try {
			// cannot create a single job, since there will be two
			jobBuilder.toAnalyzerJob();
			fail("Exception expected");
		} catch (IllegalStateException e) {
			assertEquals("This builder generates 2 jobs, but a single job was requested", e.getMessage());
		}

		assertTrue(jobBuilder.isConfigured());
		AnalyzerJob[] analyzerJobs = jobBuilder.toAnalyzerJobs();
		assertEquals(2, analyzerJobs.length);

		assertEquals(1, analyzerJobs[0].getInput().length);
		assertEquals("foo", analyzerJobs[0].getInput()[0].getName());
		assertEquals(false, analyzerJobs[0].getConfiguration().getProperty(property));

		assertEquals(1, analyzerJobs[1].getInput().length);
		assertEquals("bar", analyzerJobs[1].getInput()[0].getName());
		assertEquals(false, analyzerJobs[1].getConfiguration().getProperty(property));
	}
}
