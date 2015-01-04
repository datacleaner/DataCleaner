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
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.security.RolesAllowed;

import org.datacleaner.monitor.configuration.TenantContext;
import org.datacleaner.monitor.configuration.TenantContextFactory;
import org.datacleaner.monitor.scheduling.SchedulingService;
import org.datacleaner.monitor.scheduling.model.ExecutionIdentifier;
import org.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.datacleaner.monitor.shared.model.SecurityRoles;
import org.datacleaner.monitor.shared.model.TenantIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/{tenant}/results/{result:.+}.cancel")
public class ExecutionCancellationController {

    private static final Logger logger = LoggerFactory.getLogger(ExecutionCancellationController.class);

    @Autowired
    TenantContextFactory _tenantContextFactory;

    @Autowired
    SchedulingService _schedulingService;

    @RolesAllowed(SecurityRoles.VIEWER)
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public Map<String, String> executionLogXml(@PathVariable("tenant") final String tenant,
            @PathVariable("result") String resultName) throws IOException {

        resultName = resultName.replaceAll("\\+", " ");

        final TenantContext tenantContext = _tenantContextFactory.getContext(tenant);

        final TenantIdentifier tenantIdentifier = new TenantIdentifier(tenantContext.getTenantId());

        ExecutionLog executionLog = _schedulingService.getExecution(tenantIdentifier, new ExecutionIdentifier(
                resultName));

        if (executionLog == null) {
            throw new IllegalArgumentException("The execution '" + resultName + "' does not exist.");
        }

        final boolean cancelled = _schedulingService.cancelExecution(tenantIdentifier, executionLog);

        executionLog = _schedulingService.getExecution(tenantIdentifier, new ExecutionIdentifier(resultName));

        final Map<String, String> response = new TreeMap<String, String>();
        response.put("cancelled", cancelled + "");
        response.put("status", toString(executionLog.getExecutionStatus()));
        logger.debug("Response payload: {}", response);

        return response;
    }

    private String toString(Object obj) {
        if (obj == null) {
            return null;
        }
        return obj.toString();
    }
}
