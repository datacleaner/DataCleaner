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

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.security.RolesAllowed;

import org.eobjects.analyzer.beans.convert.ConvertToDateTransformer;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.monitor.configuration.ResultContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContextFactory;
import org.eobjects.datacleaner.monitor.job.JobContext;
import org.eobjects.datacleaner.monitor.server.dao.ResultDao;
import org.eobjects.datacleaner.monitor.shared.model.JobIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.SecurityRoles;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/{tenant}/results/{result:.+}.modify")
public class ResultModificationController {

    private static final Logger logger = LoggerFactory.getLogger(ResultModificationController.class);

    @Autowired
    TenantContextFactory _contextFactory;

    @Autowired
    ResultDao _resultDao;

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

        final Date newTimestamp;
        if (!StringUtils.isNullOrEmpty(dateInput)) {
            newTimestamp = ConvertToDateTransformer.getInternalInstance().transformValue(dateInput);
            if (newTimestamp == null) {
                throw new IllegalArgumentException("Could not convert input '" + dateInput + "' to date.");
            }
        } else {
            newTimestamp = null;
        }
        
        final JobIdentifier newJob;
        if (!StringUtils.isNullOrEmpty(jobInput) && !oldFilename.startsWith(jobInput)) {
            final JobContext newJobContext = tenantContext.getJob(jobInput);
            assert newJobContext != null;
            newJob = new JobIdentifier(newJobContext.getName());
        } else {
            newJob = null;
        }

        final ResultContext newResult = _resultDao.updateResult(new TenantIdentifier(tenant), result, newJob, newTimestamp);

        final String newFilename = newResult.getResultFile().getName();
        
        response.put("new_result_name", newFilename);
        response.put("repository_url", "/" + tenant + "/results/" + newFilename);

        logger.debug("Response payload: {}", response);
        return response;
    }
}