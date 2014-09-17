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
package org.eobjects.datacleaner.monitor.server.listeners;

import org.eobjects.datacleaner.monitor.alertnotification.AlertNotificationService;
import org.eobjects.datacleaner.monitor.events.JobExecutedEvent;
import org.eobjects.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * Listener that invokes the {@link AlertNotificationService} when a job has
 * been executed.
 */
@Component
public class JobExecutedEventNotifyAlertsListener implements ApplicationListener<JobExecutedEvent> {

    @Autowired
    AlertNotificationService alertNotificationService;

    @Override
    public void onApplicationEvent(JobExecutedEvent event) {
        ExecutionLog executionLog = event.getExecutionLog();
        Object result = event.getResult();
        if (result == null) {
            return;
        }
        alertNotificationService.notifySubscribers(executionLog);
    }

}
