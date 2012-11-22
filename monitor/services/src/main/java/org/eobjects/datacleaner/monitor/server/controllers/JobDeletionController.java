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
package org.eobjects.datacleaner.monitor.server.controllers;

import java.util.Map;
import java.util.TreeMap;

import javax.annotation.security.RolesAllowed;

import org.eobjects.datacleaner.monitor.configuration.JobContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContextFactory;
import org.eobjects.datacleaner.monitor.events.JobDeletionEvent;
import org.eobjects.datacleaner.monitor.server.SchedulingServiceImpl;
import org.eobjects.datacleaner.monitor.shared.model.SecurityRoles;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/{tenant}/jobs/{job}.delete")
public class JobDeletionController {

    private static final Logger logger = LoggerFactory.getLogger(JobDeletionController.class);

    @Autowired
    ApplicationEventPublisher _eventPublisher;

    @Autowired
    TenantContextFactory _contextFactory;

    @RequestMapping(method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    @ResponseBody
    @RolesAllowed({ SecurityRoles.JOB_EDITOR })
    public Map<String, String> deleteJob(@PathVariable("tenant") final String tenant,
            @PathVariable("job") String jobName) {

        logger.info("Request payload: {}", jobName);

        jobName = jobName.replaceAll("\\+", " ");

        final TenantContext tenantContext = _contextFactory.getContext(tenant);

        final JobContext job = tenantContext.getJob(jobName);

        RepositoryFile file = job.getJobFile();
        file.delete();
        
        final RepositoryFile scheduleFile = tenantContext.getJobFolder().getFile(
                jobName + SchedulingServiceImpl.EXTENSION_SCHEDULE_XML);
        if (scheduleFile != null) {
            scheduleFile.delete();
        }

        _eventPublisher.publishEvent(new JobDeletionEvent(this, tenant, jobName));

        final Map<String, String> response = new TreeMap<String, String>();
        response.put("job", jobName);
        logger.debug("Response payload: {}", response);

        return response;
    }

}