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
package org.eobjects.datacleaner.monitor.server.listeners;

import org.eobjects.analyzer.data.InputRow;
import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.ComponentJob;
import org.eobjects.datacleaner.monitor.events.JobFailedEvent;
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

        final InputRow row = event.getRow();
        if (row != null) {
            logger.info("Failure row: {}", row);
        }
        
        final ComponentJob componentJob = event.getComponentJob();
        if (componentJob != null) {
            logger.info("Failure component: {}", componentJob);
        }
        
        final Throwable throwable = event.getThrowable();
        if (throwable != null) {
            logger.info("Failure stack trace:", throwable);
        }
    }

}
