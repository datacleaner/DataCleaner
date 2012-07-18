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
package org.eobjects.datacleaner.monitor.shared.widgets;

import java.util.List;

import org.eobjects.datacleaner.monitor.shared.JobWizardService;
import org.eobjects.datacleaner.monitor.shared.JobWizardServiceAsync;
import org.eobjects.datacleaner.monitor.shared.model.JobWizardIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.util.DCAsyncCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.MenuBar;

public class CreateJobButton extends Button implements ClickHandler {

    private final JobWizardServiceAsync service = GWT.create(JobWizardService.class);
    private final TenantIdentifier _tenant;
    private final MenuBar _menuBar;

    public CreateJobButton(TenantIdentifier tenant) {
        super("New job");
        _tenant = tenant;

        _menuBar = new MenuBar(true);
        populateMenuBar();

        addStyleDependentName("ImageTextButton");
        addStyleName("NewJobButton");

        addClickHandler(this);
    }

    private void populateMenuBar() {
        service.getJobWizardIdentifiers(_tenant, new DCAsyncCallback<List<JobWizardIdentifier>>() {
            @Override
            public void onSuccess(List<JobWizardIdentifier> result) {
                for (JobWizardIdentifier wizard : result) {
                    final String displayName = wizard.getDisplayName();
                    final StartWizardCommand command = new StartWizardCommand(service, _tenant, wizard);
                    _menuBar.addItem(displayName, command);
                }
            }
        });
    }

    @Override
    public void onClick(ClickEvent event) {
        DCPopupPanel popup = new DCPopupPanel(null);
        popup.setGlassEnabled(false);
        popup.setWidget(_menuBar);
        popup.getButtonPanel().setVisible(false);
        popup.showRelativeTo(this);
    }
}
