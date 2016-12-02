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
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.widgets.DescriptionLabel;
import org.jdesktop.swingx.JXStatusBar;
import org.jdesktop.swingx.JXTextField;

public class ColumnNamesSetterDialog extends AbstractDialog {

    private static final long serialVersionUID = 1L;
    private static final ImageManager imageManager = ImageManager.get();

    private final JLabel _statusLabel;
    private final Map<String, JXTextField> _columnNamesTextFields;
    private final JButton _saveButton;
    private final JButton _cancelButton;
    private List<String> _columnNames;

    public ColumnNamesSetterDialog(final WindowContext windowContext, final List<String> defaultColumnNames) {
        super(windowContext, imageManager.getImage(AbstractDatastoreDialog.DEFAULT_BANNER_IMAGE));
        _statusLabel = DCLabel.bright("Please specify columns' names");
        _columnNames = defaultColumnNames;
        _columnNamesTextFields = new LinkedHashMap<>();
        for (int i = 0; i < defaultColumnNames.size(); i++) {
            final String columnName = defaultColumnNames.get(i);
            final JXTextField textField = WidgetFactory.createTextField(columnName);
            textField.getDocument().addDocumentListener(new DocumentListener() {

                @Override
                public void removeUpdate(final DocumentEvent e) {
                    checkNamesValidity();
                }

                @Override
                public void insertUpdate(final DocumentEvent e) {
                    checkNamesValidity();
                }

                @Override
                public void changedUpdate(final DocumentEvent e) {
                    checkNamesValidity();
                }
            });
            _columnNamesTextFields.put(columnName, textField);
        }
        _saveButton = WidgetFactory.createPrimaryButton("Save", IconUtils.ACTION_SAVE_BRIGHT);
        _cancelButton = WidgetFactory.createDefaultButton("Cancel", IconUtils.ACTION_CANCEL);
        _saveButton.addActionListener(e -> {
            // Update the value of column names list
            _columnNames = getNewColumnNames();
            dispose();
        });
        _cancelButton.addActionListener(e -> dispose());


    }

    @Override
    public String getWindowTitle() {
        return "Set column names";
    }

    @Override
    protected String getBannerTitle() {
        return "Column names";
    }

    @Override
    protected int getDialogWidth() {
        return 600;
    }

    @Override
    protected JComponent getDialogContent() {
        final DCPanel formPanel = new DCPanel();

        int row = 0;
        WidgetUtils.addToGridBag(DCLabel.bright("<html><b>Original name </b></html>"), formPanel, 1, row, 1, 1,
                GridBagConstraints.EAST, 20);
        WidgetUtils.addToGridBag(DCLabel.bright("<html><b>New name </b></html>"), formPanel, 2, row, 1, 1,
                GridBagConstraints.EAST, 20);

        row++;
        if (_columnNamesTextFields != null && !_columnNamesTextFields.isEmpty()) {

            for (final Entry<String, JXTextField> entry : _columnNamesTextFields.entrySet()) {
                row++;
                final String columnName = entry.getKey();
                final JXTextField textField = entry.getValue();

                WidgetUtils.addToGridBag(
                        new JLabel(imageManager.getImageIcon("images/model/variable.png", IconUtils.ICON_SIZE_SMALL)),
                        formPanel, 0, row);
                WidgetUtils.addToGridBag(DCLabel.bright(columnName), formPanel, 1, row, GridBagConstraints.WEST);
                WidgetUtils.addToGridBag(textField, formPanel, 2, row, GridBagConstraints.WEST);
            }
            row++;
        }

        final DCPanel buttonPanel = new DCPanel();
        buttonPanel.setBorder(WidgetUtils.BORDER_EMPTY);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 4, 0));
        buttonPanel.add(_saveButton);
        buttonPanel.add(_cancelButton);

        final DCPanel centerPanel = new DCPanel();
        centerPanel.setLayout(new GridBagLayout());
        WidgetUtils.addToGridBag(formPanel, centerPanel, 0, 0, 1, 1, GridBagConstraints.NORTH, 4, 0, 0);
        WidgetUtils.addToGridBag(buttonPanel, centerPanel, 0, 2, 1, 1, GridBagConstraints.CENTER, 4, 0, 0.1);
        centerPanel.setBorder(WidgetUtils.BORDER_TOP_PADDING);

        final JXStatusBar statusBar = WidgetFactory.createStatusBar(_statusLabel);
        final DescriptionLabel descriptionLabel = new DescriptionLabel();
        descriptionLabel.setText("Configure the datastore's column names");

        final DCPanel outerPanel = new DCPanel();
        outerPanel.setLayout(new BorderLayout());
        outerPanel.add(descriptionLabel, BorderLayout.NORTH);
        outerPanel.add(centerPanel, BorderLayout.CENTER);
        outerPanel.add(statusBar, BorderLayout.SOUTH);

        checkNamesValidity();
        return WidgetUtils.scrolleable(outerPanel);
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    public List<String> getColumnNames() {
        return _columnNames;
    }

    private List<String> getNewColumnNames() {
        final List<String> newColumnNames = new LinkedList<>();
        final List<JXTextField> values = new ArrayList<>(_columnNamesTextFields.values());
        for (int i = 0; i < values.size(); i++) {
            final JXTextField jxTextField = values.get(i);
            final String text;
            final String valueTextBox = jxTextField.getText().trim();
            if (StringUtils.isNullOrEmpty(valueTextBox)) {
                text = jxTextField.getPrompt();
            } else {
                text = valueTextBox;
            }
            newColumnNames.add(text);
        }
        return newColumnNames;
    }

    private void setStatusError(final String text) {
        _statusLabel.setText(text);
        _statusLabel.setIcon(imageManager.getImageIcon(IconUtils.STATUS_ERROR, IconUtils.ICON_SIZE_SMALL));
        _saveButton.setEnabled(false);
    }

    private void setStatusValid() {
        _statusLabel.setText("Valid");
        _statusLabel.setIcon(imageManager.getImageIcon(IconUtils.STATUS_VALID, IconUtils.ICON_SIZE_SMALL));
        _saveButton.setEnabled(true);
    }

    private boolean checkNamesValidity() {
        final List<String> newColumnNames = getNewColumnNames();
        final HashSet<String> hashSet = new HashSet<>(newColumnNames);
        if (newColumnNames.size() != hashSet.size()) {
            setStatusError("Column names are not unique");
            return false;
        }
        setStatusValid();
        return true;
    }

}
