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
import java.util.HashMap;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletResponse;

import org.datacleaner.monitor.configuration.TenantContextFactory;
import org.datacleaner.monitor.shared.model.SecurityRoles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

public class DatastoreUpdateController {
    
    private static final Logger logger = LoggerFactory.getLogger(DatastoreUpdateController.class);

    @Autowired
    TenantContextFactory _tenantContextFactory;
    
    @RolesAllowed(SecurityRoles.TASK_QUERY)
    @RequestMapping(value = "/{tenant}/datastores/{datastore}.update", method = RequestMethod.PUT, headers = "Accept=application/json", produces = {
            "application/json" })
    @ResponseBody
    public Map<String, Object> jsonQueryDatastoreGet(HttpServletResponse response,
            @PathVariable("tenant") final String tenant, @PathVariable("datastore") String datastoreName,
            @RequestBody String query) throws IOException {
        response.setContentType("application/json");
        return getJsonResult(tenant, datastoreName, query, response);
    }

    private Map<String, Object> getJsonResult(String tenant, String datastoreName, String query,
            HttpServletResponse response) {
        final Map<String, Object> map = new HashMap<>();
        map.put("Result", "Hoera!");
        return map;
    }

}
