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
import org.datacleaner.test.MockJobEscalatingAnalyzer;
import org.datacleaner.test.MockOutputDataStreamAnalyzer;

public class AbstractComponentBuilderTest extends TestCase {

    /**
     * See issue https://github.com/datacleaner/DataCleaner/issues/574 which
     * this test reproduces.
     * 
     * @throws Exception
     */
    public void testGetOutputDataStreamsWhenConsumingEscalateToMultipleJobs() throws Exception {
        final List<TableDataProvider<?>> tableDataProviders = Arrays
                .<TableDataProvider<?>> asList(new ArrayTableDataProvider(new SimpleTableDef("my table", new String[] {
                        "col1", "col2" }), Collections.<Object[]> emptyList()));
        final PojoDatastore dummyDatastore = new PojoDatastore("my ds", tableDataProviders);

        final AnalysisJobBuilder mainJobBuilder = new AnalysisJobBuilder(
                new DataCleanerConfigurationImpl().withDatastores(dummyDatastore));
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
}
