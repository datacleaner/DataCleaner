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

import static org.junit.Assert.assertEquals;

import org.apache.metamodel.schema.MutableColumn;
import org.datacleaner.components.convert.ConvertToDateTransformer;
import org.datacleaner.components.convert.ConvertToStringTransformer;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.junit.Before;
import org.junit.Test;

public class InputColumnLinkingTest {
    private static final String SOURCE_COLUMN_NAME = "foo";
    private static final String OUTPUT_COLUMN_NAME = SOURCE_COLUMN_NAME + " (as date)";

    private AnalysisJobBuilder _jobBuilder;
    private TransformerComponentBuilder<ConvertToDateTransformer> _dateTransformer;

    @Before
    public void setup() {
        _jobBuilder = new AnalysisJobBuilder(new DataCleanerConfigurationImpl());
        _jobBuilder.addSourceColumn(new MutableColumn(SOURCE_COLUMN_NAME));

        _dateTransformer = _jobBuilder.addTransformer(ConvertToDateTransformer.class);
        _dateTransformer.addInputColumn(_jobBuilder.getSourceColumnByName(SOURCE_COLUMN_NAME));

        assertEquals(OUTPUT_COLUMN_NAME, _dateTransformer.getOutputColumns().get(0).getName());
    }

    @Test
    public void testChangeWithAnalyzer() {
        final AnalyzerComponentBuilder<MockAnalyzer> mockAnalyzer = _jobBuilder.addAnalyzer(MockAnalyzer.class);
        final ConfiguredPropertyDescriptor columnsProperty = getPropertyDescriptor(mockAnalyzer, "Columns");
        final ConfiguredPropertyDescriptor columnProperty = getPropertyDescriptor(mockAnalyzer, "Column");

        mockAnalyzer.addInputColumn(_dateTransformer.getOutputColumns().get(0), columnsProperty);
        mockAnalyzer.addInputColumn(_jobBuilder.getSourceColumnByName(SOURCE_COLUMN_NAME), columnsProperty);

        mockAnalyzer.addInputColumn(_dateTransformer.getOutputColumns().get(0), columnProperty);

        assertEquals(3, mockAnalyzer.getInputColumns().size());

        _dateTransformer.removeInputColumn(_dateTransformer.getInputColumns().get(0));

        assertEquals(1, mockAnalyzer.getInputColumns().size());
    }

    private ConfiguredPropertyDescriptor getPropertyDescriptor(
            final AnalyzerComponentBuilder<MockAnalyzer> mockAnalyzer, final String name) {
        for (ConfiguredPropertyDescriptor propertyDescriptor : mockAnalyzer.getDescriptor()
                .getConfiguredPropertiesForInput()) {
            if (propertyDescriptor.getName().equals(name)) {
                return propertyDescriptor;
            }
        }
        return null;
    }

    @Test
    public void testRemovalWithTransformer() {
        final TransformerComponentBuilder<ConvertToStringTransformer> stringTransformer = _jobBuilder.addTransformer(
                ConvertToStringTransformer.class);

        stringTransformer.addInputColumn(_dateTransformer.getOutputColumns().get(0));

        assertEquals(OUTPUT_COLUMN_NAME, stringTransformer.getInputColumns().get(0).getName());

        _jobBuilder.removeTransformer(_dateTransformer);

        assertEquals(0, stringTransformer.getInputColumns().size());
    }
}
