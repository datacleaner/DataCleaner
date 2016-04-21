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
package org.datacleaner.windows;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.guice.Nullable;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.reference.SimpleDictionary;
import org.datacleaner.user.MutableReferenceDataCatalog;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.Alignment;
import org.datacleaner.widgets.DCCheckBox;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.widgets.DescriptionLabel;
import org.jdesktop.swingx.JXTextArea;
import org.jdesktop.swingx.JXTextField;

public final class SimpleDictionaryDialog extends AbstractDialog {

    private static final long serialVersionUID = 1L;

    private final SimpleDictionary _originalDictionary;
    private final MutableReferenceDataCatalog _catalog;
    private final JXTextField _nameTextField;
    private final JXTextArea _valuesTextArea;
    private final DCCheckBox<Boolean> _caseSensitiveCheckBox;

    @Inject
    protected SimpleDictionaryDialog(@Nullable SimpleDictionary dictionary, MutableReferenceDataCatalog catalog,
            WindowContext windowContext) {
        super(windowContext, ImageManager.get().getImage(IconUtils.DICTIONARY_SIMPLE_IMAGEPATH));
        _originalDictionary = dictionary;
        _catalog = catalog;

        _nameTextField = WidgetFactory.createTextField("Dictionary name");
        _valuesTextArea = WidgetFactory.createTextArea("Values");
        _valuesTextArea.setRows(14);

        _caseSensitiveCheckBox = new DCCheckBox<>("Case-sensitive?", false);
        _caseSensitiveCheckBox.setForeground(WidgetUtils.BG_COLOR_BRIGHTEST);
        _caseSensitiveCheckBox.setOpaque(false);
        _caseSensitiveCheckBox.setToolTipText("Only match on dictionary terms when text-case is the same.");

        if (dictionary != null) {
            _nameTextField.setText(dictionary.getName());
            final Collection<String> values = dictionary.getValueSet();
            final StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (String value : values) {
                if (first) {
                    first = false;
                } else {
                    sb.append('\n');
                }
                sb.append(value);
            }
            _valuesTextArea.setText(sb.toString());
            _caseSensitiveCheckBox.setSelected(dictionary.isCaseSensitive());
        }
    }

    @Override
    protected String getBannerTitle() {
        return "Simple dictionary";
    }

    @Override
    protected int getDialogWidth() {
        return 600;
    }

    @Override
    protected JComponent getDialogContent() {
        final DCPanel formPanel = new DCPanel();

        int row = 0;
        WidgetUtils.addToGridBag(DCLabel.bright("Dictionary name:"), formPanel, 0, row);
        WidgetUtils.addToGridBag(_nameTextField, formPanel, 1, row);

        row++;
        WidgetUtils.addToGridBag(DCLabel.bright("Values:"), formPanel, 0, row);
        WidgetUtils.addToGridBag(WidgetUtils.scrolleable(_valuesTextArea), formPanel, 1, row);

        row++;
        WidgetUtils.addToGridBag(_caseSensitiveCheckBox, formPanel, 1, row);

        final JButton createDictionaryButton = WidgetFactory.createPrimaryButton("Save dictionary",
                IconUtils.ACTION_SAVE_BRIGHT);
        createDictionaryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final String name = _nameTextField.getText();
                if (StringUtils.isNullOrEmpty(name)) {
                    JOptionPane.showMessageDialog(SimpleDictionaryDialog.this,
                            "Please fill out the name of the dictionary");
                    return;
                }

                final String values = _valuesTextArea.getText();
                if (StringUtils.isNullOrEmpty(values)) {
                    JOptionPane.showMessageDialog(SimpleDictionaryDialog.this, "Please fill out the values");
                    return;
                }

                final boolean caseSensitive = _caseSensitiveCheckBox.isSelected();

                final SimpleDictionary dict = new SimpleDictionary(name, caseSensitive, values.split("\n"));

                if (_originalDictionary != null) {
                    _catalog.removeDictionary(_originalDictionary);
                }
                _catalog.addDictionary(dict);
                SimpleDictionaryDialog.this.dispose();
            }
        });

        final DCPanel buttonPanel = DCPanel.flow(Alignment.CENTER, createDictionaryButton);

        final DescriptionLabel descriptionLabel = new DescriptionLabel(
                "A simple dictionary is a dictionary that you enter directly in DataCleaner. In the 'Values' field you can enter each value of the dictionary on a separate line.");

        final DCPanel mainPanel = new DCPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(descriptionLabel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.setPreferredSize(getDialogWidth(), 430);

        return mainPanel;
    }

    @Override
    protected boolean isWindowResizable() {
        return true;
    }

    @Override
    public String getWindowTitle() {
        return "Simple dictionary";
    }

}
