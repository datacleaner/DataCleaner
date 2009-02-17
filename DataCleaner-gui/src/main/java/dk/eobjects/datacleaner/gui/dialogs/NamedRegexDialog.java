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
package dk.eobjects.datacleaner.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import dk.eobjects.datacleaner.catalog.NamedRegex;
import dk.eobjects.datacleaner.gui.GuiHelper;
import dk.eobjects.datacleaner.gui.setup.GuiSettings;

public class NamedRegexDialog extends BanneredDialog {

	private static final long serialVersionUID = -2434857404861203054L;
	private NamedRegex _regex;
	private JTextField _nameField;
	private JTextField _expressionField;
	private JButton _testButton;

	public NamedRegexDialog(String name, String expression) {
		this(null);
		_nameField.setText(name);
		_expressionField.setText(expression);
	}

	public NamedRegexDialog(NamedRegex regex) {
		super(500, 350);

		JTextArea aboutRegexes = GuiHelper.createLabelTextArea().toComponent();
		aboutRegexes
				.setText("A regex (regular expression) is a concise and flexible means for identifying strings of text of interest, such as particular characters, words, or patterns of characters. The registered regexes can be used to identify certain types of strings and validate their pattern-correctness.");
		add(aboutRegexes, BorderLayout.SOUTH);

		_regex = regex;
		updateDialog();
	}

	private void updateDialog() {
		if (_regex != null) {
			_nameField.setText(_regex.getName());
			_expressionField.setText(_regex.getExpression());
		}
	}

	@Override
	protected Component getContent() {
		JPanel panel = GuiHelper.createPanel().toComponent();
		JLabel header = new JLabel("Regex");
		header.setFont(GuiHelper.FONT_HEADER);
		GuiHelper.addToGridBag(header, panel, 0, 0, 2, 1);

		GuiHelper.addToGridBag(new JLabel("Regex name:"), panel, 0, 1);
		_nameField = new JTextField();
		_nameField.setPreferredSize(new Dimension(200, 20));
		GuiHelper.addToGridBag(_nameField, panel, 1, 1);

		GuiHelper.addToGridBag(new JLabel("Expression:"), panel, 0, 2);
		_expressionField = new JTextField();
		_expressionField.setPreferredSize(new Dimension(200, 20));
		GuiHelper.addToGridBag(_expressionField, panel, 1, 2);

		_testButton = new JButton("Test it");
		_testButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TestRegexDialog dialog = new TestRegexDialog(_expressionField
						.getText());
				dialog.setVisible(true);
			}
		});
		GuiHelper.addToGridBag(_testButton, panel, 2, 2);

		JButton saveButton = new JButton("Save regex", GuiHelper
				.getImageIcon("images/regexes.png"));
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String name = _nameField.getText();
				String expression = _expressionField.getText();
				if (name.length() > 2) {
					GuiSettings settings = GuiSettings.getSettings();
					if (_regex == null) {
						_regex = new NamedRegex(name, expression);
						settings.getRegexes().add(_regex);
					} else {
						_regex.setName(name);
						_regex.setExpression(expression);
					}
					GuiSettings.saveSettings(settings);
					dispose();
				} else {
					GuiHelper
							.showErrorMessage(
									"Regex name required",
									"Please provide a name of minimum 3 characters for your regex.",
									new IllegalArgumentException(name));
				}
			}
		});
		GuiHelper.addToGridBag(saveButton, panel, 1, 3);

		return panel;
	}

	@Override
	protected String getDialogTitle() {
		return "Regex";
	}

}