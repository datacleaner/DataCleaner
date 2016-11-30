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

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.Border;

import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.util.convert.NowDate;
import org.datacleaner.util.convert.ShiftedToday;
import org.datacleaner.util.convert.TodayDate;
import org.datacleaner.util.convert.YesterdayDate;
import org.datacleaner.widgets.Alignment;
import org.datacleaner.widgets.DescriptionLabel;
import org.datacleaner.windows.AbstractDialog;
import org.jdesktop.swingx.JXDatePicker;

public class SingleDatePropertySettingDialog extends AbstractDialog {
    public static final String LABEL_PARTICULAR = "Particular date";
    public static final String LABEL_TODAY = "Today";
    private static final long serialVersionUID = 1L;
    private static final int DIALOG_WIDTH = 270;
    private static final int DIALOG_HEIGHT = 390;
    private static final String TITLE = "Set date value";
    private static final String STATIC_GROUP = "Fixed date";
    private static final String DYNAMIC_GROUP = "Resolved at execution";
    private static final String IMAGE_PATH = "images/window/banner-tabledef.png";
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String TODAY_PLUS_DEFAULT_VALUE = "+0d +0m +0y";
    private static final String LABEL_NOW = "Now";
    private static final String LABEL_TODAY_PLUS = "Today plus";
    private static final String LABEL_YESTERDAY = "Yesterday";

    private final SingleDatePropertyWidget _widget;
    private final ButtonGroup _buttonGroup;
    private final JXDatePicker _datePicker;
    private final JRadioButton _dateNowRadio;
    private final JRadioButton _dateTodayRadio;
    private final JRadioButton _dateYesterdayRadio;
    private final JRadioButton _todayPlusRadio;
    private final JTextField _todayPlusTextField;
    private final JButton _closeButton;

    protected SingleDatePropertySettingDialog(final WindowContext windowContext,
            final SingleDatePropertyWidget widget) {
        super(windowContext, ImageManager.get().getImage(IMAGE_PATH));

        _widget = widget;
        _buttonGroup = new ButtonGroup();
        _closeButton = createCloseButton();

        _dateNowRadio = getRadioButton(LABEL_NOW);
        _dateTodayRadio = getRadioButton(LABEL_TODAY);
        _todayPlusRadio = getRadioButton(LABEL_TODAY_PLUS);
        _dateYesterdayRadio = getRadioButton(LABEL_YESTERDAY);
        createRadioGroup();

        _datePicker = createDatePicker();
        _todayPlusTextField = createTodayPlusTextField();
    }

    private JButton createCloseButton() {
        final JButton closeButton = WidgetFactory.createPrimaryButton("Close", IconUtils.ACTION_SAVE_BRIGHT);
        closeButton.addActionListener(e -> {
            if (isParticularDateSpecified()) {
                updateWidget();
            }

            dispose();
        });

        return closeButton;
    }

    private boolean isParticularDateSpecified() {
        return (!_dateNowRadio.isSelected() && !_dateTodayRadio.isSelected() && !_dateYesterdayRadio.isSelected()
                && !_todayPlusRadio.isSelected() && !_datePicker.getEditor().getText().isEmpty());
    }

