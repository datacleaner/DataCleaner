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
import java.util.Map.Entry;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletResponse;

import org.apache.metamodel.UpdateCallback;
import org.apache.metamodel.UpdateScript;
import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.update.RowUpdationBuilder;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.UpdateableDatastore;
import org.datacleaner.connection.UpdateableDatastoreConnection;
import org.datacleaner.monitor.configuration.TenantContextFactory;
import org.datacleaner.monitor.shared.model.SecurityRoles;
import org.datacleaner.util.StringUtils;
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
public class DatastoreUpdateController {
    
    private static final Logger logger = LoggerFactory.getLogger(DatastoreUpdateController.class);

    @Autowired
    TenantContextFactory _tenantContextFactory;
        
    @RolesAllowed(SecurityRoles.TASK_QUERY)
    @RequestMapping(value = "/{tenant}/datastores/{datastore}.update", method = RequestMethod.PUT, headers = "Accept=application/json", produces = {
            "application/json" })
    @ResponseBody
    public Map<String, Object> datastorePut(HttpServletResponse response,
            @PathVariable("tenant") final String tenant, @PathVariable("datastore") String datastoreName,
            @RequestBody String query) throws IOException {
        final DataCleanerConfiguration configuration = _tenantContextFactory.getContext(tenant).getConfiguration();
        final Datastore ds = configuration.getDatastoreCatalog().getDatastore(datastoreName);
        if(ds instanceof UpdateableDatastore) {
            final UpdateableDatastore uds = (UpdateableDatastore) ds;
            final UpdateableDatastoreConnection connection = uds.openConnection();
            try {
                final UpdateableDataContext dataContext = connection.getUpdateableDataContext();
                
                if (StringUtils.isNullOrEmpty(query)) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No query defined");
                }

                final UpdateQuery parsedQuery = parseUpdateQuery(dataContext, query);
                UpdateScript script = createUpdateScript(parsedQuery);
                dataContext.executeUpdate(script);
            } finally {
                connection.close();
            }
        } else {
            throw new RuntimeException("Datastore " + datastoreName + " is not updateable.");
        }
        response.setContentType("application/json");
        return getJsonResult(tenant, datastoreName, query, response);
    }

    private UpdateScript createUpdateScript(final UpdateQuery parsedQuery) {
        final UpdateScript script = new UpdateScript() {
            
            @Override
            public void run(UpdateCallback callback) {
                RowUpdationBuilder builder = callback.update(parsedQuery.getTable());
                for(final Entry<String, Object> updateColumn: parsedQuery.getUpdateColumns().entrySet()) {
                    builder = builder.value(updateColumn.getKey(), updateColumn.getValue());
                }
                for(final Entry<String, Object> whereCondition: parsedQuery.getWhereConditions().entrySet()) {
                    builder = builder.where(whereCondition.getKey()).eq(whereCondition.getValue());
                }
            }
        };
        return script;
    }

    private UpdateQuery parseUpdateQuery(UpdateableDataContext dataContext, String query) {
        final UpdateQueryParser parser = new UpdateQueryParser(dataContext, query);
        final UpdateQuery parsed = parser.parseUpdate();
        return parsed;
    }

    private Map<String, Object> getJsonResult(String tenant, String datastoreName, String query,
            HttpServletResponse response) {
        final Map<String, Object> map = new HashMap<>();
        map.put("Result", "Hoera!");
        return map;
    }

}
