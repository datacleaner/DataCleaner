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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import dk.eobjects.datacleaner.catalog.NamedRegex;
import dk.eobjects.datacleaner.data.ColumnSelection;
import dk.eobjects.datacleaner.execution.IJobConfiguration;
import dk.eobjects.datacleaner.gui.GuiHelper;
import dk.eobjects.datacleaner.gui.setup.GuiSettings;
import dk.eobjects.datacleaner.profiler.IProfileDescriptor;
import dk.eobjects.datacleaner.profiler.ProfilerJobConfiguration;
import dk.eobjects.datacleaner.profiler.trivial.RegexProfile;
import dk.eobjects.datacleaner.util.ReflectionHelper;
import dk.eobjects.metamodel.schema.Column;

public class RegexProfileConfigurationPanel implements IConfigurationPanel {

	private JPanel _panel = GuiHelper.createPanel().applyVerticalLayout()
			.toComponent();
	private IProfileDescriptor _descriptor;
	private SubsetDataSelectionPanel _subsetDataSelectionPanel;
	private ProfilerJobConfiguration _jobConfiguration;
	private Map<NamedRegex, JCheckBox> _regexCheckBoxes = new HashMap<NamedRegex, JCheckBox>();

	public void initialize(JTabbedPane tabbedPane, Object descriptor,
			ColumnSelection columnSelection,
			IJobConfiguration configuration) {
		_descriptor = (IProfileDescriptor) descriptor;
		_jobConfiguration = (ProfilerJobConfiguration) configuration;

		_panel.removeAll();

		_subsetDataSelectionPanel = SubsetDataSelectionPanel.createPanel(
				columnSelection, _descriptor);

		JPanel regexPanel = GuiHelper.createPanel().applyTitledBorder(
				"Use regexes").applyVerticalLayout().toComponent();

		List<NamedRegex> namedRegexes = GuiSettings.getSettings().getRegexes();

		List<String> enabledRegexNames = ReflectionHelper
				.getIteratedProperties(RegexProfile.PREFIX_PROPERTY_LABEL,
						_jobConfiguration.getProfileProperties());
		if (enabledRegexNames.isEmpty()) {
			enabledRegexNames = new ArrayList<String>(namedRegexes.size());
			for (NamedRegex regex : namedRegexes) {
				enabledRegexNames.add(regex.getName());
			}
		}
		for (NamedRegex regex : namedRegexes) {
			String regexName = regex.getName();
			JCheckBox checkBox = new JCheckBox(regexName);
			checkBox.setBackground(GuiHelper.BG_COLOR_LIGHT);
			if (enabledRegexNames.contains(regexName)) {
				checkBox.setSelected(true);
			}
			_regexCheckBoxes.put(regex, checkBox);
			regexPanel.add(checkBox);
		}

		JPanel toggleRegexesPanel = GuiHelper.createPanel().toComponent();
		JButton selectAllButton = new JButton("Select all");
		selectAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Collection<JCheckBox> checkBoxes = _regexCheckBoxes.values();
				for (JCheckBox checkBox : checkBoxes) {
					checkBox.setSelected(true);
				}
			}
		});
		toggleRegexesPanel.add(selectAllButton);

		JButton selectNoneButton = new JButton("Select none");
		selectNoneButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Collection<JCheckBox> checkBoxes = _regexCheckBoxes.values();
				for (JCheckBox checkBox : checkBoxes) {
					checkBox.setSelected(false);
				}
			}
		});
		toggleRegexesPanel.add(selectNoneButton);
		regexPanel.add(toggleRegexesPanel);

		GuiHelper.addComponentAligned(_panel, regexPanel);

		Column[] columns = _jobConfiguration.getColumns();
		if (columns != null && columns.length > 0) {
			_subsetDataSelectionPanel.setSelectedColumns(columns);
		}

		GuiHelper.addComponentAligned(_panel, _subsetDataSelectionPanel);
	}

	public JPanel getPanel() {
		return _panel;
	}

	public IJobConfiguration getJobConfiguration() {
		ProfilerJobConfiguration configuration = new ProfilerJobConfiguration(
				_descriptor);
		configuration
				.setColumns(_subsetDataSelectionPanel.getSelectedColumns());

		Map<String, String> properties = configuration.getProfileProperties();
		if (properties == null) {
			properties = new HashMap<String, String>();
		}
		List<String> regexNames = new ArrayList<String>();
		List<String> regexExpressions = new ArrayList<String>();
		for (Entry<NamedRegex, JCheckBox> entry : _regexCheckBoxes.entrySet()) {
			JCheckBox checkBox = entry.getValue();
			if (checkBox.isSelected()) {
				NamedRegex namedRegex = entry.getKey();
				regexNames.add(namedRegex.getName());
				regexExpressions.add(namedRegex.getExpression());
			}
		}
		ReflectionHelper.addIteratedProperties(properties,
				RegexProfile.PREFIX_PROPERTY_LABEL, regexNames
						.toArray(new String[regexNames.size()]));
		ReflectionHelper.addIteratedProperties(properties,
				RegexProfile.PREFIX_PROPERTY_REGEX, regexExpressions
						.toArray(new String[regexExpressions.size()]));

		configuration.setProfileProperties(properties);
		return configuration;
	}

	public void destroy() throws Exception {
	}
}