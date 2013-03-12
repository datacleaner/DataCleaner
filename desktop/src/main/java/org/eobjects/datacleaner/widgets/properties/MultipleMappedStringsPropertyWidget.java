/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
public class MultipleMappedStringsPropertyWidget extends
		MultipleInputColumnsPropertyWidget {

	private final WeakHashMap<InputColumn<?>, JXTextField> _mappedTextFields;
	private final ConfiguredPropertyDescriptor _mappedStringsProperty;
	private final MinimalPropertyWidget<String[]> _mappedStringPropertyWidget;

	// indicates whether there is currently undergoing a string listener action
	private volatile boolean _stringsUpdating;

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
	public MultipleMappedStringsPropertyWidget(
			AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder,
			ConfiguredPropertyDescriptor inputColumnsProperty,
			ConfiguredPropertyDescriptor mappedStringsProperty) {
		super(beanJobBuilder, inputColumnsProperty);
		_mappedTextFields = new WeakHashMap<InputColumn<?>, JXTextField>();
		_mappedStringsProperty = mappedStringsProperty;

		_mappedStringPropertyWidget = createMappedStringsPropertyWidget();

		_stringsUpdating = false;

		final InputColumn<?>[] currentValue = getCurrentValue();
		final String[] currentMappedStringsValue = (String[]) beanJobBuilder
				.getConfiguredProperty(mappedStringsProperty);
		if (currentValue != null && currentMappedStringsValue != null) {
			// first create combo's, then set value (so combo is ready before it
			// is requested)

			_mappedStringPropertyWidget.setValue(currentMappedStringsValue);
			final int minLength = Math.min(currentValue.length,
					currentMappedStringsValue.length);
			for (int i = 0; i < minLength; i++) {
				final InputColumn<?> inputColumn = currentValue[i];
				final String mappedString = currentMappedStringsValue[i];
				createTextField(inputColumn, mappedString);
			}

			setValue(currentValue);
		}
	}

	@Override
	protected boolean isAllInputColumnsSelectedIfNoValueExist() {
		return false;
	}

	private JXTextField createTextField(InputColumn<?> inputColumn,
			String mappedString) {
		final JXTextField textField = WidgetFactory.createTextField();
		_mappedTextFields.put(inputColumn, textField);

		if (mappedString == null) {
			mappedString = getDefaultMappedString(inputColumn);
		}
		if (mappedString != null) {
			textField.setText(mappedString);
		}
		textField.getDocument().addDocumentListener(new DCDocumentListener() {
			@Override
			protected void onChange(DocumentEvent event) {
				if (isBatchUpdating()) {
					return;
				}
				_stringsUpdating = true;
				fireValueChanged();
				_mappedStringPropertyWidget.fireValueChanged();
				_stringsUpdating = false;
			}
		});
		return textField;
	}

	/**
	 * Subclasses can override this method to set a default value for a column
	 * when it is selected.
	 * 
	 * @param inputColumn
	 * @return
	 */
	protected String getDefaultMappedString(InputColumn<?> inputColumn) {
		return "";
	}

	@Override
	protected JComponent decorateCheckBox(
			final DCCheckBox<InputColumn<?>> checkBox) {
		final JXTextField textField;
		if (_mappedTextFields.containsKey(checkBox.getValue())) {
			textField = _mappedTextFields.get(checkBox.getValue());
		} else {
			textField = createTextField(checkBox.getValue(), null);
		}
		checkBox.addListenerToHead(new DCCheckBox.Listener<InputColumn<?>>() {
			@Override
			public void onItemSelected(InputColumn<?> item, boolean selected) {
				textField.setVisible(selected);
				updateUI();
			}
		});
		checkBox.addListener(new DCCheckBox.Listener<InputColumn<?>>() {
			@Override
			public void onItemSelected(InputColumn<?> item, boolean selected) {
				if (isBatchUpdating()) {
					return;
				}
				_mappedStringPropertyWidget.fireValueChanged();
			}
		});

		textField.setVisible(checkBox.isSelected());

		final DCPanel panel = new DCPanel();
		panel.setLayout(new BorderLayout());
		panel.add(checkBox, BorderLayout.CENTER);
		panel.add(textField, BorderLayout.EAST);
		return panel;
	}

	public PropertyWidget<String[]> getMappedStringsPropertyWidget() {
		return _mappedStringPropertyWidget;
	}

	private MinimalPropertyWidget<String[]> createMappedStringsPropertyWidget() {
		return new MinimalPropertyWidget<String[]>(getBeanJobBuilder(),
				_mappedStringsProperty) {

			@Override
			public JComponent getWidget() {
				// do not return a visual widget
				return null;
			}

			@Override
			public boolean isSet() {
				final InputColumn<?>[] inputColumns = MultipleMappedStringsPropertyWidget.this
						.getValue();
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
				if (_stringsUpdating) {
					// setValue of the strings will be called prematurely
					// (with previous value) by change notifications of the
					// input columns property.
					return;
				}
				
				if (value == null) {
					value = new String[0];
				}

				if (EqualsBuilder.equals(value, getValue())) {
					return;
				}
				final InputColumn<?>[] inputColumns = MultipleMappedStringsPropertyWidget.this
						.getValue();

				if (inputColumns.length != value.length) {
					// disregard this invalid value update
					return;
				}

				for (int i = 0; i < inputColumns.length; i++) {
					final InputColumn<?> inputColumn = inputColumns[i];
					final String mappedString;
					if (value == null) {
						mappedString = getDefaultMappedString(inputColumn);
					} else if (i < value.length) {
						mappedString = value[i];
					} else {
						mappedString = getDefaultMappedString(inputColumn);
					}
					final JXTextField textField = _mappedTextFields
							.get(inputColumn);

					final String previousText = textField.getText();
					if (!mappedString.equals(previousText)) {
						textField.setText(mappedString);
					}
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
			if (textField != null && textField.isVisible()) {
				if (!StringUtils.isNullOrEmpty(textField.getText())) {
					result.add(inputColumn);
				}
			}
		}
		return result.toArray(new InputColumn[result.size()]);
	}

	private String[] getMappedStrings() {
		final InputColumn<?>[] inputColumns = MultipleMappedStringsPropertyWidget.this
				.getValue();
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