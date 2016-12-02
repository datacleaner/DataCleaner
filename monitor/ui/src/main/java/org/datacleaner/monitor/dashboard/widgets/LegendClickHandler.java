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
package org.datacleaner.monitor.dashboard.widgets;

import org.datacleaner.monitor.shared.model.MetricIdentifier;
import org.datacleaner.monitor.shared.widgets.CancelPopupButton;
import org.datacleaner.monitor.shared.widgets.DCButtons;
import org.datacleaner.monitor.shared.widgets.DCPopupPanel;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;

public class LegendClickHandler implements ClickHandler {

    private final TimelinePanel _timeLinePanel;
    private String _data;
    private MetricIdentifier _metricIdentifier;
    private Legend _legend;
    private Boolean _isDashboardEditor;

    public LegendClickHandler(final String data, final MetricIdentifier metricIdentifier,
            final TimelinePanel timelinePanel, final Legend legend, final boolean isDashboardEditor) {
        _data = data;
        _metricIdentifier = metricIdentifier;
        _timeLinePanel = timelinePanel;
        _legend = legend;
        _isDashboardEditor = isDashboardEditor;
    }

    @Override
    public void onClick(final ClickEvent event) {
        if (_isDashboardEditor) {
            final PopupPanel popupPanel = new PopupPanel(true);
            final MenuBar popupMenuBar = populateLegendMenu(popupPanel);
            popupPanel.setWidget(popupMenuBar);
            popupPanel.showRelativeTo(_legend);
        }
    }

    private MenuBar populateLegendMenu(final PopupPanel popupPanel) {
        final MenuBar popupMenuBar = new MenuBar(true);
        final MenuItem editMetricNameItem = new MenuItem("Edit metric name", true, new Command() {
            @Override
            public void execute() {
                popupPanel.hide();
                showEditMetricNamePopup();
            }
        });
        popupMenuBar.addItem(editMetricNameItem);

        final MenuItem editLegendColorItem = new MenuItem("Change color", true, new Command() {
            @Override
            public void execute() {
                popupPanel.hide();
                configureEditLegendColorPopup();
            }
        });

        popupMenuBar.addItem(editLegendColorItem);
        return popupMenuBar;
    }

    protected void configureEditLegendColorPopup() {
        final DCPopupPanel popup = new DCPopupPanel("Change color");
        popup.addStyleName("CreateTimelinePopupPanel");
        final SelectColorPanel selectColorPanel = new SelectColorPanel(_metricIdentifier.getMetricColor());
        final Button saveButton = configureSaveColorButton(popup, selectColorPanel);
        popup.setWidget(selectColorPanel);
        popup.addButton(saveButton);
        popup.addButton(new CancelPopupButton(popup));
        popup.center();
        popup.show();
    }

    private Button configureSaveColorButton(final DCPopupPanel popUp, final SelectColorPanel selectColorPanel) {
        final Button saveButton = DCButtons.primaryButton("glyphicon-save", "Save");
        saveButton.setVisible(true);
        saveButton.setTitle("Save");
        saveButton.addStyleName("SaveButton");
        saveButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(final ClickEvent event) {
                final String selectedColor = selectColorPanel.getSelectedColor();
                popUp.hide();
                boolean isSaveTimelineActive = false;
                _metricIdentifier.setMetricColor(selectedColor);
                isSaveTimelineActive = true;
                _timeLinePanel.refreshTimelineDefiniton(isSaveTimelineActive);
            }
        });
        return saveButton;
    }

    private void showEditMetricNamePopup() {
        final DCPopupPanel editMetricPopUp = new DCPopupPanel("Edit Metric Name");
        final TextBox textBox = new TextBox();
        textBox.setText(_data);
        final Button saveButton = configureSaveButton(editMetricPopUp, textBox);
        editMetricPopUp.setWidget(textBox);
        editMetricPopUp.removeButtons();
        editMetricPopUp.addButton(saveButton);
        editMetricPopUp.addButton(new CancelPopupButton(editMetricPopUp));
        editMetricPopUp.center();
        editMetricPopUp.show();
    }

    private Button configureSaveButton(final DCPopupPanel popUp, final TextBox textBox) {
        final Button saveButton = DCButtons.primaryButton("glyphicon-save", "Save");
        saveButton.setVisible(true);
        saveButton.setTitle("Save");
        saveButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(final ClickEvent event) {
                final String text = textBox.getText();
                _metricIdentifier.setMetricDisplayName(text);
                popUp.hide();
                _timeLinePanel.refreshTimelineDefiniton(true);
            }
        });
        return saveButton;
    }

}