    private JTextField createTodayPlusTextField() {
        final JTextField textField = new JTextField(TODAY_PLUS_DEFAULT_VALUE);
        textField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(final FocusEvent e) {
                _todayPlusRadio.setSelected(true);
            }

            @Override
            public void focusLost(final FocusEvent e) {
                updateWidget();
            }
        });

        return textField;
    }


    private void updateWidget() {
        final Date date = getValue();

        if (date != null) {
            final String formattedString = getFormattedString();
            _widget.updateValue(formattedString);
            _widget.fireValueChangedActionListener();
        }
    }

    public String getFormattedString() {
        if (_dateNowRadio.isSelected() || _dateTodayRadio.isSelected() || _dateYesterdayRadio.isSelected()
                || _todayPlusRadio.isSelected()) {
            return String.format("%s", getLabelForFormattedString());
        } else {
            return String.format("%s", getValue().toString());
        }
    }

    private String getLabelForFormattedString() {
        final String label;

        if (_dateNowRadio.isSelected()) {
            label = LABEL_NOW;
        } else if (_dateTodayRadio.isSelected()) {
            label = LABEL_TODAY;
        } else if (_dateYesterdayRadio.isSelected()) {
            label = LABEL_YESTERDAY;
        } else if (_todayPlusRadio.isSelected()) {
            label = LABEL_TODAY_PLUS + " [" + _todayPlusTextField.getText() + "]";
        } else {
            label = LABEL_PARTICULAR;
        }

        return label;
    }

    private JXDatePicker createDatePicker() {
        final JXDatePicker datePicker = new JXDatePicker();
        datePicker.setFormats(DATE_FORMAT);
        datePicker.addActionListener(e -> {
            clearRadioGroup();
            updateWidget();
        });
        datePicker.getEditor().addFocusListener(new FocusListener() {
            @Override
            public void focusGained(final FocusEvent e) {
                clearRadioGroup();
            }

            @Override
            public void focusLost(final FocusEvent e) {
                // this for some reason does not work properly, so value can not be updated here
            }
        });

        return datePicker;
    }

    private JRadioButton getRadioButton(final String label) {
        final JRadioButton radioButton = new JRadioButton(label);
        radioButton.setBackground(WidgetUtils.BG_COLOR_BRIGHT);
        radioButton.setSelected(false);
        radioButton.addActionListener(e -> updateWidget());

        return radioButton;
    }

    @Override
    protected JComponent getDialogContent() {
        final DCPanel formPanel = createFormPanel();
        final DCPanel buttonPanel = DCPanel.flow(Alignment.CENTER, _closeButton);
        final DescriptionLabel descriptionLabel = new DescriptionLabel("Option 'Now' will set the current datetime. "
                + "Option 'Today plus' can be used for relative dates and the final value will always be computed in the "
                + "runtime. Options 'Today' and 'Yesterday' will set today's or yesterday's date with time of 0:00. ");

        final DCPanel mainPanel = new DCPanel();
        mainPanel.setOpaque(true);
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(descriptionLabel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        mainPanel.setPreferredSize(getDialogWidth(), DIALOG_HEIGHT);

        return mainPanel;
    }

    private DCPanel createFormPanel() {
        final JPanel staticPanel = createStaticPanel();
        final JPanel dynamicPanel = createDynamicPanel();
        final DCPanel formPanel = new DCPanel();
        WidgetUtils.addToGridBag(staticPanel, formPanel, 0, 1);
        WidgetUtils.addToGridBag(dynamicPanel, formPanel, 0, 2);

        return formPanel;
    }

    private JPanel createStaticPanel() {
        final JPanel panel = new JPanel(new GridLayout(0, 1));
        final Border border = BorderFactory.createTitledBorder(STATIC_GROUP);
        panel.setBorder(border);
        WidgetUtils.addToGridBag(new JLabel(LABEL_PARTICULAR), panel, 0, 0);
        WidgetUtils.addToGridBag(_datePicker, panel, 1, 0);

        return panel;
    }

    private JPanel createDynamicPanel() {
        final JPanel panel = new JPanel(new GridLayout(0, 1));
        final Border border = BorderFactory.createTitledBorder(DYNAMIC_GROUP);
        panel.setBorder(border);
        int row = 0;
        WidgetUtils.addToGridBag(_todayPlusRadio, panel, 0, row);
        WidgetUtils.addToGridBag(_todayPlusTextField, panel, 1, row);
        row++;
        WidgetUtils.addToGridBag(_dateNowRadio, panel, 0, row);
        row++;
        WidgetUtils.addToGridBag(_dateTodayRadio, panel, 0, row);
        row++;
        WidgetUtils.addToGridBag(_dateYesterdayRadio, panel, 0, row);

        return panel;
    }

    private void createRadioGroup() {
        _buttonGroup.add(_dateNowRadio);
        _buttonGroup.add(_dateTodayRadio);
        _buttonGroup.add(_dateYesterdayRadio);
        _buttonGroup.add(_todayPlusRadio);
    }

    private void clearRadioGroup() {
        _buttonGroup.clearSelection();
    }

    public Date getValue() {
        if (_dateNowRadio.isSelected()) {
            return new NowDate();
        } else if (_dateTodayRadio.isSelected()) {
            return new TodayDate();
        } else if (_dateYesterdayRadio.isSelected()) {
            return new YesterdayDate();
        } else if (_todayPlusRadio.isSelected()) {
            return new ShiftedToday(_todayPlusTextField.getText());
        } else {
            return _datePicker.getDate();
        }
    }

    protected void setValue(final Date value) {
        if (value instanceof NowDate) {
            _dateNowRadio.setSelected(true);
        } else if (value instanceof TodayDate) {
            _dateTodayRadio.setSelected(true);
        } else if (value instanceof YesterdayDate) {
            _dateYesterdayRadio.setSelected(true);
        } else if (value instanceof ShiftedToday) {
            _todayPlusRadio.setSelected(true);
            _todayPlusTextField.setText(((ShiftedToday) value).getInput());
        } else if (value != null) {
            clearRadioGroup();
            _datePicker.setDate(value);
        }
    }

    @Override
    public String getWindowTitle() {
        return TITLE;
    }

    @Override
    protected String getBannerTitle() {
        return TITLE;
    }

    @Override
    protected int getDialogWidth() {
        return DIALOG_WIDTH;
    }
}
