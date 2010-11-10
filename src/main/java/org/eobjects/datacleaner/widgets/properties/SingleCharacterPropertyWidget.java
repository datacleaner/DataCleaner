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
			return null;
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
