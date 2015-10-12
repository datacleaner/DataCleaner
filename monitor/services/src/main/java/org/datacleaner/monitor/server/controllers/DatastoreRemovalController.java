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

import java.util.Map;
import java.util.TreeMap;

import javax.annotation.security.RolesAllowed;

import org.datacleaner.monitor.configuration.TenantContext;
import org.datacleaner.monitor.configuration.TenantContextFactory;
import org.datacleaner.monitor.server.dao.DatastoreDao;
import org.datacleaner.monitor.shared.model.SecurityRoles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/{tenant}/datastores/{datastore}.remove")
public class DatastoreRemovalController {

    private static final Logger logger = LoggerFactory.getLogger(DatastoreRemovalController.class);

    @Autowired
    TenantContextFactory _contextFactory;

    @Autowired
    DatastoreDao datastoreDao;

    @RequestMapping(method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    @RolesAllowed({ SecurityRoles.CONFIGURATION_EDITOR })
    public Map<String, String> removeDatastore(@PathVariable("tenant") final String tenant,
            @PathVariable("datastore") String datastoreName) {

        logger.info("Request payload: {} - {}", tenant, datastoreName);

        datastoreName = datastoreName.replaceAll("\\+", " ");

        final Map<String, String> response = new TreeMap<String, String>();
        response.put("datastore", datastoreName);
        response.put("action", "remove");

        final TenantContext tenantContext = _contextFactory.getContext(tenant);
        if (tenantContext.getConfiguration().getDatastoreCatalog().getDatastore(datastoreName) == null) {
            response.put("status", "FAILURE");
            response.put("message", "No such datastore: " + datastoreName);
            return response;
        }

        try {
            datastoreDao.removeDatastore(tenantContext, datastoreName);
        } catch (Exception e) {
            logger.error("Removing datastore '" + datastoreName + "' from tenant '" + tenant
                    + "'s configuration failed", e);
            response.put("status", "FAILURE");
            response.put("message", e.getMessage());
            return response;
        }

        response.put("status", "SUCCESS");
        response.put("message", "Datastore was removed succesfully");

        logger.debug("Response payload: {}", response);
        return response;
    }

}
