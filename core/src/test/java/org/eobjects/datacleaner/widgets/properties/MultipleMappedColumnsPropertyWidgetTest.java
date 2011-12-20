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

	public void testCustomMutableTable() throws Exception {
		final TransformerBeanDescriptor<MockMultipleMappedColumnsTransformer> descriptor = Descriptors
				.ofTransformer(MockMultipleMappedColumnsTransformer.class);
		final ConfiguredPropertyDescriptor inputColumnsProperty = descriptor.getConfiguredProperty("Input columns");
		assertNotNull(inputColumnsProperty);
		final ConfiguredPropertyDescriptor mappedColumnsProperty = descriptor.getConfiguredProperty("Column names");
		assertNotNull(mappedColumnsProperty);

		final AnalysisJobBuilder ajb = new AnalysisJobBuilder(new AnalyzerBeansConfigurationImpl());
		ajb.addSourceColumns(new MutableColumn("source1").setType(ColumnType.VARCHAR),
				new MutableColumn("source2").setType(ColumnType.INTEGER),
				new MutableColumn("source3").setType(ColumnType.VARCHAR));
		final InputColumn<?> source1 = ajb.getSourceColumnByName("source1");
		final InputColumn<?> source2 = ajb.getSourceColumnByName("source2");
		final InputColumn<?> source3 = ajb.getSourceColumnByName("source3");
		final TransformerJobBuilder<MockMultipleMappedColumnsTransformer> tjb = ajb
				.addTransformer(MockMultipleMappedColumnsTransformer.class);

		final MultipleMappedColumnsPropertyWidget propertyWidget = new MultipleMappedColumnsPropertyWidget(tjb,
				inputColumnsProperty, mappedColumnsProperty);
		propertyWidget.initialize(null);
		final Map<InputColumn<?>, DCCheckBox<InputColumn<?>>> checkBoxes = propertyWidget.getCheckBoxes();
		final Map<InputColumn<?>, SourceColumnComboBox> comboBoxes = propertyWidget.getMappedColumnComboBoxes();

		// initial state should be that source1 and source3 are available, but
		// not checked
		Set<InputColumn<?>> inputColumns = checkBoxes.keySet();
		assertEquals(2, inputColumns.size());
		assertTrue(inputColumns.contains(source1));
		assertFalse(inputColumns.contains(source2));
		assertTrue(inputColumns.contains(source3));
		assertFalse(checkBoxes.get(source1).isSelected());
		assertFalse(comboBoxes.get(source1).isVisible());
		assertFalse(checkBoxes.get(source3).isSelected());
		assertFalse(comboBoxes.get(source3).isVisible());

		// check source3's checkbox
		checkBoxes.get(source3).doClick();
		assertFalse(checkBoxes.get(source1).isSelected());
		assertFalse(comboBoxes.get(source1).isVisible());
		assertTrue(checkBoxes.get(source3).isSelected());
		assertTrue(comboBoxes.get(source3).isVisible());
		assertEquals(null, comboBoxes.get(source3).getSelectedItem());
		assertEquals(0, comboBoxes.get(source3).getModel().getSize());

		// still the value should be an empty array, since no combo item has
		// been selected
		assertEquals(0, propertyWidget.getValue().length);

		// set a table on the widget
		propertyWidget.setTable(new MutableTable("some_table").addColumn(new MutableColumn("foo")).addColumn(
				new MutableColumn("bar")));

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

		comboBoxes.get(source3).setSelectedIndex(1);
		assertEquals("foo", comboBoxes.get(source3).getSelectedItem().getName());

		// now the values should be non-empty
		assertEquals(1, propertyWidget.getValue().length);
		assertEquals("[MetaModelInputColumn[source3]]", Arrays.toString(propertyWidget.getValue()));
		assertEquals("[foo]", Arrays.toString(propertyWidget.getMappedColumnNamesPropertyWidget().getValue()));
	}
}
