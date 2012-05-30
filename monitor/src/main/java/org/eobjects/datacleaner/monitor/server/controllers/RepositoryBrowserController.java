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

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import org.eobjects.analyzer.cli.HtmlAnalysisResultWriter;
import org.eobjects.analyzer.result.AnalysisResult;
import org.eobjects.datacleaner.monitor.configuration.ConfigurationCache;
import org.eobjects.datacleaner.monitor.server.TimelineServiceImpl;
import org.eobjects.datacleaner.repository.Repository;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.eobjects.datacleaner.repository.RepositoryFile.Type;
import org.eobjects.datacleaner.repository.RepositoryFolder;
import org.eobjects.metamodel.util.ImmutableRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.w3c.tidy.Tidy;

@Controller
@RequestMapping("/{tenant}")
public class RepositoryBrowserController {

    @Autowired
    Repository _repository;

    @Autowired
    ConfigurationCache _configurationCache;

    @RequestMapping(value = "/results", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public String resultsFolderHtml(@PathVariable("tenant") String tenant) {
        final RepositoryFolder tenantFolder = _repository.getFolder(tenant);
        final RepositoryFolder resultsFolder = tenantFolder.getFolder("results");

        final StringBuilder sb = new StringBuilder();
        sb.append("<html><body>");

        sb.append("<h1>" + resultsFolder.getQualifiedPath() + "</h1>");

        {
            sb.append("<ul>");
            final List<RepositoryFile> files = resultsFolder.getFiles();
            for (RepositoryFile file : files) {
                if (file.getType() == Type.ANALYSIS_RESULT) {
                    sb.append("<li><a href=\"results/" + file.getName() + "\">" + file.getName() + "</a></li>");
                }
            }
            sb.append("</ul>");
        }

        sb.append("</body></html>");

        return sb.toString();
    }

    @RequestMapping(value = "/results/{id:.+}", produces = "text/html")
    @ResponseBody
    public String resultHtml(@PathVariable("tenant") String tenant, @PathVariable("id") String id) {
        final RepositoryFolder tenantFolder = _repository.getFolder(tenant);
        final RepositoryFolder resultsFolder = tenantFolder.getFolder("results");
        final RepositoryFile resultFile = resultsFolder.getFile(id);

        final String rawAnalysisResult;
        {
            final AnalysisResult analysisResult = TimelineServiceImpl.readAnalysisResult(resultFile);
            final HtmlAnalysisResultWriter htmlWriter = new HtmlAnalysisResultWriter();
            final StringWriter writer = new StringWriter();
            try {
                htmlWriter.write(analysisResult, _configurationCache.getAnalyzerBeansConfiguration(tenant),
                        new ImmutableRef<Writer>(writer), null);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            rawAnalysisResult = writer.toString();
        }

        final String tidyResult;
        {
            final Tidy tidy = new Tidy();
            tidy.setHideComments(true);
            tidy.setTrimEmptyElements(false);
            final StringWriter writer = new StringWriter();
            tidy.parse(new StringReader(rawAnalysisResult), writer);
            tidyResult = writer.toString();
        }

        return tidyResult;
    }
}
