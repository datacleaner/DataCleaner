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
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletResponse;

import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.configuration.AnalyzerBeansConfiguration;
import org.datacleaner.job.ComponentJob;
import org.datacleaner.result.AnalysisResult;
import org.datacleaner.result.html.HtmlAnalysisResultWriter;
import org.datacleaner.monitor.configuration.ResultContext;
import org.datacleaner.monitor.configuration.TenantContext;
import org.datacleaner.monitor.configuration.TenantContextFactory;
import org.datacleaner.monitor.job.JobContext;
import org.datacleaner.monitor.server.HtmlAnalysisResultWriterFactory;
import org.datacleaner.monitor.shared.model.SecurityRoles;
import org.datacleaner.repository.RepositoryFile;
import org.datacleaner.repository.RepositoryFolder;
import org.datacleaner.util.FileFilters;
import org.apache.metamodel.util.Action;
import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.Predicate;
import org.apache.metamodel.util.TruePredicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/{tenant}/results/{result:.+}")
public class ResultFileController {

    private static final Logger logger = LoggerFactory.getLogger(ResultFileController.class);

    private static final String EXTENSION = FileFilters.ANALYSIS_RESULT_SER.getExtension();

    @Autowired
    TenantContextFactory _contextFactory;

    @Autowired
    HtmlAnalysisResultWriterFactory _htmlAnalysisResultWriterFactory;

    @RolesAllowed(SecurityRoles.RESULT_EDITOR)
    @RequestMapping(method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public Map<String, String> uploadAnalysisResult(@PathVariable("tenant") String tenant,
            @PathVariable("result") String resultName, @RequestParam("file") final MultipartFile file) {
        if (file == null) {
            throw new IllegalArgumentException(
                    "No file upload provided. Please provide a multipart file using the 'file' HTTP parameter.");
        }

        resultName = resultName.replaceAll("\\+", " ");

        final TenantContext context = _contextFactory.getContext(tenant);

        final RepositoryFolder resultsFolder = context.getResultFolder();

        final long timestamp = new Date().getTime();
        final String filename;
        if (resultName.endsWith(EXTENSION)) {
            filename = resultName.substring(0, resultName.length() - EXTENSION.length()) + "-" + timestamp + EXTENSION;
        } else {
            filename = resultName + "-" + timestamp + EXTENSION;
        }

        final RepositoryFile resultFile = resultsFolder.createFile(filename, new Action<OutputStream>() {
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
        result.put("file_type", resultFile.getType().toString());
        result.put("filename", resultFile.getName());
        result.put("repository_path", resultFile.getQualifiedPath());

        return result;
    }

    @RolesAllowed(SecurityRoles.VIEWER)
    @RequestMapping(method = RequestMethod.GET, produces = "text/html")
    public void resultHtml(@PathVariable("tenant") final String tenant, @PathVariable("result") String resultName,
            @RequestParam(value = "tabs", required = false) Boolean tabsParam,
            @RequestParam(value = "comp_name", required = false) String componentParamName,
            @RequestParam(value = "comp_index", required = false) Integer componentIndexParam,
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
                JobContext job = context.getJob(jobName);
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "No results for job: " + job.getName());
                return;
            }
        } else {
            resultFile = resultsFolder.getFile(resultName);
        }

        if (resultFile == null) {
            throw new IllegalArgumentException("No such result file: " + resultName);
        }

        final ResultContext resultContext = context.getResult(resultFile.getName());
        final AnalysisResult analysisResult;
        try {
            analysisResult = resultContext.getAnalysisResult();
        } catch (IllegalStateException e) {
            logger.error("Failed to read AnalysisResult in file: " + resultFile, e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to read result file: "
                    + resultFile.getName() + ". See server logs for details.");
            return;
        }
        final AnalyzerBeansConfiguration configuration = context.getConfiguration();
        final boolean tabs = (tabsParam == null ? true : tabsParam.booleanValue());

        final boolean headers;
        final Predicate<Entry<ComponentJob, AnalyzerResult>> jobInclusionPredicate;
        if (org.datacleaner.util.StringUtils.isNullOrEmpty(componentParamName)) {
            jobInclusionPredicate = new TruePredicate<Entry<ComponentJob, AnalyzerResult>>();
            headers = true;
        } else {
            jobInclusionPredicate = createInclusionPredicate(componentParamName, componentIndexParam);
            headers = false;
        }

        final HtmlAnalysisResultWriter htmlWriter = _htmlAnalysisResultWriterFactory.create(tabs,
                jobInclusionPredicate, headers);
        final PrintWriter out = response.getWriter();

        try {
            htmlWriter.write(analysisResult, configuration, out);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private Predicate<Entry<ComponentJob, AnalyzerResult>> createInclusionPredicate(final String componentParamName,
            final Integer componentIndexParam) {
        return new Predicate<Map.Entry<ComponentJob, AnalyzerResult>>() {

            private int index = 0;

            @Override
            public Boolean eval(Entry<ComponentJob, AnalyzerResult> entry) {
                ComponentJob component = entry.getKey();
                String name = component.getName();
                if (name != null && name.equals(componentParamName)) {
                    return matchesIndex();
                }
                String displayName = component.getDescriptor().getDisplayName();
                if (displayName != null && displayName.equals(componentParamName)) {
                    return matchesIndex();
                }
                return false;
            }

            protected Boolean matchesIndex() {
                if (componentIndexParam == null) {
                    return true;
                } else {
                    boolean result = (index == componentIndexParam.intValue());
                    index++;
                    return result;
                }
            }
        };
    }
}
