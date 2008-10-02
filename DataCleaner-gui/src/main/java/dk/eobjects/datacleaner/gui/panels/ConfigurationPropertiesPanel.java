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

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import dk.eobjects.datacleaner.gui.GuiBuilder;
import dk.eobjects.datacleaner.gui.GuiHelper;

public class ConfigurationPropertiesPanel {

	private Map<String, JTextField> _propertyFields = new HashMap<String, JTextField>();
	private JPanel _panel = GuiHelper.createPanel().toComponent();
	private int _rows = 0;

	public ConfigurationPropertiesPanel() {
		_panel.setLayout(new GridBagLayout());
	}

	public ConfigurationPropertiesPanel(String title) {
		this();
		new GuiBuilder<JPanel>(_panel).applyTitledBorder(title);
	}

	public void addManagedProperties(String[] propertyNames) {
		for (String string : propertyNames) {
			addManagedProperty(string);
		}
	}

	protected JTextField addManagedProperty(String propertyName) {
		JTextField textField = _propertyFields.get(propertyName);
		if (textField == null) {
			textField = new JTextField(30);
			textField.setName("managed_property_" + propertyName);
			_propertyFields.put(propertyName, textField);
			addComponentToPanel(propertyName, textField);
		}
		return textField;
	}

	public void updateManagedFields(Map<String, String> properties) {
		Set<Entry<String, String>> entrySet = properties.entrySet();
		for (Entry<String, String> entry : entrySet) {
			JTextField textField = _propertyFields.get(entry.getKey());
			if (textField != null) {
				textField.setText(entry.getValue());
			}
		}
	}

	public void addComponentToPanel(String label, Component component) {
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridy = _rows;
		constraints.ipadx = 10;
		constraints.gridx = 0;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		_panel.add(new JLabel(label), constraints);
		constraints.gridx = 1;
		_panel.add(component, constraints);
		_rows++;
	}

	public Map<String, String> getProperties() {
		Map<String, String> properties = new HashMap<String, String>();
		Set<Entry<String, JTextField>> entrySet = _propertyFields.entrySet();
		for (Entry<String, JTextField> entry : entrySet) {
			properties.put(entry.getKey(), entry.getValue().getText());
		}
		return properties;
	}

	public JTextField getFieldForProperty(String propertyName) {
		return _propertyFields.get(propertyName);
	}

	public JPanel getPanel() {
		return _panel;
	}
}