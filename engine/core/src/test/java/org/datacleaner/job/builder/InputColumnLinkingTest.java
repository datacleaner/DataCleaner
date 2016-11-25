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

import static org.junit.Assert.*;

import org.apache.metamodel.schema.MutableColumn;
import org.apache.metamodel.schema.MutableTable;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.MappedProperty;
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

        final MutableColumn column = new MutableColumn(SOURCE_COLUMN_NAME);
        column.setTable(new MutableTable("table"));

        _jobBuilder.addSourceColumn(column);

        _dateTransformer = _jobBuilder.addTransformer(ConvertToDateTransformer.class);
        _dateTransformer.addInputColumn(_jobBuilder.getSourceColumnByName(SOURCE_COLUMN_NAME));

        assertEquals(OUTPUT_COLUMN_NAME, _dateTransformer.getOutputColumns().get(0).getName());
    }

    @Test
    public void testChangeWithAnalyzer() {
        final AnalyzerComponentBuilder<MockAnalyzer> analyzer = _jobBuilder.addAnalyzer(MockAnalyzer.class);
        final ConfiguredPropertyDescriptor columnsProperty = getPropertyDescriptor(analyzer, "Columns");
        final ConfiguredPropertyDescriptor columnProperty = getPropertyDescriptor(analyzer, "Column");

        analyzer.addInputColumn(_dateTransformer.getOutputColumns().get(0), columnsProperty);
        analyzer.addInputColumn(_jobBuilder.getSourceColumnByName(SOURCE_COLUMN_NAME), columnsProperty);

        analyzer.addInputColumn(_dateTransformer.getOutputColumns().get(0), columnProperty);

        assertEquals(3, analyzer.getInputColumns().size());
        assertEquals(2, analyzer.getComponentInstance()._columns.length);
        assertNotNull(analyzer.getComponentInstance()._column);

        _dateTransformer.removeInputColumn(_dateTransformer.getInputColumns().get(0));

        assertEquals(1, analyzer.getInputColumns().size());
        assertEquals(1, analyzer.getComponentInstance()._columns.length);
        assertNull(analyzer.getComponentInstance()._column);
    }

    private ConfiguredPropertyDescriptor getPropertyDescriptor(final AnalyzerComponentBuilder<?> mockAnalyzer,
            final String name) {
        for (final ConfiguredPropertyDescriptor propertyDescriptor : mockAnalyzer.getDescriptor()
                .getConfiguredPropertiesForInput()) {
            if (propertyDescriptor.getName().equals(name)) {
                return propertyDescriptor;
            }
        }
        return null;
    }

    @Test
    public void testRemovalWithTransformer() {
        final TransformerComponentBuilder<ConvertToStringTransformer> stringTransformer =
                _jobBuilder.addTransformer(ConvertToStringTransformer.class);

        stringTransformer.addInputColumn(_dateTransformer.getOutputColumns().get(0));

        assertEquals(OUTPUT_COLUMN_NAME, stringTransformer.getInputColumns().get(0).getName());

        _jobBuilder.removeTransformer(_dateTransformer);

        assertEquals(0, stringTransformer.getInputColumns().size());
    }

    @Test
    public void testEscalateToMultipleJobs() {
        final AnalyzerComponentBuilder<MultipleJobsAnalyzer> analyzer1 =
                _jobBuilder.addAnalyzer(MultipleJobsAnalyzer.class);
        final ConfiguredPropertyDescriptor columnProperty1 = getPropertyDescriptor(analyzer1, "Column");

        final AnalyzerComponentBuilder<MultipleJobsAnalyzer> analyzer2 =
                _jobBuilder.addAnalyzer(MultipleJobsAnalyzer.class);
        final ConfiguredPropertyDescriptor columnProperty2 = getPropertyDescriptor(analyzer2, "Column");

        analyzer1.addInputColumn(_jobBuilder.getSourceColumnByName(SOURCE_COLUMN_NAME), columnProperty1);
        analyzer1.addInputColumn(_dateTransformer.getOutputColumns().get(0), columnProperty1);

        analyzer2.setConfiguredProperty(columnProperty2,
                new InputColumn[] { _jobBuilder.getSourceColumnByName(SOURCE_COLUMN_NAME),
                        _dateTransformer.getOutputColumns().get(0) });

        assertEquals(2, analyzer1.getInputColumns().size());
        assertEquals(2, analyzer2.getInputColumns().size());

        _dateTransformer.removeInputColumn(_dateTransformer.getInputColumns().get(0));

        assertEquals(1, analyzer1.getInputColumns().size());
        assertEquals(1, analyzer2.getInputColumns().size());
    }

    @Test
    public void testMultipleOutputColumns() {
        final AnalyzerComponentBuilder<MockAnalyzer> analyzer = _jobBuilder.addAnalyzer(MockAnalyzer.class);
        final ConfiguredPropertyDescriptor columnsProperty = getPropertyDescriptor(analyzer, "Columns");

        final TransformerComponentBuilder<ConvertToStringTransformer> stringTransformer =
                _jobBuilder.addTransformer(ConvertToStringTransformer.class);

        stringTransformer.addInputColumn(_jobBuilder.getSourceColumnByName(SOURCE_COLUMN_NAME));

        analyzer.addInputColumn(stringTransformer.getOutputColumns().get(0), columnsProperty);
        analyzer.addInputColumn(_dateTransformer.getOutputColumns().get(0), columnsProperty);

        assertEquals(2, analyzer.getInputColumns().size());
        assertEquals(2, analyzer.getComponentInstance()._columns.length);

        _jobBuilder.removeTransformer(_dateTransformer);

        assertEquals(1, analyzer.getInputColumns().size());
        assertEquals(1, analyzer.getComponentInstance()._columns.length);
    }

    /**
     * Tests if for an analyzer with a field mapped to an {@link InputColumn} through the
     * {@link MappedProperty} annotation, the value(s) of the field are be synchronized in the same manner as
     * the value(s) of the input column.
     */
    @Test
    public void testMappedProperties() {
        final AnalyzerComponentBuilder<MockAnalyzer> analyzer = _jobBuilder.addAnalyzer(MockAnalyzer.class);
        final ConfiguredPropertyDescriptor columnsProperty = getPropertyDescriptor(analyzer, "Columns");
        final ConfiguredPropertyDescriptor columnProperty = getPropertyDescriptor(analyzer, "Column");

        analyzer.addInputColumn(_dateTransformer.getOutputColumns().get(0), columnsProperty);
        analyzer.addInputColumn(_jobBuilder.getSourceColumnByName(SOURCE_COLUMN_NAME), columnsProperty);
        analyzer.setConfiguredProperty("Column names", new String[] { "first", "second" });

        analyzer.addInputColumn(_dateTransformer.getOutputColumns().get(0), columnProperty);
        analyzer.setConfiguredProperty("Column name", "test");

        assertEquals(3, analyzer.getInputColumns().size());
        assertEquals(2, analyzer.getComponentInstance()._columns.length);
        assertNotNull(analyzer.getComponentInstance()._column);
        assertNotNull(analyzer.getComponentInstance()._columnName);

        _dateTransformer.removeInputColumn(_dateTransformer.getInputColumns().get(0));

        assertEquals(1, analyzer.getInputColumns().size());
        assertEquals(1, analyzer.getComponentInstance()._columns.length);
        assertEquals(1, analyzer.getComponentInstance()._columnNames.length);
        assertEquals("second", analyzer.getComponentInstance()._columnNames[0]);
        assertNull(analyzer.getComponentInstance()._column);
        assertNull(analyzer.getComponentInstance()._columnName);

        analyzer.removeInputColumn(_jobBuilder.getSourceColumnByName(SOURCE_COLUMN_NAME), columnsProperty);

        assertEquals(0, analyzer.getInputColumns().size());
        assertEquals(0, analyzer.getComponentInstance()._columns.length);
        assertEquals(0, analyzer.getComponentInstance()._columnNames.length);
    }

    /**
     * Tests if all mapped properties are copied correctly when creating a copy of a job and the generated ids
     * for the input column object identifiers are not in sync with the ones in the source job.
     */
    @Test
    public void testCopyJob() {
        // Add and remove at transformer to make sure the id's for job copies are out of sync with the source job id's.
        _jobBuilder.removeTransformer(_jobBuilder.addTransformer(ConvertToStringTransformer.class));

        final TransformerComponentBuilder<ConvertToStringTransformer> stringTransformer =
                _jobBuilder.addTransformer(ConvertToStringTransformer.class);
        stringTransformer.addInputColumn(_dateTransformer.getOutputColumns().get(0));

        final AnalyzerComponentBuilder<MockAnalyzer> analyzer = _jobBuilder.addAnalyzer(MockAnalyzer.class);
        final ConfiguredPropertyDescriptor columnsProperty = getPropertyDescriptor(analyzer, "Columns");

        analyzer.addInputColumn(stringTransformer.getOutputColumns().get(0), columnsProperty);
        analyzer.setConfiguredProperty("Column names", new String[] { "first" });

        final AnalysisJobBuilder jobBuilderCopy =
                new AnalysisJobBuilder(_jobBuilder.getConfiguration(), _jobBuilder.toAnalysisJob(false));

        assertNotNull(((MockAnalyzer) jobBuilderCopy.getAnalyzerComponentBuilders().get(0)
                .getComponentInstance())._columnNames[0]);

        jobBuilderCopy.close();
    }
}
