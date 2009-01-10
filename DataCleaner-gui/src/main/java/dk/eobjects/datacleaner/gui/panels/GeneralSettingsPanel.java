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

import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import dk.eobjects.datacleaner.gui.GuiBuilder;
import dk.eobjects.datacleaner.gui.GuiHelper;
import dk.eobjects.datacleaner.gui.setup.GuiSettings;

public class GeneralSettingsPanel extends JPanel {

	private static final long serialVersionUID = 4806801697950234450L;
	private ButtonGroup _buttonGroup;
	private JComboBox _tableLayoutComboBox;
	private JTextField _usernameField;
	private JButton _clearUsernameButton;

	public GeneralSettingsPanel(GuiSettings settings) {
		super();
		new GuiBuilder<JPanel>(this).applyLightBackground()
				.applyVerticalLayout();

		// User registration
		JPanel userRegistrationPanel = GuiHelper.createPanel()
				.applyTitledBorder("User registration").toComponent();
		userRegistrationPanel.add(new JLabel("Registered user:"));

		_usernameField = new JTextField(settings.getUsername(), 18);
		_usernameField.setEnabled(false);
		userRegistrationPanel.add(_usernameField);

		_clearUsernameButton = new JButton(GuiHelper
				.getImageIcon("images/toolbar_remove.png"));
		_clearUsernameButton.setToolTipText("Clear user registration");
		_clearUsernameButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				GuiSettings settings = GuiSettings.getSettings();
				settings.setUsername(null);
				GuiSettings.saveSettings(settings);
				_usernameField.setText("");
			}
		});
		if (settings.getUsername() == null) {
			_clearUsernameButton.setEnabled(false);
		}
		userRegistrationPanel.add(_clearUsernameButton);

		add(userRegistrationPanel);

		// Layout
		JPanel matrixTableLayoutPanel = GuiHelper.createPanel()
				.applyTitledBorder("Layout").toComponent();
		matrixTableLayoutPanel
				.add(new JLabel("Profiling result-table layout:"));
		_tableLayoutComboBox = new JComboBox(new Object[] { "Horizontal",
				"Vertical" });
		if (!settings.isHorisontalMatrixTables()) {
			_tableLayoutComboBox.setSelectedIndex(1);
		}
		matrixTableLayoutPanel.add(_tableLayoutComboBox);
		add(matrixTableLayoutPanel);

		// Look and feel
		JPanel lookAndFeelPanel = GuiHelper.createPanel().applyTitledBorder(
				"Look and feel").toComponent();
		_buttonGroup = new ButtonGroup();
		String selectedLookAndFeelClassName = settings
				.getLookAndFeelClassName();
		final LookAndFeelInfo[] lookAndFeelInfos = UIManager
				.getInstalledLookAndFeels();
		for (int i = 0; i < lookAndFeelInfos.length; i++) {
			String lafName = lookAndFeelInfos[i].getName();
			String lafClassName = lookAndFeelInfos[i].getClassName();
			JRadioButton radioButton = GuiHelper.createRadio(lafName,
					_buttonGroup).toComponent();
			if (lafClassName.equals(selectedLookAndFeelClassName)) {
				radioButton.setSelected(true);
			}
			radioButton.setActionCommand(lookAndFeelInfos[i].getClassName());
			GuiHelper.addToGridBag(radioButton, lookAndFeelPanel, 0, i);
		}

		add(lookAndFeelPanel);
	}

	public boolean isTableLayoutHorizontal() {
		return _tableLayoutComboBox.getSelectedIndex() == 0;
	}

	public String getLookAndFeelClassName() {
		ButtonModel selection = _buttonGroup.getSelection();
		String newLookAndFeelClassName = selection.getActionCommand();
		return newLookAndFeelClassName;
	}
}