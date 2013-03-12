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

import java.io.IOException;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eobjects.analyzer.cluster.SlaveJobInterceptor;
import org.eobjects.analyzer.cluster.http.SlaveServletHelper;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContextFactory;
import org.eobjects.datacleaner.monitor.shared.model.SecurityRoles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * A controller for slaves in a cluster to recieve execution requests. See Sla
 */
@Controller
@RequestMapping("/{tenant}/cluster_slave_endpoint")
public class ClusterSlaveController {

    private static final Logger logger = LoggerFactory.getLogger(ClusterSlaveController.class);

    @Autowired
    TenantContextFactory _tenantContextFactory;

    @Autowired(required = false)
    SlaveJobInterceptor _slaveJobInterceptor;

    @RolesAllowed(SecurityRoles.TASK_SLAVE_EXECUTOR)
    @RequestMapping(method = RequestMethod.POST, produces = "application/octet-stream")
    @ResponseBody
    public void executeJob(@PathVariable("tenant") final String tenant, final HttpServletRequest request,
            final HttpServletResponse response) throws IOException {
        final TenantContext tenantContext = _tenantContextFactory.getContext(tenant);
        final AnalyzerBeansConfiguration configuration = tenantContext.getConfiguration();

        logger.info("Accepting slave job request for tenant: {}", tenant);

        try {
            final SlaveServletHelper slaveServletHelper = new SlaveServletHelper(configuration, _slaveJobInterceptor);
            slaveServletHelper.handleRequest(request, response);
        } catch (RuntimeException e) {
            logger.error("Unexpected runtime exception occurred during slave job execution", e);
            throw e;
        } catch (IOException e) {
            logger.error("Unexpected I/O exception occurred during slave job execution", e);
            throw e;
        }
    }
}
