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
import java.util.Collection;

import org.datacleaner.configuration.AnalyzerBeansConfigurationImpl;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.FilterComponentBuilder;
import org.datacleaner.job.builder.LazyFilterOutcome;
import org.datacleaner.test.MockFilter;
import org.datacleaner.test.MockFilter.Category;

import junit.framework.TestCase;

public class AbstractFilterOutcomeTest extends TestCase {

    public void testEqualsAndHashCodeOnDifferentSubclasses() throws Exception {
        FilterOutcome fo1;
        FilterOutcome fo2;

        try (final AnalysisJobBuilder ajb = new AnalysisJobBuilder(new AnalyzerBeansConfigurationImpl())) {
            FilterComponentBuilder<MockFilter, Category> filterJobBuilder = ajb.addFilter(MockFilter.class);
            filterJobBuilder.addInputColumn(new MockInputColumn<String>("foo"));
            filterJobBuilder.getComponentInstance().setSomeEnum(Category.INVALID);
            filterJobBuilder.getComponentInstance().setSomeFile(new File("."));

            fo1 = filterJobBuilder.getFilterOutcome(Category.VALID);
            assertTrue(fo1 instanceof LazyFilterOutcome);

            final FilterJob filterJob = filterJobBuilder.toFilterJob();
            
            Collection<FilterOutcome> filterOutcomes = filterJob.getFilterOutcomes();
            fo2 = null;
            for (FilterOutcome filterOutcome : filterOutcomes) {
                if (filterOutcome.getCategory() == Category.VALID) {
                    fo2 = filterOutcome;
                }
            }

            assertNotNull(fo2);
            assertTrue(fo2 instanceof ImmutableFilterOutcome);
        }
        
        final AnalysisJobImmutabilizer immutabilizer = new AnalysisJobImmutabilizer();
        final FilterOutcome loadedFilterOutcome1 = immutabilizer.load(fo1);
        final FilterOutcome loadedFilterOutcome2 = immutabilizer.load(fo2);
        
        assertTrue(loadedFilterOutcome1.equals(loadedFilterOutcome2));
        assertEquals(loadedFilterOutcome1.hashCode(), loadedFilterOutcome2.hashCode());
    }
}
