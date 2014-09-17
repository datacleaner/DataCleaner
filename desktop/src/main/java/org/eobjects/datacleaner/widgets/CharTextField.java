/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Neopost - Customer Information Management
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.eobjects.datacleaner.widgets;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.DCDocumentListener;
import org.eobjects.datacleaner.util.SingleCharacterDocument;
import org.jdesktop.swingx.HorizontalLayout;

/**
 * A widget used to display/edit a single char.
 * 
 * @author Kasper Sørensen
 */
public class CharTextField extends DCPanel {

	private static final long serialVersionUID = 1L;

	private final JTextField _textField;
	private final DCLabel _label;

	public CharTextField() {
		_textField = new JTextField(2);
		_textField.setDocument(new SingleCharacterDocument());
		_label = DCLabel.dark("");

		addDocumentListener(new DCDocumentListener() {
			@Override
			protected void onChange(DocumentEvent e) {
				Character value = getValue();
				if (value == null) {
					_label.setText("");
				} else {
					char c = value.charValue();
					if (c == ' ') {
						_label.setText("[whitespace]");
					} else if (c == '\t') {
						_label.setText("[tab]");
					} else if (c == '\n') {
						_label.setText("[newline]");
					} else if (c == '\r') {
						_label.setText("[carriage return]");
					} else if (c == '\f') {
						_label.setText("[form feed]");
					} else if (c == '\b') {
						_label.setText("[backspace]");
					} else if (c == '~') {
                        _label.setText("[tilde]");
					} else {
						_label.setText(value.toString());
					}
				}
			}
		});

		setLayout(new HorizontalLayout(2));
		add(_textField);
		add(_label);
	}

	public void addDocumentListener(DocumentListener documentListener) {
		_textField.getDocument().addDocumentListener(documentListener);
	}

	public void removeDocumentListener(DocumentListener documentListener) {
		_textField.getDocument().removeDocumentListener(documentListener);
	}

	public void setValue(Character value) {
		if (value == null) {
			_textField.setText("");
		} else {
			char c = value.charValue();

			if ('\t' == c) {
				_textField.setText("\\t");
			} else if ('\n' == c) {
				_textField.setText("\\n");
			} else if ('\r' == c) {
				_textField.setText("\\r");
			} else if ('\f' == c) {
				_textField.setText("\\f");
			} else if ('\b' == c) {
				_textField.setText("\\b");
			} else {
				_textField.setText(value.toString());
			}
		}
	}

	public Character getValue() {
		String text = _textField.getText();
		if (text == null || text.length() == 0) {
			return null;
		}

		// check for common escaped conversions
		if ("\\t".equals(text)) {
			return '\t';
		} else if ("\\n".equals(text)) {
			return '\n';
		} else if ("\\r".equals(text)) {
			return '\r';
		} else if ("\\f".equals(text)) {
			return '\f';
		} else if ("\\b".equals(text)) {
			return '\b';
		}

		if (text.length() == 2) {
			// first char is an escape char
			return text.charAt(1);
		}
		return text.charAt(0);
	}
}
