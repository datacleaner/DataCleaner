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

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.mock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.apache.metamodel.schema.MutableColumn;
import org.apache.metamodel.schema.MutableTable;
import org.datacleaner.api.OutputDataStream;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.data.MetaModelInputColumn;
import org.datacleaner.data.MutableInputColumn;
import org.datacleaner.test.MockAnalyzer;
import org.datacleaner.test.MockFilter;
import org.datacleaner.test.MockFilter.Category;
import org.datacleaner.test.MockOutputDataStreamAnalyzer;
import org.datacleaner.test.MockTransformer;
import org.datacleaner.test.mock.MockDatastore;
import org.easymock.EasyMock;

import junit.framework.TestCase;

public class AnalysisJobBuilderTest extends TestCase {
    private MutableTable _table;
    private MutableColumn _column;
    private AnalysisJobBuilder _ajb;

    @Override
    protected void setUp() throws Exception {
        _table = new MutableTable("table");
        _column = new MutableColumn("foo").setTable(_table);
        _table.addColumn(_column);
        _ajb = new AnalysisJobBuilder(new DataCleanerConfigurationImpl());
        _ajb.addSourceColumn(new MetaModelInputColumn(_column));
    }

    @Override
    protected void tearDown() throws Exception {
        _ajb.close();
    }

    public void testGetAvailableInputColumnsForComponentPreventsCyclicDependencies() throws Exception {
        final TransformerComponentBuilder<MockTransformer> transformer1 = _ajb.addTransformer(MockTransformer.class);
        transformer1.addInputColumn(_ajb.getSourceColumnByName("foo"));
        transformer1.getOutputColumns().get(0).setName("out1");

        assertEquals("[TransformedInputColumn[id=trans-0001-0002,name=out1]]",
                transformer1.getOutputColumns().toString());
        assertEquals("[MetaModelInputColumn[table.foo]]", _ajb.getAvailableInputColumns(transformer1).toString());

        final TransformerComponentBuilder<MockTransformer> transformer2 = _ajb.addTransformer(MockTransformer.class);
        transformer2.addInputColumn(transformer1.getOutputColumns().get(0));
        transformer2.getOutputColumns().get(0).setName("out2");

        assertEquals("[MetaModelInputColumn[table.foo]]", _ajb.getAvailableInputColumns(transformer1).toString());
        assertEquals("[MetaModelInputColumn[table.foo], TransformedInputColumn[id=trans-0001-0002,name=out1]]",
                _ajb.getAvailableInputColumns(transformer2).toString());

        final TransformerComponentBuilder<MockTransformer> transformer3 = _ajb.addTransformer(MockTransformer.class);
        assertEquals("[MetaModelInputColumn[table.foo]]", _ajb.getAvailableInputColumns(transformer1).toString());
        assertEquals("[MetaModelInputColumn[table.foo], TransformedInputColumn[id=trans-0001-0002,name=out1]]",
                _ajb.getAvailableInputColumns(transformer2).toString());
        assertEquals(
                "[MetaModelInputColumn[table.foo], TransformedInputColumn[id=trans-0001-0002,name=out1], TransformedInputColumn[id=trans-0003-0004,name=out2]]",
                _ajb.getAvailableInputColumns(transformer3).toString());

        transformer3.addInputColumn(_ajb.getSourceColumns().get(0));
        transformer3.getOutputColumns().get(0).setName("out3");

        assertEquals("[MetaModelInputColumn[table.foo], TransformedInputColumn[id=trans-0005-0006,name=out3]]",
                _ajb.getAvailableInputColumns(transformer1).toString());
        assertEquals(
                "[MetaModelInputColumn[table.foo], TransformedInputColumn[id=trans-0001-0002,name=out1], TransformedInputColumn[id=trans-0005-0006,name=out3]]",
                _ajb.getAvailableInputColumns(transformer2).toString());
        assertEquals(
                "[MetaModelInputColumn[table.foo], TransformedInputColumn[id=trans-0001-0002,name=out1], TransformedInputColumn[id=trans-0003-0004,name=out2]]",
                _ajb.getAvailableInputColumns(transformer3).toString());

        transformer2.clearInputColumns();
        transformer2.addInputColumn(transformer3.getOutputColumns().get(0));

        assertEquals(
                "[MetaModelInputColumn[table.foo], TransformedInputColumn[id=trans-0003-0004,name=out2], TransformedInputColumn[id=trans-0005-0006,name=out3]]",
                _ajb.getAvailableInputColumns(transformer1).toString());
        assertEquals(
                "[MetaModelInputColumn[table.foo], TransformedInputColumn[id=trans-0001-0002,name=out1], TransformedInputColumn[id=trans-0005-0006,name=out3]]",
                _ajb.getAvailableInputColumns(transformer2).toString());
        assertEquals("[MetaModelInputColumn[table.foo], TransformedInputColumn[id=trans-0001-0002,name=out1]]",
                _ajb.getAvailableInputColumns(transformer3).toString());
    }

