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

import junit.framework.TestCase;
import dk.eobjects.datacleaner.data.ColumnSelection;
import dk.eobjects.datacleaner.gui.widgets.DataCleanerTable;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.ColumnType;
import dk.eobjects.metamodel.schema.Table;

public class MetadataPanelTest extends TestCase {

	public void testChangeDataSelection() throws Exception {
		ColumnSelection dataSelection = new ColumnSelection(null);
		MetadataPanel metadataPanel = new MetadataPanel(dataSelection);
		DataCleanerTable jtable = metadataPanel.getTable();
		assertEquals(0, jtable.getRowCount());

		Table table = new Table("my table");
		Column column = new Column("col1", ColumnType.BIGINT).setNullable(true);
		column.setRemarks("foobar");
		column.setNativeType("w00p");
		column.setIndexed(true);
		column.setColumnSize(10);
		table.addColumn(column);
		column.setTable(table);

		dataSelection.toggleColumn(column);
		jtable = metadataPanel.getTable();
		assertEquals(1, jtable.getRowCount());

		assertEquals("col1", jtable.getValueAt(0, MetadataPanel.COLUMN_COLUMN));
		assertEquals("my table", jtable.getValueAt(0,
				MetadataPanel.COLUMN_TABLE));
		assertEquals("BIGINT", jtable.getValueAt(0, MetadataPanel.COLUMN_TYPE)
				.toString());
		assertEquals("w00p", jtable.getValueAt(0,
				MetadataPanel.COLUMN_NATIVE_TYPE));
		assertEquals(10, jtable.getValueAt(0, MetadataPanel.COLUMN_SIZE));
		assertEquals(true, jtable.getValueAt(0, MetadataPanel.COLUMN_INDEXED));
		assertEquals(true, jtable.getValueAt(0, MetadataPanel.COLUMN_NULLABLE));
		assertEquals("foobar", jtable.getValueAt(0,
				MetadataPanel.COLUMN_REMARKS));
	}
}