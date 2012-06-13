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
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;

import org.eobjects.analyzer.result.AnalysisResult;
import org.eobjects.analyzer.result.html.HtmlAnalysisResultWriter;
import org.eobjects.datacleaner.monitor.configuration.ConfigurationCache;
import org.eobjects.datacleaner.monitor.server.TimelineServiceImpl;
import org.eobjects.datacleaner.repository.Repository;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.eobjects.datacleaner.repository.RepositoryFolder;
import org.eobjects.datacleaner.util.FileFilters;
import org.eobjects.metamodel.util.Action;
import org.eobjects.metamodel.util.FileHelper;
import org.eobjects.metamodel.util.ImmutableRef;
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

    @Autowired
    Repository _repository;

    @Autowired
    ConfigurationCache _configurationCache;

    @RequestMapping(method = RequestMethod.POST, produces = "text/html")
    @ResponseBody
    public String uploadAnalysisResult(@PathVariable("tenant") String tenant,
            @PathVariable("result") String resultName, @RequestParam("file") final MultipartFile file) {
        if (file == null) {
            throw new IllegalArgumentException(
                    "No file upload provided. Please provide a multipart file using the 'file' HTTP parameter.");
        }

        final RepositoryFolder tenantFolder = _repository.getFolder(tenant);
        if (tenantFolder == null) {
            throw new IllegalArgumentException("No such tenant: " + tenant);
        }

        final RepositoryFolder resultsFolder = tenantFolder.getFolder(TimelineServiceImpl.PATH_RESULTS);

        final long timestamp = new Date().getTime();
        final String filename = resultName + "-" + timestamp + FileFilters.ANALYSIS_RESULT_SER.getExtension();

        resultsFolder.createFile(filename, new Action<OutputStream>() {
            @Override
            public void run(OutputStream out) throws Exception {
                final InputStream in = file.getInputStream();
                FileHelper.copy(in, out);
            }
        });

        return "Success";
    }

    @RequestMapping(method = RequestMethod.GET, produces = "text/html")
    @ResponseBody
    public String resultHtml(@PathVariable("tenant") String tenant, @PathVariable("result") String resultName) {
        final RepositoryFolder tenantFolder = _repository.getFolder(tenant);
        if (tenantFolder == null) {
            throw new IllegalArgumentException("No such tenant: " + tenant);
        }

        final RepositoryFolder resultsFolder = tenantFolder.getFolder("results");
        final RepositoryFile resultFile = resultsFolder.getFile(resultName);
        if (resultFile == null) {
            throw new IllegalArgumentException("No such result file: " + resultName);
        }

        final String rawAnalysisResult;
        {
            final AnalysisResult analysisResult = TimelineServiceImpl.readAnalysisResult(resultFile);
            final HtmlAnalysisResultWriter htmlWriter = new HtmlAnalysisResultWriter();

            // TODO: Replace with request's writer
            final StringWriter writer = new StringWriter();
            try {
                htmlWriter.write(analysisResult, _configurationCache.getAnalyzerBeansConfiguration(tenant),
                        new ImmutableRef<Writer>(writer), null);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            rawAnalysisResult = writer.toString();
        }

        return rawAnalysisResult;
    }
}
