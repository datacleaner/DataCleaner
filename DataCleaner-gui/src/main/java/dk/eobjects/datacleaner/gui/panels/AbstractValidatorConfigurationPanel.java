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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import dk.eobjects.datacleaner.data.ColumnSelection;
import dk.eobjects.datacleaner.execution.IJobConfiguration;
import dk.eobjects.datacleaner.gui.GuiHelper;
import dk.eobjects.datacleaner.validator.IValidationRule;
import dk.eobjects.datacleaner.validator.IValidationRuleDescriptor;
import dk.eobjects.datacleaner.validator.ValidatorJobConfiguration;

public abstract class AbstractValidatorConfigurationPanel implements
		IConfigurationPanel {

	private JPanel _panel = GuiHelper.createPanel().applyVerticalLayout()
			.toComponent();
	protected IValidationRuleDescriptor _descriptor;
	protected ColumnSelection _columnSelection;
	protected ValidatorJobConfiguration _jobConfiguration;
	protected ConfigurationPropertiesPanel _propertiesPanel = new ConfigurationPropertiesPanel(
			"Validation rule properties");
	protected JTabbedPane _tabbedPane;
	protected JTextField _nameField;

	private ActionListener _updateNameAction = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			String name = _nameField.getText().trim();
			if (name.length() > 0) {
				updateTabTitle(name);
			}
		}
	};

	private KeyListener _updateNameKeyListener = new KeyListener() {

		public void keyPressed(KeyEvent e) {
		}

		public void keyReleased(KeyEvent e) {
			_updateNameAction.actionPerformed(null);
		}

		public void keyTyped(KeyEvent e) {
		}
	};

	public void destroy() throws Exception {
		_descriptor = null;
		_columnSelection = null;
		_jobConfiguration = null;
		_nameField.removeActionListener(_updateNameAction);
		_nameField.removeKeyListener(_updateNameKeyListener);
	}

	public void initialize(JTabbedPane tabbedPane, Object descriptor,
			ColumnSelection columnSelection, IJobConfiguration jobConfiguration) {
		_tabbedPane = tabbedPane;
		_descriptor = (IValidationRuleDescriptor) descriptor;
		_columnSelection = columnSelection;
		_jobConfiguration = (ValidatorJobConfiguration) jobConfiguration;
	}

	public JPanel getPanel() {
		_panel.removeAll();
		initNameField();
		_propertiesPanel.updateManagedFields(_jobConfiguration
				.getValidationRuleProperties());
		GuiHelper.addComponentAligned(_panel, _propertiesPanel.getPanel());
		createPanel(_panel, _jobConfiguration);
		return _panel;
	}

	private void initNameField() {
		_nameField = _propertiesPanel
				.addManagedProperty(IValidationRule.PROPERTY_NAME);
		_nameField.setText(_descriptor.getDisplayName());
		_nameField.addActionListener(_updateNameAction);
		_nameField.addKeyListener(_updateNameKeyListener);
	}

	protected abstract void createPanel(JPanel panel,
			ValidatorJobConfiguration configuration);

	public ValidatorJobConfiguration getJobConfiguration() {
		_jobConfiguration.setValidationRuleProperties(_propertiesPanel
				.getProperties());
		updateConfiguration();
		return _jobConfiguration;
	}

	public void updateTabTitle(String newTitle) {
		Component[] components = _tabbedPane.getComponents();
		for (int i = 0; i < components.length; i++) {
			Component component = components[i];
			if (component instanceof JScrollPane) {
				component = ((JScrollPane) component).getViewport()
						.getComponent(0);
			}
			if (component == _panel) {
				_tabbedPane.setTitleAt(i, newTitle);
				break;
			}
		}
	}

	protected abstract void updateConfiguration();
}