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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Date;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

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
    private static final long serialVersionUID = 1L;
    
    private static final int DIALOG_WIDTH = 350;
    private static final int DIALOG_HEIGHT = 300;

    private static final String TITLE = "Set date value";
    private static final String IMAGE_PATH = "images/window/banner-tabledef.png";
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String TODAY_PLUS_DEFAULT_VALUE = "+0d +0m +0y";
    public static final String LABEL_PARTICULAR = "Particular date";
    private static final String LABEL_NOW = "Now";
    public static final String LABEL_TODAY = "Today";
    private static final String LABEL_TODAY_PLUS = "Today plus";
    private static final String LABEL_YESTERDAY = "Yesterday";

    private final SingleDatePropertyWidget _widget;
    private final JXDatePicker _datePicker;
    private final JRadioButton _dateCustomRadio;
    private final JRadioButton _dateNowRadio;
    private final JRadioButton _dateTodayRadio;
    private final JRadioButton _dateYesterdayRadio;
    private final JRadioButton _todayPlusRadio;
    private final JTextField _todayPlusTextField;
    private final JButton _closeButton;

    public SingleDatePropertySettingDialog(final SingleDatePropertyWidget widget) {
        super(null, ImageManager.get().getImage(IMAGE_PATH));

        _widget = widget;
        _closeButton = createCloseButton();
        
        _dateCustomRadio = getRadioButton(LABEL_PARTICULAR);
        _dateNowRadio = getRadioButton(LABEL_NOW);
        _dateTodayRadio = getRadioButton(LABEL_TODAY);
        _todayPlusRadio = getRadioButton(LABEL_TODAY_PLUS);
        _dateYesterdayRadio = getRadioButton(LABEL_YESTERDAY);
        createButtonGroup();

        _datePicker = createDatePicker();
        _todayPlusTextField = createTodayPlusTextField();
    }
    
    private JButton createCloseButton() {
        final JButton closeButton = WidgetFactory.createPrimaryButton("Close", IconUtils.ACTION_SAVE_BRIGHT);
        closeButton .addActionListener(e -> {
            dispose();
        });
        
        return closeButton;
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
            final String formattedString = getFormattedString(getLabelForFormattedString(), date);
            _widget.updateValue(formattedString);
            _widget.fireValueChangedActionListener();
        }
    }
    
    public String getFormattedString(String label, Date date) {
        return String.format("%s (%s)", label, date.toString());
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
            _dateCustomRadio.setSelected(true);
            updateWidget();
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
        final DCPanel formPanel = new DCPanel();
        int row = 0;
        WidgetUtils.addToGridBag(_dateCustomRadio, formPanel, 0, row);
        WidgetUtils.addToGridBag(_datePicker, formPanel, 1, row);
        row++;
        WidgetUtils.addToGridBag(_todayPlusRadio, formPanel, 0, row);
        WidgetUtils.addToGridBag(_todayPlusTextField, formPanel, 1, row);
        row++;
        WidgetUtils.addToGridBag(_dateNowRadio, formPanel, 0, row);
        row++;
        WidgetUtils.addToGridBag(_dateTodayRadio, formPanel, 0, row);
        row++;
        WidgetUtils.addToGridBag(_dateYesterdayRadio, formPanel, 0, row);
        
        return formPanel;
    }
    
    private void createButtonGroup() {
        final ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(_dateCustomRadio);
        buttonGroup.add(_dateNowRadio);
        buttonGroup.add(_dateTodayRadio);
        buttonGroup.add(_dateYesterdayRadio);
        buttonGroup.add(_todayPlusRadio);
        _dateCustomRadio.setSelected(true);
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
            _todayPlusTextField.setText(((ShiftedToday)value).getInput());
        } else {
            _dateCustomRadio.setSelected(true);
            if (value != null) {
                _datePicker.setDate(value);
            }
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
