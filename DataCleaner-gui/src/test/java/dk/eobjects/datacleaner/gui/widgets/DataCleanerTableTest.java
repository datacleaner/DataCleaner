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

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.Reader;

import javax.swing.table.DefaultTableModel;

import junit.framework.TestCase;
import dk.eobjects.datacleaner.profiler.MatrixValue;
import dk.eobjects.metamodel.query.Query;
import dk.eobjects.metamodel.schema.Column;

public class DataCleanerTableTest extends TestCase {

	/**
	 * TODO: Ticket #148: Clipboard is overwriten during mvn install
	 * 
	 * @see http://eobjects.org/trac/ticket/148
	 */
	private Reader _previousClipboardReader;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		try {
			DataFlavor dataFlavor = DataFlavor.selectBestTextFlavor(clipboard
					.getAvailableDataFlavors());
			_previousClipboardReader = dataFlavor.getReaderForText(clipboard
					.getContents(null));
		} catch (Exception e) {
			System.out.println("Could not preserve clipboard contents");
			e.printStackTrace();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		if (_previousClipboardReader != null) {
			BufferedReader br = new BufferedReader(_previousClipboardReader);
			StringBuilder sb = new StringBuilder();
			for (String line = br.readLine(); line != null; line = br
					.readLine()) {
				if (sb.length() != 0) {
					sb.append("\n");
				}
				sb.append(line);
			}
			br.close();

			Clipboard clipboard = Toolkit.getDefaultToolkit()
					.getSystemClipboard();
			String string = sb.toString();
			System.out.println("Writing on clipboard: " + string);
			StringSelection stsl = new StringSelection(string);
			clipboard.setContents(stsl, stsl);
		}
	}

	public void testCopyToClipboard() throws Exception {
		DataCleanerTable table = new DataCleanerTable();
		DefaultTableModel model = new DefaultTableModel(new Object[] { "f",
				"o", "o", "b", "a", "r" }, 5);
		for (int i = 0; i < model.getRowCount(); i++) {
			for (int j = 0; j < model.getColumnCount(); j++) {
				int value = i * model.getColumnCount() + j;
				model.setValueAt(value, i, j);
			}
		}
		Query q = new Query().from(
				new dk.eobjects.metamodel.schema.Table("bar")).select(
				new Column("foo"));
		MatrixValue mv = new MatrixValue(0).setDetailSource(q);
		assertEquals("MatrixValue[value=0,detailQuery=SELECT foo FROM bar]", mv
				.toString());
		model.setValueAt(mv, 0, 0);
		model.setValueAt(null, 1, 1);
		table.setModel(model);

		table.copyToClipboard(0, 0, 2, 2);
		assertEquals("0\t1\n6\t<null>\n", Toolkit.getDefaultToolkit()
				.getSystemClipboard().getData(DataFlavor.stringFlavor)
				.toString());

		table.copyToClipboard(0, 5, 1, 2);
		assertEquals("5\n11\n", Toolkit.getDefaultToolkit()
				.getSystemClipboard().getData(DataFlavor.stringFlavor)
				.toString());

		table.copyToClipboard(0, 0, 6, 5);
		assertEquals(
				"f\to\to\tb\tar\n0\t1\t2\t3\t4\t5\n6\t<null>\t8\t9\t10\t11\n12\t13\t14\t15\t16\t17\n18\t19\t20\t21\t22\t23\n24\t25\t26\t27\t28\t29\n",
				Toolkit.getDefaultToolkit().getSystemClipboard().getData(
						DataFlavor.stringFlavor).toString());
	}

	public void testCopyEntireTable() throws Exception {
		DataCleanerTable dcTable = new DataCleanerTable();
		DefaultTableModel model = new DefaultTableModel(new Object[] { "col1",
				"col2", "col3" }, 3);
		model.setValueAt("a", 0, 0);
		model.setValueAt("b", 0, 1);
		model.setValueAt("c", 0, 2);
		model.setValueAt("d", 1, 0);
		model.setValueAt("e", 1, 1);
		model.setValueAt("f", 1, 2);
		model.setValueAt("g", 2, 0);
		model.setValueAt("h", 2, 1);
		model.setValueAt("i", 2, 2);
		dcTable.setModel(model);

		assertEquals("c", dcTable.getValueAt(0, 2));
		
		
		dcTable.copyToClipboard(0, 0, dcTable.getColumnCount(), dcTable.getRowCount());

		assertEquals("col1\tcol2\tcol3\na\tb\tc\nd\te\tf\ng\th\ti\n", Toolkit
				.getDefaultToolkit().getSystemClipboard().getData(
						DataFlavor.stringFlavor).toString());
	}
}