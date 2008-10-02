/**
 *  This file is part of DataCleaner.
 *
 *  DataCleaner is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DataCleaner is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DataCleaner.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.eobjects.datacleaner.gui.panels;

import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JRadioButton;

import junit.framework.TestCase;
import dk.eobjects.datacleaner.data.ColumnSelection;
import dk.eobjects.datacleaner.profiler.BasicProfileDescriptor;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.ColumnType;
import dk.eobjects.metamodel.schema.Table;

public class SubsetDataSelectionPanelTest extends TestCase {

	public void testSetSelectedColumns() throws Exception {
		ColumnSelection columnSelection = new ColumnSelection(null);
		SubsetDataSelectionPanel panel = new SubsetDataSelectionPanel(
				columnSelection) {
			private static final long serialVersionUID = 1377366745727355643L;

			@Override
			protected boolean isSupported(ColumnType type) {
				return true;
			}
		};

		Table table = new Table("foobar");
		Column ca = new Column("a", ColumnType.VARCHAR, table, 0, true);
		Column cb = new Column("b", ColumnType.VARCHAR, table, 1, true);
		Column cc = new Column("c", ColumnType.VARCHAR, table, 2, true);
		table.addColumn(ca).addColumn(cb).addColumn(cc);

		columnSelection.toggleTable(table);

		panel.setSelectedColumns(new Column[] { ca, cc });
		assertEquals(2, panel.getSelectedColumns().size());
	}

	public void testDeactivationOfUnsupportedMetadata() throws Exception {
		final BasicProfileDescriptor profileDescriptor = new BasicProfileDescriptor(
				"My profile", null);

		// Set up test-metadata
		Table table1 = new Table("Table1");
		Table table2 = new Table("Table1");
		Column intColumn = new Column("intColumn", ColumnType.INTEGER);
		Column dateColumn = new Column("dateColumn", ColumnType.DATE);
		Column varcharColumn = new Column("varcharColumn", ColumnType.VARCHAR);
		Column charColumn = new Column("charColumn", ColumnType.CHAR);
		table1.addColumn(intColumn);
		intColumn.setTable(table1);
		table1.addColumn(dateColumn);
		dateColumn.setTable(table1);
		table2.addColumn(varcharColumn);
		varcharColumn.setTable(table2);
		table2.addColumn(charColumn);
		charColumn.setTable(table2);

		ColumnSelection columnSelection = new ColumnSelection(null);
		SubsetDataSelectionPanel panel = SubsetDataSelectionPanel.createPanel(
				columnSelection, profileDescriptor);

		columnSelection.toggleColumn(intColumn);

		JRadioButton allDataRadio = panel.getAllDataRadio();
		JRadioButton subsetRadio = panel.getSubsetRadio();
		Map<Column, JCheckBox> subsetCheckBoxes = panel.getSubsetCheckBoxes();

		assertTrue(allDataRadio.isEnabled());
		assertTrue(allDataRadio.isSelected());
		assertEquals(1, subsetCheckBoxes.size());
		assertFalse(subsetCheckBoxes.get(intColumn).isSelected());

		profileDescriptor.setNumbersRequired(true);
		columnSelection.toggleColumn(varcharColumn);

		assertFalse(allDataRadio.isEnabled());
		assertFalse(allDataRadio.isSelected());
		assertTrue(subsetRadio.isSelected());
		assertEquals(2, subsetCheckBoxes.size());
		assertTrue(subsetCheckBoxes.get(intColumn).isEnabled());
		assertFalse(subsetCheckBoxes.get(varcharColumn).isEnabled());

		columnSelection.toggleColumn(varcharColumn);
		columnSelection.toggleTable(table2);

		assertFalse(allDataRadio.isEnabled());
		assertFalse(allDataRadio.isSelected());
		assertTrue(subsetRadio.isSelected());
		assertEquals(3, subsetCheckBoxes.size());
		assertTrue(subsetCheckBoxes.get(intColumn).isSelected());
		assertFalse(subsetCheckBoxes.get(varcharColumn).isSelected());

		profileDescriptor.setNumbersRequired(false);
		profileDescriptor.setLiteralsRequired(true);

		columnSelection.toggleTable(table1);

		assertFalse(allDataRadio.isEnabled());
		assertFalse(allDataRadio.isSelected());
		assertTrue(subsetRadio.isSelected());
		assertEquals(3, subsetCheckBoxes.size());

		assertTrue(subsetCheckBoxes.get(varcharColumn).isSelected());
		assertFalse(subsetCheckBoxes.get(dateColumn).isSelected());
		assertTrue(subsetCheckBoxes.get(charColumn).isSelected());
	}
}