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
package org.datacleaner.monitor.scheduling.widgets;

import java.util.ArrayList;
import java.util.List;

import org.datacleaner.monitor.scheduling.SchedulingServiceAsync;
import org.datacleaner.monitor.scheduling.model.ScheduleDefinition;
import org.datacleaner.monitor.shared.ClientConfig;
import org.datacleaner.monitor.shared.model.JobIdentifier;
import org.datacleaner.monitor.util.DCAsyncCallback;

import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;

class ScheduleDataProvider extends AsyncDataProvider<ScheduleDefinition> {
    private final List<JobIdentifier> _jobs;

    private final ClientConfig _clientConfig;
    private final SchedulingServiceAsync _service;

    ScheduleDataProvider(final ClientConfig clientConfig, final SchedulingServiceAsync service,
            final List<JobIdentifier> jobs) {
        super();

        _clientConfig = clientConfig;
        _service = service;
        _jobs = jobs;
    }

    @Override
    protected void onRangeChanged(final HasData<ScheduleDefinition> display) {
        final Range range = display.getVisibleRange();

        final int rangeStart = range.getStart();
        int rangeEnd = rangeStart + range.getLength();

        if (rangeEnd > _jobs.size()) {
            rangeEnd = _jobs.size();
        }

        final List<JobIdentifier> jobsInRange = new ArrayList<>(_jobs.subList(rangeStart, rangeEnd));

        _service.getSchedules(_clientConfig.getTenant(), jobsInRange, new DCAsyncCallback<List<ScheduleDefinition>>() {
                @Override
            public void onSuccess(List<ScheduleDefinition> result) {
                updateRowData(rangeStart, result);
                }
            });
    }
}
