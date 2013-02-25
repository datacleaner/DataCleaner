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

import org.eobjects.datacleaner.monitor.shared.WizardService;
import org.eobjects.datacleaner.monitor.shared.WizardServiceAsync;
import org.eobjects.datacleaner.monitor.shared.model.WizardIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.util.DCAsyncCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.MenuBar;

/**
 * A button which will pop up a list of available job creation wizards, and let the user start it
 */
public class CreateJobButton extends Button implements ClickHandler {

    private final WizardServiceAsync service = GWT.create(WizardService.class);
    
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
        service.getJobWizardIdentifiers(_tenant, new DCAsyncCallback<List<WizardIdentifier>>() {
            @Override
            public void onSuccess(List<WizardIdentifier> result) {
                if (result.isEmpty()) {
                    _menuBar.addItem("(no job wizards installed)", new Command() {
                        @Override
                        public void execute() {
                            // do nothing
                        }
                    });
                }

                for (WizardIdentifier wizard : result) {
                    final String displayName = wizard.getDisplayName();
                    final StartJobWizardCommand command = new StartJobWizardCommand(service, _tenant, wizard);
                    _menuBar.addItem(displayName, command);
                }
            }
        });
    }

    @Override
    public void onClick(ClickEvent event) {
        final DCPopupPanel popup = new DCPopupPanel(null);
        popup.setGlassEnabled(false);
        popup.setAutoHideEnabled(true);
        popup.setWidget(_menuBar);
        popup.getButtonPanel().setVisible(false);
        popup.showRelativeTo(this);
    }
}
