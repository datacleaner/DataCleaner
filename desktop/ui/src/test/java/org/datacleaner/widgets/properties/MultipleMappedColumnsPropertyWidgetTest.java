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

import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.datacleaner.api.InputColumn;
import org.datacleaner.configuration.AnalyzerBeansConfigurationImpl;
import org.datacleaner.data.MutableInputColumn;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.descriptors.Descriptors;
import org.datacleaner.descriptors.TransformerBeanDescriptor;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.builder.TransformerChangeListener;
import org.datacleaner.job.builder.TransformerJobBuilder;
import org.datacleaner.widgets.DCCheckBox;
import org.datacleaner.widgets.SourceColumnComboBox;
import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.MutableColumn;
import org.apache.metamodel.schema.MutableTable;

import cern.colt.Arrays;

public class MultipleMappedColumnsPropertyWidgetTest extends TestCase {

    private ConfiguredPropertyDescriptor inputColumnsProperty;
    private ConfiguredPropertyDescriptor mappedColumnsProperty;
    private AnalysisJobBuilder ajb;
    private MultipleMappedColumnsPropertyWidget propertyWidget;
    private MultipleMappedColumnsPropertyWidget.MappedColumnNamesPropertyWidget mappedColumnNamesPropertyWidget;
    private TransformerJobBuilder<MockMultipleMappedColumnsTransformer> tjb;
    private InputColumn<?> source1;
    private InputColumn<?> source2;
    private InputColumn<?> source3;
    private InputColumn<?> source4;
    private InputColumn<?> source5;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        final TransformerBeanDescriptor<MockMultipleMappedColumnsTransformer> descriptor = Descriptors
                .ofTransformer(MockMultipleMappedColumnsTransformer.class);

        inputColumnsProperty = descriptor.getConfiguredProperty("Input columns");
        assertNotNull(inputColumnsProperty);
        mappedColumnsProperty = descriptor.getConfiguredProperty("Column names");
        assertNotNull(mappedColumnsProperty);

        ajb = new AnalysisJobBuilder(new AnalyzerBeansConfigurationImpl());
        ajb.addSourceColumns(new MutableColumn("source1").setType(ColumnType.VARCHAR),
                new MutableColumn("source2").setType(ColumnType.INTEGER),
                new MutableColumn("source3").setType(ColumnType.VARCHAR),
                new MutableColumn("source4").setType(ColumnType.VARCHAR),
                new MutableColumn("source5").setType(ColumnType.VARCHAR));

        source1 = ajb.getSourceColumnByName("source1");
        source2 = ajb.getSourceColumnByName("source2");
        source3 = ajb.getSourceColumnByName("source3");
        source4 = ajb.getSourceColumnByName("source4");
        source5 = ajb.getSourceColumnByName("source5");
        tjb = ajb.addTransformer(MockMultipleMappedColumnsTransformer.class);

        propertyWidget = new MultipleMappedColumnsPropertyWidget(tjb, inputColumnsProperty, mappedColumnsProperty);
        mappedColumnNamesPropertyWidget = propertyWidget.getMappedColumnNamesPropertyWidget();

        final PropertyWidgetCollection propertyWidgetCollection = new PropertyWidgetCollection(tjb);
        propertyWidgetCollection.registerWidget(inputColumnsProperty, propertyWidget);
        propertyWidgetCollection.registerWidget(mappedColumnsProperty, mappedColumnNamesPropertyWidget);

        tjb.addChangeListener(new TransformerChangeListener() {
            @Override
            public void onRequirementChanged(TransformerJobBuilder<?> arg0) {
            }

            @Override
            public void onRemove(TransformerJobBuilder<?> arg0) {
            }

            @Override
            public void onOutputChanged(TransformerJobBuilder<?> arg0, List<MutableInputColumn<?>> arg1) {
            }

            @Override
            public void onConfigurationChanged(TransformerJobBuilder<?> tjb) {
                propertyWidgetCollection.onConfigurationChanged();
            }

            @Override
            public void onAdd(TransformerJobBuilder<?> arg0) {
            }
        });

