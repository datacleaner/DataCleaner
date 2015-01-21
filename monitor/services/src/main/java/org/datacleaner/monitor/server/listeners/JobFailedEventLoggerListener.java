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
package org.datacleaner.monitor.server.listeners;

import org.datacleaner.job.AnalysisJob;
import org.datacleaner.monitor.events.JobFailedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * Listener that logs when {@link AnalysisJob} executions fail.
 */
@Component
public class JobFailedEventLoggerListener implements ApplicationListener<JobFailedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(JobFailedEventLoggerListener.class);

    @Override
    public void onApplicationEvent(JobFailedEvent event) {
        logger.warn("Job execution failed: {}", event.getExecutionLog());

        final Object data = event.getData();
        if (data != null) {
            logger.info("Failure input data: {}", data);
        }
        
        final Object component = event.getComponent();
        if (component != null) {
            logger.info("Failure component: {}", component);
        }
        
        final Throwable throwable = event.getThrowable();
        if (throwable != null) {
            logger.info("Failure stack trace:", throwable);
        }
    }

}
