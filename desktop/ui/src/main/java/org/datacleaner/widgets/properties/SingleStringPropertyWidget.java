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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.text.JTextComponent;

import org.apache.metamodel.util.CollectionUtils;
import org.datacleaner.api.StringProperty;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.util.DCDocumentListener;
import org.datacleaner.util.WidgetFactory;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

/**
 * Property widget for a single String property. This widget will take into
 * account the additional metadata provided by the {@link StringProperty}
 * annotation.
 */
public class SingleStringPropertyWidget extends AbstractPropertyWidget<String> {

    private final JTextComponent _textComponent;
    private final StringProperty _stringPropertyAnnotation;

    @Inject
    public SingleStringPropertyWidget(final ConfiguredPropertyDescriptor propertyDescriptor,
            final ComponentBuilder componentBuilder) {
        super(componentBuilder, propertyDescriptor);

        _stringPropertyAnnotation = propertyDescriptor.getAnnotation(StringProperty.class);
        _textComponent = getTextComponent(propertyDescriptor);
        final String currentValue = getCurrentValue();
        if (currentValue != null) {
            _textComponent.setText(currentValue);
        }
        add(_textComponent);
    }

    protected JTextComponent getTextComponent(final ConfiguredPropertyDescriptor propertyDescriptor) {
        final boolean multiline;
        final boolean password;
        final String mimeType;

        if (_stringPropertyAnnotation == null) {
            multiline = false;
            mimeType = null;
            password = false;
        } else {
            multiline = _stringPropertyAnnotation.multiline();
            final String[] mimeTypes = _stringPropertyAnnotation.mimeType();
            mimeType = getTextAreaMimeType(mimeTypes);
            password = _stringPropertyAnnotation.password();
        }

        final JTextComponent textComponent;
        if (multiline) {
            if (mimeType != null) {
                final RSyntaxTextArea syntaxArea = new RSyntaxTextArea(8, 17);
                syntaxArea.setTabSize(2);
                syntaxArea.setSyntaxEditingStyle(mimeType);
                textComponent = syntaxArea;
            } else {
                textComponent = WidgetFactory.createTextArea(propertyDescriptor.getName());
            }
        } else {
            if (password) {
                textComponent = WidgetFactory.createPasswordField();
            } else {
                textComponent = WidgetFactory.createTextField(propertyDescriptor.getName());
            }
        }

        textComponent.getDocument().addDocumentListener(new DCDocumentListener() {
            @Override
            protected void onChange(final DocumentEvent e) {
                fireValueChanged();
            }
        });

        return textComponent;
    }

    /**
     * Finds a mimetype that is supported by the {@link RSyntaxTextArea}. These
     * are defined in the {@link SyntaxConstants} class.
     *
     * @param mimeTypes
     * @return
     */
    private String getTextAreaMimeType(final String[] mimeTypes) {
        if (mimeTypes == null) {
            return null;
        }

        List<Field> fields = Arrays.asList(SyntaxConstants.class.getFields());
        fields = CollectionUtils.filter(fields, f -> {
            if (f.getName().startsWith("SYNTAX_STYLE_")) {
                if (f.getType() == String.class) {
                    final int modifiers = f.getModifiers();
                    final boolean accessible = f.isAccessible() || Modifier.isPublic(modifiers);
                    final boolean isStatic = Modifier.isStatic(modifiers);
                    return accessible && isStatic;
                }
            }
            return false;
        });
        final List<String> acceptedMimeTypes = CollectionUtils.map(fields, f -> {
            try {
                return (String) f.get(null);
            } catch (final Exception e) {
                return null;
            }
        });

        for (final String mimeType : mimeTypes) {
            if (acceptedMimeTypes.contains(mimeType)) {
                return mimeType;
            }
        }

        return null;
    }

    @Override
    public boolean isSet() {
        final String text = _textComponent.getText();
        if (text == null) {
            return false;
        }
        if (_stringPropertyAnnotation != null && _stringPropertyAnnotation.emptyString()) {
            return true;
        }
        return text.length() > 0;
    }

    @Override
    public String getValue() {
        String text = _textComponent.getText();
        if (_textComponent instanceof JTextField) {
            // look for escaped new lines
            text = text.replaceAll("\\\\r", "\r");
            text = text.replaceAll("\\\\n", "\n");
        }
        return text;
    }

    @Override
    protected void setValue(final String value) {
        _textComponent.setText(value);
    }
}