    /**
     * Builds a scenario with 2 transformers and a filter inbetween. When a
     * filter outcome is set as the default requirement, that requirement should
     * only be set on the succeeding (not preceeding) transformer.
     */
    public void testSetDefaultRequirementNonCyclic() throws Exception {
        MockDatastore datastore = new MockDatastore();
        _ajb.setDatastore(datastore);
        _ajb.addSourceColumn(new MetaModelInputColumn(_column));

        // add a transformer
        TransformerComponentBuilder<MockTransformer> tjb1 = _ajb.addTransformer(MockTransformer.class);
        tjb1.addInputColumn(_ajb.getSourceColumns().get(0));
        assertTrue(tjb1.isConfigured(true));

        // add filter
        FilterComponentBuilder<MockFilter, Category> filter = _ajb.addFilter(MockFilter.class);
        filter.addInputColumn(tjb1.getOutputColumns().get(0));
        filter.getComponentInstance().setSomeEnum(Category.VALID);
        filter.getComponentInstance().setSomeFile(new File("."));
        assertTrue(filter.isConfigured(true));

        // set default requirement
        _ajb.setDefaultRequirement(filter, Category.VALID);

        // add another transformer
        TransformerComponentBuilder<MockTransformer> tjb2 = _ajb.addTransformer(MockTransformer.class);
        tjb2.addInputColumn(tjb1.getOutputColumns().get(0));
        assertTrue(tjb2.isConfigured(true));

        // assertions
        assertEquals("Mock filter=VALID", tjb2.getComponentRequirement().toString());
        assertEquals(null, filter.getComponentRequirement());
        assertEquals(null, tjb1.getComponentRequirement());

        final Collection<ComponentBuilder> componentBuilders = _ajb.getComponentBuilders();
        assertEquals(
                "[FilterComponentBuilder[filter=Mock filter,inputColumns=[TransformedInputColumn[id=trans-0001-0002,name=mock output]]], "
                        + "TransformerComponentBuilder[transformer=Mock transformer,inputColumns=[MetaModelInputColumn[table.foo]]], "
                        + "TransformerComponentBuilder[transformer=Mock transformer,inputColumns=[TransformedInputColumn[id=trans-0001-0002,name=mock output]]]]",
                componentBuilders.toString());
    }

