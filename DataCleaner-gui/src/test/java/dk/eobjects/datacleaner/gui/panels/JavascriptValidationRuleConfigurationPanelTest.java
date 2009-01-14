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

import java.awt.event.KeyEvent;

import javax.swing.JTabbedPane;

import junit.framework.TestCase;
import dk.eobjects.datacleaner.data.ColumnSelection;
import dk.eobjects.datacleaner.gui.widgets.TableDataSelectionComboBox;
import dk.eobjects.datacleaner.validator.BasicValidationRuleDescriptor;
import dk.eobjects.datacleaner.validator.IValidationRuleDescriptor;
import dk.eobjects.datacleaner.validator.ValidatorJobConfiguration;
import dk.eobjects.datacleaner.validator.condition.JavascriptValidationRule;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.ColumnType;
import dk.eobjects.metamodel.schema.Table;
import dk.eobjects.thirdparty.textarea.JEditTextArea;

public class JavascriptValidationRuleConfigurationPanelTest extends TestCase {

	/**
	 * Tests that the column name of the example script matches the selected
	 * table
	 */
	public void testTableSelection() throws Exception {
		JTabbedPane tabbedPane = new JTabbedPane();
		IValidationRuleDescriptor descriptor = new BasicValidationRuleDescriptor(
				"Javascript evaluation", JavascriptValidationRule.class);
		ColumnSelection columnSelection = new ColumnSelection(null);

		Table table1 = new Table("table1");
		Table table2 = new Table("table2");
		Column col1 = new Column("col1", ColumnType.VARCHAR);
		Column col2 = new Column("col2", ColumnType.VARCHAR);
		Column col3 = new Column("col3", ColumnType.VARCHAR);
		table1.addColumn(col1);
		col1.setTable(table1);
		table2.addColumn(col2);
		col2.setTable(table2);
		table2.addColumn(col3);
		col3.setTable(table2);

		JavascriptValidationRuleConfigurationPanel panel = new JavascriptValidationRuleConfigurationPanel();
		panel.initialize(tabbedPane, descriptor, columnSelection,
				new ValidatorJobConfiguration(descriptor));
		panel.getPanel();

		JEditTextArea expressionTextArea = panel.getExpressionTextArea();
		TableDataSelectionComboBox tableComboBox = panel.getTableComboBox();

		assertEquals(
				"var value = values.get('COLUMN_NAME');\nvalue != null && value != '';",
				expressionTextArea.getText());
		assertEquals(0, tableComboBox.getItemCount());

		columnSelection.toggleColumn(col1);
		assertEquals(1, tableComboBox.getItemCount());
		columnSelection.toggleColumn(col2);
		assertEquals(2, tableComboBox.getItemCount());
		columnSelection.toggleColumn(col3);
		assertEquals(2, tableComboBox.getItemCount());
		assertEquals("table1", tableComboBox.getItemAt(0));
		assertEquals("table2", tableComboBox.getItemAt(1));
		assertEquals("table1", tableComboBox.getSelectedItem());

		assertEquals(
				"var value = values.get('col1');\nvalue != null && value != '';",
				expressionTextArea.getText());

		tableComboBox.setSelectedIndex(1);

		assertEquals(
				"var value = values.get('col2');\nvalue != null && value != '';",
				expressionTextArea.getText());
	}

	/**
	 * Ticket #167: Javascript editor content gets wiped out when column
	 * selection changes.
	 */
	public void testChangeInColumnSelection() throws Exception {
		JTabbedPane tabbedPane = new JTabbedPane();
		IValidationRuleDescriptor descriptor = new BasicValidationRuleDescriptor(
				"Javascript evaluation", JavascriptValidationRule.class);
		ColumnSelection columnSelection = new ColumnSelection(null);

		Table table1 = new Table("table1");
		Table table2 = new Table("table2");
		Column col1 = new Column("col1", ColumnType.VARCHAR);
		Column col2 = new Column("col2", ColumnType.VARCHAR);
		Column col3 = new Column("col3", ColumnType.VARCHAR);
		table1.addColumn(col1);
		col1.setTable(table1);
		table2.addColumn(col2);
		col2.setTable(table2);
		table2.addColumn(col3);
		col3.setTable(table2);

		columnSelection.toggleTable(table1);
		columnSelection.toggleTable(table2);

		JavascriptValidationRuleConfigurationPanel panel = new JavascriptValidationRuleConfigurationPanel();
		panel.initialize(tabbedPane, descriptor, columnSelection,
				new ValidatorJobConfiguration(descriptor));
		panel.getPanel();
		JEditTextArea expressionTextArea = panel.getExpressionTextArea();

		assertEquals(
				"var value = values.get('col1');\nvalue != null && value != '';",
				expressionTextArea.getText());

		columnSelection.toggleColumn(col1);

		assertEquals(
				"var value = values.get('col2');\nvalue != null && value != '';",
				expressionTextArea.getText());

		expressionTextArea.setText("fooba");
		KeyEvent event = new KeyEvent(expressionTextArea, KeyEvent.KEY_TYPED,
				System.currentTimeMillis(), KeyEvent.SHIFT_DOWN_MASK,
				KeyEvent.VK_UNDEFINED, 'r');
		expressionTextArea.processKeyEvent(event);
		assertEquals("foobar", expressionTextArea.getText());

		columnSelection.toggleColumn(col2);
		assertEquals("foobar", expressionTextArea.getText());
	}
}