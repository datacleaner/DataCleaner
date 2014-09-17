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
package org.eobjects.datacleaner.monitor.scheduling.quartz;

import java.util.Arrays;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerContext;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * Abstract quartz job which provides a few convenience methods for subclasses.
 */
public abstract class AbstractQuartzJob extends QuartzJobBean implements Job {

    public static final String APPLICATION_CONTEXT = "DataCleaner.schedule.applicationContext";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final ApplicationContext getApplicationContext(JobExecutionContext context) {
        try {
            final SchedulerContext schedulerContext = context.getScheduler().getContext();
            final ApplicationContext applicationContext = (ApplicationContext) schedulerContext
                    .get(APPLICATION_CONTEXT);
            if (applicationContext == null) {
                logger.warn("Couldn't find application context in keys: {}",
                        Arrays.toString(schedulerContext.getKeys()));
            }
            return applicationContext;
        } catch (SchedulerException e) {
            logger.error("Couldn't retrieve ApplicationContext from scheduler context", e);
            throw new IllegalStateException(e);
        }
    }
}
