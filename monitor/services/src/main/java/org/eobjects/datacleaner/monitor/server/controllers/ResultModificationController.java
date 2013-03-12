/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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

import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.security.RolesAllowed;

import org.eobjects.analyzer.beans.convert.ConvertToDateTransformer;
import org.eobjects.analyzer.result.AnalysisResult;
import org.eobjects.analyzer.result.SimpleAnalysisResult;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.monitor.configuration.JobContext;
import org.eobjects.datacleaner.monitor.configuration.ResultContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContextFactory;
import org.eobjects.datacleaner.monitor.events.ResultModificationEvent;
import org.eobjects.datacleaner.monitor.shared.model.SecurityRoles;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.eobjects.datacleaner.repository.RepositoryFolder;
import org.eobjects.datacleaner.util.FileFilters;
import org.eobjects.metamodel.util.Action;
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
@RequestMapping("/{tenant}/results/{result:.+}.modify")
public class ResultModificationController {

    private static final String EXTENSION = FileFilters.ANALYSIS_RESULT_SER.getExtension();

    private static final Logger logger = LoggerFactory.getLogger(ResultModificationController.class);

    @Autowired
    ApplicationEventPublisher _eventPublisher;

    @Autowired
    TenantContextFactory _contextFactory;

    @RequestMapping(method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    @ResponseBody
    @RolesAllowed(SecurityRoles.RESULT_EDITOR)
    public Map<String, String> modifyResult(@PathVariable("tenant") final String tenant,
            @PathVariable("result") String resultName, @RequestBody final ResultModificationPayload input) {

        logger.info("Request payload: {}", input);

        resultName = resultName.replaceAll("\\+", " ");

        final Map<String, String> response = new TreeMap<String, String>();

        final TenantContext tenantContext = _contextFactory.getContext(tenant);

        final ResultContext result = tenantContext.getResult(resultName);
        final RepositoryFile existingFile = result.getResultFile();
        final String oldFilename = existingFile.getName();
        response.put("old_result_name", oldFilename);

        final String jobInput = input.getJob();
        final String dateInput = input.getDate();

        final long newTimestamp;
        final AnalysisResult newAnalysisResult;
        if (!StringUtils.isNullOrEmpty(dateInput)) {
            final Date newDate = ConvertToDateTransformer.getInternalInstance().transformValue(dateInput);
            if (newDate == null) {
                throw new IllegalArgumentException("Could not convert input '" + dateInput + "' to date.");

            }

            final AnalysisResult existinAnalysisResult = result.getAnalysisResult();
            newAnalysisResult = new SimpleAnalysisResult(existinAnalysisResult.getResultMap(), newDate);

            newTimestamp = newDate.getTime();
        } else {
            newAnalysisResult = result.getAnalysisResult();
            newTimestamp = newAnalysisResult.getCreationDate().getTime();
        }

        final String newJobName;
        if (!StringUtils.isNullOrEmpty(jobInput) && !oldFilename.startsWith(jobInput)) {
            final JobContext newJob = tenantContext.getJob(jobInput);
            assert newJob != null;
            newJobName = jobInput;
        } else {
            // we assume a filename pattern like this:
            // {job}-{timestamp}.analysis.result.dat
            final int lastIndexOfDash = resultName.lastIndexOf('-');
            assert lastIndexOfDash != -1;
            newJobName = resultName.substring(0, lastIndexOfDash);
        }

        final String newFilename = newJobName + '-' + newTimestamp + EXTENSION;
        response.put("new_result_name", newFilename);
        response.put("repository_url", "/" + tenant + "/results/" + newFilename);

        final RepositoryFolder resultFolder = tenantContext.getResultFolder();
        final RepositoryFile newFile = resultFolder.getFile(newFilename);

        final Action<OutputStream> writeAction = new Action<OutputStream>() {
            @Override
            public void run(OutputStream out) throws Exception {
                final ObjectOutputStream oos = new ObjectOutputStream(out);
                oos.writeObject(newAnalysisResult);
            }
        };

        if (newFile == null) {
            resultFolder.createFile(newFilename, writeAction);
        } else {
            final Boolean overwrite = input.getOverwrite();
            if (overwrite == null || !overwrite.booleanValue()) {
                throw new IllegalStateException("A result file with the name '" + newFilename
                        + "' already exists, and the 'overwrite' flag is non-true.");
            }
            newFile.writeFile(writeAction);
        }
        existingFile.delete();

        _eventPublisher.publishEvent(new ResultModificationEvent(this, tenant, oldFilename, newFilename, newJobName,
                newTimestamp));

        logger.debug("Response payload: {}", response);
        return response;
    }
}