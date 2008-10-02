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

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import dk.eobjects.datacleaner.gui.GuiBuilder;
import dk.eobjects.datacleaner.gui.GuiHelper;
import dk.eobjects.datacleaner.gui.setup.GuiSettings;

public class LookAndFeelSettingsPanel extends JPanel {

	private static final long serialVersionUID = 4806801697950234450L;
	private ButtonGroup _buttonGroup;
	private JComboBox _tableLayoutComboBox;

	public LookAndFeelSettingsPanel(GuiSettings settings) {
		super();
		new GuiBuilder<JPanel>(this).applyLightBackground().applyLayout(
				new FlowLayout(FlowLayout.LEFT, 10, 10));

		Dimension d = new Dimension();
		d.width = 430;
		setSize(d);
		setPreferredSize(d);

		setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));

		JLabel header = new JLabel("Look and feel");
		header.setFont(GuiHelper.FONT_HEADER);
		d = new Dimension();
		d.width = 400;
		d.height = 20;
		header.setSize(d);
		header.setPreferredSize(d);
		add(header);

		JPanel buttonPanel = GuiHelper.createPanel().applyBorder()
				.applyVerticalLayout().toComponent();

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
			buttonPanel.add(radioButton);
		}

		add(buttonPanel);

		header = new JLabel("Layout");
		header.setFont(GuiHelper.FONT_HEADER);
		d = new Dimension();
		d.width = 400;
		d.height = 20;
		header.setSize(d);
		header.setPreferredSize(d);
		add(header);

		JPanel matrixTableLayoutPanel = GuiHelper.createPanel().applyBorder()
				.toComponent();
		matrixTableLayoutPanel.add(new JLabel("Profiling result-table layout"));
		_tableLayoutComboBox = new JComboBox(new Object[] { "Horizontal",
				"Vertical" });
		if (!settings.isHorisontalMatrixTables()) {
			_tableLayoutComboBox.setSelectedIndex(1);
		}
		matrixTableLayoutPanel.add(_tableLayoutComboBox);
		add(matrixTableLayoutPanel);
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