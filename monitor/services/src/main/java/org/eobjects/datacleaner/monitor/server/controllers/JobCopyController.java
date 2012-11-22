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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.security.RolesAllowed;

import org.eobjects.datacleaner.monitor.configuration.JobContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContextFactory;
import org.eobjects.datacleaner.monitor.events.JobCopyEvent;
import org.eobjects.datacleaner.monitor.shared.model.SecurityRoles;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.eobjects.datacleaner.repository.RepositoryFolder;
import org.eobjects.datacleaner.util.FileFilters;
import org.eobjects.metamodel.util.Action;
import org.eobjects.metamodel.util.FileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/{tenant}/jobs/{job}.copy")
public class JobCopyController {

    private static final String EXTENSION = FileFilters.ANALYSIS_XML.getExtension();

    private static final Logger logger = LoggerFactory.getLogger(JobCopyController.class);

    @Autowired
    ApplicationEventPublisher _eventPublisher;

    @Autowired
    TenantContextFactory _contextFactory;

    @RequestMapping(method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    @ResponseBody
    @RolesAllowed({ SecurityRoles.JOB_EDITOR })
    public Map<String, String> copyJob(@PathVariable("tenant") final String tenant,
            @PathVariable("job") String jobName, @RequestBody final JobCopyPayload input) {

        logger.info("Request payload: {}", input);

        jobName = jobName.replaceAll("\\+", " ");

        final TenantContext tenantContext = _contextFactory.getContext(tenant);

        String newJobFilename = input.getName();
        if (!newJobFilename.endsWith(EXTENSION)) {
            newJobFilename = newJobFilename + EXTENSION;
        }

        final RepositoryFolder jobFolder = tenantContext.getJobFolder();
        if (jobFolder.getFile(newJobFilename) != null) {
            throw new IllegalArgumentException("The job '" + newJobFilename + "' already exists.");
        }

        final JobContext sourceJob = tenantContext.getJob(jobName);

        final RepositoryFile newJobFile = jobFolder.createFile(newJobFilename, new Action<OutputStream>() {
            @Override
            public void run(final OutputStream out) throws Exception {
                sourceJob.getJobFile().readFile(new Action<InputStream>() {
                    @Override
                    public void run(final InputStream in) throws Exception {
                        FileHelper.copy(in, out);
                    }
                });
            }
        });

        final JobContext newJob = tenantContext.getJob(newJobFilename);

        _eventPublisher.publishEvent(new JobCopyEvent(this, tenant, sourceJob, newJob));

        final Map<String, String> response = new TreeMap<String, String>();
        response.put("source_job", sourceJob.getName());
        response.put("target_job", newJob.getName());
        response.put("repository_url", "/" + tenant + "/jobs/" + newJobFile.getName());
        logger.debug("Response payload: {}", response);

        return response;
    }

}