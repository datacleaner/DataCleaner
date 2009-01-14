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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import dk.eobjects.datacleaner.data.ColumnSelection;
import dk.eobjects.datacleaner.execution.IJobConfiguration;
import dk.eobjects.datacleaner.gui.GuiHelper;
import dk.eobjects.datacleaner.profiler.IProfileDescriptor;
import dk.eobjects.datacleaner.profiler.ProfilerJobConfiguration;
import dk.eobjects.datacleaner.profiler.trivial.DateMaskProfile;
import dk.eobjects.datacleaner.util.ReflectionHelper;
import dk.eobjects.metamodel.schema.Column;

public class DateMaskProfileConfigurationPanel implements IConfigurationPanel {

	private JPanel _panel = GuiHelper.createPanel().applyVerticalLayout()
			.toComponent();
	private IProfileDescriptor _descriptor;
	private SubsetDataSelectionPanel _subsetDataSelectionPanel;
	private ProfilerJobConfiguration _jobConfiguration;
	private List<JTextField> _dateMaskFields;
	private JPanel _dateMaskPanel;

	public void initialize(JTabbedPane tabbedPane, Object descriptor,
			ColumnSelection columnSelection,
			IJobConfiguration jobConfiguration) {
		_dateMaskFields = new ArrayList<JTextField>();
		_descriptor = (IProfileDescriptor) descriptor;
		_jobConfiguration = (ProfilerJobConfiguration) jobConfiguration;

		_panel.removeAll();

		_subsetDataSelectionPanel = SubsetDataSelectionPanel.createPanel(
				columnSelection, _descriptor);

		_dateMaskPanel = GuiHelper.createPanel().toComponent();

		List<String> dateMasks = ReflectionHelper.getIteratedProperties(
				DateMaskProfile.PREFIX_PROPERTY_REGEX, _jobConfiguration
						.getProfileProperties());
		if (dateMasks.isEmpty()) {
			// Set up default date masks
			dateMasks.add("yyyy-MM-dd");
			dateMasks.add("yyyy/MM/dd");
			dateMasks.add("dd.MM.yyyy");
			dateMasks.add("dd/MM/yyyy");
			dateMasks.add("MM/dd/yy");
			dateMasks.add("d MMM yyyy HH:mm:ss");
		}

		for (String dateMask : dateMasks) {
			JTextField textField = new JTextField(20);
			textField.setText(dateMask);
			addTextField(textField);
		}

		JPanel splitPanel = GuiHelper.createPanel().applyTitledBorder(
				"Use date masks").toComponent();
		GuiHelper.addToGridBag(_dateMaskPanel, splitPanel, 0, 0, 1, 2);
		JTextArea informationLabel = GuiHelper.createLabelTextArea()
				.applyWhiteBackground().applySize(190, 170).toComponent();
		informationLabel
				.setText("Date masks define the format of date and time fields. "
						+ "The main formatting characters are:\n\n"
						+ "y = year\nM = month\nd = date\nH = hour\nm = minute\ns = second");
		GuiHelper.addToGridBag(GuiHelper.createPanel().applySize(10, null)
				.toComponent(), splitPanel, 1, 0, 1, 2);
		GuiHelper.addToGridBag(informationLabel, splitPanel, 2, 0);
		JButton addDateMaskButton = GuiHelper.createButton("Add date mask",
				"images/toolbar_add.png").toComponent();
		addDateMaskButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addTextField(new JTextField(20));
				_dateMaskPanel.updateUI();
			}
		});
		GuiHelper.addToGridBag(addDateMaskButton, splitPanel, 2, 1);

		GuiHelper.addComponentAligned(_panel, splitPanel);

		Column[] columns = _jobConfiguration.getColumns();
		if (columns != null && columns.length > 0) {
			_subsetDataSelectionPanel.setSelectedColumns(columns);
		}

		GuiHelper.addComponentAligned(_panel, _subsetDataSelectionPanel);
	}

	private void addTextField(final JTextField textField) {
		_dateMaskFields.add(textField);
		GuiHelper.addToGridBag(textField, _dateMaskPanel, 0, _dateMaskFields
				.size() - 1);
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
		List<String> dateMasks = new ArrayList<String>();
		for (JTextField field : _dateMaskFields) {
			String dateMask = field.getText().trim();
			if (!"".equals(dateMask)) {
				dateMasks.add(dateMask);
			}
		}
		ReflectionHelper.addIteratedProperties(properties,
				DateMaskProfile.PREFIX_PROPERTY_REGEX, dateMasks
						.toArray(new String[dateMasks.size()]));

		configuration.setProfileProperties(properties);
		return configuration;
	}

	public void destroy() throws Exception {
	}
}