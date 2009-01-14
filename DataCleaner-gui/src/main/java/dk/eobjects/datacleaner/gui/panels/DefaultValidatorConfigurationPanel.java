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

import javax.swing.JPanel;

import dk.eobjects.datacleaner.gui.GuiHelper;
import dk.eobjects.datacleaner.validator.ValidatorJobConfiguration;
import dk.eobjects.metamodel.schema.Column;

public class DefaultValidatorConfigurationPanel extends
		AbstractValidatorConfigurationPanel {

	private SubsetDataSelectionPanel _subsetDataSelectionPanel;

	@Override
	protected void createPanel(JPanel panel,
			ValidatorJobConfiguration configuration) {
		_subsetDataSelectionPanel = SubsetDataSelectionPanel.createPanel(
				_columnSelection, _descriptor);
		_propertiesPanel.addManagedProperties(_descriptor.getPropertyNames());
		_propertiesPanel.updateManagedFields(configuration
				.getValidationRuleProperties());

		Column[] columns = _jobConfiguration.getColumns();
		if (columns != null && columns.length > 0) {
			_subsetDataSelectionPanel.setSelectedColumns(columns);
		}

		GuiHelper.addComponentAligned(panel, _subsetDataSelectionPanel);
	}

	@Override
	public void destroy() throws Exception {
	}

	@Override
	protected void updateConfiguration() {
		_jobConfiguration.setColumns(_subsetDataSelectionPanel
				.getSelectedColumns());
	}
}