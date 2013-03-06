/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.security.RolesAllowed;

import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContextFactory;
import org.eobjects.datacleaner.monitor.configuration.WriteUpdatedConfigurationFileAction;
import org.eobjects.datacleaner.monitor.shared.model.SecurityRoles;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.eobjects.metamodel.util.Action;
import org.eobjects.metamodel.util.FileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/{tenant}/conf.xml")
public class ConfigurationFileController {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationFileController.class);

    @Autowired
    TenantContextFactory _contextFactory;

    @RolesAllowed(SecurityRoles.CONFIGURATION_EDITOR)
    @RequestMapping(method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public Map<String, String> uploadConfigurationFile(@PathVariable("tenant") final String tenant,
            @RequestParam("file") final MultipartFile file) throws Exception {
        if (file == null) {
            logger.warn("No upload file provided, throwing IllegalArgumentException");
            throw new IllegalArgumentException(
                    "No file upload provided. Please provide a multipart file using the 'file' HTTP parameter.");
        }

        logger.info("Upload of new configuration file beginning");

        try {
            final TenantContext context = _contextFactory.getContext(tenant);
            final RepositoryFile configurationFile = context.getConfigurationFile();

            final InputStream inputStream = file.getInputStream();
            final WriteUpdatedConfigurationFileAction writeAction = new WriteUpdatedConfigurationFileAction(
                    inputStream, configurationFile);

            try {
                configurationFile.writeFile(writeAction);
            } finally {
                FileHelper.safeClose(inputStream);
            }

            final Map<String, String> result = new HashMap<String, String>();
            result.put("status", "Success");
            result.put("file_type", configurationFile.getType().toString());
            result.put("filename", configurationFile.getName());
            result.put("repository_path", configurationFile.getQualifiedPath());

            return result;
        } catch (Exception e) {
            logger.warn("An error occurred while uploading new configuration file for tenant " + tenant, e);
            throw e;
        }
    }

    @RolesAllowed(SecurityRoles.CONFIGURATION_EDITOR)
    @RequestMapping(method = RequestMethod.GET, produces = "application/xml")
    public void downloadConfigurationFile(@PathVariable("tenant") final String tenant, final OutputStream out) {

        final TenantContext context = _contextFactory.getContext(tenant);
        final RepositoryFile configurationFile = context.getConfigurationFile();

        if (configurationFile == null) {
            return;
        }

        configurationFile.readFile(new Action<InputStream>() {
            @Override
            public void run(InputStream in) throws Exception {
                FileHelper.copy(in, out);
            }
        });
    }
}
