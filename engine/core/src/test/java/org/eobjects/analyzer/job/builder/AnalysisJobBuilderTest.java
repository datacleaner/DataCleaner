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
package org.eobjects.analyzer.job.builder;

import java.io.File;
import java.util.Collection;

import junit.framework.TestCase;

import org.apache.metamodel.schema.MutableColumn;
import org.apache.metamodel.schema.MutableTable;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl;
import org.eobjects.analyzer.data.MetaModelInputColumn;
import org.eobjects.analyzer.test.MockFilter;
import org.eobjects.analyzer.test.MockFilter.Category;
import org.eobjects.analyzer.test.MockTransformer;
import org.eobjects.analyzer.test.mock.MockDatastore;
import org.junit.Test;

public class AnalysisJobBuilderTest extends TestCase {

    @Test
    public void testGetAvailableInputColumnsForComponentPreventsCyclicDependencies() throws Exception {
        final MutableTable table = new MutableTable("table");
        final MutableColumn column = new MutableColumn("foo").setTable(table);
        table.addColumn(column);

        try (AnalysisJobBuilder ajb = new AnalysisJobBuilder(new AnalyzerBeansConfigurationImpl())) {
            final MockDatastore datastore = new MockDatastore();
            ajb.setDatastore(datastore);
            ajb.addSourceColumn(new MetaModelInputColumn(column));

            final TransformerJobBuilder<MockTransformer> transformer1 = ajb.addTransformer(MockTransformer.class);
            transformer1.addInputColumn(ajb.getSourceColumnByName("foo"));
            transformer1.getOutputColumns().get(0).setName("out1");

            assertEquals("[TransformedInputColumn[id=trans-0001-0002,name=out1]]", transformer1.getOutputColumns()
                    .toString());
            assertEquals("[MetaModelInputColumn[table.foo]]", ajb.getAvailableInputColumns(transformer1).toString());

            final TransformerJobBuilder<MockTransformer> transformer2 = ajb.addTransformer(MockTransformer.class);
            transformer2.addInputColumn(transformer1.getOutputColumns().get(0));
            transformer2.getOutputColumns().get(0).setName("out2");

            assertEquals("[MetaModelInputColumn[table.foo]]", ajb.getAvailableInputColumns(transformer1).toString());
            assertEquals("[MetaModelInputColumn[table.foo], TransformedInputColumn[id=trans-0001-0002,name=out1]]", ajb
                    .getAvailableInputColumns(transformer2).toString());

            final TransformerJobBuilder<MockTransformer> transformer3 = ajb.addTransformer(MockTransformer.class);
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
        try (AnalysisJobBuilder ajb = new AnalysisJobBuilder(new AnalyzerBeansConfigurationImpl())) {
            MockDatastore datastore = new MockDatastore();
            ajb.setDatastore(datastore);
            ajb.addSourceColumn(new MetaModelInputColumn(column));

            // add a transformer
            TransformerJobBuilder<MockTransformer> tjb1 = ajb.addTransformer(MockTransformer.class);
            tjb1.addInputColumn(ajb.getSourceColumns().get(0));
            assertTrue(tjb1.isConfigured(true));

            // add filter
            FilterJobBuilder<MockFilter, Category> filter = ajb.addFilter(MockFilter.class);
            filter.addInputColumn(tjb1.getOutputColumns().get(0));
            filter.getComponentInstance().setSomeEnum(Category.VALID);
            filter.getComponentInstance().setSomeFile(new File("."));
            assertTrue(filter.isConfigured(true));

            // set default requirement
            ajb.setDefaultRequirement(filter, Category.VALID);

            // add another transformer
            TransformerJobBuilder<MockTransformer> tjb2 = ajb.addTransformer(MockTransformer.class);
            tjb2.addInputColumn(tjb1.getOutputColumns().get(0));
            assertTrue(tjb2.isConfigured(true));

            // assertions
            assertEquals("Mock filter=VALID", tjb2.getComponentRequirement().toString());
            assertEquals(null, filter.getComponentRequirement());
            assertEquals(null, tjb1.getComponentRequirement());

            final Collection<ComponentBuilder> componentBuilders = ajb.getComponentBuilders();
            assertEquals(
                    "[FilterJobBuilder[filter=Mock filter,inputColumns=[TransformedInputColumn[id=trans-0001-0002,name=mock output]]], "
                            + "TransformerJobBuilder[transformer=Mock transformer,inputColumns=[MetaModelInputColumn[table.foo]]], "
                            + "TransformerJobBuilder[transformer=Mock transformer,inputColumns=[TransformedInputColumn[id=trans-0001-0002,name=mock output]]]]",
                    componentBuilders.toString());
        }
    }
}
