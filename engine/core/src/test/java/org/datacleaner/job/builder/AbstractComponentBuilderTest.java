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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.apache.metamodel.pojo.ArrayTableDataProvider;
import org.apache.metamodel.pojo.TableDataProvider;
import org.apache.metamodel.util.SimpleTableDef;
import org.datacleaner.api.OutputDataStream;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.connection.PojoDatastore;
import org.datacleaner.test.MockDynamicOutputDataStreamAnalyzer;
import org.datacleaner.test.MockJobEscalatingAnalyzer;
import org.datacleaner.test.MockOutputDataStreamAnalyzer;

public class AbstractComponentBuilderTest extends TestCase {

    private PojoDatastore dummyDatastore;
    private DataCleanerConfigurationImpl configuration;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        final List<TableDataProvider<?>> tableDataProviders = Arrays
                .<TableDataProvider<?>> asList(new ArrayTableDataProvider(new SimpleTableDef("my table", new String[] {
                        "col1", "col2" }), Collections.<Object[]> emptyList()));
        dummyDatastore = new PojoDatastore("my ds", tableDataProviders);
        configuration = new DataCleanerConfigurationImpl().withDatastores(dummyDatastore);
    }

    /**
     * See issue https://github.com/datacleaner/DataCleaner/issues/574 which
     * this test reproduces.
     * 
     * @throws Exception
     */
    public void testGetOutputDataStreamsWhenConsumingEscalateToMultipleJobs() throws Exception {
        final AnalysisJobBuilder mainJobBuilder = new AnalysisJobBuilder(configuration);
        try {
            mainJobBuilder.setDatastore(dummyDatastore);
            mainJobBuilder.addSourceColumns("col1", "col2");
            final AnalyzerComponentBuilder<MockOutputDataStreamAnalyzer> analyzer1 = mainJobBuilder
                    .addAnalyzer(MockOutputDataStreamAnalyzer.class);
            analyzer1.addInputColumn(mainJobBuilder.getSourceColumns().get(0));

            final List<OutputDataStream> streams1 = analyzer1.getOutputDataStreams();
            final AnalysisJobBuilder childJobBuilder = analyzer1.getOutputDataStreamJobBuilder(streams1.get(0));
            final AnalyzerComponentBuilder<MockJobEscalatingAnalyzer> analyzer2 = childJobBuilder
                    .addAnalyzer(MockJobEscalatingAnalyzer.class);
            analyzer2.addInputColumns(childJobBuilder.getSourceColumns());

            // this is the call that would provoke the failure before the fix to
            // #574
            analyzer2.getOutputDataStreams();
        } finally {
            mainJobBuilder.close();
        }
    }

    public void testGetOutputDataStreamsWithIncrementalChanges() throws Exception {
        final AnalysisJobBuilder mainJobBuilder = new AnalysisJobBuilder(configuration);
        try {
            mainJobBuilder.setDatastore(dummyDatastore);
            mainJobBuilder.addSourceColumns("col1", "col2");

            final AnalyzerComponentBuilder<MockDynamicOutputDataStreamAnalyzer> analyzer = mainJobBuilder
                    .addAnalyzer(MockDynamicOutputDataStreamAnalyzer.class);
            analyzer.setConfiguredProperty("Stream name", "my stream");
            analyzer.addInputColumn(mainJobBuilder.getSourceColumns().get(0));

            final List<OutputDataStream> streams1 = analyzer.getOutputDataStreams();
            assertEquals(1, streams1.size());

            // add a column (will add also a column to the same stream)
            analyzer.addInputColumn(mainJobBuilder.getSourceColumns().get(1));
            final List<OutputDataStream> streams2 = analyzer.getOutputDataStreams();
            assertEquals(1, streams2.size());

            // the two collections are not the same
            assertNotSame(streams1, streams2);
            // but the stream itself should be mutated, not replaced, so same
            // instance is expected
            assertSame(streams1.get(0), streams2.get(0));
            
            // now change the name of the stream
            analyzer.setConfiguredProperty("Stream name", "another stream name");
            final List<OutputDataStream> streams3 = analyzer.getOutputDataStreams();
            
            // now we expect a completely new stream instance
            assertNotSame(streams1.get(0), streams3.get(0));
        } finally {
            mainJobBuilder.close();
        }
    }
}
