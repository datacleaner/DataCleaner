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
package org.eobjects.datacleaner.monitor.scheduling.model;

import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Represents information about a scheduled job execution.
 */
public class ScheduleDefinition implements IsSerializable {

    private TenantIdentifier _tenant;
    private JobIdentifier _job;
    private JobIdentifier _scheduleAfterJob;
    private String _scheduleExpression;
    private boolean _active;

    // no-args constructor
    public ScheduleDefinition() {
    }

    public ScheduleDefinition(TenantIdentifier tenant, JobIdentifier job, String scheduleExpression, boolean active) {
        _tenant = tenant;
        _job = job;
        _scheduleExpression = scheduleExpression;
        _active = active;
    }
    
    public ScheduleDefinition(TenantIdentifier tenant, JobIdentifier job, JobIdentifier scheduleAfterJob, boolean active) {
        _tenant = tenant;
        _job = job;
        _scheduleAfterJob = scheduleAfterJob;
        _active = active;
    }

    public TenantIdentifier getTenant() {
        return _tenant;
    }

    public JobIdentifier getJob() {
        return _job;
    }

    public String getScheduleExpression() {
        return _scheduleExpression;
    }

    public boolean isActive() {
        return _active;
    }

    public void setActive(boolean active) {
        _active = active;
    }
    
    public JobIdentifier getScheduleAfterJob() {
        return _scheduleAfterJob;
    }
}
