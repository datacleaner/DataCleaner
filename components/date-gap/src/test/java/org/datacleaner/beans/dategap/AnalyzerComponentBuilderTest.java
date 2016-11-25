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
package org.datacleaner.beans.dategap;

import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.data.MockInputColumn;
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

    public void testAddUnnamedColumnToMultiColumnAnalyzer() throws Exception {
        final AnalyzerComponentBuilder<DateGapAnalyzer> analyzer = ajb.addAnalyzer(DateGapAnalyzer.class);
        try {
            analyzer.addInputColumn(new MockInputColumn<>("foo", String.class));
            fail("Exception expected");
        } catch (final Exception e) {
            assertEquals(
                    "There are 2 named input columns in \"Date gap analyzer\", please specify which one to configure",
                    e.getMessage());
        }
    }
}
