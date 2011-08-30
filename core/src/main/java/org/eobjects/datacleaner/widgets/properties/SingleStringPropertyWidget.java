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

import javax.inject.Inject;
import javax.swing.event.DocumentEvent;
import javax.swing.text.JTextComponent;

import org.eobjects.analyzer.beans.api.StringProperty;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.datacleaner.util.DCDocumentListener;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;

public class SingleStringPropertyWidget extends AbstractPropertyWidget<String> {

	private static final long serialVersionUID = 1L;

	private static final String[] MONOSPACE_MIME_TYPES = { "text/x-java-source", "application/x-javascript",
			"text/javascript" };

	private final JTextComponent _textComponent;

	@Inject
	public SingleStringPropertyWidget(ConfiguredPropertyDescriptor propertyDescriptor,
			AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder) {
		super(beanJobBuilder, propertyDescriptor);

		StringProperty stringPropertyAnnotation = propertyDescriptor.getAnnotation(StringProperty.class);
		_textComponent = getTextComponent(propertyDescriptor, stringPropertyAnnotation);
		String currentValue = (String) beanJobBuilder.getConfiguredProperty(propertyDescriptor);
		if (currentValue != null) {
			_textComponent.setText(currentValue);
		}
		add(_textComponent);
	}

	private JTextComponent getTextComponent(ConfiguredPropertyDescriptor propertyDescriptor,
			StringProperty stringPropertyAnnotation) {
		JTextComponent textComponent;
		if (stringPropertyAnnotation != null && stringPropertyAnnotation.multiline()) {
			textComponent = WidgetFactory.createTextArea(propertyDescriptor.getName());
		} else {
			textComponent = WidgetFactory.createTextField(propertyDescriptor.getName());
		}

		textComponent.getDocument().addDocumentListener(new DCDocumentListener() {
			@Override
			protected void onChange(DocumentEvent e) {
				fireValueChanged();
			}
		});

		if (stringPropertyAnnotation != null) {
			boolean monospace = false;
			String[] mimeTypes = stringPropertyAnnotation.mimeType();
			for (String mt1 : mimeTypes) {
				for (String mt2 : MONOSPACE_MIME_TYPES) {
					if (mt1.equalsIgnoreCase(mt2)) {
						monospace = true;
						break;
					}
				}
			}

			if (monospace) {
				textComponent.setFont(WidgetUtils.FONT_MONOSPACE);
			}
		}

		return textComponent;
	}

	@Override
	public boolean isSet() {
		return _textComponent.getText() != null && _textComponent.getText().length() > 0;
	}

	@Override
	public String getValue() {
		return _textComponent.getText();
	}

	@Override
	protected void setValue(String value) {
		_textComponent.setText(value);
	}
}
