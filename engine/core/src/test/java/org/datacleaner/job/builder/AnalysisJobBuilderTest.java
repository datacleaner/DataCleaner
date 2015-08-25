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

import java.io.File;
import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;

import org.apache.metamodel.schema.MutableColumn;
import org.apache.metamodel.schema.MutableTable;
import org.datacleaner.api.OutputDataStream;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.data.MetaModelInputColumn;
import org.datacleaner.test.MockFilter;
import org.datacleaner.test.MockFilter.Category;
import org.datacleaner.test.MockOutputDataStreamAnalyzer;
import org.datacleaner.test.MockTransformer;
import org.datacleaner.test.mock.MockDatastore;

public class AnalysisJobBuilderTest extends TestCase {

    public void testGetAvailableInputColumnsForComponentPreventsCyclicDependencies() throws Exception {
        final MutableTable table = new MutableTable("table");
        final MutableColumn column = new MutableColumn("foo").setTable(table);
        table.addColumn(column);

        try (AnalysisJobBuilder ajb = new AnalysisJobBuilder(new DataCleanerConfigurationImpl())) {
            final MockDatastore datastore = new MockDatastore();
            ajb.setDatastore(datastore);
            ajb.addSourceColumn(new MetaModelInputColumn(column));

            final TransformerComponentBuilder<MockTransformer> transformer1 = ajb.addTransformer(MockTransformer.class);
            transformer1.addInputColumn(ajb.getSourceColumnByName("foo"));
            transformer1.getOutputColumns().get(0).setName("out1");

            assertEquals("[TransformedInputColumn[id=trans-0001-0002,name=out1]]", transformer1.getOutputColumns()
                    .toString());
            assertEquals("[MetaModelInputColumn[table.foo]]", ajb.getAvailableInputColumns(transformer1).toString());

            final TransformerComponentBuilder<MockTransformer> transformer2 = ajb.addTransformer(MockTransformer.class);
            transformer2.addInputColumn(transformer1.getOutputColumns().get(0));
            transformer2.getOutputColumns().get(0).setName("out2");

            assertEquals("[MetaModelInputColumn[table.foo]]", ajb.getAvailableInputColumns(transformer1).toString());
            assertEquals("[MetaModelInputColumn[table.foo], TransformedInputColumn[id=trans-0001-0002,name=out1]]", ajb
                    .getAvailableInputColumns(transformer2).toString());

            final TransformerComponentBuilder<MockTransformer> transformer3 = ajb.addTransformer(MockTransformer.class);
            assertEquals("[MetaModelInputColumn[table.foo]]", ajb.getAvailableInputColumns(transformer1).toString());
            assertEquals("[MetaModelInputColumn[table.foo], TransformedInputColumn[id=trans-0001-0002,name=out1]]", ajb
                    .getAvailableInputColumns(transformer2).toString());
            assertEquals(
                    "[MetaModelInputColumn[table.foo], TransformedInputColumn[id=trans-0001-0002,name=out1], TransformedInputColumn[id=trans-0003-0004,name=out2]]",
                    ajb.getAvailableInputColumns(transformer3).toString());

            transformer3.addInputColumn(ajb.getSourceColumns().get(0));
            transformer3.getOutputColumns().get(0).setName("out3");

            assertEquals("[MetaModelInputColumn[table.foo], TransformedInputColumn[id=trans-0005-0006,name=out3]]", ajb
                    .getAvailableInputColumns(transformer1).toString());
            assertEquals(
                    "[MetaModelInputColumn[table.foo], TransformedInputColumn[id=trans-0001-0002,name=out1], TransformedInputColumn[id=trans-0005-0006,name=out3]]",
                    ajb.getAvailableInputColumns(transformer2).toString());
            assertEquals(
                    "[MetaModelInputColumn[table.foo], TransformedInputColumn[id=trans-0001-0002,name=out1], TransformedInputColumn[id=trans-0003-0004,name=out2]]",
                    ajb.getAvailableInputColumns(transformer3).toString());

            transformer2.clearInputColumns();
            transformer2.addInputColumn(transformer3.getOutputColumns().get(0));

            assertEquals(
                    "[MetaModelInputColumn[table.foo], TransformedInputColumn[id=trans-0003-0004,name=out2], TransformedInputColumn[id=trans-0005-0006,name=out3]]",
                    ajb.getAvailableInputColumns(transformer1).toString());
            assertEquals(
                    "[MetaModelInputColumn[table.foo], TransformedInputColumn[id=trans-0001-0002,name=out1], TransformedInputColumn[id=trans-0005-0006,name=out3]]",
                    ajb.getAvailableInputColumns(transformer2).toString());
            assertEquals("[MetaModelInputColumn[table.foo], TransformedInputColumn[id=trans-0001-0002,name=out1]]", ajb
                    .getAvailableInputColumns(transformer3).toString());
        }
    }

