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

import javax.inject.Inject;
import javax.swing.event.DocumentEvent;

import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.util.DCDocumentListener;
import org.datacleaner.widgets.CharTextField;

public class SingleCharacterPropertyWidget extends AbstractPropertyWidget<Character> {

    private final CharTextField _textField;
    private final DCDocumentListener _listener = new DCDocumentListener() {
        @Override
        protected void onChange(final DocumentEvent e) {
            fireValueChanged();
        }
    };

    @Inject
    public SingleCharacterPropertyWidget(final ConfiguredPropertyDescriptor propertyDescriptor,
            final ComponentBuilder componentBuilder) {
        super(componentBuilder, propertyDescriptor);
        _textField = new CharTextField();
        final Character currentValue = getCurrentValue();
        setValue(currentValue);

        _textField.addDocumentListener(_listener);

        add(_textField);
    }

    public boolean isSet() {
        final Character value = _textField.getValue();
        return value != null;
    }

    @Override
    public Character getValue() {
        final Character value = _textField.getValue();
        if (value == null) {
            if (getPropertyDescriptor().getBaseType().isPrimitive()) {
                // cannot return null if it's a primitive char.
                return (char) 0;
            } else {
                return null;
            }
        }
        return value;
    }

    @Override
    protected void setValue(final Character value) {
        _textField.removeDocumentListener(_listener);
        _textField.setValue(value);
        _textField.addDocumentListener(_listener);
    }
}
