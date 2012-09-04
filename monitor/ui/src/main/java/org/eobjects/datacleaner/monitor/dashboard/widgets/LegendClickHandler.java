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
package org.eobjects.datacleaner.monitor.dashboard.widgets;

import org.eobjects.datacleaner.monitor.shared.model.MetricIdentifier;
import org.eobjects.datacleaner.monitor.shared.widgets.CancelPopupButton;
import org.eobjects.datacleaner.monitor.shared.widgets.DCPopupPanel;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;

public class LegendClickHandler implements ClickHandler {

    private String _data;
    private MetricIdentifier _metricIdentifier;
    private final TimelinePanel _timeLinePanel;
    private Legend _legend;

    public LegendClickHandler(String data, MetricIdentifier metricIdentifier, TimelinePanel timelinePanel, Legend legend) {
        _data = data;
        _metricIdentifier = metricIdentifier;
        _timeLinePanel = timelinePanel;
        _legend = legend;
    }

    @Override
    public void onClick(ClickEvent event) {
        final PopupPanel popupPanel = new PopupPanel(true);
        MenuBar popupMenuBar = new MenuBar(true);
        MenuItem alertItem = new MenuItem("Edit Metric Name", true, new Command() {

            @Override
            public void execute() {
                popupPanel.hide();
                final DCPopupPanel editMetricPopUp = new DCPopupPanel("Edit Metric Name");
                final TextBox textBox = new TextBox();
                textBox.setText(_data);
                Button saveButton = configureSaveButton(editMetricPopUp, textBox);
                editMetricPopUp.setWidget(textBox);
                editMetricPopUp.removeButtons();
                editMetricPopUp.addButton(saveButton);
                editMetricPopUp.addButton(new CancelPopupButton(editMetricPopUp));
                editMetricPopUp.center();
                editMetricPopUp.show();
            }

        });

        popupMenuBar.addItem(alertItem);
        popupPanel.setWidget(popupMenuBar);
        popupPanel.showRelativeTo(_legend);
    }

    private Button configureSaveButton(final DCPopupPanel popUp, final TextBox textBox) {
        Button saveButton = new Button("Save");
        saveButton.setVisible(true);
        saveButton.setTitle("Save");
        saveButton.addStyleName("SaveButton");
        saveButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                String text = textBox.getText();
                _metricIdentifier.setMetricDisplayName(text);
                popUp.hide();
                _timeLinePanel.refreshTimelineDefiniton();
            }
        });
        return saveButton;
    }

}