    /**
     * Builds a scenario with 2 transformers and a filter inbetween. When a
     * filter outcome is set as the default requirement, that requirement should
     * only be set on the succeeding (not preceeding) transformer.
     */
    public void testSetDefaultRequirementNonCyclic() throws Exception {
        MutableTable table = new MutableTable("table");
        MutableColumn column = new MutableColumn("foo").setTable(table);
        table.addColumn(column);

        // set up
        try (AnalysisJobBuilder ajb = new AnalysisJobBuilder(new DataCleanerConfigurationImpl())) {
            MockDatastore datastore = new MockDatastore();
            ajb.setDatastore(datastore);
            ajb.addSourceColumn(new MetaModelInputColumn(column));

            // add a transformer
            TransformerComponentBuilder<MockTransformer> tjb1 = ajb.addTransformer(MockTransformer.class);
            tjb1.addInputColumn(ajb.getSourceColumns().get(0));
            assertTrue(tjb1.isConfigured(true));

            // add filter
            FilterComponentBuilder<MockFilter, Category> filter = ajb.addFilter(MockFilter.class);
            filter.addInputColumn(tjb1.getOutputColumns().get(0));
            filter.getComponentInstance().setSomeEnum(Category.VALID);
            filter.getComponentInstance().setSomeFile(new File("."));
            assertTrue(filter.isConfigured(true));

            // set default requirement
            ajb.setDefaultRequirement(filter, Category.VALID);

            // add another transformer
            TransformerComponentBuilder<MockTransformer> tjb2 = ajb.addTransformer(MockTransformer.class);
            tjb2.addInputColumn(tjb1.getOutputColumns().get(0));
            assertTrue(tjb2.isConfigured(true));

            // assertions
            assertEquals("Mock filter=VALID", tjb2.getComponentRequirement().toString());
            assertEquals(null, filter.getComponentRequirement());
            assertEquals(null, tjb1.getComponentRequirement());

            final Collection<ComponentBuilder> componentBuilders = ajb.getComponentBuilders();
            assertEquals(
                    "[FilterComponentBuilder[filter=Mock filter,inputColumns=[TransformedInputColumn[id=trans-0001-0002,name=mock output]]], "
                            + "TransformerComponentBuilder[transformer=Mock transformer,inputColumns=[MetaModelInputColumn[table.foo]]], "
                            + "TransformerComponentBuilder[transformer=Mock transformer,inputColumns=[TransformedInputColumn[id=trans-0001-0002,name=mock output]]]]",
                    componentBuilders.toString());
        }
    }

