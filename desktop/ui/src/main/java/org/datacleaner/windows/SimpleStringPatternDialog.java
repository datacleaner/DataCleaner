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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.reference.SimpleStringPattern;
import org.datacleaner.user.MutableReferenceDataCatalog;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.Alignment;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.widgets.DescriptionLabel;
import org.jdesktop.swingx.JXTextField;

public final class SimpleStringPatternDialog extends AbstractDialog {

    private static final long serialVersionUID = 1L;

    private final MutableReferenceDataCatalog _catalog;
    private final JXTextField _expressionField;
    private final JXTextField _expressionNameField;

    private List<JTextField> _inputFields;
    private String _expressionString;
    private String _expressionNameString;
    private List<JLabel> _statusLabels;
    private JLabel _errorLabel;
    private static final int NUM_TEST_FIELDS = 6;
    private JButton _resetButton;
    final JButton _saveButton;
    private static final ImageManager imageManager = ImageManager.get();
    private SimpleStringPattern _simpleStringPattern;

    private static final Icon ICON_ERROR = imageManager.getImageIcon(IconUtils.STATUS_ERROR, IconUtils.ICON_SIZE_SMALL);

    private static final Icon ICON_SUCCESS = imageManager.getImageIcon(IconUtils.STATUS_VALID,
            IconUtils.ICON_SIZE_SMALL);

    public SimpleStringPatternDialog(MutableReferenceDataCatalog catalog, WindowContext windowContext) {
        super(windowContext, ImageManager.get().getImage(IconUtils.STRING_PATTERN_SIMPLE_IMAGEPATH));
        _catalog = catalog;
        _expressionNameField = WidgetFactory.createTextField("String pattern name");
        _expressionField = WidgetFactory.createTextField("Expression");
        _resetButton = WidgetFactory.createSmallButton("Reset", IconUtils.ACTION_RESET);
        _saveButton = WidgetFactory.createPrimaryButton("Save Pattern", IconUtils.ACTION_SAVE_BRIGHT);
    }

    public SimpleStringPatternDialog(SimpleStringPattern stringPattern, MutableReferenceDataCatalog catalog,
            WindowContext windowContext) {
        this(stringPattern.getName(), stringPattern.getExpression(), catalog, windowContext);
    }

    public SimpleStringPatternDialog(String expressionName, String expression, MutableReferenceDataCatalog catalog,
            WindowContext windowContext) {
        this(catalog, windowContext);
        _expressionString = expression;
        _expressionNameString = expressionName;
        _expressionNameField.setText(expressionName);
        _expressionField.setText(expression);
        _simpleStringPattern = (SimpleStringPattern) _catalog.getStringPattern(_expressionNameString);
    }

    @Override
    protected String getBannerTitle() {
        return "Simple string pattern";
    }

    @Override
    protected int getDialogWidth() {
        return 465;
    }

    @Override
    protected JComponent getDialogContent() {
        final DCPanel formPanel = new DCPanel();
        int row = 0;
        WidgetUtils.addToGridBag(DCLabel.bright("String pattern name:"), formPanel, 0, row);
        WidgetUtils.addToGridBag(_expressionNameField, formPanel, 1, row, 1.0, 0.0);

        row++;
        WidgetUtils.addToGridBag(DCLabel.bright("Expression:"), formPanel, 0, row);

        _expressionField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                checkInputFields();
            }

            public void insertUpdate(DocumentEvent e) {
                checkInputFields();
            }

