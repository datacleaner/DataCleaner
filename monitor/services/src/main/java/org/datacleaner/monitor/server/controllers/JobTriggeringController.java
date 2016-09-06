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

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.annotation.security.RolesAllowed;

import org.datacleaner.monitor.configuration.TenantContextFactory;
import org.datacleaner.monitor.scheduling.SchedulingService;
import org.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.datacleaner.monitor.shared.model.JobIdentifier;
import org.datacleaner.monitor.shared.model.SecurityRoles;
import org.datacleaner.monitor.shared.model.TenantIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

@Controller
@RequestMapping(value = "/{tenant}/jobs/{job:.+}.trigger")
public class JobTriggeringController {

    private static final int POLL_INCREMENT_MILLIS = 1000;

    @Autowired
    TenantContextFactory _contextFactory;

    @Autowired
    SchedulingService _schedulingService;

    @RequestMapping(method = { RequestMethod.DELETE, RequestMethod.GET, RequestMethod.HEAD, RequestMethod.OPTIONS,
            RequestMethod.PATCH, RequestMethod.PUT, RequestMethod.TRACE }, produces = "application/json")
    @ResponseBody
    @RolesAllowed(SecurityRoles.SCHEDULE_EDITOR)
    public Map<String, String> invokeJob(@PathVariable("tenant") final String tenant,
            @PathVariable("job") String jobName, @RequestParam(value = "block", required = false) Boolean block,
            @RequestParam(value = "timeoutMillis", required = false) Integer timeoutMillis) throws Throwable {
        return handleJob(tenant, jobName, block, timeoutMillis, null);
    }

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, 
            consumes = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    @RolesAllowed(SecurityRoles.SCHEDULE_EDITOR)
    public Map<String, String> handlePlainText(@PathVariable("tenant") final String tenant,
            @PathVariable("job") String jobName, @RequestParam(value = "block", required = false) Boolean block,
            @RequestParam(value = "timeoutMillis", required = false) Integer timeoutMillis,
            @RequestBody final String overrideProperties) throws Throwable {
        Properties properties = new Properties();
        if (overrideProperties != null && !overrideProperties.isEmpty()) {
            properties.load(new StringReader(overrideProperties));
        }
        
        return handleJob(tenant, jobName, block, timeoutMillis, properties);
    }
    
    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, 
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    @RolesAllowed(SecurityRoles.SCHEDULE_EDITOR)
    public Map<String, String> handleMultipartFormData(@PathVariable("tenant") final String tenant,
            @PathVariable("job") String jobName, @RequestParam(value = "block", required = false) Boolean block,
            @RequestParam(value = "timeoutMillis", required = false) Integer timeoutMillis,
            @RequestParam(value = "overrideProperties") final MultipartFile overrideProperties) throws Throwable {
        Properties properties = new Properties();
        if (overrideProperties != null && !overrideProperties.isEmpty()) {
            properties.load(overrideProperties.getInputStream());
        }
        
        return handleJob(tenant, jobName, block, timeoutMillis, properties);
    }
    
    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, 
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @RolesAllowed(SecurityRoles.SCHEDULE_EDITOR)
    public Map<String, String> handleJob(@PathVariable("tenant") final String tenant,
            @PathVariable("job") String jobName, @RequestParam(value = "block", required = false) Boolean block,
            @RequestParam(value = "timeoutMillis", required = false) Integer timeoutMillis,
            @RequestBody final Properties overrideProperties) throws Throwable {
        final boolean blocking = block != null && block.booleanValue();

        jobName = jobName.replaceAll("\\+", " ");

        TenantIdentifier tenantIdentifier = new TenantIdentifier(tenant);

        ExecutionLog executionLog = _schedulingService.triggerExecution(tenantIdentifier, new JobIdentifier(jobName),
                mapProperties(overrideProperties));

        if (blocking) {
            int millisWaited = 0;
            while (!executionLog.isFinished() && !isTimedOut(millisWaited, timeoutMillis)) {
                Thread.sleep(POLL_INCREMENT_MILLIS);
                millisWaited += POLL_INCREMENT_MILLIS;
                ExecutionLog updatedExecutionLog = _schedulingService.getExecution(tenantIdentifier, executionLog);
                if (updatedExecutionLog != null) {
                    executionLog = updatedExecutionLog;
                }
            }
        }

        final Map<String, String> result = new HashMap<String, String>();
        result.put("status", toString(executionLog.getExecutionStatus()));
        result.put("logOutput", executionLog.getLogOutput());
        result.put("resultId", executionLog.getResultId());
        result.put("triggeredBy", executionLog.getTriggeredBy());
        result.put("beginDate", toString(executionLog.getJobBeginDate()));
        result.put("endDate", toString(executionLog.getJobEndDate()));
        return result;
    }

    private ImmutableMap<String, String> mapProperties(final Properties properties) {
        if (properties == null) {
            return null;
        }
        return Maps.fromProperties(properties);
    }

    private boolean isTimedOut(int millisWaited, Integer timeoutMillis) {
        if (timeoutMillis == null) {
            return false;
        }
        if (millisWaited > timeoutMillis.intValue()) {
            return true;
        }
        return false;
    }

    private String toString(Object obj) {
        if (obj == null) {
            return null;
        }
        return obj.toString();
    }
}
