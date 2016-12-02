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
package org.datacleaner.job.builder;

import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.MutableColumn;
import org.apache.metamodel.schema.MutableTable;
import org.apache.metamodel.schema.Table;
import org.datacleaner.beans.StringAnalyzer;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.data.MetaModelInputColumn;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.job.AnalyzerJob;

import junit.framework.TestCase;

public class AnalyzerComponentBuilderTest extends TestCase {

    private AnalysisJobBuilder ajb;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ajb = new AnalysisJobBuilder(new DataCleanerConfigurationImpl());
    }

    public void testNoOriginatingTableBecauseOfMockColumns() throws Exception {
        final AnalyzerComponentBuilder<StringAnalyzer> jobBuilder = ajb.addAnalyzer(StringAnalyzer.class);
        jobBuilder.addInputColumn(new MockInputColumn<>("foo", String.class));
        jobBuilder.addInputColumn(new MockInputColumn<>("bar", String.class));

        try {
            jobBuilder.toAnalyzerJob();
            fail("Exception expected");
        } catch (final IllegalStateException e) {
            assertEquals("Could not determine source for analyzer 'AnalyzerComponentBuilder[analyzer=String analyzer,"
                    + "inputColumns=[MockInputColumn[name=foo], MockInputColumn[name=bar]]]'", e.getMessage());
        }
    }

    public void testBuildMultipleJobsForEachTable() throws Exception {
        final AnalyzerComponentBuilder<StringAnalyzer> jobBuilder = ajb.addAnalyzer(StringAnalyzer.class);

        final Table table1 = new MutableTable("table1");
        jobBuilder.addInputColumn(
                new MetaModelInputColumn(new MutableColumn("foo", ColumnType.VARCHAR, table1, 0, true)));
        jobBuilder.addInputColumn(
                new MetaModelInputColumn(new MutableColumn("bar", ColumnType.VARCHAR, table1, 1, true)));

        final Table table2 = new MutableTable("table2");
        jobBuilder.addInputColumn(
                new MetaModelInputColumn(new MutableColumn("w00p", ColumnType.VARCHAR, table2, 0, true)));
        jobBuilder.addInputColumn(
                new MetaModelInputColumn(new MutableColumn("weee", ColumnType.VARCHAR, table2, 1, true)));
        jobBuilder.addInputColumn(
                new MetaModelInputColumn(new MutableColumn("wohoo", ColumnType.VARCHAR, table2, 2, true)));

        final AnalyzerJob[] analyzerJobs = jobBuilder.toAnalyzerJobs();
        assertEquals(2, analyzerJobs.length);

        assertEquals(2, analyzerJobs[0].getInput().length);
        assertEquals("foo", analyzerJobs[0].getInput()[0].getName());
        assertEquals("bar", analyzerJobs[0].getInput()[1].getName());

        assertEquals(3, analyzerJobs[1].getInput().length);
        assertEquals("w00p", analyzerJobs[1].getInput()[0].getName());
        assertEquals("weee", analyzerJobs[1].getInput()[1].getName());
        assertEquals("wohoo", analyzerJobs[1].getInput()[2].getName());
    }
}
