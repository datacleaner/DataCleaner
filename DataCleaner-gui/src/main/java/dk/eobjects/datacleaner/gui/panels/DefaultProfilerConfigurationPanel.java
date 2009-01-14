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
import javax.swing.JTabbedPane;

import dk.eobjects.datacleaner.data.ColumnSelection;
import dk.eobjects.datacleaner.execution.IJobConfiguration;
import dk.eobjects.datacleaner.gui.GuiHelper;
import dk.eobjects.datacleaner.profiler.IProfileDescriptor;
import dk.eobjects.datacleaner.profiler.ProfilerJobConfiguration;
import dk.eobjects.metamodel.schema.Column;

public class DefaultProfilerConfigurationPanel implements IConfigurationPanel {

	private ConfigurationPropertiesPanel _propertiesPanel = new ConfigurationPropertiesPanel(
			"Profile properties");
	private JPanel _panel = GuiHelper.createPanel().applyVerticalLayout()
			.toComponent();
	private IProfileDescriptor _descriptor;
	private SubsetDataSelectionPanel _subsetDataSelectionPanel;
	private ProfilerJobConfiguration _jobConfiguration;

	public void initialize(JTabbedPane tabbedPane, Object descriptor,
			ColumnSelection columnSelection,
			IJobConfiguration configuration) {
		_descriptor = (IProfileDescriptor) descriptor;
		_jobConfiguration = (ProfilerJobConfiguration) configuration;

		_panel.removeAll();

		_subsetDataSelectionPanel = SubsetDataSelectionPanel.createPanel(
				columnSelection, _descriptor);

		String[] propertyNames = _descriptor.getPropertyNames();
		if (propertyNames.length > 0) {
			_propertiesPanel.addManagedProperties(propertyNames);
			_propertiesPanel.updateManagedFields(_jobConfiguration
					.getProfileProperties());
			GuiHelper.addComponentAligned(_panel, _propertiesPanel.getPanel());
		}

		Column[] columns = _jobConfiguration.getColumns();
		if (columns != null && columns.length > 0) {
			_subsetDataSelectionPanel.setSelectedColumns(columns);
		}

		GuiHelper.addComponentAligned(_panel, _subsetDataSelectionPanel);
	}

	public ProfilerJobConfiguration getJobConfiguration() {
		ProfilerJobConfiguration configuration = new ProfilerJobConfiguration(
				_descriptor);
		configuration
				.setColumns(_subsetDataSelectionPanel.getSelectedColumns());
		configuration.setProfileProperties(_propertiesPanel.getProperties());
		return configuration;
	}

	public JPanel getPanel() {
		return _panel;
	}

	public void destroy() throws Exception {
	}
}