    public void testGetAllJobBuilders() {
        MutableTable table = new MutableTable("table");
        MutableColumn column = new MutableColumn("foo").setTable(table);
        table.addColumn(column);

        // set up
        try (final AnalysisJobBuilder ajb = new AnalysisJobBuilder(new DataCleanerConfigurationImpl())) {
            final MockDatastore datastore = new MockDatastore();
            ajb.setDatastore(datastore);
            ajb.addSourceColumn(new MetaModelInputColumn(column));

            final AnalyzerComponentBuilder<MockOutputDataStreamAnalyzer> analyzer0 =
                    ajb.addAnalyzer(MockOutputDataStreamAnalyzer.class);
            analyzer0.setName("analyzer0");
            analyzer0.addInputColumn(ajb.getSourceColumns().get(0));

            final List<OutputDataStream> analyzer0OutputDataStreams = analyzer0.getOutputDataStreams();

            final AnalysisJobBuilder analyzer0DataStream0JobBuilder =
                    analyzer0.getOutputDataStreamJobBuilder(analyzer0OutputDataStreams.get(0));
            final AnalyzerComponentBuilder<MockOutputDataStreamAnalyzer> analyzer0Analyzer0 =
                    analyzer0DataStream0JobBuilder.addAnalyzer(MockOutputDataStreamAnalyzer.class);
            analyzer0Analyzer0.setName("analyzer0Analyzer0");
            analyzer0Analyzer0.addInputColumn(analyzer0DataStream0JobBuilder.getSourceColumns().get(0));

            final List<OutputDataStream> analyzer0Analyzer0OutputDataStreams =
                    analyzer0Analyzer0.getOutputDataStreams();

            final AnalysisJobBuilder analyzer0Analyzer0DataStream0JobBuilder =
                    analyzer0Analyzer0.getOutputDataStreamJobBuilder(analyzer0Analyzer0OutputDataStreams.get(0));
            final AnalyzerComponentBuilder<MockOutputDataStreamAnalyzer> analyzer0Analyzer0Analyzer0 =
                    analyzer0Analyzer0DataStream0JobBuilder.addAnalyzer(MockOutputDataStreamAnalyzer.class);
            analyzer0Analyzer0Analyzer0.setName("analyzer0Analyzer0Analyzer0");
            analyzer0Analyzer0Analyzer0.addInputColumn(analyzer0Analyzer0DataStream0JobBuilder.getSourceColumns().get(0));

            final AnalysisJobBuilder analyzer0DataStream1JobBuilder =
                    analyzer0.getOutputDataStreamJobBuilder(analyzer0Analyzer0OutputDataStreams.get(1));
            final AnalyzerComponentBuilder<MockOutputDataStreamAnalyzer> analyzer0Analyzer1 =
                    analyzer0DataStream1JobBuilder.addAnalyzer(MockOutputDataStreamAnalyzer.class);
            analyzer0Analyzer1.setName("analyzer0Analyzer1");
            analyzer0Analyzer1.addInputColumn(analyzer0DataStream1JobBuilder.getSourceColumns().get(0));

            final AnalyzerComponentBuilder<MockOutputDataStreamAnalyzer> analyzer1 =
                    ajb.addAnalyzer(MockOutputDataStreamAnalyzer.class);
            analyzer1.setName("analyzer1");
            analyzer1.addInputColumn(ajb.getSourceColumns().get(0));

            final List<OutputDataStream> analyzer1OutputDataStreams = analyzer1.getOutputDataStreams();

            final AnalysisJobBuilder analyzer1DataStream0JobBuilder =
                    analyzer1.getOutputDataStreamJobBuilder(analyzer1OutputDataStreams.get(0));
            final AnalyzerComponentBuilder<MockOutputDataStreamAnalyzer> analyzer1Analyzer0 =
                    analyzer1DataStream0JobBuilder.addAnalyzer(MockOutputDataStreamAnalyzer.class);
            analyzer1Analyzer0.setName("analyzer1Analyzer0");
            analyzer1Analyzer0.addInputColumn(analyzer1DataStream0JobBuilder.getSourceColumns().get(0));

            final List<OutputDataStream> analyzer1Analyzer0OutputDataStreams =
                    analyzer1Analyzer0.getOutputDataStreams();

            final AnalysisJobBuilder analyzer1Analyzer0DataStream0JobBuilder =
                    analyzer1Analyzer0.getOutputDataStreamJobBuilder(analyzer1Analyzer0OutputDataStreams.get(0));
            final AnalyzerComponentBuilder<MockOutputDataStreamAnalyzer> analyzer1Analyzer0Analyzer0 =
                    analyzer1Analyzer0DataStream0JobBuilder.addAnalyzer(MockOutputDataStreamAnalyzer.class);
            analyzer1Analyzer0Analyzer0.setName("analyzer1Analyzer0Analyzer0");
            analyzer1Analyzer0Analyzer0.addInputColumn(analyzer0Analyzer0DataStream0JobBuilder.getSourceColumns().get(0));

            final AnalysisJobBuilder analyzer1DataStream1JobBuilder =
                    analyzer1.getOutputDataStreamJobBuilder(analyzer1Analyzer0OutputDataStreams.get(1));
            final AnalyzerComponentBuilder<MockOutputDataStreamAnalyzer> analyzer1Analyzer1 =
                    analyzer1DataStream1JobBuilder.addAnalyzer(MockOutputDataStreamAnalyzer.class);
            analyzer1Analyzer1.setName("analyzer1Analyzer1");
            analyzer1Analyzer1.addInputColumn(analyzer0DataStream1JobBuilder.getSourceColumns().get(0));


            // Any random analyzer should work:
            assertEquals(ajb, analyzer1Analyzer0.getAnalysisJobBuilder().getTopLevelJobBuilder());
            assertEquals(ajb, analyzer0Analyzer0Analyzer0.getAnalysisJobBuilder().getTopLevelJobBuilder());
            assertEquals(ajb, analyzer0.getAnalysisJobBuilder().getTopLevelJobBuilder());
            assertEquals(ajb, analyzer0Analyzer0.getAnalysisJobBuilder().getTopLevelJobBuilder());

            assertEquals(16, ajb.getDescendants().size());

            // First should be a0 stream 0
            assertEquals(analyzer0DataStream0JobBuilder, ajb.getDescendants().get(0));

            // Last should be a1a1 stream 1
            assertEquals(analyzer1Analyzer1.getOutputDataStreamJobBuilder(analyzer1Analyzer1.getOutputDataStreams().get(1)), ajb.getDescendants().get(15));
        }
    }
}
