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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datacleaner.configuration.AnalyzerBeansConfiguration;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.FileDatastore;
import org.datacleaner.connection.ResourceDatastore;
import org.datacleaner.monitor.configuration.TenantContextFactory;
import org.datacleaner.monitor.shared.model.SecurityRoles;
import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class DatastoreDownloadController {

    private static final Logger logger = LoggerFactory.getLogger(DatastoreDownloadController.class);

    @Autowired
    TenantContextFactory _tenantContextFactory;

    @RolesAllowed(SecurityRoles.CONFIGURATION_EDITOR)
    @RequestMapping(value = "/{tenant}/datastores/{datastore}.download", method = RequestMethod.GET)
    protected void downloadFileRepo(HttpServletRequest request, HttpServletResponse response,
            @PathVariable("tenant") final String tenant, @PathVariable("datastore") String datastoreName)
            throws IOException {
        datastoreName = datastoreName.replaceAll("\\+", " ");

        final AnalyzerBeansConfiguration configuration = _tenantContextFactory.getContext(tenant).getConfiguration();
        final Datastore ds = configuration.getDatastoreCatalog().getDatastore(datastoreName);
        if (ds == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "No such datastore: " + datastoreName);
            return;
        }

        InputStream is = getInputStream(ds, response);
        if (is == null) {
            return;
        }

        String filename = getFilename(ds);

        try {
            response.setHeader("Content-Disposition", "attachment; filename=" + filename);

            final OutputStream os = response.getOutputStream();
            FileHelper.copy(is, os);
        } finally {
            FileHelper.safeClose(is);
        }
    }

    private String getFilename(Datastore ds) {
        if (ds instanceof ResourceDatastore) {
            final Resource resource = ((ResourceDatastore) ds).getResource();
            return resource.getName();
        } else if (ds instanceof FileDatastore) {
            final String filename = ((FileDatastore) ds).getFilename();
            return new File(filename).getName();
        }
        return ds.getName();
    }

    private InputStream getInputStream(Datastore ds, HttpServletResponse response) throws IOException {
        if (ds instanceof ResourceDatastore) {
            final Resource resource = ((ResourceDatastore) ds).getResource();

            if (resource == null || !resource.isExists()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Datastore resource not found: " + ds);
                return null;
            }

            String username = getUsername();
            logger.info("Serving datastore resource {} to user: {}", resource, username);

            return resource.read();
        } else if (ds instanceof FileDatastore) {
            final FileDatastore fileDatastore = (FileDatastore) ds;
            final String filename = fileDatastore.getFilename();
            final File file = new File(filename);

            if (!file.exists() || !file.isFile()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Datastore file not found: " + filename);
                return null;
            }

            String username = getUsername();

            logger.info("Serving datastore file {} to user: {}", filename, username);

            return new FileInputStream(file);
        }

        response.sendError(HttpServletResponse.SC_NO_CONTENT, "Datastore is not file based: " + ds.getName());
        return null;
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