    public void testGetAllJobBuilders() {
        final AnalyzerComponentBuilder<MockOutputDataStreamAnalyzer> analyzer0 = _ajb
                .addAnalyzer(MockOutputDataStreamAnalyzer.class);
        analyzer0.setName("analyzer0");
        analyzer0.addInputColumn(_ajb.getSourceColumns().get(0));

        final List<OutputDataStream> analyzer0OutputDataStreams = analyzer0.getOutputDataStreams();

        final AnalysisJobBuilder analyzer0DataStream0JobBuilder = analyzer0
                .getOutputDataStreamJobBuilder(analyzer0OutputDataStreams.get(0));
        final AnalyzerComponentBuilder<MockOutputDataStreamAnalyzer> analyzer0Analyzer0 = analyzer0DataStream0JobBuilder
                .addAnalyzer(MockOutputDataStreamAnalyzer.class);
        analyzer0Analyzer0.setName("analyzer0Analyzer0");
        analyzer0Analyzer0.addInputColumn(analyzer0DataStream0JobBuilder.getSourceColumns().get(0));

        final List<OutputDataStream> analyzer0Analyzer0OutputDataStreams = analyzer0Analyzer0.getOutputDataStreams();

        final AnalysisJobBuilder analyzer0Analyzer0DataStream0JobBuilder = analyzer0Analyzer0
                .getOutputDataStreamJobBuilder(analyzer0Analyzer0OutputDataStreams.get(0));
        final AnalyzerComponentBuilder<MockOutputDataStreamAnalyzer> analyzer0Analyzer0Analyzer0 = analyzer0Analyzer0DataStream0JobBuilder
                .addAnalyzer(MockOutputDataStreamAnalyzer.class);
        analyzer0Analyzer0Analyzer0.setName("analyzer0Analyzer0Analyzer0");
        analyzer0Analyzer0Analyzer0.addInputColumn(analyzer0Analyzer0DataStream0JobBuilder.getSourceColumns().get(0));

        final AnalysisJobBuilder analyzer0DataStream1JobBuilder = analyzer0
                .getOutputDataStreamJobBuilder(analyzer0Analyzer0OutputDataStreams.get(1));
        final AnalyzerComponentBuilder<MockOutputDataStreamAnalyzer> analyzer0Analyzer1 = analyzer0DataStream1JobBuilder
                .addAnalyzer(MockOutputDataStreamAnalyzer.class);
        analyzer0Analyzer1.setName("analyzer0Analyzer1");
        analyzer0Analyzer1.addInputColumn(analyzer0DataStream1JobBuilder.getSourceColumns().get(0));

        final AnalyzerComponentBuilder<MockOutputDataStreamAnalyzer> analyzer1 = _ajb
                .addAnalyzer(MockOutputDataStreamAnalyzer.class);
        analyzer1.setName("analyzer1");
        analyzer1.addInputColumn(_ajb.getSourceColumns().get(0));

        final List<OutputDataStream> analyzer1OutputDataStreams = analyzer1.getOutputDataStreams();

        final AnalysisJobBuilder analyzer1DataStream0JobBuilder = analyzer1
                .getOutputDataStreamJobBuilder(analyzer1OutputDataStreams.get(0));
        final AnalyzerComponentBuilder<MockOutputDataStreamAnalyzer> analyzer1Analyzer0 = analyzer1DataStream0JobBuilder
                .addAnalyzer(MockOutputDataStreamAnalyzer.class);
        analyzer1Analyzer0.setName("analyzer1Analyzer0");
        analyzer1Analyzer0.addInputColumn(analyzer1DataStream0JobBuilder.getSourceColumns().get(0));

        final List<OutputDataStream> analyzer1Analyzer0OutputDataStreams = analyzer1Analyzer0.getOutputDataStreams();

        final AnalysisJobBuilder analyzer1Analyzer0DataStream0JobBuilder = analyzer1Analyzer0
                .getOutputDataStreamJobBuilder(analyzer1Analyzer0OutputDataStreams.get(0));
        final AnalyzerComponentBuilder<MockOutputDataStreamAnalyzer> analyzer1Analyzer0Analyzer0 = analyzer1Analyzer0DataStream0JobBuilder
                .addAnalyzer(MockOutputDataStreamAnalyzer.class);
        analyzer1Analyzer0Analyzer0.setName("analyzer1Analyzer0Analyzer0");
        analyzer1Analyzer0Analyzer0.addInputColumn(analyzer0Analyzer0DataStream0JobBuilder.getSourceColumns().get(0));

        final AnalysisJobBuilder analyzer1DataStream1JobBuilder = analyzer1
                .getOutputDataStreamJobBuilder(analyzer1Analyzer0OutputDataStreams.get(1));
        final AnalyzerComponentBuilder<MockOutputDataStreamAnalyzer> analyzer1Analyzer1 = analyzer1DataStream1JobBuilder
                .addAnalyzer(MockOutputDataStreamAnalyzer.class);
        analyzer1Analyzer1.setName("analyzer1Analyzer1");
        analyzer1Analyzer1.addInputColumn(analyzer0DataStream1JobBuilder.getSourceColumns().get(0));

        // Any random analyzer should work:
        assertEquals(_ajb, analyzer1Analyzer0.getAnalysisJobBuilder().getRootJobBuilder());
        assertEquals(_ajb, analyzer0Analyzer0Analyzer0.getAnalysisJobBuilder().getRootJobBuilder());
        assertEquals(_ajb, analyzer0.getAnalysisJobBuilder().getRootJobBuilder());
        assertEquals(_ajb, analyzer0Analyzer0.getAnalysisJobBuilder().getRootJobBuilder());
    }

