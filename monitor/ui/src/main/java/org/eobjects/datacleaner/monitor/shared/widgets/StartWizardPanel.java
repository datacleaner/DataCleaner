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
package org.eobjects.datacleaner.monitor.shared.widgets;

import java.util.List;

import org.eobjects.datacleaner.monitor.shared.DatastoreService;
import org.eobjects.datacleaner.monitor.shared.DatastoreServiceAsync;
import org.eobjects.datacleaner.monitor.shared.WizardServiceAsync;
import org.eobjects.datacleaner.monitor.shared.model.DCUserInputException;
import org.eobjects.datacleaner.monitor.shared.model.DatastoreIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.WizardIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.WizardPage;
import org.eobjects.datacleaner.monitor.shared.model.WizardSessionIdentifier;
import org.eobjects.datacleaner.monitor.util.DCAsyncCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

final class StartWizardPanel extends Composite implements WizardPanel {
    
    private static final DatastoreServiceAsync datastoreService = GWT.create(DatastoreService.class);

    interface MyUiBinder extends UiBinder<Widget, StartWizardPanel> {
    }

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    private final WizardServiceAsync _service;
    private final TenantIdentifier _tenant;
    private final WizardIdentifier _wizard;

    @UiField(provided = true)
    WizardProgressBar progressBar;

    @UiField
    ListBox datastoreListBox;

    @UiField
    TextBox jobNameTextBox;

    public StartWizardPanel(WizardServiceAsync service, TenantIdentifier tenant, WizardIdentifier wizard,
            WizardProgressBar wizardProgressBar) {
        _service = service;
        _tenant = tenant;
        _wizard = wizard;

        progressBar = wizardProgressBar;

        initWidget(uiBinder.createAndBindUi(this));

        datastoreService.getAvailableDatastores(_tenant, new DCAsyncCallback<List<DatastoreIdentifier>>() {
            @Override
            public void onSuccess(List<DatastoreIdentifier> result) {
                for (DatastoreIdentifier datastoreIdentifier : result) {
                    datastoreListBox.addItem(datastoreIdentifier.getName());
                }
                jobNameTextBox.setText(_wizard.getDisplayName() + " job");
                jobNameTextBox.setFocus(true);
            }
        });
    }

    @Override
    public WizardSessionIdentifier getSessionIdentifier() {
        return null;
    }

    @Override
    public void requestNextPage(AsyncCallback<WizardPage> callback) throws DCUserInputException {
        final String jobName = jobNameTextBox.getText();
        if (jobName == null || jobName.trim().isEmpty()) {
            throw new DCUserInputException("Please enter a valid job name");
        }

        final int selectedIndex = datastoreListBox.getSelectedIndex();
        if (selectedIndex == -1) {
            throw new DCUserInputException("Please select a valid source datastore");
        }

        final String datastoreName = datastoreListBox.getItemText(selectedIndex);
        final DatastoreIdentifier selectedDatastore = new DatastoreIdentifier(datastoreName);

        _service.startJobWizard(_tenant, _wizard, selectedDatastore, jobName, callback);
    }
}
