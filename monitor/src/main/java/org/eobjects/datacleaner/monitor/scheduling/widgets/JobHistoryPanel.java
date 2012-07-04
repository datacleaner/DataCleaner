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
package org.eobjects.datacleaner.monitor.scheduling.widgets;

import java.util.List;

import org.eobjects.datacleaner.monitor.scheduling.SchedulingServiceAsync;
import org.eobjects.datacleaner.monitor.scheduling.model.ExecutionIdentifier;
import org.eobjects.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.util.DCAsyncCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Panel for showing the history of a job.
 */
public class JobHistoryPanel extends Composite {

    interface MyUiBinder extends UiBinder<Widget, JobHistoryPanel> {
    }

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    private final JobIdentifier _job;
    private final SchedulingServiceAsync _service;
    private final TenantIdentifier _tenant;

    @UiField
    SimplePanel executionLogPanelTarget;

    public JobHistoryPanel(JobIdentifier job, SchedulingServiceAsync service, TenantIdentifier tenant) {
        super();
        _job = job;
        _service = service;
        _tenant = tenant;

        initWidget(uiBinder.createAndBindUi(this));

        final CellList<ExecutionIdentifier> cellList = new CellList<ExecutionIdentifier>(new ExecutionIdentifierCell());
        cellList.setWidth("200px");
        cellList.setHeight("400px");

        _service.getLatestExecution(_tenant, _job, new DCAsyncCallback<ExecutionLog>() {
            @Override
            public void onSuccess(ExecutionLog result) {
                executionLogPanelTarget.setWidget(new ExecutionLogPanel(result));
            }
        });

        _service.getAllExecutions(_tenant, _job, new DCAsyncCallback<List<ExecutionIdentifier>>() {

            @Override
            public void onSuccess(List<ExecutionIdentifier> result) {
                cellList.setRowData(result);
                
                // TODO: Place CellList to the left.
            }
        });

    }
}
