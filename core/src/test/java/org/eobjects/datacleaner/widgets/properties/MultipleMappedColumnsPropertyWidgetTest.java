/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.widgets.properties;

import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfigurationImpl;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.descriptors.Descriptors;
import org.eobjects.analyzer.descriptors.TransformerBeanDescriptor;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.eobjects.datacleaner.widgets.DCCheckBox;
import org.eobjects.datacleaner.widgets.SourceColumnComboBox;
import org.eobjects.metamodel.schema.ColumnType;
import org.eobjects.metamodel.schema.MutableColumn;
import org.eobjects.metamodel.schema.MutableTable;

import cern.colt.Arrays;

public class MultipleMappedColumnsPropertyWidgetTest extends TestCase {

	private ConfiguredPropertyDescriptor inputColumnsProperty;
	private ConfiguredPropertyDescriptor mappedColumnsProperty;
	private AnalysisJobBuilder ajb;
	private MultipleMappedColumnsPropertyWidget propertyWidget;
	private PropertyWidget<String[]> mappedColumnNamesPropertyWidget;
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
		inputColumnsProperty = descriptor
				.getConfiguredProperty("Input columns");
		assertNotNull(inputColumnsProperty);
		mappedColumnsProperty = descriptor
				.getConfiguredProperty("Column names");
		assertNotNull(mappedColumnsProperty);

		ajb = new AnalysisJobBuilder(new AnalyzerBeansConfigurationImpl());
		ajb.addSourceColumns(
				new MutableColumn("source1").setType(ColumnType.VARCHAR),
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

		propertyWidget = new MultipleMappedColumnsPropertyWidget(tjb,
				inputColumnsProperty, mappedColumnsProperty);
		mappedColumnNamesPropertyWidget = propertyWidget
				.getMappedColumnNamesPropertyWidget();
		propertyWidget.initialize(null);
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
		assertEquals(2, value.length);
		
		Map<InputColumn<?>, SourceColumnComboBox> mappedColumnComboBoxes = propertyWidget.getMappedColumnComboBoxes();
		for (SourceColumnComboBox comboBox : mappedColumnComboBoxes.values()) {
			assertTrue(comboBox.isVisible());
		}
	}

	public void testCustomMutableTable() throws Exception {
		final Map<InputColumn<?>, DCCheckBox<InputColumn<?>>> checkBoxes = propertyWidget
				.getCheckBoxes();
		final Map<InputColumn<?>, SourceColumnComboBox> comboBoxes = propertyWidget
				.getMappedColumnComboBoxes();

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

		// still the value should be an empty array, since no combo item has
		// been selected
		assertEquals(0, propertyWidget.getValue().length);

		// set a table on the widget
		propertyWidget.setTable(new MutableTable("some_table").addColumn(
				new MutableColumn("foo")).addColumn(new MutableColumn("bar")));

		assertEquals(null, comboBoxes.get(source3).getSelectedItem());
		assertEquals(3, comboBoxes.get(source3).getModel().getSize());
		assertEquals(null, comboBoxes.get(source3).getModel().getElementAt(0));
		assertEquals(
				"Column[name=foo,columnNumber=0,type=null,nullable=null,indexed=false,nativeType=null,columnSize=null]",
				comboBoxes.get(source3).getModel().getElementAt(1).toString());
		assertEquals(
				"Column[name=bar,columnNumber=0,type=null,nullable=null,indexed=false,nativeType=null,columnSize=null]",
				comboBoxes.get(source3).getModel().getElementAt(2).toString());

		// still the value should be an empty array, since no combo item has
		// been selected
		assertEquals(0, propertyWidget.getValue().length);

		// make a combo box selection
		comboBoxes.get(source3).setSelectedIndex(1);
		assertEquals("foo", comboBoxes.get(source3).getSelectedItem().getName());

		// now the values should be non-empty
		assertEquals(1, propertyWidget.getValue().length);
		assertEquals("[MetaModelInputColumn[source3]]",
				Arrays.toString(propertyWidget.getValue()));
		assertEquals("[foo]",
				Arrays.toString(mappedColumnNamesPropertyWidget.getValue()));

		// check source4's combo
		checkBoxes.get(source4).doClick();
		assertFalse(checkBoxes.get(source1).isSelected());
		assertFalse(comboBoxes.get(source1).isVisible());
		assertTrue(checkBoxes.get(source3).isSelected());
		assertTrue(comboBoxes.get(source3).isVisible());
		assertTrue(checkBoxes.get(source4).isSelected());
		assertTrue(comboBoxes.get(source4).isVisible());
		assertFalse(checkBoxes.get(source5).isSelected());
		assertFalse(comboBoxes.get(source5).isVisible());
		assertEquals(3, comboBoxes.get(source3).getModel().getSize());
		assertEquals(3, comboBoxes.get(source4).getModel().getSize());

		// the values should be unchanged, since we still did not make any
		// combobox selection
		assertEquals(1, propertyWidget.getValue().length);
		assertEquals("[MetaModelInputColumn[source3]]",
				Arrays.toString(propertyWidget.getValue()));
		assertEquals("[foo]",
				Arrays.toString(mappedColumnNamesPropertyWidget.getValue()));

		// make a combo box selection
		comboBoxes.get(source4).setSelectedIndex(2);
		assertEquals("foo", comboBoxes.get(source3).getSelectedItem().getName());
		assertEquals("bar", comboBoxes.get(source4).getSelectedItem().getName());

		// now there should be 2 values
		assertEquals(2, propertyWidget.getValue().length);
		assertEquals(
				"[MetaModelInputColumn[source3], MetaModelInputColumn[source4]]",
				Arrays.toString(propertyWidget.getValue()));
		assertEquals("[foo, bar]",
				Arrays.toString(mappedColumnNamesPropertyWidget.getValue()));
	}
}