        propertyWidget.initialize(null);
        mappedColumnNamesPropertyWidget.initialize(null);
    }

    public void testRemoveColumnRemovesString() throws Exception {
        MutableTable table = new MutableTable();
        table.addColumn(new MutableColumn("source1").setTable(table));
        table.addColumn(new MutableColumn("source2").setTable(table));
        table.addColumn(new MutableColumn("source3").setTable(table));
        table.addColumn(new MutableColumn("source4").setTable(table));
        table.addColumn(new MutableColumn("source5").setTable(table));
        propertyWidget.setTable(table);

        propertyWidget.selectAll();

        // all string columns selected now
        assertEquals(4, propertyWidget.getValue().length);
        assertEquals(
                "[MetaModelInputColumn[source1], MetaModelInputColumn[source3], MetaModelInputColumn[source4], MetaModelInputColumn[source5]]",
                Arrays.toString(propertyWidget.getValue()));
        assertEquals("[source1, source3, source4, source5]", Arrays.toString(mappedColumnNamesPropertyWidget.getValue()));

        Map<InputColumn<?>, DCCheckBox<InputColumn<?>>> checkBoxes = propertyWidget.getCheckBoxes();

        // uncheck one of the columns (remove it)
        checkBoxes.get(source4).doClick();

        assertEquals("[MetaModelInputColumn[source1], MetaModelInputColumn[source3], MetaModelInputColumn[source5]]",
                Arrays.toString(propertyWidget.getValue()));
        assertEquals("[source1, source3, source5]", Arrays.toString(mappedColumnNamesPropertyWidget.getValue()));
    }

    public void testSetTableAndThenSelectAll() throws Exception {
        MutableTable table = new MutableTable();
        table.addColumn(new MutableColumn("source1").setTable(table));
        table.addColumn(new MutableColumn("source3").setTable(table));
        table.addColumn(new MutableColumn("foo").setTable(table));
        table.addColumn(new MutableColumn("bar").setTable(table));
        propertyWidget.setTable(table);

        propertyWidget.selectAll();

        InputColumn<?>[] value = propertyWidget.getValue();
        assertEquals(4, value.length);

        Map<InputColumn<?>, SourceColumnComboBox> mappedColumnComboBoxes = propertyWidget.getMappedColumnComboBoxes();
        for (SourceColumnComboBox comboBox : mappedColumnComboBoxes.values()) {
            assertTrue(comboBox.isVisible());
        }
    }

    public void testCustomMutableTable() throws Exception {
        final Map<InputColumn<?>, DCCheckBox<InputColumn<?>>> checkBoxes = propertyWidget.getCheckBoxes();
        final Map<InputColumn<?>, SourceColumnComboBox> comboBoxes = propertyWidget.getMappedColumnComboBoxes();

        // initial state should be that source1 and source3 are available, but
        // not checked
        Set<InputColumn<?>> inputColumns = checkBoxes.keySet();
        assertEquals(4, inputColumns.size());
        assertTrue(inputColumns.contains(source1));
        assertFalse(inputColumns.contains(source2));
        assertTrue(inputColumns.contains(source3));
        assertTrue(inputColumns.contains(source4));
        assertTrue(inputColumns.contains(source5));
        assertFalse(checkBoxes.get(source1).isSelected());
        assertFalse(comboBoxes.get(source1).isVisible());
        assertFalse(checkBoxes.get(source3).isSelected());
        assertFalse(comboBoxes.get(source3).isVisible());
        assertFalse(checkBoxes.get(source4).isSelected());
        assertFalse(comboBoxes.get(source4).isVisible());
        assertFalse(checkBoxes.get(source5).isSelected());
        assertFalse(comboBoxes.get(source5).isVisible());

        // check source3's checkbox
        checkBoxes.get(source3).doClick();
        assertFalse(checkBoxes.get(source1).isSelected());
        assertFalse(comboBoxes.get(source1).isVisible());
        assertTrue(checkBoxes.get(source3).isSelected());
        assertTrue(comboBoxes.get(source3).isVisible());
        assertFalse(checkBoxes.get(source4).isSelected());
        assertFalse(comboBoxes.get(source4).isVisible());
        assertFalse(checkBoxes.get(source5).isSelected());
        assertFalse(comboBoxes.get(source5).isVisible());
        assertEquals(null, comboBoxes.get(source3).getSelectedItem());
        assertEquals(0, comboBoxes.get(source3).getModel().getSize());

        assertEquals("[MetaModelInputColumn[source3]]", Arrays.toString(propertyWidget.getValue()));

        // set a table on the widget
        propertyWidget.setTable(new MutableTable("some_table").addColumn(new MutableColumn("foo")).addColumn(
                new MutableColumn("bar")));

        assertEquals(null, comboBoxes.get(source3).getSelectedItem());
        assertEquals(3, comboBoxes.get(source3).getModel().getSize());
        assertEquals(null, comboBoxes.get(source3).getModel().getElementAt(0));
        assertEquals("Column[name=foo,columnNumber=0,type=null,nullable=null,nativeType=null,columnSize=null]",
                comboBoxes.get(source3).getModel().getElementAt(1).toString());
        assertEquals("Column[name=bar,columnNumber=0,type=null,nullable=null,nativeType=null,columnSize=null]",
                comboBoxes.get(source3).getModel().getElementAt(2).toString());

        assertEquals("[MetaModelInputColumn[source3]]", Arrays.toString(propertyWidget.getValue()));

        // make a combo box selection
        comboBoxes.get(source3).setSelectedIndex(1);
        assertEquals("foo", comboBoxes.get(source3).getSelectedItem().getName());

        // now the values should be non-empty
        assertEquals(1, propertyWidget.getValue().length);
        assertEquals("[MetaModelInputColumn[source3]]", Arrays.toString(propertyWidget.getValue()));
        assertEquals("[foo]", Arrays.toString(mappedColumnNamesPropertyWidget.getValue()));

        // check source4's combo
        checkBoxes.get(source5).doClick();
        assertFalse(checkBoxes.get(source1).isSelected());
        assertFalse(comboBoxes.get(source1).isVisible());
        assertTrue(checkBoxes.get(source3).isSelected());
        assertTrue(comboBoxes.get(source3).isVisible());
        assertFalse(checkBoxes.get(source4).isSelected());
        assertFalse(comboBoxes.get(source4).isVisible());
        assertTrue(checkBoxes.get(source5).isSelected());
        assertTrue(comboBoxes.get(source5).isVisible());
        assertEquals(3, comboBoxes.get(source3).getModel().getSize());
        assertEquals(3, comboBoxes.get(source5).getModel().getSize());

        assertEquals("[MetaModelInputColumn[source3], MetaModelInputColumn[source5]]",
                Arrays.toString(propertyWidget.getValue()));
        assertEquals("[foo, null]", Arrays.toString(mappedColumnNamesPropertyWidget.getValue()));

        // make a combo box selection
        comboBoxes.get(source5).setSelectedIndex(2);
        assertEquals("foo", comboBoxes.get(source3).getSelectedItem().getName());
        assertEquals("bar", comboBoxes.get(source5).getSelectedItem().getName());

        // now there should be 2 values
        assertEquals(2, propertyWidget.getValue().length);
        assertEquals("[MetaModelInputColumn[source3], MetaModelInputColumn[source5]]",
                Arrays.toString(propertyWidget.getValue()));
        assertEquals("[foo, bar]", Arrays.toString(mappedColumnNamesPropertyWidget.getValue()));

        // now select source4 - it's inbetween source3 and source5 and that
        // presents a special challenge to also add the "null" in mapped column
        // names inbetween "foo" and "bar".

        checkBoxes.get(source4).doClick();

        assertEquals("[MetaModelInputColumn[source3], MetaModelInputColumn[source4], MetaModelInputColumn[source5]]",
                Arrays.toString(propertyWidget.getValue()));
        assertEquals("[foo, null, bar]", Arrays.toString(mappedColumnNamesPropertyWidget.getValue()));
    }
}
