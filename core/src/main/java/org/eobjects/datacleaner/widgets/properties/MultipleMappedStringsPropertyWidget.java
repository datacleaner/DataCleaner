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
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;

import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.DCDocumentListener;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.widgets.DCCheckBox;
import org.eobjects.metamodel.util.EqualsBuilder;
import org.jdesktop.swingx.JXTextField;

/**
 * A specialized property widget for multiple input columns that are mapped to
 * string values. This widget looks like the
 * {@link MultipleInputColumnsPropertyWidget}, but is enhanced with string text
 * fields.
 * 
 * @author Kasper Sørensen
 */
public class MultipleMappedStringsPropertyWidget extends MultipleInputColumnsPropertyWidget {

	private final WeakHashMap<InputColumn<?>, JXTextField> _mappedTextFields;
	private final ConfiguredPropertyDescriptor _mappedStringsProperty;
	private final MinimalPropertyWidget<String[]> _mappedStringPropertyWidget;

	/**
	 * Constructs the property widget
	 * 
	 * @param beanJobBuilder
	 *            the transformer job builder for the table lookup
	 * @param inputColumnsProperty
	 *            the property represeting the columns to use for settig up
	 *            conditional lookup (InputColumn[])
	 * @param mappedStringsProperty
	 *            the property representing the mapped strings (String[])
	 */
	public MultipleMappedStringsPropertyWidget(AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
			ConfiguredPropertyDescriptor inputColumnsProperty, ConfiguredPropertyDescriptor mappedStringsProperty) {
		super(beanJobBuilder, inputColumnsProperty);
		_mappedTextFields = new WeakHashMap<InputColumn<?>, JXTextField>();
		_mappedStringsProperty = mappedStringsProperty;

		_mappedStringPropertyWidget = createMappedStringsPropertyWidget();

		InputColumn<?>[] currentValue = getCurrentValue();
		if (currentValue != null) {
			setValue(currentValue);
		}

		String[] currentMappedStrings = (String[]) beanJobBuilder.getConfiguredProperty(mappedStringsProperty);
		if (currentValue != null && currentMappedStrings != null) {
			int minLength = Math.min(currentValue.length, currentMappedStrings.length);
			for (int i = 0; i < minLength; i++) {
				InputColumn<?> inputColumn = currentValue[i];
				String mappedString = currentMappedStrings[i];
				createTextField(inputColumn, mappedString);
			}
		}
	}

	@Override
	protected boolean isAllInputColumnsSelectedIfNoValueExist() {
		return false;
	}

	private JXTextField createTextField(InputColumn<?> inputColumn, String mappedString) {
		final JXTextField textField = WidgetFactory.createTextField();
		_mappedTextFields.put(inputColumn, textField);
		if (mappedString != null) {
			textField.setText(mappedString);
		}
		textField.getDocument().addDocumentListener(new DCDocumentListener() {

			@Override
			protected void onChange(DocumentEvent event) {
				fireValueChanged();
				_mappedStringPropertyWidget.fireValueChanged();
			}
		});
		return textField;
	}

	@Override
	protected JComponent decorateCheckBox(final DCCheckBox<InputColumn<?>> checkBox) {
		final JXTextField textField;
		if (_mappedTextFields.containsKey(checkBox.getValue())) {
			textField = _mappedTextFields.get(checkBox.getValue());
		} else {
			textField = createTextField(checkBox.getValue(), null);
		}
		checkBox.addListener(new DCCheckBox.Listener<InputColumn<?>>() {
			@Override
			public void onItemSelected(InputColumn<?> item, boolean selected) {
				textField.setVisible(selected);
			}
		});

		textField.setVisible(checkBox.isSelected());

		final DCPanel panel = new DCPanel();
		panel.setLayout(new BorderLayout());
		panel.add(checkBox, BorderLayout.WEST);
		panel.add(textField, BorderLayout.EAST);
		return panel;
	}

	public PropertyWidget<String[]> getMappedStringsPropertyWidget() {
		return _mappedStringPropertyWidget;
	}

	private MinimalPropertyWidget<String[]> createMappedStringsPropertyWidget() {
		return new MinimalPropertyWidget<String[]>(getBeanJobBuilder(), _mappedStringsProperty) {

			@Override
			public JComponent getWidget() {
				// do not return a visual widget
				return null;
			}

			@Override
			public boolean isSet() {
				final InputColumn<?>[] inputColumns = MultipleMappedStringsPropertyWidget.this.getValue();
				for (InputColumn<?> inputColumn : inputColumns) {
					JXTextField textField = _mappedTextFields.get(inputColumn);
					if (StringUtils.isNullOrEmpty(textField.getText())) {
						return false;
					}
				}
				return true;
			}

			@Override
			public String[] getValue() {
				return getMappedStrings();
			}

			@Override
			protected void setValue(String[] value) {
				if (EqualsBuilder.equals(value, getValue())) {
					return;
				}
				final InputColumn<?>[] inputColumns = MultipleMappedStringsPropertyWidget.this.getValue();
				for (int i = 0; i < inputColumns.length; i++) {
					final InputColumn<?> inputColumn = inputColumns[i];
					final String mappedColumnName;
					if (value == null) {
						mappedColumnName = "";
					} else if (i < value.length) {
						mappedColumnName = value[i];
					} else {
						mappedColumnName = "";
					}
					final JXTextField textField = _mappedTextFields.get(inputColumn);
					textField.setText(mappedColumnName);
				}
			}
		};
	}

	@Override
	public InputColumn<?>[] getValue() {
		InputColumn<?>[] checkedInputColumns = super.getValue();
		List<InputColumn<?>> result = new ArrayList<InputColumn<?>>();
		for (InputColumn<?> inputColumn : checkedInputColumns) {
			// exclude input columns that have not been mapped yet
			final JXTextField textField = _mappedTextFields.get(inputColumn);
			if (textField != null) {
				if (!StringUtils.isNullOrEmpty(textField.getText())) {
					result.add(inputColumn);
				}
			}
		}
		return result.toArray(new InputColumn[result.size()]);
	}

	private String[] getMappedStrings() {
		final InputColumn<?>[] inputColumns = MultipleMappedStringsPropertyWidget.this.getValue();
		final List<String> result = new ArrayList<String>();
		for (InputColumn<?> inputColumn : inputColumns) {
			JXTextField textField = _mappedTextFields.get(inputColumn);
			if (textField != null) {
				String value = textField.getText();
				if (!StringUtils.isNullOrEmpty(value)) {
					result.add(value);
				}
			}
		}

		return result.toArray(new String[result.size()]);
	}

	@Override
	protected void selectAll() {
		for (JXTextField textField : _mappedTextFields.values()) {
			textField.setVisible(true);
		}
		super.selectAll();
	}

	@Override
	protected void selectNone() {
		for (JXTextField textField : _mappedTextFields.values()) {
			textField.setVisible(false);
		}
		super.selectNone();
	}
}