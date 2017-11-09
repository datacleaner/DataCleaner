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
package org.datacleaner.panels.textcasetransformer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JComponent;

import org.datacleaner.beans.transform.TextCaseTransformer;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.descriptors.TransformerDescriptor;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.job.builder.TransformerComponentBuilder;
import org.datacleaner.panels.ConfiguredPropertyTaskPane;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.panels.TransformerComponentBuilderPanel;
import org.datacleaner.panels.TransformerComponentBuilderPresenter;
import org.datacleaner.util.IconUtils;
import org.datacleaner.widgets.properties.PropertyWidget;
import org.datacleaner.widgets.properties.PropertyWidgetFactory;
import org.datacleaner.widgets.properties.SingleEnumPropertyWidget;
import org.jdesktop.swingx.JXTaskPane;

public class TextCaseTransformerPresenter extends TransformerComponentBuilderPanel
        implements TransformerComponentBuilderPresenter {
    
    private static final long serialVersionUID = 1L;
    
    private static final String TASK_PANE_TITLE_DICTIONARIES = "Dictionaries";
    private final ConfiguredPropertyDescriptor _valueProperty;
    private final ConfiguredPropertyDescriptor _modeProperty;
    private final ConfiguredPropertyDescriptor _allWordsDictionaryProperty;
    private final ConfiguredPropertyDescriptor _wordDictionaryProperty;
    private final ConfiguredPropertyDescriptor _beginWordDictionaryProperty;
    private final ConfiguredPropertyDescriptor _endWordDictionaryProperty;
    private JXTaskPane _dictionaryTaskPane;

    TextCaseTransformerPresenter(final TransformerComponentBuilder<?> transformerJobBuilder,
            final WindowContext windowContext, final PropertyWidgetFactory propertyWidgetFactory,
            final DataCleanerConfiguration configuration) {
        super(transformerJobBuilder, windowContext, propertyWidgetFactory, configuration);

        final TransformerDescriptor<?> descriptor = transformerJobBuilder.getDescriptor();
        assert descriptor.getComponentClass() == TextCaseTransformer.class;

        _valueProperty = descriptor.getConfiguredProperty(TextCaseTransformer.VALUE_PROPERTY);
        _modeProperty = descriptor.getConfiguredProperty(TextCaseTransformer.MODE_PROPERTY);
        _allWordsDictionaryProperty =
                descriptor.getConfiguredProperty(TextCaseTransformer.ALL_WORDS_DICTIONARY_PROPERTY);
        _wordDictionaryProperty = descriptor.getConfiguredProperty(TextCaseTransformer.WORD_DICTIONARY_PROPERTY);
        _beginWordDictionaryProperty =
                descriptor.getConfiguredProperty(TextCaseTransformer.BEGIN_WORD_DICTIONARY_PROPERTY);
        _endWordDictionaryProperty = descriptor.getConfiguredProperty(TextCaseTransformer.END_WORD_DICTIONARY_PROPERTY);
    }

    @Override
    protected List<ConfiguredPropertyTaskPane> createPropertyTaskPanes() {
        final List<ConfiguredPropertyTaskPane> result = new ArrayList<>();

        result.add(new ConfiguredPropertyTaskPane("Input columns", IconUtils.MODEL_COLUMN,
                Collections.singletonList(_valueProperty)));
        result.add(new ConfiguredPropertyTaskPane("Required properties", IconUtils.MENU_OPTIONS,
                Collections.singletonList(_modeProperty)));
        result.add(new ConfiguredPropertyTaskPane(TASK_PANE_TITLE_DICTIONARIES, IconUtils.ACTION_EDIT,
                Arrays.asList(_allWordsDictionaryProperty, _wordDictionaryProperty, _beginWordDictionaryProperty,
                        _endWordDictionaryProperty)));

        return result;
    }

    @Override
    protected PropertyWidget<?> createPropertyWidget(ComponentBuilder componentBuilder,
            ConfiguredPropertyDescriptor propertyDescriptor) {
        final PropertyWidget<?> propertyWidget = super.createPropertyWidget(componentBuilder, propertyDescriptor);
        if (propertyDescriptor == _modeProperty) {
            ((SingleEnumPropertyWidget) propertyWidget).addComboListener(this::updateDictionaryVisibility);
        }

        return propertyWidget;
    }

    @Override
    protected JXTaskPane addTaskPane(Icon icon, String title, JComponent content, boolean expanded) {
        final JXTaskPane taskPane = super.addTaskPane(icon, title, content, expanded);

        if (title.equals(TASK_PANE_TITLE_DICTIONARIES)) {
            _dictionaryTaskPane = taskPane;
        }

        return taskPane;
    }


    @Override
    protected JComponent decorateMainPanel(DCPanel panel) {
        final JComponent result = super.decorateMainPanel(panel);

        final TextCaseTransformer.TransformationMode transformationMode =
                (TextCaseTransformer.TransformationMode) getComponentBuilder().getConfiguredProperty(_modeProperty);
        updateDictionaryVisibility(transformationMode);

        return result;
    }

    private void updateDictionaryVisibility(final Enum<?> item) {
        _dictionaryTaskPane.setVisible(item == TextCaseTransformer.TransformationMode.CAPITALIZE_WORDS);
    }
}
