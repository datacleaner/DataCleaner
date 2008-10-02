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

import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import dk.eobjects.datacleaner.catalog.IDictionary;
import dk.eobjects.datacleaner.gui.GuiHelper;
import dk.eobjects.datacleaner.validator.ValidationRuleConfiguration;
import dk.eobjects.datacleaner.validator.dictionary.DictionaryManager;
import dk.eobjects.datacleaner.validator.dictionary.DictionaryValidationRule;
import dk.eobjects.metamodel.schema.Column;

public class DictionaryValidationRuleConfigurationPanel extends
		AbstractValidatorConfigurationPanel {

	private JComboBox _dictionaryDropDown = new JComboBox();
	private SubsetDataSelectionPanel _subsetDataSelectionPanel;

	@Override
	protected void createPanel(JPanel panel,
			ValidationRuleConfiguration configuration) {
		createDictionaryDropDown();

		_dictionaryDropDown.setName("dictionaryDropDown");
		_propertiesPanel.addComponentToPanel("Dictionary", _dictionaryDropDown);

		_subsetDataSelectionPanel = SubsetDataSelectionPanel.createPanel(
				_columnSelection, _descriptor);

		String dictionaryName = configuration.getValidationRuleProperties()
				.get(DictionaryValidationRule.PROPERTY_DICTIONARY_NAME);
		if (dictionaryName != null) {
			_dictionaryDropDown.setSelectedItem(dictionaryName);
		}

		Column[] columns = configuration.getColumns();
		if (columns != null && columns.length > 0) {
			_subsetDataSelectionPanel.setSelectedColumns(columns);
		}

		GuiHelper.addComponentAligned(panel, _subsetDataSelectionPanel);
	}

	@Override
	protected void updateConfiguration() {
		List<Column> columns = _subsetDataSelectionPanel.getSelectedColumns();
		_configuration.setColumns(columns);
		Object selectedItem = _dictionaryDropDown.getSelectedItem();

		Map<String, String> properties = _configuration
				.getValidationRuleProperties();
		if (selectedItem != null) {
			properties.put(DictionaryValidationRule.PROPERTY_DICTIONARY_NAME,
					selectedItem.toString());
		}
	}

	@Override
	public void destroy() throws Exception {
	}

	private void createDictionaryDropDown() {
		IDictionary[] dictionaries = DictionaryManager.getDictionaries();
		for (int i = 0; i < dictionaries.length; i++) {
			IDictionary d = dictionaries[i];
			String dictionaryName = d.getName();
			if (dictionaryName != null) {
				_dictionaryDropDown.addItem(dictionaryName);
			}
		}
	}

	public JComboBox getDictionaryDropDown() {
		return _dictionaryDropDown;
	}
}