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
package org.eobjects.datacleaner.windows;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.eobjects.analyzer.reference.SimpleDictionary;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.guice.Nullable;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.user.MutableReferenceDataCatalog;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.DCLabel;
import org.eobjects.datacleaner.widgets.DescriptionLabel;
import org.jdesktop.swingx.JXTextArea;
import org.jdesktop.swingx.JXTextField;

public final class SimpleDictionaryDialog extends AbstractDialog {

    private static final long serialVersionUID = 1L;

    private final SimpleDictionary _originalDictionary;
    private final MutableReferenceDataCatalog _catalog;
    private final JXTextField _nameTextField;
    private final JXTextArea _valuesTextArea;

    @Inject
    protected SimpleDictionaryDialog(@Nullable SimpleDictionary dictionary, MutableReferenceDataCatalog catalog,
            WindowContext windowContext) {
        super(windowContext, ImageManager.get().getImage("images/window/banner-dictionaries.png"));
        _originalDictionary = dictionary;
        _catalog = catalog;

        _nameTextField = WidgetFactory.createTextField("Dictionary name");
        _valuesTextArea = WidgetFactory.createTextArea("Values");
        _valuesTextArea.setRows(14);

        if (dictionary != null) {
            _nameTextField.setText(dictionary.getName());
            Collection<String> values = dictionary.getValues().getValues();
            StringBuilder sb = new StringBuilder();
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
        }
    }

    @Override
    protected String getBannerTitle() {
        return "Simple dictionary";
    }

    @Override
    protected int getDialogWidth() {
        return 400;
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

        final JButton createDictionaryButton = WidgetFactory.createButton("Save dictionary",
                "images/model/dictionary.png");
        createDictionaryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = _nameTextField.getText();
                if (StringUtils.isNullOrEmpty(name)) {
                    JOptionPane.showMessageDialog(SimpleDictionaryDialog.this,
                            "Please fill out the name of the dictionary");
                    return;
                }

                String values = _valuesTextArea.getText();
                if (StringUtils.isNullOrEmpty(values)) {
                    JOptionPane.showMessageDialog(SimpleDictionaryDialog.this, "Please fill out the values");
                    return;
                }

                SimpleDictionary dict = new SimpleDictionary(name, values.split("\n"));

                if (_originalDictionary != null) {
                    _catalog.removeDictionary(_originalDictionary);
                }
                _catalog.addDictionary(dict);
                SimpleDictionaryDialog.this.dispose();
            }
        });

        final DCPanel buttonPanel = new DCPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 4, 4));
        buttonPanel.add(createDictionaryButton);

        final DescriptionLabel descriptionLabel = new DescriptionLabel(
                "A simple dictionary is a dictionary that you enter directly in DataCleaner. In the 'Values' field you can enter each value of the dictionary on a separate line.");

        final DCPanel mainPanel = new DCPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(descriptionLabel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        mainPanel.setPreferredSize(getDialogWidth(), 400);

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
