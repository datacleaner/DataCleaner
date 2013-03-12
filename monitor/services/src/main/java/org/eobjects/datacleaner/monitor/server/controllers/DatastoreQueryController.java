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
import java.io.Writer;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.eobjects.analyzer.beans.convert.ConvertToStringTransformer;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreConnection;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.monitor.configuration.TenantContextFactory;
import org.eobjects.datacleaner.monitor.shared.model.SecurityRoles;
import org.eobjects.metamodel.DataContext;
import org.eobjects.metamodel.data.DataSet;
import org.eobjects.metamodel.data.Row;
import org.eobjects.metamodel.query.SelectItem;
import org.eobjects.metamodel.query.parser.QueryParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DatastoreQueryController {

    private static final Logger logger = LoggerFactory.getLogger(DatastoreQueryController.class);

    @Autowired
    TenantContextFactory _tenantContextFactory;

    @RolesAllowed(SecurityRoles.TASK_QUERY)
    @RequestMapping(value = "/{tenant}/datastores/{datastore}.query", method = RequestMethod.POST, produces = {
            "text/xml", "application/xml", "application/xhtml+xml", "text/html" })
    public void queryDatastorePost(HttpServletResponse response, @PathVariable("tenant") final String tenant,
            @PathVariable("datastore") String datastoreName, @RequestBody String query) throws IOException {
        queryDatastore(tenant, datastoreName, query, response);
    }

    @RolesAllowed(SecurityRoles.TASK_QUERY)
    @RequestMapping(value = "/{tenant}/datastores/{datastore}.query", method = RequestMethod.GET, produces = {
            "text/xml", "application/xml", "application/xhtml+xml", "text/html" })
    public void queryDatastoreGet(HttpServletResponse response, @PathVariable("tenant") final String tenant,
            @PathVariable("datastore") String datastoreName, @RequestParam("q") String query) throws IOException {
        queryDatastore(tenant, datastoreName, query, response);
    }

    private void queryDatastore(String tenant, String datastoreName, String query, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/xhtml+xml");
        
        datastoreName = datastoreName.replaceAll("\\+", " ");

        final AnalyzerBeansConfiguration configuration = _tenantContextFactory.getContext(tenant).getConfiguration();
        final Datastore ds = configuration.getDatastoreCatalog().getDatastore(datastoreName);
        if (ds == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "No such datastore: " + datastoreName);
            return;
        }

        String username = getUsername();

        if (StringUtils.isNullOrEmpty(query)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No query defined");
            return;
        }

        final DatastoreConnection con = ds.openConnection();
        try {
            final DataContext dataContext = con.getDataContext();
            final DataSet dataSet = dataContext.executeQuery(query);
            try {
                logger.info("Serving query result of datastore {} to user: {}. Query: {}", new Object[] {
                        datastoreName, username, query });

                final Writer writer = response.getWriter();
                writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                writer.write("\n<table xmlns=\"http://www.w3.org/1999/xhtml\">");

                writer.write("\n<thead>\n<tr>");
                final SelectItem[] selectItems = dataSet.getSelectItems();
                for (SelectItem selectItem : selectItems) {
                    final String label = selectItem.getSuperQueryAlias(false);
                    writer.write("<th>");
                    writer.write(StringEscapeUtils.escapeXml(label));
                    writer.write("</th>");
                }
                writer.write("</tr>\n</thead>");
                writer.flush();

                writer.write("\n<tbody>");
                int rowNumber = 1;
                while (dataSet.next()) {
                    writer.write("\n<tr>");
                    Row row = dataSet.getRow();
                    for (int i = 0; i < selectItems.length; i++) {
                        Object value = row.getValue(i);
                        if (value == null) {
                            writer.write("<td />");
                        } else {
                            writer.write("<td>");
                            writer.write(StringEscapeUtils.escapeXml(ConvertToStringTransformer.transformValue(value)));
                            writer.write("</td>");
                        }
                    }
                    writer.write("</tr>");

                    if (rowNumber % 20 == 0) {
                        writer.flush();
                    }

                    rowNumber++;
                }
                writer.write("\n</tbody>");
                writer.write("\n</table>");
            } finally {
                dataSet.close();
            }
        } catch (QueryParserException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Query parsing failed: " + e.getMessage());
        } finally {
            con.close();
        }
    }

    private String getUsername() {
        try {
            final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            return authentication.getName();
        } catch (Exception e) {
            logger.warn("Error occurred retreiving username", e);
            return null;
        }
    }

}
