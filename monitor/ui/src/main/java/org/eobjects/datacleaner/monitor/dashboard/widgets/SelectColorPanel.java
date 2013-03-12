/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.monitor.dashboard.widgets;

import org.eobjects.datacleaner.monitor.shared.widgets.ColorBox;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;

public class SelectColorPanel extends FlowPanel {

    private String _colorString;
    private RadioButton _rbAutoSelectColor;
    private RadioButton _rbManualSelectColor;
    private RadioButton _rbChoosePredefinedColor;
    private ColorBox _colorBox;
    private ListBox _predefinedListBox;

    public SelectColorPanel(String colorString) {
        _colorString = colorString;
        renderColorPanel();
    }

    private void renderColorPanel() {
        addStyleName("SelectColorPanel");
        HTML description = new HTML("<p>Please select one of the following approaches for determining the metric's color in the timeline.</p>");
        description.setStyleName("descriptionLabel");
        add(description);
        _rbAutoSelectColor = new RadioButton("colorGroup", "Automatically apply a color");
        _rbManualSelectColor = new RadioButton("colorGroup", "Select color from palette");
        _rbChoosePredefinedColor = new RadioButton("colorGroup", "Choose predefined color");
        if ("".equals(_colorString) || _colorString == null) {
            _rbAutoSelectColor.setValue(true);
        } else {
            _rbManualSelectColor.setValue(true);
        }
        configureColorPanels();

    }

    private void configureColorPanels() {
        configureAutoSelectColorPanel();
        configureManualSelectColorPanel();
        configurePredefinedColorPanel();
    }

    private void configureAutoSelectColorPanel() {
        FlowPanel autoSelectPanel = new FlowPanel();
        autoSelectPanel.add(_rbAutoSelectColor);
        autoSelectPanel.addStyleName("colorPanel");

        add(autoSelectPanel);
    }

    private void configureManualSelectColorPanel() {
        FlowPanel manualSelectPanel = new FlowPanel();
        FlowPanel manualSelectColorPanel = new FlowPanel();

        manualSelectColorPanel.addStyleName("configurationPanel");
        _colorBox = new ColorBox(_colorString);
        
        _colorBox.getTextBox().addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {
                _rbManualSelectColor.setValue(true);
            }
        });
        
        manualSelectColorPanel.add(_colorBox);

        manualSelectPanel.add(_rbManualSelectColor);
        manualSelectPanel.add(manualSelectColorPanel);
        manualSelectPanel.addStyleName("colorPanel");

        add(manualSelectPanel);
    }

    private void configurePredefinedColorPanel() {
        FlowPanel predefinedPanel = new FlowPanel();
        FlowPanel predefinedColorPanel = new FlowPanel();

        _predefinedListBox = new ListBox();
        for (int i = 0; i < PredefinedColors.values().length; i++) {
            _predefinedListBox.addItem(PredefinedColors.values()[i].getName(), PredefinedColors.values()[i].getColor().toHexString());
        }
        _predefinedListBox.addClickHandler(new ClickHandler(){

            @Override
            public void onClick(ClickEvent arg0) {
                _rbChoosePredefinedColor.setValue(true);
            }
            
        });
        
        predefinedColorPanel.addStyleName("configurationPanel");
        predefinedColorPanel.add(_predefinedListBox);
        
        predefinedPanel.add(_rbChoosePredefinedColor);
        predefinedPanel.add(predefinedColorPanel);
        predefinedPanel.addStyleName("colorPanel");
        add(predefinedPanel);
    }

    public String getSelectedColor() {

        if (_rbManualSelectColor.getValue()) {
            return _colorBox.getTextBox().getText();
        }

        if (_rbChoosePredefinedColor.getValue()) {
            return _predefinedListBox.getValue(_predefinedListBox.getSelectedIndex());
        }

        return "";
    }

}
