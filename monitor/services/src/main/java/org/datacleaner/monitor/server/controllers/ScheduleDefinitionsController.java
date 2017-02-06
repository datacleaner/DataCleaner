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

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;

import org.datacleaner.monitor.scheduling.SchedulingService;
import org.datacleaner.monitor.scheduling.model.ScheduleDefinition;
import org.datacleaner.monitor.scheduling.model.ScheduleDefinitionModel;
import org.datacleaner.monitor.shared.model.JobIdentifier;
import org.datacleaner.monitor.shared.model.SecurityRoles;
import org.datacleaner.monitor.shared.model.TenantIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping(value = "/{tenant}/schedules/{jobname}")
public class ScheduleDefinitionsController {
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    private class NoSuchResourceException extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

    private static final Logger logger = LoggerFactory.getLogger(ScheduleDefinitionsController.class);

    private final SchedulingService _schedulingService;

    @Autowired
    public ScheduleDefinitionsController(final SchedulingService schedulingService) {
        _schedulingService = schedulingService;
    }

    @RolesAllowed(SecurityRoles.VIEWER)
    @RequestMapping(method = { RequestMethod.GET, RequestMethod.HEAD }, produces = MediaType.APPLICATION_JSON_VALUE)
    public ScheduleDefinitionModel getSchedule(@PathVariable("tenant") final String tenant,
            @PathVariable("jobname") final String jobName, final HttpServletRequest request) {

        final ScheduleDefinition scheduleDefinition = getScheduleDefinition(tenant, jobName);

        if (request.getMethod().equals(RequestMethod.HEAD.name())) {
            return null;
        }

        return new ScheduleDefinitionModel(scheduleDefinition.getHotFolder());
    }

    @RolesAllowed(SecurityRoles.SCHEDULE_EDITOR)
    @RequestMapping(method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> putSchedule(@PathVariable("tenant") final String tenant,
            @PathVariable("jobname") final String jobName,
            @RequestBody final ScheduleDefinitionModel scheduleDefinitionModel) {
        final TenantIdentifier tenantIdentifier = new TenantIdentifier(tenant);

        final ScheduleDefinition scheduleDefinition = getScheduleDefinition(tenant, jobName);

        scheduleDefinition.setHotFolder(scheduleDefinitionModel.getHotFolder());

        _schedulingService.updateSchedule(tenantIdentifier, scheduleDefinition);

        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequestUri().build().toUri()).build();
    }

    private ScheduleDefinition getScheduleDefinition(final String tenant, final String jobName) {
        final TenantIdentifier tenantIdentifier = new TenantIdentifier(tenant);
        final ScheduleDefinition scheduleDefinition =
                _schedulingService.getSchedule(tenantIdentifier, new JobIdentifier(jobName));

        if (scheduleDefinition == null) {
            logger.warn("Could not get schedule for job \"{}\"", jobName);
            throw new NoSuchResourceException();
        }
        return scheduleDefinition;
    }
}
