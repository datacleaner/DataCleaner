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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.metamodel.MetaModelHelper;
import org.apache.metamodel.pojo.ArrayTableDataProvider;
import org.apache.metamodel.pojo.TableDataProvider;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.SimpleTableDef;
import org.datacleaner.api.InputColumn;
import org.datacleaner.components.maxrows.MaxRowsFilter;
import org.datacleaner.components.maxrows.MaxRowsFilter.Category;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.PojoDatastore;
import org.datacleaner.customcolumn.MockConvertToMonthObjectTransformer;
import org.datacleaner.customcolumn.Month;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.FilterOutcome;
import org.datacleaner.job.TransformerJob;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.job.builder.FilterComponentBuilder;
import org.datacleaner.job.builder.TransformerComponentBuilder;
import org.datacleaner.test.MockAnalyzer;
import org.datacleaner.test.MockTransformer;

public class SourceColumnFinderTest extends TestCase {

    public void testFindInputColumns() throws Exception {
        SourceColumnFinder columnFinder = new SourceColumnFinder();

        AnalysisJobBuilder analysisJobBuilder = new AnalysisJobBuilder(new DataCleanerConfigurationImpl());
        analysisJobBuilder.addTransformer(MockConvertToMonthObjectTransformer.class)
                .addInputColumn(new MockInputColumn<String>("month", String.class));
        columnFinder.addSources(analysisJobBuilder);
        List<InputColumn<?>> findInputColumns = columnFinder.findInputColumns(Month.class);
        assertEquals(1, findInputColumns.size());
    }

    // see issue #706
    public void testFindOriginatingTableFromMaxRowsFilter() throws Exception {
        final TableDataProvider<?> tableProvider1 = new ArrayTableDataProvider(
                new SimpleTableDef("table1", new String[] { "id", "foo" }), new ArrayList<Object[]>());
        final TableDataProvider<?> tableProvider2 = new ArrayTableDataProvider(
                new SimpleTableDef("table2", new String[] { "id", "bar" }), new ArrayList<Object[]>());

        final Datastore ds = new PojoDatastore("my datastore", tableProvider1, tableProvider2);
        final DataCleanerConfiguration configuration = new DataCleanerConfigurationImpl().withDatastores(ds);

        final AnalysisJob job;
        try (AnalysisJobBuilder jobBuilder = new AnalysisJobBuilder(configuration)) {
            // add source columns from two tables
            jobBuilder.setDatastore(ds);
            jobBuilder.addSourceColumns("table1.id", "table1.foo", "table2.id", "table2.bar");

            final FilterComponentBuilder<MaxRowsFilter, Category> filter = jobBuilder.addFilter(MaxRowsFilter.class);
            filter.addInputColumn(jobBuilder.getSourceColumnByName("foo"));

            final TransformerComponentBuilder<MockTransformer> transformer = jobBuilder
                    .addTransformer(MockTransformer.class);
            transformer.addInputColumn(jobBuilder.getSourceColumnByName("foo"));
            transformer.setRequirement(filter, MaxRowsFilter.Category.VALID);

            // only wire columns from one of the tables (table1.foo)
            final AnalyzerComponentBuilder<MockAnalyzer> analyzer = jobBuilder.addAnalyzer(MockAnalyzer.class);
            analyzer.addInputColumn(transformer.getOutputColumns().get(0));

            job = jobBuilder.toAnalysisJob();
        }

        final TransformerJob transformerJob = job.getTransformerJobs().get(0);
        final  InputColumn<?> transformedColumn = transformerJob.getOutput()[0];
        final FilterOutcome outcome = transformerJob.getComponentRequirement().getProcessingDependencies().iterator()
                .next();

        final SourceColumnFinder sourceColumnFinder = new SourceColumnFinder();
        sourceColumnFinder.addSources(job);
        
        final Table transformerOriginTable = sourceColumnFinder.findOriginatingTable(transformedColumn);
        assertEquals("table1", transformerOriginTable.getName());

        assertNotNull(outcome);
        final Table filterOriginTable = sourceColumnFinder.findOriginatingTable(outcome);
        assertEquals("table1", filterOriginTable.getName());
        
        final Set<Column> transformerOriginColumns = sourceColumnFinder.findOriginatingColumns(transformedColumn);
        assertEquals(1, MetaModelHelper.getTables(transformerOriginColumns).length);
    }
}
