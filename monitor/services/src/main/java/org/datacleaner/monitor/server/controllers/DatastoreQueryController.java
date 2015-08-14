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
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.metamodel.DataContext;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.query.SelectItem;
import org.apache.metamodel.query.parser.QueryParserException;
import org.datacleaner.components.convert.ConvertToStringTransformer;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.monitor.configuration.TenantContextFactory;
import org.datacleaner.monitor.shared.model.SecurityRoles;
import org.datacleaner.util.StringUtils;
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

    private static final Logger logger = LoggerFactory
            .getLogger(DatastoreQueryController.class);

    @Autowired
    TenantContextFactory _tenantContextFactory;

    @RolesAllowed(SecurityRoles.TASK_QUERY)
    @RequestMapping(value = "/{tenant}/datastores/{datastore}.query", method = RequestMethod.POST, produces = {
            "text/xml", "application/xml", "application/xhtml+xml", "text/html" })
    public void queryDatastorePost(HttpServletResponse response,
            @PathVariable("tenant") final String tenant,
            @PathVariable("datastore") String datastoreName,
            @RequestBody String query) throws IOException {
        queryDatastore(tenant, datastoreName, query, response);
    }

    @RolesAllowed(SecurityRoles.TASK_QUERY)
    @RequestMapping(value = "/{tenant}/datastores/{datastore}.query", method = RequestMethod.GET, produces = {
            "text/xml", "application/xml", "application/xhtml+xml", "text/html" })
    public void queryDatastoreGet(HttpServletResponse response,
            @PathVariable("tenant") final String tenant,
            @PathVariable("datastore") String datastoreName,
            @RequestParam("q") String query) throws IOException {
        response.setContentType("application/xhtml+xml");
        DataSet dataSet = queryDatastore(tenant, datastoreName, query, response);
        writeHtmlResult(response, dataSet);

    }

    @RolesAllowed(SecurityRoles.TASK_QUERY)
    @RequestMapping(value = "/{tenant}/datastores/{datastore}.query", method = RequestMethod.GET, headers = "Accept=application/json", produces = { "application/json" })
    public Map<String, Object> jsonQueryDatastoreGet(
            HttpServletResponse response,
            @PathVariable("tenant") final String tenant,
            @PathVariable("datastore") String datastoreName,
            @RequestParam("q") String query) throws IOException {
        response.setContentType("application/json");
        DataSet dataSet = queryDatastore(tenant, datastoreName, query, response);
        return writeJsonResult(dataSet);
    }

    private Map<String, Object> writeJsonResult(DataSet dataSet) throws IOException {

        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("table", createTableMap(dataSet));
        return map;
    }

    private Object createTableMap(DataSet dataSet) {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("header", createColumnHeaderList(dataSet.getSelectItems()));
        map.put("rows", createRowList(dataSet));
        return map;
    }

    private List<String> createColumnHeaderList(SelectItem[] selectItems) {
        List<String> columns = new ArrayList<String>();
        for (SelectItem selectItem : selectItems) {
            final String label = selectItem.getSuperQueryAlias(false);
            columns.add(label);
        }
        return columns;
    }

    private List<List<String>> createRowList(DataSet dataSet) {
        List<List<String>> rows = new ArrayList<List<String>>();
        while (dataSet.next()) {
            rows.add(createRowValueList(dataSet));
        }
        return rows;
    }

    private List<String> createRowValueList(DataSet dataSet) {
        List<String> values = new ArrayList<String>();
        Row row = dataSet.getRow();
        for (int i = 0; i < dataSet.getSelectItems().length; i++) {
            Object value = row.getValue(i);
            values.add(ConvertToStringTransformer.transformValue(value));
        }
        return values;
    }

    private DataSet queryDatastore(String tenant, String datastoreName,
            String query, HttpServletResponse response) throws IOException {

        datastoreName = datastoreName.replaceAll("\\+", " ");

        final DataCleanerConfiguration configuration = _tenantContextFactory
                .getContext(tenant).getConfiguration();
        final Datastore ds = configuration.getDatastoreCatalog().getDatastore(
                datastoreName);
        if (ds == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "No such datastore: " + datastoreName);
            return null;
        }

        String username = getUsername();

        if (StringUtils.isNullOrEmpty(query)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "No query defined");
            return null;
        }

        try (final DatastoreConnection con = ds.openConnection()) {
            final DataContext dataContext = con.getDataContext();
            try (final DataSet dataSet = dataContext.executeQuery(query)) {
                logger.info(
                        "Serving query result of datastore {} to user: {}. Query: {}",
                        new Object[] { datastoreName, username, query });

                return dataSet;
            }
        } catch (QueryParserException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Query parsing failed: " + e.getMessage());
            return null;
        }
    }

    private void writeHtmlResult(HttpServletResponse response,
            final DataSet dataSet) throws IOException {
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
                    writer.write(StringEscapeUtils
                            .escapeXml(ConvertToStringTransformer
                                    .transformValue(value)));
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
