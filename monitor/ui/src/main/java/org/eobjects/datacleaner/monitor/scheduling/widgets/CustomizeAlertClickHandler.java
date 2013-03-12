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
package org.eobjects.datacleaner.monitor.scheduling.widgets;

import org.eobjects.datacleaner.monitor.scheduling.model.AlertDefinition;
import org.eobjects.datacleaner.monitor.shared.DescriptorService;
import org.eobjects.datacleaner.monitor.shared.DescriptorServiceAsync;
import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.JobMetrics;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.shared.widgets.CancelPopupButton;
import org.eobjects.datacleaner.monitor.shared.widgets.DCPopupPanel;
import org.eobjects.datacleaner.monitor.util.DCAsyncCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.UIObject;

/**
 * Clickhandler invoked when an alert is clicked. The user will be presented
 * with a dialog to customize/edit the alert.
 */
public class CustomizeAlertClickHandler implements ClickHandler {

    private static final DescriptorServiceAsync descriptorService = GWT.create(DescriptorService.class);
    
    private final AlertPanel _alertPanel;

    public CustomizeAlertClickHandler(AlertPanel alertPanel) {
        _alertPanel = alertPanel;
    }

    @Override
    public void onClick(ClickEvent event) {
        final MenuBar menuBar = new MenuBar(true);

        menuBar.addItem("Edit alert", new Command() {
            @Override
            public void execute() {
                final DCPopupPanel popup = new DCPopupPanel("Edit alert");

                final TenantIdentifier tenant = _alertPanel.getSchedule().getTenant();
                final JobIdentifier job = _alertPanel.getSchedule().getJob();
                final AlertDefinition alert = _alertPanel.getAlert();
                
                descriptorService.getJobMetrics(tenant, job, new DCAsyncCallback<JobMetrics>() {
                    @Override
                    public void onSuccess(JobMetrics jobMetrics) {
                        final CustomizeAlertPanel customizeAlertPanel = new CustomizeAlertPanel(tenant, job, alert, jobMetrics);
                        final Button button = new Button("Save alert");
                        button.addClickHandler(new ClickHandler() {
                            @Override
                            public void onClick(ClickEvent event) {
                                customizeAlertPanel.updateAlert();
                                _alertPanel.updateAlert();
                                popup.hide();
                            }
                        });
                        
                        popup.setWidget(customizeAlertPanel);
                        popup.addButton(button);
                        popup.addButton(new CancelPopupButton(popup));
                        popup.center();
                        popup.show();
                    }
                });
            }
        });
        
        menuBar.addItem("Remove alert", new Command() {
            @Override
            public void execute() {
                _alertPanel.removeAlert();
            }
        });

        final DCPopupPanel popup = new DCPopupPanel(null);
        popup.setGlassEnabled(false);
        popup.setWidget(menuBar);
        popup.setAutoHideEnabled(true);
        popup.getButtonPanel().setVisible(false);
        popup.showRelativeTo((UIObject) event.getSource());
    }

}
