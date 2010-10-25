package org.eobjects.datacleaner.widgets.properties;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.datacleaner.util.SingleCharacterDocument;

public class SingleCharacterPropertyWidget extends AbstractPropertyWidget<Character> {

	private static final long serialVersionUID = 1L;

	private final JTextField _textField;

	public SingleCharacterPropertyWidget(ConfiguredPropertyDescriptor propertyDescriptor,
			AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder) {
		super(propertyDescriptor);
		_textField = new JTextField(1);
		_textField.setDocument(new SingleCharacterDocument());
		Character currentValue = (Character) beanJobBuilder.getConfiguredProperty(propertyDescriptor);
		if (currentValue != null) {
			_textField.setText(currentValue.toString());
		}
		_textField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				fireValueChanged();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				fireValueChanged();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				fireValueChanged();
			}
		});

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

}
