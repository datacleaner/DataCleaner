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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.swing.JPasswordField;
import javax.swing.event.DocumentEvent;
import javax.swing.text.JTextComponent;

import org.eobjects.analyzer.beans.api.StringProperty;
import org.eobjects.analyzer.descriptors.ConfiguredPropertyDescriptor;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.datacleaner.util.DCDocumentListener;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.metamodel.util.CollectionUtils;
import org.eobjects.metamodel.util.Func;
import org.eobjects.metamodel.util.Predicate;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

/**
 * Property widget for a single String property. This widget will take into
 * account the additional metadata provided by the {@link StringProperty}
 * annotation.
 */
public class SingleStringPropertyWidget extends AbstractPropertyWidget<String> {

    private final JTextComponent _textComponent;

    @Inject
    public SingleStringPropertyWidget(ConfiguredPropertyDescriptor propertyDescriptor,
            AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder) {
        super(beanJobBuilder, propertyDescriptor);

        StringProperty stringPropertyAnnotation = propertyDescriptor.getAnnotation(StringProperty.class);
        _textComponent = getTextComponent(propertyDescriptor, stringPropertyAnnotation);
        String currentValue = getCurrentValue();
        if (currentValue != null) {
            _textComponent.setText(currentValue);
        }
        add(_textComponent);
    }

    protected JTextComponent getTextComponent(ConfiguredPropertyDescriptor propertyDescriptor,
            StringProperty stringPropertyAnnotation) {
        final boolean multiline;
        final boolean password;
        final String mimeType;

        if (stringPropertyAnnotation == null) {
            multiline = false;
            mimeType = null;
            password = false;
        } else {
            multiline = stringPropertyAnnotation.multiline();
            String[] mimeTypes = stringPropertyAnnotation.mimeType();
            mimeType = getTextAreaMimeType(mimeTypes);
            password = stringPropertyAnnotation.password();
        }

        final JTextComponent textComponent;
        if (multiline) {
            if (mimeType != null) {
                RSyntaxTextArea syntaxArea = new RSyntaxTextArea(8, 17);
                syntaxArea.setSyntaxEditingStyle(mimeType);
                textComponent = syntaxArea;
            } else {
                textComponent = WidgetFactory.createTextArea(propertyDescriptor.getName());
            }
        } else {
            if (password) {
                textComponent = new JPasswordField(WidgetFactory.TEXT_FIELD_COLUMNS);
            } else {
                textComponent = WidgetFactory.createTextField(propertyDescriptor.getName());
            }
        }

        textComponent.getDocument().addDocumentListener(new DCDocumentListener() {
            @Override
            protected void onChange(DocumentEvent e) {
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
    private String getTextAreaMimeType(String[] mimeTypes) {
        if (mimeTypes == null) {
            return null;
        }

        List<Field> fields = Arrays.asList(SyntaxConstants.class.getFields());
        fields = CollectionUtils.filter(fields, new Predicate<Field>() {
            @Override
            public Boolean eval(Field f) {
                if (f.getName().startsWith("SYNTAX_STYLE_")) {
                    if (f.getType() == String.class) {
                        final int modifiers = f.getModifiers();
                        final boolean accessible = f.isAccessible() || Modifier.isPublic(modifiers);
                        final boolean isStatic = Modifier.isStatic(modifiers);
                        return accessible && isStatic;
                    }
                }
                return false;
            }
        });
        List<String> acceptedMimeTypes = CollectionUtils.map(fields, new Func<Field, String>() {
            @Override
            public String eval(Field f) {
                try {
                    return (String) f.get(null);
                } catch (Exception e) {
                    return null;
                }
            }
        });

        for (String mimeType : mimeTypes) {
            if (acceptedMimeTypes.contains(mimeType)) {
                return mimeType;
            }
        }

        return null;
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
