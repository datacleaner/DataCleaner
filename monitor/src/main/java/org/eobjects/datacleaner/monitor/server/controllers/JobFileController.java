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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eobjects.datacleaner.monitor.server.TimelineServiceImpl;
import org.eobjects.datacleaner.repository.Repository;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.eobjects.datacleaner.repository.RepositoryFolder;
import org.eobjects.datacleaner.util.FileFilters;
import org.eobjects.metamodel.util.Action;
import org.eobjects.metamodel.util.FileHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/{tenant}/jobs/{job}.analysis.xml")
public class JobFileController {

    private static final String EXTENSION = FileFilters.ANALYSIS_XML.getExtension();

    @Autowired
    Repository _repository;

    @RequestMapping(method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public Map<String, String> uploadAnalysisJob(@PathVariable("tenant") final String tenant,
            @PathVariable("job") String jobName, @RequestParam("file") final MultipartFile file) {
        if (file == null) {
            throw new IllegalArgumentException(
                    "No file upload provided. Please provide a multipart file using the 'file' HTTP parameter.");
        }

        final RepositoryFolder tenantFolder = _repository.getFolder(tenant);
        if (tenantFolder == null) {
            throw new IllegalArgumentException("No such tenant: " + tenant);
        }

        final RepositoryFolder resultsFolder = tenantFolder.getFolder(TimelineServiceImpl.PATH_JOBS);

        final long timestamp = new Date().getTime();
        final String filename;
        if (jobName.endsWith(EXTENSION)) {
            filename = jobName.substring(0, jobName.length() - EXTENSION.length()) + "-" + timestamp + EXTENSION;
        } else {
            filename = jobName + "-" + timestamp + EXTENSION;
        }

        final RepositoryFile jobFile = resultsFolder.createFile(filename, new Action<OutputStream>() {
            @Override
            public void run(OutputStream out) throws Exception {
                final InputStream in = file.getInputStream();
                try {
                    FileHelper.copy(in, out);
                } finally {
                    FileHelper.safeClose(in);
                }
            }
        });

        final Map<String, String> result = new HashMap<String, String>();
        result.put("status", "Success");
        result.put("file_type", jobFile.getType().toString());
        result.put("filename", jobFile.getName());
        result.put("repository_path", jobFile.getQualifiedPath());

        return result;
    }

    @RequestMapping(method = RequestMethod.GET, produces = "application/xml")
    public void jobXml(@PathVariable("tenant") final String tenant, @PathVariable("job") String jobName, final OutputStream out) {
        final RepositoryFolder tenantFolder = _repository.getFolder(tenant);
        if (tenantFolder == null) {
            throw new IllegalArgumentException("No such tenant: " + tenant);
        }

        if (!jobName.endsWith(EXTENSION)) {
            jobName = jobName + EXTENSION;
        }

        final RepositoryFolder jobsFolder = tenantFolder.getFolder(TimelineServiceImpl.PATH_JOBS);
        final RepositoryFile jobFile = jobsFolder.getFile(jobName);
        if (jobFile == null) {
            throw new IllegalArgumentException("No such job file: " + jobName);
        }

        final InputStream in = jobFile.readFile();
        try {
            FileHelper.copy(in, out);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } finally {
            FileHelper.safeClose(in);
        }
    }
}
