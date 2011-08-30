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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Array;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.text.JTextComponent;

import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.util.ReflectionUtils;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.DCDocumentListener;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.metamodel.util.EqualsBuilder;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.VerticalLayout;

/**
 * {@link PropertyWidget} for Number arrays. Displays number arrays as a set of
 * text boxes and plus/minus buttons to grow/shrink the array.
 * 
 * @author Kasper Sørensen
 */
public class MultipleNumberPropertyWidget extends AbstractPropertyWidget<Number[]> {

	private static final long serialVersionUID = 1L;

	private final NumberFormat _numberFormat = NumberFormat.getInstance();
	private final DCPanel _textFieldPanel;

	@Inject
	public MultipleNumberPropertyWidget(ConfiguredPropertyDescriptor propertyDescriptor,
			AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder) {
		super(beanJobBuilder, propertyDescriptor);

		_textFieldPanel = new DCPanel();
		_textFieldPanel.setLayout(new VerticalLayout(2));

		Number[] currentValue = (Number[]) beanJobBuilder.getConfiguredProperty(propertyDescriptor);
		updateComponents(currentValue);

		final JButton addButton = WidgetFactory.createSmallButton("images/actions/add.png");
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addTextField(null, true);
				fireValueChanged();
			}
		});

		final JButton removeButton = WidgetFactory.createSmallButton("images/actions/remove.png");
		removeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int componentCount = _textFieldPanel.getComponentCount();
				if (componentCount > 0) {
					_textFieldPanel.remove(componentCount - 1);
					_textFieldPanel.updateUI();
					fireValueChanged();
				}
			}
		});

		final DCPanel buttonPanel = new DCPanel();
		buttonPanel.setBorder(new EmptyBorder(0, 4, 0, 0));
		buttonPanel.setLayout(new VerticalLayout(2));
		buttonPanel.add(addButton);
		buttonPanel.add(removeButton);

		final DCPanel outerPanel = new DCPanel();
		outerPanel.setLayout(new BorderLayout());

		outerPanel.add(_textFieldPanel, BorderLayout.CENTER);
		outerPanel.add(buttonPanel, BorderLayout.EAST);

		add(outerPanel);
	}

	private Number[] createArray(int length) {
		return (Number[]) Array.newInstance(getPropertyDescriptor().getBaseType(), length);
	}

	public void updateComponents(Number[] values) {
		if (values == null) {
			values = createArray(2);
		}
		final Number[] previousValues = getValue();
		if (!EqualsBuilder.equals(values, previousValues)) {
			for (int i = 0; i < Math.min(previousValues.length, values.length); i++) {
				// modify text boxes
				if (!EqualsBuilder.equals(previousValues[i], values[i])) {
					JTextComponent component = (JTextComponent) _textFieldPanel.getComponent(i);
					component.setText(_numberFormat.format(values[i]));
				}
			}

			while (_textFieldPanel.getComponentCount() < values.length) {
				// add text boxes if there are too few
				Number nextValue = values[_textFieldPanel.getComponentCount()];
				addTextField(nextValue, false);
			}

			while (_textFieldPanel.getComponentCount() > values.length) {
				// remove text boxes if there are too many
				_textFieldPanel.remove(_textFieldPanel.getComponentCount() - 1);
			}
			_textFieldPanel.updateUI();
		}
	}

	private void addTextField(Number value, boolean updateUI) {
		JXTextField textField = WidgetFactory.createTextField();
		if (value != null) {
			textField.setText(_numberFormat.format(value));
		}
		textField.getDocument().addDocumentListener(new DCDocumentListener() {
			@Override
			protected void onChange(DocumentEvent e) {
				fireValueChanged();
			}
		});
		_textFieldPanel.add(textField);
		if (updateUI) {
			_textFieldPanel.updateUI();
		}
	}

	@Override
	public Number[] getValue() {
		Component[] components = _textFieldPanel.getComponents();

		final Number[] result = createArray(components.length);
		for (int i = 0; i < components.length; i++) {
			JXTextField textField = (JXTextField) components[i];
			String stringValue = textField.getText();
			result[i] = convertToNumber(stringValue);
		}
		return result;
	}

	private Number convertToNumber(String stringValue) {
		if (StringUtils.isNullOrEmpty(stringValue)) {
			return null;
		}
		Class<?> type = getPropertyDescriptor().getBaseType();
		try {
			final Number number = _numberFormat.parse(stringValue);
			if (type != Number.class) {
				if (ReflectionUtils.isInteger(type)) {
					return Integer.valueOf(number.intValue());
				} else if (ReflectionUtils.isLong(type)) {
					return Long.valueOf(number.longValue());
				} else if (ReflectionUtils.isDouble(type)) {
					return Double.valueOf(number.doubleValue());
				} else if (ReflectionUtils.isShort(type)) {
					return Short.valueOf(number.shortValue());
				} else if (ReflectionUtils.isByte(type)) {
					return Byte.valueOf(number.byteValue());
				} else if (ReflectionUtils.isFloat(type)) {
					return Float.valueOf(number.floatValue());
				}
			}
			return number;
		} catch (ParseException e) {
			throw new IllegalStateException("Cannot parse to number: " + stringValue);
		}
	}

	@Override
	protected void setValue(Number[] value) {
		updateComponents(value);
	}

}
