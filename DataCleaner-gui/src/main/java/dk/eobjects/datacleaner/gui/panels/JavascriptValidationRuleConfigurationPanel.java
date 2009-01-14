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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import dk.eobjects.datacleaner.gui.GuiHelper;
import dk.eobjects.datacleaner.gui.widgets.TableDataSelectionComboBox;
import dk.eobjects.datacleaner.validator.ValidatorJobConfiguration;
import dk.eobjects.datacleaner.validator.condition.JavascriptValidationRule;
import dk.eobjects.metamodel.MetaModelHelper;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Table;
import dk.eobjects.thirdparty.textarea.DefaultInputHandler;
import dk.eobjects.thirdparty.textarea.JEditTextArea;
import dk.eobjects.thirdparty.textarea.tokermarkers.JavaScriptTokenMarker;

public class JavascriptValidationRuleConfigurationPanel extends
		AbstractValidatorConfigurationPanel {

	private static final String EXAMPLE_SCRIPT_PREFIX = "var value = values.get('";
	private static final String EXAMPLE_SCRIPT_SUFFIX = "');\nvalue != null && value != '';";
	private boolean _updateScriptOnColumnSelectionChange = true;
	private JEditTextArea _expressionTextArea;
	private TableDataSelectionComboBox _tableComboBox;

	@Override
	protected void createPanel(JPanel panel,
			ValidatorJobConfiguration configuration) {
		_tableComboBox = new TableDataSelectionComboBox(_columnSelection);
		_tableComboBox.setName("tableComboBox");
		_tableComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateScript();
			}
		});

		_propertiesPanel.addComponentToPanel("Table", _tableComboBox);

		JPanel expressionPanel = GuiHelper.createPanel().applyBorderLayout()
				.applyTitledBorder("Expression").toComponent();
		_expressionTextArea = new JEditTextArea();
		_expressionTextArea.setTokenMarker(new JavaScriptTokenMarker());
		DefaultInputHandler inputHandler = new DefaultInputHandler() {
			@Override
			public void keyTyped(KeyEvent evt) {
				_updateScriptOnColumnSelectionChange = false;
				super.keyTyped(evt);
			}
		};
		inputHandler.addDefaultKeyBindings();
		_expressionTextArea.setInputHandler(inputHandler);

		Column[] columns = configuration.getColumns();
		if (columns != null && columns.length > 0) {
			Table table = columns[0].getTable();
			if (table != null) {
				_tableComboBox.setSelectedTable(table);
			}
		}
		updateScript();
		_expressionTextArea.setBorder(new LineBorder(Color.DARK_GRAY));
		expressionPanel.add(_expressionTextArea, BorderLayout.CENTER);
		GuiHelper.addComponentAligned(panel, expressionPanel);
	}

	@Override
	protected void updateConfiguration() {
		Map<String, String> properties = _jobConfiguration
				.getValidationRuleProperties();
		properties.put(JavascriptValidationRule.PROPERTY_JAVASCRIPT_EXPRESSION,
				_expressionTextArea.getText());

		Table selectedTable = _tableComboBox.getSelectedTable();
		if (selectedTable != null) {
			Column[] tableColumns = MetaModelHelper.getTableColumns(
					selectedTable, _columnSelection.getColumns());
			_jobConfiguration.setColumns(tableColumns);
		}
	}

	@Override
	public void destroy() throws Exception {
	}

	public JEditTextArea getExpressionTextArea() {
		return _expressionTextArea;
	}

	public TableDataSelectionComboBox getTableComboBox() {
		return _tableComboBox;
	}

	private void updateScript() {
		if (_updateScriptOnColumnSelectionChange) {
			String expression = _jobConfiguration
					.getValidationRuleProperties()
					.get(
							JavascriptValidationRule.PROPERTY_JAVASCRIPT_EXPRESSION);
			if (expression == null) {
				// Create example script
				String columnName = "COLUMN_NAME";
				List<Column> columns = _columnSelection.getColumns();
				if (columns.size() > 0) {
					Table table = _tableComboBox.getSelectedTable();
					for (Iterator<Column> it = columns.iterator(); it.hasNext();) {
						Column column = it.next();
						if (table == column.getTable()) {
							columnName = column.getName();
							break;
						}
					}
				}
				_expressionTextArea.setText(EXAMPLE_SCRIPT_PREFIX + columnName
						+ EXAMPLE_SCRIPT_SUFFIX);
				_expressionTextArea.setSelectionStart(EXAMPLE_SCRIPT_PREFIX
						.length());
				_expressionTextArea
						.setSelectionEnd((EXAMPLE_SCRIPT_PREFIX + columnName)
								.length());
			} else {
				_expressionTextArea.setText(expression);
				_updateScriptOnColumnSelectionChange = false;
			}
		}
	}
}