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
package dk.eobjects.datacleaner.gui.widgets;

import javax.swing.table.TableModel;

import junit.framework.TestCase;
import dk.eobjects.datacleaner.gui.setup.GuiSettings;
import dk.eobjects.datacleaner.profiler.IMatrix;
import dk.eobjects.datacleaner.profiler.MatrixBuilder;

public class MatrixTableTest extends TestCase {

	public void testUpdateTableModel() throws Exception {
		MatrixBuilder mb = new MatrixBuilder();
		mb.addColumn("col1");
		mb.addColumn("col2");
		mb.addColumn("col3");
		mb.addRow("measure1", 1, 2, 3);
		mb.addRow("measure2", 4, 5, 6);
		mb.addRow("measure3", 7, 8, 9);
		mb.addRow("measure4", 10, 11, 12);
		mb.addRow("measure5", 13, 14, 15);

		IMatrix matrix = mb.getMatrix();

		GuiSettings.getSettings().setHorisontalMatrixTables(true);
		MatrixTable table = new MatrixTable(matrix, null);

		TableModel model = table.getModel();
		assertEquals(5, model.getRowCount());
		assertEquals(4, model.getColumnCount());
		assertEquals("1", model.getValueAt(0, 1).toString());
		assertEquals("2", model.getValueAt(0, 2).toString());
		assertEquals("3", model.getValueAt(0, 3).toString());
		assertEquals("4", model.getValueAt(1, 1).toString());
		assertEquals("5", model.getValueAt(1, 2).toString());
		assertEquals("6", model.getValueAt(1, 3).toString());
		assertEquals("7", model.getValueAt(2, 1).toString());
		assertEquals("8", model.getValueAt(2, 2).toString());
		assertEquals("9", model.getValueAt(2, 3).toString());
		assertEquals("10", model.getValueAt(3, 1).toString());
		assertEquals("11", model.getValueAt(3, 2).toString());
		assertEquals("12", model.getValueAt(3, 3).toString());
		assertEquals("13", model.getValueAt(4, 1).toString());
		assertEquals("14", model.getValueAt(4, 2).toString());
		assertEquals("15", model.getValueAt(4, 3).toString());

		table.updateTableModel(false);
		model = table.getModel();
		assertEquals(3, model.getRowCount());
		assertEquals(6, model.getColumnCount());
		assertEquals("1", model.getValueAt(0, 1).toString());
		assertEquals("4", model.getValueAt(0, 2).toString());
		assertEquals("7", model.getValueAt(0, 3).toString());
		assertEquals("10", model.getValueAt(0, 4).toString());
		assertEquals("13", model.getValueAt(0, 5).toString());
		assertEquals("2", model.getValueAt(1, 1).toString());
		assertEquals("5", model.getValueAt(1, 2).toString());
		assertEquals("8", model.getValueAt(1, 3).toString());
		assertEquals("11", model.getValueAt(1, 4).toString());
		assertEquals("14", model.getValueAt(1, 5).toString());
		assertEquals("3", model.getValueAt(2, 1).toString());
		assertEquals("6", model.getValueAt(2, 2).toString());
		assertEquals("9", model.getValueAt(2, 3).toString());
		assertEquals("12", model.getValueAt(2, 4).toString());
		assertEquals("15", model.getValueAt(2, 5).toString());
	}
}