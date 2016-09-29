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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

public class SingleDatePropertySettingDialog extends AbstractDialog implements ActionListener {
    private static final long serialVersionUID = 1L;
    
    private static final int DIALOG_WIDTH = 350;
    private static final int DIALOG_HEIGHT = 300;

    private final SingleDatePropertyWidget _widget;
    private final JXDatePicker _datePicker;
    private final JRadioButton _dateCustomRadio;
    private final JRadioButton _dateNowRadio;
    private final JRadioButton _dateTodayRadio;
    private final JRadioButton _dateYesterdayRadio;
    private final JRadioButton _nowPlusRadio;
    private final JTextField _nowPlusTextField;
    private final JButton _closeButton;

    public SingleDatePropertySettingDialog(final SingleDatePropertyWidget widget) {
        super(null, ImageManager.get().getImage("images/window/banner-tabledef.png"));

        _widget = widget;
        
        _closeButton = WidgetFactory.createPrimaryButton("Close", IconUtils.ACTION_SAVE_BRIGHT);
        _closeButton .addActionListener(e -> dispose());

        _datePicker = new JXDatePicker();
        _datePicker.setFormats("yyyy-MM-dd");
        _datePicker.addActionListener(this);

        _nowPlusTextField = new JTextField("+0d +0m +0y");
        _nowPlusTextField.addActionListener(this);
        _nowPlusTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(final FocusEvent e) {
            }

            @Override
            public void focusLost(final FocusEvent e) {
                actionPerformed(null);
            }
        });

        _dateCustomRadio = getRadioButton("Select: ");
        _dateNowRadio = getRadioButton("Now");
        _dateTodayRadio = getRadioButton("Today");
        _dateYesterdayRadio = getRadioButton("Yesterday");
        _nowPlusRadio = getRadioButton("Now plus ");
        createButtonGroup();
    }
    
    private JRadioButton getRadioButton(final String label) {
        final JRadioButton radioButton = new JRadioButton(label);
        radioButton.setBackground(WidgetUtils.BG_COLOR_BRIGHT);
        radioButton.setSelected(false);
        radioButton.addActionListener(this);
        
        return radioButton;
    }

    @Override
    protected JComponent getDialogContent() {
        final DCPanel formPanel = createFormPanel();
        final DCPanel buttonPanel = DCPanel.flow(Alignment.CENTER, _closeButton);
        final DescriptionLabel descriptionLabel = new DescriptionLabel("Option 'Now' will set the current datetime. "
            + "Option 'Now plus' can be used for relative dates and the final value will always be computed in the " 
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
        WidgetUtils.addToGridBag(_nowPlusRadio, formPanel, 0, row);
        WidgetUtils.addToGridBag(_nowPlusTextField, formPanel, 1, row);
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
        buttonGroup.add(_nowPlusRadio);
    } 
    
    public Date getValue() {
        if (_dateNowRadio.isSelected()) {
            return new NowDate();
        } else if (_dateTodayRadio.isSelected()) {
            return new TodayDate();
        } else if (_dateYesterdayRadio.isSelected()) {
            return new YesterdayDate();
        } else if (_nowPlusRadio.isSelected()) {
            return new ShiftedToday(_nowPlusTextField.getText());
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
            _nowPlusRadio.setSelected(true);
            _nowPlusTextField.setText(((ShiftedToday)value).getInput());
        } else {
            _dateCustomRadio.setSelected(true);
            if (value != null) {
                _datePicker.setDate(value);
            }
        }
    }

    @Override
    public String getWindowTitle() {
        return "Set date value";
    }
    
    @Override
    protected String getBannerTitle() {
        return "Set date value";
    }

    @Override
    protected int getDialogWidth() {
        return DIALOG_WIDTH;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final Date date = getValue();
        
        if (date != null) {
            _widget.updateValue(date.toString());
            _widget.fireValueChangedActionListener();
        }
    }
}
