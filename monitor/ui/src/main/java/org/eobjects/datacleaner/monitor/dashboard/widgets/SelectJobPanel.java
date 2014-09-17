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
package org.eobjects.datacleaner.monitor.dashboard.widgets;

import java.util.List;

import org.eobjects.datacleaner.monitor.dashboard.DashboardServiceAsync;
import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.shared.widgets.HeadingLabel;
import org.eobjects.datacleaner.monitor.shared.widgets.LoadingIndicator;
import org.eobjects.datacleaner.monitor.util.DCAsyncCallback;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;

/**
 * Panel which prompts the user to select a job from the repository.
 */
public abstract class SelectJobPanel extends FlowPanel {

    private final DashboardServiceAsync _service;
    private final TenantIdentifier _tenant;
    private final LoadingIndicator _loadingIndicator;
    private ListBox _listBox;

    public SelectJobPanel(DashboardServiceAsync service, TenantIdentifier tenant) {
        _service = service;
        _tenant = tenant;
        _loadingIndicator = new LoadingIndicator();
        _listBox = new ListBox(false);
        addStyleName("SelectJobPanel");

        add(new HeadingLabel("Select job to build timeline from"));
        add(_loadingIndicator);

        _service.getJobs(_tenant, new DCAsyncCallback<List<JobIdentifier>>() {
            @Override
            public void onSuccess(List<JobIdentifier> result) {
                setAvailableJobs(result);
            }
        });
    }

    public void setAvailableJobs(List<JobIdentifier> availableJobs) {
        remove(_loadingIndicator);

        _listBox.clear();

        for (JobIdentifier job : availableJobs) {
            _listBox.addItem(job.getName(), job.getName());
        }

        add(_listBox);
    }

    public Button createSelectButton() {
        final Button button = new Button("Select job");
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final int index = _listBox.getSelectedIndex();
                final String name = _listBox.getItemText(index);
                final JobIdentifier job = new JobIdentifier(name);
                onJobSelected(job);
            }
        });
        return button;
    }

    public abstract void onJobSelected(JobIdentifier job);
}
