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

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.monitor.configuration.TenantContext;
import org.datacleaner.monitor.configuration.TenantContextFactory;
import org.datacleaner.monitor.server.dao.DatastoreDao;
import org.datacleaner.monitor.shared.model.SecurityRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.w3c.dom.Element;

@Controller
@RequestMapping(value = "/{tenant}/datastores")
public class DatastoresFolderController {

    @Autowired
    TenantContextFactory _contextFactory;

    @Autowired
    DatastoreDao _datastoreDao;

    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public List<Map<String, String>> datastoresFolderJson(@PathVariable("tenant") String tenant) {
        final TenantContext context = _contextFactory.getContext(tenant);

        final DatastoreCatalog datastoreCatalog = context.getConfiguration().getDatastoreCatalog();
        final String[] names = datastoreCatalog.getDatastoreNames();

        final List<Map<String, String>> result = new ArrayList<Map<String, String>>();

        for (String name : names) {
            final Datastore datastore = datastoreCatalog.getDatastore(name);
            final Map<String, String> map = new LinkedHashMap<String, String>();
            map.put("name", name);
            map.put("description", datastore.getDescription());
            map.put("type", datastore.getClass().getSimpleName());
            result.add(map);
        }

        return result;
    }

    @RolesAllowed(SecurityRoles.CONFIGURATION_EDITOR)
    @RequestMapping(method = RequestMethod.POST)
    protected void registerDatastore(@PathVariable("tenant") String tenant, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        final TenantContext tenantContext = _contextFactory.getContext(tenant);

        final BufferedReader reader = request.getReader();

        final Element datastoreElement = _datastoreDao.parseDatastoreElement(reader);

        _datastoreDao.addDatastore(tenantContext, datastoreElement);
    }
}
