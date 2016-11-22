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
package org.datacleaner.widgets.properties;

import java.awt.Color;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.inject.Inject;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.datacleaner.api.PatternProperty;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.util.WidgetFactory;
import org.jdesktop.swingx.JXTextField;

/**
 * Property widget for regular expression Pattern properties.
 *
 * @author Stefan Janssen
 */
public class SinglePatternPropertyWidget extends AbstractPropertyWidget<Pattern> implements DocumentListener {

    private final JXTextField _textField;
    private final PatternProperty patternPropertyAnnotation;

    @Inject
    public SinglePatternPropertyWidget(final ConfiguredPropertyDescriptor propertyDescriptor,
            final ComponentBuilder componentBuilder) {
        super(componentBuilder, propertyDescriptor);

        patternPropertyAnnotation = propertyDescriptor.getAnnotation(PatternProperty.class);

        _textField = WidgetFactory.createTextField(propertyDescriptor.getName());
        _textField.getDocument().addDocumentListener(this);
        final Pattern currentValue = getCurrentValue();
        setValue(currentValue);
        updateColor();
        add(_textField);
    }

    @Override
    public boolean isSet() {
        if (_textField.getText() == null) {
            return false;
        }

        return ((patternPropertyAnnotation != null && patternPropertyAnnotation.emptyString())
                || _textField.getText().length() > 0) && isValidPattern();
    }

    @Override
    public Pattern getValue() {
        try {
            return Pattern.compile(_textField.getText());
        } catch (final PatternSyntaxException e) {
            return null;
        }
    }

    @Override
    protected void setValue(final Pattern value) {
        if (value != null) {
            final String pattern = value.pattern();
            if (!pattern.equals(_textField.getText())) {
                _textField.setText(pattern);
            }
        }
    }

    public boolean isValidPattern() {
        try {
            Pattern.compile(_textField.getText());
        } catch (final PatternSyntaxException e) {
            return false;
        }
        return true;
    }

    private void updateColor() {
        if (_textField.getText() == null || _textField.getText().length() == 0) {
            _textField.setBackground(Color.white);
        } else if (isValidPattern()) {
            _textField.setBackground(Color.green);
            fireValueChanged();
        } else {
            _textField.setBackground(Color.red);
        }
    }

    @Override
    public void changedUpdate(final DocumentEvent e) {
        updateColor();
    }

    @Override
    public void insertUpdate(final DocumentEvent e) {
        updateColor();
    }

    @Override
    public void removeUpdate(final DocumentEvent e) {
        updateColor();
    }
}
