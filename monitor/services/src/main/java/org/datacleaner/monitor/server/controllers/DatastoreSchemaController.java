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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletResponse;

import org.apache.metamodel.DataContext;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.ColumnType;
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

    @RolesAllowed(SecurityRoles.TASK_QUERY)
    @RequestMapping(value = "/{tenant}/datastores/{datastore}.schemas", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Map<String, Object> getSchemas(HttpServletResponse response,
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

        logger.info("Serving schemas in datastore {} to user: {}.",
                new Object[] { datastoreName, username });
        
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("schemas", createSchemaList(dataContext));

        return map;
    }

    private List<Map<String,Object>> createSchemaList(DataContext dataContext) {
        List<Map<String,Object>> schemas = new ArrayList<Map<String,Object>>();
        for (Schema schema : dataContext.getSchemas()) {
            schemas.add(createSchemaMap(schema));
        }
        return schemas;
    }

    private Map<String, Object> createSchemaMap(Schema schema) {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("name", schema.getName());
        map.put("tables", createTableList(schema));
        return map;
    }

    private List<Map<String,Object>> createTableList(Schema schema) {
        List<Map<String,Object>> tables = new ArrayList<Map<String,Object>>();
        for (Table table : schema.getTables()) {
            tables.add(createTableMap(table));
        }
        return tables;
    }

    private Map<String, Object> createTableMap(Table table) {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("name", table.getName());
        map.put("columns", createColumnList(table));
        return map;
    }

    private List<Map<String,Object>> createColumnList(Table table) {
        List<Map<String,Object>> columns = new ArrayList<Map<String,Object>>();
        for (Column column : table.getColumns()) {
            columns.add(createColumnMap(column));
        }
        return columns;
    }

    private Map<String, Object> createColumnMap(Column column) {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("name", column.getName());
        map.put("number", column.getColumnNumber());
        map.put("type", getTypeName(column));
        map.put("size", column.getColumnSize());
        map.put("nativeType", column.getNativeType());
        map.put("nullable", column.isNullable());
        map.put("remarks", column.getRemarks());
        map.put("indexed", column.isIndexed());
        map.put("quote", column.getQuote());
        map.put("primaryKey", column.isPrimaryKey());
        return map;
    }

    private String getTypeName(Column column) {
        ColumnType type = column.getType();
        return type == null ? null : type.getName();
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
