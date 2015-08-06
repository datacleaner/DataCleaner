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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletResponse;

import org.apache.metamodel.DataContext;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.monitor.configuration.TenantContextFactory;
import org.datacleaner.monitor.shared.model.SecurityRoles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class DatastoreSchemaController {

    private static final Logger logger = LoggerFactory
            .getLogger(DatastoreSchemaController.class);

    @Autowired
    TenantContextFactory _tenantContextFactory;

//    @RolesAllowed(SecurityRoles.TASK_QUERY)
//    @RequestMapping(value = "/{tenant}/datastores/{datastore}.schema2", method = RequestMethod.GET, produces = {
//            "application/json" })
//    public void queryDatastoreGet(HttpServletResponse response,
//            @PathVariable("tenant") final String tenant,
//            @PathVariable("datastore") String datastoreName) throws IOException {
//        getSchema(tenant, datastoreName, response);
//    }
//
//    private void getSchema(String tenant, String datastoreName,
//            HttpServletResponse response) throws IOException {
//        response.setContentType("application/xhtml+xml");
//
//        datastoreName = datastoreName.replaceAll("\\+", " ");
//
//        final DataCleanerConfiguration configuration = _tenantContextFactory
//                .getContext(tenant).getConfiguration();
//        final Datastore datastore = configuration.getDatastoreCatalog()
//                .getDatastore(datastoreName);
//        if (datastore == null) {
//            response.sendError(HttpServletResponse.SC_NOT_FOUND,
//                    "No such datastore: " + datastoreName);
//            return;
//        }
//
//        String username = getUsername();
//
//        try (final DatastoreConnection con = datastore.openConnection()) {
//            final DataContext dataContext = con.getDataContext();
//            logger.info("Serving schema result of datastore {} to user: {}.",
//                    new Object[] { datastoreName, username });
//
//            final String[] schemaNames = dataContext.getSchemaNames();
//
//            final Writer writer = response.getWriter();
//            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
//            writer.write("\n<schema>");
//            writer.write(StringEscapeUtils.escapeXml(schemaNames[0]));
//            writer.write("</schema>");
//        }
//    }
    
    @RolesAllowed(SecurityRoles.TASK_QUERY)
    @RequestMapping(value = "/{tenant}/datastores/{datastore}.schemas", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Map<String, Object> getSchema(HttpServletResponse response,
            @PathVariable("tenant") final String tenant,
            @PathVariable("datastore") String datastoreName) throws IOException {
        
        datastoreName = datastoreName.replaceAll("\\+", " ");

        final DataCleanerConfiguration configuration = _tenantContextFactory
                .getContext(tenant).getConfiguration();
        final Datastore datastore = configuration.getDatastoreCatalog()
                .getDatastore(datastoreName);
        if (datastore == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "No such datastore: " + datastoreName);
            return Collections.emptyMap();
        }

        String username = getUsername();

        final DataContext dataContext = getDataContext(datastore);

        logger.info("Serving schema result of datastore {} to user: {}.",
                new Object[] { datastoreName, username });
        
        final String[] schemaNames = dataContext.getSchemaNames();
        
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("schemas", schemaNames);

        return map;
    }

    @RolesAllowed(SecurityRoles.TASK_QUERY)
    @RequestMapping(value = "/{tenant}/datastores/{datastore}/{schema}.tables", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Map<String, Object> getTables(HttpServletResponse response,
            @PathVariable("tenant") final String tenant,
            @PathVariable("datastore") String datastoreName,
            @PathVariable("schema") String schemaName) throws IOException {
        
        datastoreName = datastoreName.replaceAll("\\+", " ");

        final DataCleanerConfiguration configuration = _tenantContextFactory
                .getContext(tenant).getConfiguration();
        final Datastore datastore = configuration.getDatastoreCatalog()
                .getDatastore(datastoreName);
        if (datastore == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "No such datastore: " + datastoreName);
            return Collections.emptyMap();
        }

        String username = getUsername();

        final DataContext dataContext = getDataContext(datastore);

        logger.info("Serving tables in schema {} in datastore {} to user: {}.",
                new Object[] { schemaName, datastoreName, username });
        
        final Schema schema  = dataContext.getSchemaByName(schemaName);
        if (schema == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "No such schema: " + schemaName);
            return Collections.emptyMap();
        }

        final String[] tableNames = schema.getTableNames();

        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("tables", tableNames);
        
        return map;
    }
    
    @RolesAllowed(SecurityRoles.TASK_QUERY)
    @RequestMapping(value = "/{tenant}/datastores/{datastore}/{schema}/{table}.columns", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Map<String, Object> getColumns(HttpServletResponse response,
            @PathVariable("tenant") final String tenant,
            @PathVariable("datastore") String datastoreName,
            @PathVariable("schema") String schemaName,
            @PathVariable("table") String tableName) throws IOException {
        
        datastoreName = datastoreName.replaceAll("\\+", " ");

        final DataCleanerConfiguration configuration = _tenantContextFactory
                .getContext(tenant).getConfiguration();
        final Datastore datastore = configuration.getDatastoreCatalog()
                .getDatastore(datastoreName);
        if (datastore == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "No such datastore: " + datastoreName);
            return Collections.emptyMap();
        }

        String username = getUsername();

        final DataContext dataContext = getDataContext(datastore);

        logger.info("Serving columns of table {} in schema {} in datastore {} to user: {}.",
                new Object[] { tableName, schemaName, datastoreName, username });
        
        final Schema schema = dataContext.getSchemaByName(schemaName);
        if (schema == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "No such schema: " + schemaName);
            return Collections.emptyMap();
        }

        final Table table = schema.getTableByName(tableName);
        if (table == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "No such table: " + tableName);
            return Collections.emptyMap();
        }
        
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("columns", table.getColumnNames());

        return map;
    }

    private DataContext getDataContext(Datastore datastore) {
        try (final DatastoreConnection connection = datastore.openConnection()) {
            return connection.getDataContext();
        }
    }

    private String getUsername() {
        try {
            final Authentication authentication = SecurityContextHolder
                    .getContext().getAuthentication();
            return authentication.getName();
        } catch (Exception e) {
            logger.warn("Error occurred retreiving username", e);
            return null;
        }
    }

}
