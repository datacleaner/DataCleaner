/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.widgets.properties;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;

import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.datacleaner.util.DCDocumentListener;
import org.eobjects.datacleaner.util.SingleCharacterDocument;

public class SingleCharacterPropertyWidget extends AbstractPropertyWidget<Character> {

	private static final long serialVersionUID = 1L;

	private final JTextField _textField;
	private final DCDocumentListener _listener = new DCDocumentListener() {
		@Override
		protected void onChange(DocumentEvent e) {
			fireValueChanged();
		}
	};

	public SingleCharacterPropertyWidget(ConfiguredPropertyDescriptor propertyDescriptor,
			AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder) {
		super(beanJobBuilder, propertyDescriptor);
		_textField = new JTextField(1);
		_textField.setDocument(new SingleCharacterDocument());
		Character currentValue = (Character) beanJobBuilder.getConfiguredProperty(propertyDescriptor);
		if (currentValue != null) {
			_textField.setText(currentValue.toString());
		}
		_textField.getDocument().addDocumentListener(_listener);

		add(_textField);
	}

	@Override
	public Character getValue() {
		String text = _textField.getText();
		if (text == null || text.length() == 0) {
			if (getPropertyDescriptor().getBaseType().isPrimitive()) {
				// cannot return null if it's a primitive char.
				return (char) 0;
			} else {
				return null;
			}
		}
		return text.charAt(0);
	}

	@Override
	protected void setValue(Character value) {
		_textField.getDocument().removeDocumentListener(_listener);
		if (value == null) {
			_textField.setText("");
		} else {
			_textField.setText(value.toString());
		}
		_textField.getDocument().addDocumentListener(_listener);
	}
}
