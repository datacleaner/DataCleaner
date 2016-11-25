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
package org.datacleaner;

import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.MutableColumn;
import org.apache.metamodel.schema.MutableTable;
import org.apache.metamodel.schema.Table;
import org.datacleaner.beans.stringpattern.PatternFinderAnalyzer;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.data.MetaModelInputColumn;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.AnalyzerJob;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;

import junit.framework.TestCase;

public class AnalyzerComponentBuilderTest extends TestCase {

    private AnalysisJobBuilder ajb;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ajb = new AnalysisJobBuilder(new DataCleanerConfigurationImpl());
    }

    public void testBuildMultipleJobsForSingleInputAnalyzer() throws Exception {
        final AnalyzerComponentBuilder<PatternFinderAnalyzer> analyzerBuilder =
                ajb.addAnalyzer(PatternFinderAnalyzer.class);

        assertFalse(analyzerBuilder.isConfigured());

        final Table table = new MutableTable("table");
        analyzerBuilder
                .addInputColumn(new MetaModelInputColumn(new MutableColumn("foo", ColumnType.VARCHAR, table, 0, true)));
        analyzerBuilder
                .addInputColumn(new MetaModelInputColumn(new MutableColumn("bar", ColumnType.VARCHAR, table, 1, true)));

        // change a property
        final ConfiguredPropertyDescriptor property =
                analyzerBuilder.getDescriptor().getConfiguredProperty("Discriminate negative numbers");
        analyzerBuilder.setConfiguredProperty(property, false);

        try {
            // cannot create a single job, since there will be two
            analyzerBuilder.toAnalyzerJob();
            fail("Exception expected");
        } catch (final IllegalStateException e) {
            assertEquals("This builder generates 2 jobs, but a single job was requested", e.getMessage());
        }

        assertTrue(analyzerBuilder.isConfigured());
        final AnalyzerJob[] analyzerJobs = analyzerBuilder.toAnalyzerJobs();
        assertEquals(2, analyzerJobs.length);

        assertEquals(1, analyzerJobs[0].getInput().length);
        assertEquals("foo", analyzerJobs[0].getInput()[0].getName());
        assertEquals(false, analyzerJobs[0].getConfiguration().getProperty(property));

        assertEquals(1, analyzerJobs[1].getInput().length);
        assertEquals("bar", analyzerJobs[1].getInput()[0].getName());
        assertEquals(false, analyzerJobs[1].getConfiguration().getProperty(property));
    }
}
