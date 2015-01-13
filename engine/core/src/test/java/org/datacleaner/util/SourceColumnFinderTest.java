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
package org.datacleaner.util;

import java.util.List;

import junit.framework.TestCase;

import org.datacleaner.api.InputColumn;
import org.datacleaner.configuration.AnalyzerBeansConfigurationImpl;
import org.datacleaner.customcolumn.MockConvertToMonthObjectTransformer;
import org.datacleaner.customcolumn.Month;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.job.builder.AnalysisJobBuilder;

public class SourceColumnFinderTest extends TestCase {
	
	public void testFindInputColumns() throws Exception {
		SourceColumnFinder columnFinder = new SourceColumnFinder();
		
		AnalysisJobBuilder analysisJobBuilder = new AnalysisJobBuilder(new AnalyzerBeansConfigurationImpl());
		analysisJobBuilder.addTransformer(MockConvertToMonthObjectTransformer.class).addInputColumn(new MockInputColumn<String>("month", String.class));
		columnFinder.addSources(analysisJobBuilder);
		List<InputColumn<?>> findInputColumns = columnFinder.findInputColumns(Month.class);
		assertEquals(1, findInputColumns.size());
	}

}
 
