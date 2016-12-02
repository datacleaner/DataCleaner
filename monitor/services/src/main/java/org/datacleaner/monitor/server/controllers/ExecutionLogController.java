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

import javax.annotation.security.RolesAllowed;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.metamodel.util.FileHelper;
import org.datacleaner.monitor.configuration.TenantContext;
import org.datacleaner.monitor.configuration.TenantContextFactory;
import org.datacleaner.monitor.job.JobContext;
import org.datacleaner.monitor.shared.model.SecurityRoles;
import org.datacleaner.repository.RepositoryFile;
import org.datacleaner.repository.RepositoryFolder;
import org.datacleaner.util.FileFilters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping({ "/{tenant}/logs/{result:.+}", "/{tenant}/results/{result:.+}.analysis.execution.log.xml" })
public class ExecutionLogController {

    private static final String EXTENSION = FileFilters.ANALYSIS_EXECUTION_LOG_XML.getExtension();

    @Autowired
    TenantContextFactory _contextFactory;

    @RolesAllowed(SecurityRoles.VIEWER)
    @RequestMapping(method = RequestMethod.GET, produces = "text/html")
    public void executionLogXml(@PathVariable("tenant") final String tenant, @PathVariable("result") String resultName,
            final HttpServletResponse response) throws IOException {

        resultName = resultName.replaceAll("\\+", " ");

        final TenantContext context = _contextFactory.getContext(tenant);

        final RepositoryFolder resultsFolder = context.getResultFolder();

        if (!resultName.endsWith(EXTENSION)) {
            resultName = resultName + EXTENSION;
        }

        final RepositoryFile resultFile;
        if (resultName.endsWith("-latest" + EXTENSION)) {
            final String jobName = resultName.substring(0, resultName.length() - ("-latest" + EXTENSION).length());
            resultFile = resultsFolder.getLatestFile(jobName, EXTENSION);
            if (resultFile == null) {
                final JobContext job = context.getJob(jobName);
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "No execution logs for job: " + job.getName());
                return;
            }
        } else {
            resultFile = resultsFolder.getFile(resultName);
        }

        if (resultFile == null) {
            throw new IllegalArgumentException("No such result file: " + resultName);
        }

        response.setContentType("application/xml");
        response.setCharacterEncoding("UTF-8");

        final ServletOutputStream out = response.getOutputStream();
        resultFile.readFile(in -> {
            FileHelper.copy(in, out);
        });
    }
}
