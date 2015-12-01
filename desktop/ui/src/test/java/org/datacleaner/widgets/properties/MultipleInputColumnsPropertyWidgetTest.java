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
package org.datacleaner.widgets.properties;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;

import junit.framework.TestCase;

import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.MutableColumn;
import org.apache.metamodel.util.CollectionUtils;
import org.datacleaner.actions.AddExpressionBasedColumnActionListener;
import org.datacleaner.actions.ReorderColumnsActionListener;
import org.datacleaner.api.InputColumn;
import org.datacleaner.beans.StringAnalyzer;
import org.datacleaner.beans.transform.ConcatenatorTransformer;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.data.ELInputColumn;
import org.datacleaner.data.MutableInputColumn;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.AnalyzerComponentBuilder;
import org.datacleaner.job.builder.TransformerComponentBuilder;
import org.datacleaner.widgets.DCCheckBox;

public class MultipleInputColumnsPropertyWidgetTest extends TestCase {

    public void testDefaultSelectAll() throws Exception {
        DataCleanerConfiguration configuration = new DataCleanerConfigurationImpl();

        try (AnalysisJobBuilder ajb = new AnalysisJobBuilder(configuration)) {
            ajb.addSourceColumn(new MutableColumn("foo", ColumnType.VARCHAR));
            MutableColumn barColumn = new MutableColumn("bar", ColumnType.VARCHAR);
            ajb.addSourceColumn(barColumn);
            ajb.addSourceColumn(new MutableColumn("baz", ColumnType.INTEGER));

            AnalyzerComponentBuilder<StringAnalyzer> beanJobBuilder = ajb.addAnalyzer(StringAnalyzer.class);
            ConfiguredPropertyDescriptor property = beanJobBuilder.getDescriptor().getConfiguredPropertiesForInput()
                    .iterator().next();

            MultipleInputColumnsPropertyWidget widget = new MultipleInputColumnsPropertyWidget(beanJobBuilder, property);
            widget.onPanelAdd();

            // initialize with null
            widget.initialize(null);
            widget.selectAll();
            InputColumn<?>[] value = widget.getValue();
            assertEquals("[MetaModelInputColumn[foo], MetaModelInputColumn[bar]]", Arrays.toString(value));

            // add another available column
            ajb.addSourceColumn(new MutableColumn("foobar", ColumnType.VARCHAR));

            value = widget.getValue();
            assertEquals("[MetaModelInputColumn[foo], MetaModelInputColumn[bar]]", Arrays.toString(value));
            assertEquals("foo,bar,foobar", getAvailableCheckBoxValues(widget));

            // remove a column
            ajb.removeSourceColumn(barColumn);

            value = widget.getValue();
            assertEquals("[MetaModelInputColumn[foo]]", Arrays.toString(value));
            assertEquals("foo,foobar", getAvailableCheckBoxValues(widget));
            
            widget.onPanelRemove();
        }
    }

    public void testAddAndRemoveExpressionColumn() throws Exception {
        DataCleanerConfiguration configuration = new DataCleanerConfigurationImpl();

        try (AnalysisJobBuilder ajb = new AnalysisJobBuilder(configuration)) {

            ajb.addSourceColumn(new MutableColumn("foo", ColumnType.VARCHAR));
            ajb.addSourceColumn(new MutableColumn("bar", ColumnType.VARCHAR));

            AnalyzerComponentBuilder<StringAnalyzer> beanJobBuilder = ajb.addAnalyzer(StringAnalyzer.class);
            ConfiguredPropertyDescriptor property = beanJobBuilder.getDescriptor().getConfiguredPropertiesForInput()
                    .iterator().next();

            // initialize with a expression column
            MultipleInputColumnsPropertyWidget widget = new MultipleInputColumnsPropertyWidget(beanJobBuilder, property);
            InputColumn<?>[] value = new InputColumn[] { new ELInputColumn("Hello #{name}") };
            widget.initialize(value);

            value = widget.getValue();
            assertEquals("[ELInputColumn[Hello #{name}]]", Arrays.toString(value));
            assertEquals("\"Hello #{name}\",foo,bar", getAvailableCheckBoxValues(widget));

            // select another column
            widget.setValue(new InputColumn[] { ajb.getSourceColumnByName("bar") });

            value = widget.getValue();
            assertEquals("[MetaModelInputColumn[bar]]", Arrays.toString(value));
            assertEquals("\"Hello #{name}\",foo,bar", getAvailableCheckBoxValues(widget));

            // simulate clicking the EL button
            AddExpressionBasedColumnActionListener.forMultipleColumns(widget).addExpressionBasedInputColumn(
                    "Hi #{nickname}");

            value = widget.getValue();
            assertEquals("[MetaModelInputColumn[bar], ELInputColumn[Hi #{nickname}]]", Arrays.toString(value));
            assertEquals("\"Hello #{name}\",foo,bar,\"Hi #{nickname}\"", getAvailableCheckBoxValues(widget));

            // add the same expression two times!
            AddExpressionBasedColumnActionListener.forMultipleColumns(widget).addExpressionBasedInputColumn(
                    "Hi #{nickname}");
            value = widget.getValue();
            assertEquals("[MetaModelInputColumn[bar], ELInputColumn[Hi #{nickname}]]", Arrays.toString(value));
            assertEquals("\"Hello #{name}\",foo,bar,\"Hi #{nickname}\"", getAvailableCheckBoxValues(widget));
        }
    }

