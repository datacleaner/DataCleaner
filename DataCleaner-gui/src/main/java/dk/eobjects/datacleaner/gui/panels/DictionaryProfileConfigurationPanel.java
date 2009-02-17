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

import dk.eobjects.datacleaner.catalog.IDictionary;
import dk.eobjects.datacleaner.data.ColumnSelection;
import dk.eobjects.datacleaner.execution.IJobConfiguration;
import dk.eobjects.datacleaner.gui.GuiHelper;
import dk.eobjects.datacleaner.gui.setup.GuiSettings;
import dk.eobjects.datacleaner.profiler.IProfileDescriptor;
import dk.eobjects.datacleaner.profiler.ProfilerJobConfiguration;
import dk.eobjects.datacleaner.profiler.trivial.DictionaryProfile;
import dk.eobjects.datacleaner.util.ReflectionHelper;
import dk.eobjects.metamodel.schema.Column;

public class DictionaryProfileConfigurationPanel implements IConfigurationPanel {

	private JPanel _panel = GuiHelper.createPanel().applyVerticalLayout()
			.toComponent();
	private IProfileDescriptor _descriptor;
	private SubsetDataSelectionPanel _subsetDataSelectionPanel;
	private ProfilerJobConfiguration _jobConfiguration;
	private Map<IDictionary, JCheckBox> _dictionaryCheckBoxes = new HashMap<IDictionary, JCheckBox>();

	public void initialize(JTabbedPane tabbedPane, Object descriptor,
			ColumnSelection columnSelection, IJobConfiguration configuration) {
		_descriptor = (IProfileDescriptor) descriptor;
		_jobConfiguration = (ProfilerJobConfiguration) configuration;

		_panel.removeAll();

		_subsetDataSelectionPanel = SubsetDataSelectionPanel.createPanel(
				columnSelection, _descriptor);

		JPanel dictionaryPanel = GuiHelper.createPanel().applyTitledBorder(
				"Use dictionaries").applyVerticalLayout().toComponent();

		List<IDictionary> dictionaries = GuiSettings.getSettings()
				.getDictionaries();

		List<String> enabledDictionaryNames = ReflectionHelper
				.getIteratedProperties(
						DictionaryProfile.PREFIX_PROPERTY_DICTIONARY,
						_jobConfiguration.getProfileProperties());
		if (enabledDictionaryNames.isEmpty()) {
			enabledDictionaryNames = new ArrayList<String>(dictionaries.size());
			for (IDictionary dictionary : dictionaries) {
				enabledDictionaryNames.add(dictionary.getName());
			}
		}
		for (IDictionary dictionary : dictionaries) {
			String dictionaryName = dictionary.getName();
			JCheckBox checkBox = new JCheckBox(dictionaryName);
			checkBox.setBackground(GuiHelper.BG_COLOR_LIGHT);
			if (enabledDictionaryNames.contains(dictionaryName)) {
				checkBox.setSelected(true);
			}
			_dictionaryCheckBoxes.put(dictionary, checkBox);
			dictionaryPanel.add(checkBox);
		}

		JPanel toggleDictionariesPanel = GuiHelper.createPanel().toComponent();
		JButton selectAllDictionariesButton = new JButton("Select all");
		selectAllDictionariesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Collection<JCheckBox> checkBoxes = _dictionaryCheckBoxes
						.values();
				for (JCheckBox checkBox : checkBoxes) {
					checkBox.setSelected(true);
				}
			}
		});
		toggleDictionariesPanel.add(selectAllDictionariesButton);

		JButton selectNoneDictionariesButton = new JButton("Select none");
		selectNoneDictionariesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Collection<JCheckBox> checkBoxes = _dictionaryCheckBoxes
						.values();
				for (JCheckBox checkBox : checkBoxes) {
					checkBox.setSelected(false);
				}
			}
		});
		toggleDictionariesPanel.add(selectNoneDictionariesButton);
		dictionaryPanel.add(toggleDictionariesPanel);

		GuiHelper.addComponentAligned(_panel, dictionaryPanel);

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
		List<String> dictionaryNames = new ArrayList<String>();
		for (Entry<IDictionary, JCheckBox> entry : _dictionaryCheckBoxes
				.entrySet()) {
			JCheckBox checkBox = entry.getValue();
			if (checkBox.isSelected()) {
				IDictionary dictionary = entry.getKey();
				dictionaryNames.add(dictionary.getName());
			}
		}
		ReflectionHelper.addIteratedProperties(properties,
				DictionaryProfile.PREFIX_PROPERTY_DICTIONARY, dictionaryNames
						.toArray(new String[dictionaryNames.size()]));

		configuration.setProfileProperties(properties);
		return configuration;
	}

	public void destroy() throws Exception {
	}
}