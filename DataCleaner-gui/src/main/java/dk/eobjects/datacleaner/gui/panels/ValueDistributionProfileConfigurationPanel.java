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

import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dk.eobjects.datacleaner.data.ColumnSelection;
import dk.eobjects.datacleaner.execution.IRunnableConfiguration;
import dk.eobjects.datacleaner.gui.GuiBuilder;
import dk.eobjects.datacleaner.gui.GuiHelper;
import dk.eobjects.datacleaner.profiler.IProfileDescriptor;
import dk.eobjects.datacleaner.profiler.ProfileConfiguration;
import dk.eobjects.datacleaner.profiler.trivial.ValueDistributionProfile;

public class ValueDistributionProfileConfigurationPanel implements
		IConfigurationPanel {

	protected final Log _log = LogFactory.getLog(getClass());
	private JPanel _panel = GuiHelper.createPanel().applyVerticalLayout()
			.toComponent();
	private IProfileDescriptor _descriptor;
	private ProfileConfiguration _configuration;
	private SubsetDataSelectionPanel _subsetDataSelectionPanel;
	private JSlider _topSlider;
	private JSlider _bottomSlider;
	private JLabel _topValueLabel;
	private JLabel _bottomValueLabel;
	private JCheckBox _fixedTopBottomCheckbox;

	public void initialize(JTabbedPane tabbedPane, Object descriptor,
			ColumnSelection columnSelection,
			IRunnableConfiguration configuration) {
		_descriptor = (IProfileDescriptor) descriptor;
		_configuration = (ProfileConfiguration) configuration;

		_subsetDataSelectionPanel = SubsetDataSelectionPanel.createPanel(
				columnSelection, _descriptor);

		// Panel for property sliders
		JPanel sliderPanel = GuiHelper.createPanel().applyBorder()
				.toComponent();
		_fixedTopBottomCheckbox = GuiHelper.createCheckBox(
				"Fixed distribution of top/bottom N values?", true)
				.toComponent();
		GuiHelper
				.addToGridBag(_fixedTopBottomCheckbox, sliderPanel, 0, 0, 4, 1);

		// PROPERTY_TOP_N slider
		_topValueLabel = new JLabel("5");
		_topSlider = new GuiBuilder<JSlider>(new JSlider(0, 50, 5))
				.applyLightBackground().toComponent();
		_topSlider.setPaintLabels(true);
		_topSlider.addChangeListener(new UpdateLabelChangeListener(
				_topValueLabel));
		GuiHelper.addToGridBag(new JLabel(
				ValueDistributionProfile.PROPERTY_TOP_N), sliderPanel, 1, 1);
		GuiHelper.addToGridBag(_topSlider, sliderPanel, 2, 1);
		GuiHelper.addToGridBag(_topValueLabel, sliderPanel, 3, 1);

		// PROPERTY_BOTTOM_N slider
		_bottomValueLabel = new JLabel("5");
		_bottomSlider = new GuiBuilder<JSlider>(new JSlider(0, 50, 5))
				.applyLightBackground().toComponent();
		_bottomSlider.setPaintLabels(true);
		_bottomSlider.addChangeListener(new UpdateLabelChangeListener(
				_bottomValueLabel));
		GuiHelper.addToGridBag(new JLabel(
				ValueDistributionProfile.PROPERTY_BOTTOM_N), sliderPanel, 1, 2);
		GuiHelper.addToGridBag(_bottomSlider, sliderPanel, 2, 2);
		GuiHelper.addToGridBag(_bottomValueLabel, sliderPanel, 3, 2);

		GuiHelper.addComponentAligned(_panel, sliderPanel);
		GuiHelper.addComponentAligned(_panel, _subsetDataSelectionPanel);

		_fixedTopBottomCheckbox.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JCheckBox checkBox = (JCheckBox) e.getSource();
				boolean selected = checkBox.isSelected();
				_topSlider.setEnabled(selected);
				_topValueLabel.setEnabled(selected);
				_bottomSlider.setEnabled(selected);
				_bottomValueLabel.setEnabled(selected);
			}
		});

		Map<String, String> properties = _configuration.getProfileProperties();
		String topValueString = properties
				.get(ValueDistributionProfile.PROPERTY_TOP_N);
		if (topValueString != null) {
			try {
				_topSlider.setValue(Integer.parseInt(topValueString));
			} catch (NumberFormatException e) {
				_log.warn("Couldn't parse TOP_N property", e);
			}
		}
		String bottomValueString = properties
				.get(ValueDistributionProfile.PROPERTY_BOTTOM_N);
		if (bottomValueString != null) {
			try {
				_bottomSlider.setValue(Integer.parseInt(bottomValueString));
			} catch (NumberFormatException e) {
				_log.warn("Couldn't parse BOTTOM_N property", e);
			}
		}
	}

	public IRunnableConfiguration getConfiguration() {
		ProfileConfiguration configuration = new ProfileConfiguration(
				_descriptor);
		configuration
				.setColumns(_subsetDataSelectionPanel.getSelectedColumns());

		Map<String, String> properties = configuration.getProfileProperties();
		if (properties == null) {
			properties = new HashMap<String, String>();
		}

		if (_fixedTopBottomCheckbox.isSelected()) {
			properties.put(ValueDistributionProfile.PROPERTY_TOP_N,
					_topValueLabel.getText());
			properties.put(ValueDistributionProfile.PROPERTY_BOTTOM_N,
					_bottomValueLabel.getText());
		}

		configuration.setProfileProperties(properties);
		return configuration;
	}

	public JPanel getPanel() {
		return _panel;
	}

	public void destroy() throws Exception {
	}

	private final class UpdateLabelChangeListener implements ChangeListener {

		private JLabel _label;

		public UpdateLabelChangeListener(JLabel label) {
			_label = label;
		}

		public void stateChanged(ChangeEvent e) {
			JSlider slider = (JSlider) e.getSource();
			int value = slider.getValue();
			_label.setText(Integer.toString(value));
		}
	}
}