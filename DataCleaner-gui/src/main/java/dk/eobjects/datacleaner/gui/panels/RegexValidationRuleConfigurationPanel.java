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

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import dk.eobjects.datacleaner.catalog.NamedRegex;
import dk.eobjects.datacleaner.gui.GuiHelper;
import dk.eobjects.datacleaner.gui.dialogs.TestRegexDialog;
import dk.eobjects.datacleaner.gui.setup.GuiSettings;
import dk.eobjects.datacleaner.util.ReflectionHelper;
import dk.eobjects.datacleaner.validator.ValidatorJobConfiguration;
import dk.eobjects.datacleaner.validator.trivial.RegexValidationRule;
import dk.eobjects.metamodel.schema.Column;

public class RegexValidationRuleConfigurationPanel extends
		AbstractValidatorConfigurationPanel {

	private SubsetDataSelectionPanel _subsetDataSelectionPanel;
	private JTextField _expressionField;
	private JButton _testButton;

	@Override
	protected void createPanel(JPanel panel,
			ValidatorJobConfiguration configuration) {
		JPanel expressionPanel = GuiHelper.createPanel().applyLightBackground()
				.applyLayout(new FlowLayout(FlowLayout.LEFT, 0, 0))
				.toComponent();
		_expressionField = new JTextField(30);
		_expressionField.setName("expressionField");
		expressionPanel.add(_expressionField);
		String expression = configuration.getValidationRuleProperties().get(
				RegexValidationRule.PROPERTY_REGEX);
		if (expression != null) {
			_expressionField.setText(expression);
		}

		_testButton = new JButton("Test it");
		_testButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TestRegexDialog dialog = new TestRegexDialog(_expressionField
						.getText());
				dialog.setVisible(true);
			}
		});
		expressionPanel.add(_testButton);

		_propertiesPanel.addComponentToPanel("Expression", expressionPanel);

		final List<NamedRegex> regexes = GuiSettings.getSettings().getRegexes();
		Object[] items = ReflectionHelper.getProperties(regexes, "name");
		ArrayList<Object> list = new ArrayList<Object>(Arrays.asList(items));
		list.add(0, "-");
		final JComboBox regexCatalogDropdown = new JComboBox(list.toArray());
		regexCatalogDropdown.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index = regexCatalogDropdown.getSelectedIndex();
				if (index == 0) {
					_expressionField.setText("");
				} else {
					NamedRegex regex = regexes.get(index - 1);
					_expressionField.setText(regex.getExpression());
				}
			}
		});

		_propertiesPanel.addComponentToPanel("Select from catalog...",
				regexCatalogDropdown);

		_subsetDataSelectionPanel = SubsetDataSelectionPanel.createPanel(
				_columnSelection, _descriptor);

		Column[] columns = configuration.getColumns();
		if (columns != null && columns.length > 0) {
			_subsetDataSelectionPanel.setSelectedColumns(columns);
		}
		GuiHelper.addComponentAligned(panel, _subsetDataSelectionPanel);
	}

	@Override
	protected void updateConfiguration() {
		List<Column> columns = _subsetDataSelectionPanel.getSelectedColumns();
		_jobConfiguration.setColumns(columns);
		Map<String, String> properties = _jobConfiguration
				.getValidationRuleProperties();
		properties.put(RegexValidationRule.PROPERTY_REGEX, _expressionField
				.getText());
	}

	@Override
	public void destroy() throws Exception {
	}
}