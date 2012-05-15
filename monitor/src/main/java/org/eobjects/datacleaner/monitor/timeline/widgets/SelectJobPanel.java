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
package org.eobjects.datacleaner.monitor.timeline.widgets;

import java.util.List;

import org.eobjects.datacleaner.monitor.timeline.TimelineServiceAsync;
import org.eobjects.datacleaner.monitor.timeline.model.JobIdentifier;
import org.eobjects.datacleaner.monitor.timeline.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.util.DCAsyncCallback;
import org.eobjects.datacleaner.monitor.util.LoadingIndicator;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;

/**
 * Panel which prompts the user to select a job from the repository.
 */
public abstract class SelectJobPanel extends FlowPanel {

    private final TimelineServiceAsync _service;
    private final TenantIdentifier _tenant;

    public SelectJobPanel(TimelineServiceAsync service, TenantIdentifier tenant) {
        _service = service;
        _tenant = tenant;
        addStyleName("SelectJobPanel");

        add(new LoadingIndicator());

        _service.getSavedJobs(_tenant, new DCAsyncCallback<List<JobIdentifier>>() {
            @Override
            public void onSuccess(List<JobIdentifier> result) {
                setAvailableJobs(result);
            }
        });
    }

    public void setAvailableJobs(List<JobIdentifier> availableJobs) {
        clear();

        final ListBox listBox = new ListBox(false);
        for (JobIdentifier job : availableJobs) {
            listBox.addItem(job.getName(), job.getPath());
        }

        add(listBox);

        final Button button = new Button("Select job");
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final int index = listBox.getSelectedIndex();
                final String name = listBox.getItemText(index);
                final String path = listBox.getValue(index);
                final JobIdentifier job = new JobIdentifier(name, path);
                onJobSelected(job);
            }
        });

        add(button);
    }

    public abstract void onJobSelected(JobIdentifier job);
}
