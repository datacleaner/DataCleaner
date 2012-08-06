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
package org.eobjects.datacleaner.monitor.scheduling;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * Abstract quartz job which provides a few convenience methods for subclasses.
 */
public abstract class AbstractQuartzJob extends QuartzJobBean implements Job {

    public static final String APPLICATION_CONTEXT = "applicationContext";
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    protected ApplicationContext getApplicationContext(JobExecutionContext context) {
        final JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        final Object object = jobDataMap.get(APPLICATION_CONTEXT);
        if (object == null) {
            logger.warn("Could not find ApplicationContext in JobDataMap. Available keys: {}", jobDataMap.keySet());
        }
        return (ApplicationContext) object;
    }
}
