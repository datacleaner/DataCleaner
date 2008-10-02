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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import dk.eobjects.datacleaner.gui.GuiHelper;

public class TestRegexDialog extends BanneredDialog {

	private static final ImageIcon ICON_ERROR = GuiHelper
			.getImageIcon("images/driver_error.png");
	private static final ImageIcon ICON_SUCCESS = GuiHelper
			.getImageIcon("images/driver_success.png");
	private static final int NUM_TEST_FIELDS = 10;
	private static final long serialVersionUID = -127974180219204755L;
	private Pattern _pattern;
	private String _expression;
	private List<JTextField> _inputFields;
	private List<JLabel> _statusLabels;
	private JTextField _expressionField;
	private JLabel _errorLabel;

	public TestRegexDialog(String expression) {
		super();
		_expression = expression;

		_expressionField.setText(_expression);

		_pattern = Pattern.compile(expression);

		JTextArea aboutTesting = GuiHelper.createLabelTextArea().toComponent();
		aboutTesting
				.setText("You can test your regular expression by typing input sentences into the fields of this form.");

		add(aboutTesting, BorderLayout.SOUTH);
	}

	@Override
	protected Component getContent() {
		JPanel panel = GuiHelper.createPanel().toComponent();
		JLabel header = new JLabel("Test expression");
		header.setFont(GuiHelper.FONT_HEADER);
		GuiHelper.addToGridBag(header, panel, 0, 0, 2, 1);

		Dimension d = new Dimension(200, 20);

		_expressionField = new JTextField(_expression);
		_expressionField.setPreferredSize(d);
		_expressionField.getDocument().addDocumentListener(
				new DocumentListener() {
					public void changedUpdate(DocumentEvent e) {
						checkInputFields();
					}

					public void insertUpdate(DocumentEvent e) {
						checkInputFields();
					}

					public void removeUpdate(DocumentEvent e) {
						checkInputFields();
					}
				});
		GuiHelper.addToGridBag(_expressionField, panel, 0, 1);

		JButton resetButton = new JButton("Reset");
		resetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				_expressionField.setText(_expression);
			}
		});
		GuiHelper.addToGridBag(resetButton, panel, 1, 1);

		_errorLabel = new JLabel("");
		GuiHelper.addToGridBag(_errorLabel, panel, 0, 2);

		header = new JLabel("Test input");
		header.setFont(GuiHelper.FONT_HEADER);
		GuiHelper.addToGridBag(header, panel, 0, 3, 2, 1);

		_inputFields = new ArrayList<JTextField>(NUM_TEST_FIELDS);
		_statusLabels = new ArrayList<JLabel>(NUM_TEST_FIELDS);
		for (int i = 0; i < NUM_TEST_FIELDS; i++) {
			final int index = i;
			JTextField inputField = new JTextField();
			inputField.getDocument().addDocumentListener(
					new DocumentListener() {
						public void changedUpdate(DocumentEvent e) {
							checkInputField(index);
						}

						public void insertUpdate(DocumentEvent e) {
							checkInputField(index);
						}

						public void removeUpdate(DocumentEvent e) {
							checkInputField(index);
						}
					});
			inputField.setPreferredSize(d);
			GuiHelper.addToGridBag(inputField, panel, 0, 4 + i);

			JLabel statusLabel = new JLabel();
			GuiHelper.addToGridBag(statusLabel, panel, 1, 4 + i);

			_inputFields.add(inputField);
			_statusLabels.add(statusLabel);
		}

		return panel;
	}

	private void checkInputFields() {
		try {
			_pattern = Pattern.compile(_expressionField.getText());
			_errorLabel.setText("");
			for (int i = 0; i < NUM_TEST_FIELDS; i++) {
				checkInputField(i);
			}
		} catch (PatternSyntaxException e) {
			_errorLabel.setText(e.getMessage());
		}

	}

	private void checkInputField(int index) {
		String text = _inputFields.get(index).getText();
		JLabel label = _statusLabels.get(index);
		if ("".equals(text)) {
			label.setIcon(null);
		} else {
			Matcher matcher = _pattern.matcher(text);
			if (matcher.matches()) {
				label.setIcon(ICON_SUCCESS);
			} else {
				label.setIcon(ICON_ERROR);
			}
		}
	}

	@Override
	protected String getDialogTitle() {
		return "Test regular expression";
	}

}