    public void testChildOutputDataStreamListeners() {
        // Set up expectations for the listeners
        final AnalysisJobChangeListener mockedAnalysisJobChangeListener = mock(AnalysisJobChangeListener.class);
        mockedAnalysisJobChangeListener.onActivation(anyObject(AnalysisJobBuilder.class));
        expectLastCall().times(3);
        mockedAnalysisJobChangeListener.onDeactivation(anyObject(AnalysisJobBuilder.class));
        expectLastCall().times(3);

        final AnalyzerChangeListener analyzerChangeListener = mock(AnalyzerChangeListener.class);
        analyzerChangeListener.onAdd(anyObject(AnalyzerComponentBuilder.class));
        expectLastCall().times(2);
        analyzerChangeListener.onRemove(anyObject(AnalyzerComponentBuilder.class));
        expectLastCall().times(2);
        analyzerChangeListener.onConfigurationChanged(anyObject(AnalyzerComponentBuilder.class));
        expectLastCall().atLeastOnce();

        final TransformerChangeListener transformerChangeListener = mock(TransformerChangeListener.class);
        transformerChangeListener.onAdd(anyObject(TransformerComponentBuilder.class));
        transformerChangeListener.onRemove(anyObject(TransformerComponentBuilder.class));
        transformerChangeListener.onOutputChanged(anyObject(TransformerComponentBuilder.class),
                EasyMock.<List<MutableInputColumn<?>>> anyObject());
        expectLastCall().times(2); // Both configuration and removal will
                                   // trigger this
        transformerChangeListener.onConfigurationChanged(anyObject(TransformerComponentBuilder.class));
        expectLastCall().times(1);

        final FilterChangeListener filterChangeListener = mock(FilterChangeListener.class);
        filterChangeListener.onAdd(anyObject(FilterComponentBuilder.class));
        filterChangeListener.onRemove(anyObject(FilterComponentBuilder.class));
        filterChangeListener.onConfigurationChanged(anyObject(FilterComponentBuilder.class));

        replay(mockedAnalysisJobChangeListener, analyzerChangeListener, transformerChangeListener,
                filterChangeListener);
        final AnalysisJobChangeListener realAnalysisJobChangeListener = new AnalysisJobChangeListener() {
            @Override
            public void onActivation(final AnalysisJobBuilder builder) {
                builder.addAnalysisJobChangeListener(this);
                builder.addAnalysisJobChangeListener(mockedAnalysisJobChangeListener);
                builder.addAnalyzerChangeListener(analyzerChangeListener);
                builder.addTransformerChangeListener(transformerChangeListener);
                builder.addFilterChangeListener(filterChangeListener);
            }

            @Override
            public void onDeactivation(final AnalysisJobBuilder builder) {
                builder.removeAnalysisJobChangeListener(this);
                builder.removeAnalysisJobChangeListener(mockedAnalysisJobChangeListener);
                builder.removeAnalyzerChangeListener(analyzerChangeListener);
                builder.removeTransformerChangeListener(transformerChangeListener);
                builder.removeFilterChangeListener(filterChangeListener);
            }
        };

        realAnalysisJobChangeListener.onActivation(_ajb);

        final AnalyzerComponentBuilder<MockOutputDataStreamAnalyzer> analyzer0 = _ajb
                .addAnalyzer(MockOutputDataStreamAnalyzer.class);
        analyzer0.setName("analyzer0");
        analyzer0.addInputColumn(_ajb.getSourceColumns().get(0));
        final List<OutputDataStream> analyzer0OutputDataStreams = analyzer0.getOutputDataStreams();
        final AnalysisJobBuilder childAnalysisJobBuilder = analyzer0
                .getOutputDataStreamJobBuilder(analyzer0OutputDataStreams.get(0));

        final AnalyzerComponentBuilder<MockAnalyzer> childAnalyzer = childAnalysisJobBuilder
                .addAnalyzer(MockAnalyzer.class);
        childAnalyzer.setName("childAnalyzer");
        childAnalyzer.addInputColumn(childAnalysisJobBuilder.getSourceColumns().get(0));
        childAnalysisJobBuilder.removeAllAnalyzers();

        final TransformerComponentBuilder<MockTransformer> childTransformer = childAnalysisJobBuilder
                .addTransformer(MockTransformer.class);
        childTransformer.setName("childTransformer");
        childTransformer.addInputColumn(childAnalysisJobBuilder.getSourceColumns().get(0));
        childAnalysisJobBuilder.removeAllTransformers();

        final FilterComponentBuilder<MockFilter, Category> childFilter = childAnalysisJobBuilder
                .addFilter(MockFilter.class);
        childFilter.setName("childFilter");
        childFilter.addInputColumn(childAnalysisJobBuilder.getSourceColumns().get(0));
        childAnalysisJobBuilder.removeAllFilters();

        _ajb.removeAllAnalyzers();
        verify(mockedAnalysisJobChangeListener, analyzerChangeListener, transformerChangeListener,
                filterChangeListener);
    }

}
