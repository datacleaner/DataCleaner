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
package org.datacleaner.monitor.server.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.security.RolesAllowed;

import org.apache.metamodel.util.Action;
import org.apache.metamodel.util.FileHelper;
import org.datacleaner.monitor.configuration.TenantContext;
import org.datacleaner.monitor.configuration.TenantContextFactory;
import org.datacleaner.monitor.job.JobContext;
import org.datacleaner.monitor.job.XmlJobContext;
import org.datacleaner.monitor.shared.model.SecurityRoles;
import org.datacleaner.repository.RepositoryFile;
import org.datacleaner.repository.RepositoryFolder;
import org.datacleaner.util.FileFilters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.net.UrlEscapers;

@Controller
@RequestMapping("/{tenant}/jobs/{job:.+}.analysis.xml")
public class JobFileController {

    private static final Logger logger = LoggerFactory.getLogger(JobFileController.class);

    private static final String EXTENSION = FileFilters.ANALYSIS_XML.getExtension();

    @Autowired
    TenantContextFactory _contextFactory;

    @RolesAllowed(SecurityRoles.JOB_EDITOR)
    @RequestMapping(method = RequestMethod.POST, produces = MediaType.TEXT_HTML_VALUE,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String uploadAnalysisJobHtml(@PathVariable("tenant") final String tenant,
            @PathVariable("job") final String jobName, @RequestParam("file") final MultipartFile file) {
        final Map<String, String> outcome = uploadAnalysisJobJson(tenant, jobName, file);
        final String status = outcome.get("status");
        final String filename = UrlEscapers.urlFormParameterEscaper().escape(outcome.get("filename"));
        return "redirect:/repository?job_upload=" + status + "&job_filename=" + filename;
    }

    @RolesAllowed(SecurityRoles.JOB_EDITOR)
    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public Map<String, String> uploadAnalysisJobJson(@PathVariable("tenant") final String tenant,
            @PathVariable("job") String jobName, @RequestParam("file") final MultipartFile file) {
        if (file == null) {
            throw new IllegalArgumentException(
                    "No file upload provided. Please provide a multipart file using the 'file' HTTP parameter.");
        }

        jobName = jobName.replaceAll("\\+", " ");

        final Action<OutputStream> writeCallback = out -> {
            final InputStream in = file.getInputStream();
            try {
                FileHelper.copy(in, out);
            } finally {
                FileHelper.safeClose(in);
            }
        };

        final TenantContext context = _contextFactory.getContext(tenant);
        final JobContext existingJob = context.getJob(jobName);
        final RepositoryFile jobFile;
        if (existingJob == null) {
            final RepositoryFolder jobsFolder = context.getJobFolder();

            final String filename;
            if (jobName.endsWith(EXTENSION)) {
                filename = jobName;
            } else {
                filename = jobName + EXTENSION;
            }

            logger.info("Creating new job from uploaded file: {}", filename);
            jobFile = jobsFolder.createFile(filename, writeCallback);
        } else {
            jobFile = existingJob.getJobFile();
            logger.info("Overwriting job from uploaded file: {}", jobFile.getName());
            jobFile.writeFile(writeCallback);
        }

        final Map<String, String> result = new HashMap<>();
        result.put("status", "Success");
        result.put("file_type", jobFile.getType().toString());
        result.put("filename", jobFile.getName());
        result.put("repository_path", jobFile.getQualifiedPath());

        return result;
    }

    @RolesAllowed(SecurityRoles.JOB_EDITOR)
    @RequestMapping(method = RequestMethod.GET, produces = "application/xml")
    public void jobXml(@PathVariable("tenant") final String tenant, @PathVariable("job") String jobName,
            final OutputStream out) throws IOException {

        jobName = jobName.replaceAll("\\+", " ");

        final TenantContext context = _contextFactory.getContext(tenant);
        final JobContext job = context.getJob(jobName);

        if (!(job instanceof XmlJobContext)) {
            throw new UnsupportedOperationException("Job not compatible with operation: " + job);
        }

        final XmlJobContext xmlJob = (XmlJobContext) job;
        xmlJob.toXml(out);
    }
}