    public void testInitializeReordered() throws Exception {
        DataCleanerConfiguration configuration = new DataCleanerConfigurationImpl();

        try (AnalysisJobBuilder ajb = new AnalysisJobBuilder(configuration)) {
            ajb.addSourceColumn(new MutableColumn("foo", ColumnType.VARCHAR));
            ajb.addSourceColumn(new MutableColumn("bar", ColumnType.VARCHAR));
            ajb.addSourceColumn(new MutableColumn("baz", ColumnType.NVARCHAR));

            AnalyzerComponentBuilder<StringAnalyzer> beanJobBuilder = ajb.addAnalyzer(StringAnalyzer.class);
            ConfiguredPropertyDescriptor property = beanJobBuilder.getDescriptor().getConfiguredPropertiesForInput()
                    .iterator().next();

            // initialize with "baz" + "foo"
            MultipleInputColumnsPropertyWidget widget = new MultipleInputColumnsPropertyWidget(beanJobBuilder, property);
            InputColumn<?>[] value = new InputColumn[] { ajb.getSourceColumnByName("baz"),
                    ajb.getSourceColumnByName("foo") };
            widget.initialize(value);

            assertEquals("baz,foo,bar", getAvailableCheckBoxValues(widget));
            assertEquals("[MetaModelInputColumn[baz], MetaModelInputColumn[foo]]", Arrays.toString(widget.getValue()));
        }
    }

    public void testShouldNotReorderColumnsWhenGettingAndSettingValue() throws Exception {
        final DataCleanerConfiguration configuration = new DataCleanerConfigurationImpl();

        try (AnalysisJobBuilder ajb = new AnalysisJobBuilder(configuration)) {
            ajb.addSourceColumn(new MutableColumn("foo", ColumnType.VARCHAR));
            ajb.addSourceColumn(new MutableColumn("bar", ColumnType.VARCHAR));

            final TransformerComponentBuilder<ConcatenatorTransformer> transformer = ajb
                    .addTransformer(ConcatenatorTransformer.class);
            transformer.addInputColumns(ajb.getSourceColumns());
            final MutableInputColumn<?> transformedColumn = transformer.getOutputColumns().get(0);
            transformedColumn.setName("baz");

            final AnalyzerComponentBuilder<StringAnalyzer> analyzer = ajb.addAnalyzer(StringAnalyzer.class);
            final ConfiguredPropertyDescriptor property = analyzer.getDescriptor().getConfiguredProperty("Columns");

            // add columns in this sequence: foo, baz, bar
            analyzer.addInputColumn(ajb.getSourceColumnByName("foo"), property);
            analyzer.addInputColumn(transformedColumn, property);
            analyzer.addInputColumn(ajb.getSourceColumnByName("bar"), property);

            final InputColumn<?>[] propertyValue = (InputColumn<?>[]) analyzer.getConfiguredProperty(property);
            assertEquals(3, propertyValue.length);

            final MultipleInputColumnsPropertyWidget widget = new MultipleInputColumnsPropertyWidget(analyzer, property);
            widget.onOutputChanged(transformer, transformer.getOutputColumns());

            widget.initialize(propertyValue);

            assertEquals(
                    "[MetaModelInputColumn[foo], TransformedInputColumn[id=trans-0001-0002,name=baz], MetaModelInputColumn[bar]]",
                    Arrays.toString(widget.getValue()));
        }
    }

    public void testReorderColumns() throws Exception {
        DataCleanerConfiguration configuration = new DataCleanerConfigurationImpl();

        try (AnalysisJobBuilder ajb = new AnalysisJobBuilder(configuration)) {
            ajb.addSourceColumn(new MutableColumn("foo", ColumnType.VARCHAR));
            ajb.addSourceColumn(new MutableColumn("bar", ColumnType.VARCHAR));
            ajb.addSourceColumn(new MutableColumn("baz", ColumnType.LONGVARCHAR));

            AnalyzerComponentBuilder<StringAnalyzer> beanJobBuilder = ajb.addAnalyzer(StringAnalyzer.class);
            ConfiguredPropertyDescriptor property = beanJobBuilder.getDescriptor().getConfiguredPropertiesForInput()
                    .iterator().next();

            // initialize with all 3 columns + an expression
            MultipleInputColumnsPropertyWidget widget = new MultipleInputColumnsPropertyWidget(beanJobBuilder, property);
            InputColumn<?>[] value = CollectionUtils.array(ajb.getSourceColumns().toArray(new InputColumn[0]),
                    new ELInputColumn("Hello #{name}"));
            widget.initialize(value);

            assertEquals("foo,bar,baz,\"Hello #{name}\"", getAvailableCheckBoxValues(widget));

            ArrayList<InputColumn<?>> reorderedValue = new ArrayList<InputColumn<?>>(Arrays.asList(value));
            reorderedValue.add(1, reorderedValue.remove(3));

            new ReorderColumnsActionListener(widget).saveReorderedValue(reorderedValue);

            assertEquals("foo,\"Hello #{name}\",bar,baz", getAvailableCheckBoxValues(widget));
            assertEquals(
                    "[MetaModelInputColumn[foo], ELInputColumn[Hello #{name}], MetaModelInputColumn[bar], MetaModelInputColumn[baz]]",
                    Arrays.toString(widget.getValue()));
        }
    }

    /**
     * Helper method to determine which checkboxes are shown
     * 
     * @param widget
     * @return
     */
    private String getAvailableCheckBoxValues(MultipleInputColumnsPropertyWidget widget) {
        StringBuilder sb = new StringBuilder();
        Component[] components = widget.getWidget().getComponents();
        for (Component component : components) {
            if (component instanceof DCCheckBox) {
                @SuppressWarnings("unchecked")
                DCCheckBox<InputColumn<?>> checkBox = (DCCheckBox<InputColumn<?>>) component;
                String name = checkBox.getValue().getName();
                if (sb.length() > 0) {
                    sb.append(',');
                }
                sb.append(name);
            }
        }
        return sb.toString();
    }
}