            public void removeUpdate(DocumentEvent e) {
                checkInputFields();
            }
        });
        WidgetUtils.addToGridBag(_expressionField, formPanel, 1, row, 1.0, 0.0);

        _resetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                _expressionField.setText(_expressionString);
            }
        });
        WidgetUtils.addToGridBag(_resetButton, formPanel, 2, row);

        row++;

        _saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String expressionName = _expressionNameField.getText();
                if (StringUtils.isNullOrEmpty(expressionName)) {
                    JOptionPane.showMessageDialog(SimpleStringPatternDialog.this,
                            "Please fill out the name of the string expression");
                    return;
                }

                String expression = _expressionField.getText();
                if (StringUtils.isNullOrEmpty(expression)) {
                    JOptionPane.showMessageDialog(SimpleStringPatternDialog.this,
                            "Please fill out the string expression");
                    return;
                }
                final SimpleStringPattern simpleStringPattern = new SimpleStringPattern(expressionName, expression);
                if (_simpleStringPattern != null && _catalog.containsStringPattern(_simpleStringPattern.getName())) {
                    _catalog.changeStringPattern(_simpleStringPattern, simpleStringPattern);
                } else {
                    _catalog.addStringPattern(simpleStringPattern);
                }
                _simpleStringPattern = simpleStringPattern;
                SimpleStringPatternDialog.this.dispose();
            }
        });

        final DCPanel buttonPanel = DCPanel.flow(Alignment.CENTER, _saveButton);
        WidgetUtils.addToGridBag(buttonPanel, formPanel, 0, row, 2, 1);

        final DCPanel testitPanel = new DCPanel();
        testitPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));

        _errorLabel = DCLabel.bright("");
        WidgetUtils.addToGridBag(_errorLabel, testitPanel, 0, row);

        row++;
        JLabel testInputLabel = DCLabel.bright("You can test your expression here");
        testInputLabel.setIcon(imageManager.getImageIcon("images/actions/test-pattern.png"));
        testInputLabel.setFont(WidgetUtils.FONT_HEADER2);
        WidgetUtils.addToGridBag(testInputLabel, testitPanel, 0, row);

        _inputFields = new ArrayList<JTextField>(NUM_TEST_FIELDS);
        _statusLabels = new ArrayList<JLabel>(NUM_TEST_FIELDS);
        for (int i = 0; i < NUM_TEST_FIELDS; i++) {
            final int index = i;
            JTextField inputField = WidgetFactory.createTextField("Test Input");
            inputField.getDocument().addDocumentListener(new DocumentListener() {
                public void changedUpdate(DocumentEvent e) {
                    checkInputField(index);
                }

                public void insertUpdate(DocumentEvent e) {
                    checkInputField(index);
                }

                public void removeUpdate(DocumentEvent e) {
                    checkInputField(index);
                }
            });
            // inputField.setPreferredSize(d);
            WidgetUtils.addToGridBag(inputField, testitPanel, 0, 4 + i);

            JLabel statusLabel = new JLabel();
            WidgetUtils.addToGridBag(statusLabel, testitPanel, 1, 4 + i);

            _inputFields.add(inputField);
            _statusLabels.add(statusLabel);
        }

        final DescriptionLabel descriptionLabel = new DescriptionLabel(
                "<p>Simple string patterns are tokenized patterns made up of these elements.</p>"
                        + "<p>* A = upper case letter<br>* a = lower case letters<br>* 9 = digits</p>");

        final DCPanel mainPanel = new DCPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(descriptionLabel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(testitPanel, BorderLayout.SOUTH);
        mainPanel.setPreferredSize(getDialogWidth(), 450);

        return mainPanel;
    }

    @Override
    protected boolean isWindowResizable() {
        return true;
    }

    @Override
    public String getWindowTitle() {
        return "Simple string pattern";
    }

    private void checkInputFields() {
        _errorLabel.setText("");
        for (int i = 0; i < NUM_TEST_FIELDS; i++) {
            checkInputField(i);
        }

    }

    private void checkInputField(int index) {
        String text = _inputFields.get(index).getText();
        JLabel label = _statusLabels.get(index);
        if ("".equals(text)) {
            label.setIcon(null);
        } else {
            final SimpleStringPattern simpleStringPattern = new SimpleStringPattern(_expressionNameField.getText(), _expressionField.getText());
            if (simpleStringPattern.matches(text)) {
                label.setIcon(ICON_SUCCESS);
            } else {
                label.setIcon(ICON_ERROR);
            }
        }
    }

}
