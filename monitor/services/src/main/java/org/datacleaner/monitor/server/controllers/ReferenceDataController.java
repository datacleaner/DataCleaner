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
import java.io.InputStream;

import javax.annotation.security.RolesAllowed;
import javax.xml.bind.JAXBException;

import org.apache.metamodel.util.FileHelper;
import org.datacleaner.monitor.configuration.TenantContext;
import org.datacleaner.monitor.configuration.TenantContextFactory;
import org.datacleaner.monitor.configuration.WriteUpdatedConfigurationFileAction;
import org.datacleaner.monitor.shared.model.SecurityRoles;
import org.datacleaner.repository.RepositoryFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping(value = "/{tenant}/referencedata")
public class ReferenceDataController {
    @Autowired
    TenantContextFactory _contextFactory;

    @RolesAllowed(SecurityRoles.CONFIGURATION_EDITOR)
    @RequestMapping(method = RequestMethod.POST, produces = MediaType.TEXT_HTML_VALUE,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String uploadReferenceData(@PathVariable("tenant") final String tenant,
            @RequestParam("file") final MultipartFile file) {
        String status = "success";

        try {
            processUploadedFile(tenant, file.getInputStream());
        } catch (Exception e) {
            status = "failure";
        }

        return "redirect:/referencedata?upload_status=" + status;
    }

    private void processUploadedFile(String tenantId, InputStream inStream) throws IOException, JAXBException {
        try {
            final TenantContext context = _contextFactory.getContext(tenantId);
            final RepositoryFile configurationFile = context.getConfigurationFile();
            final WriteUpdatedConfigurationFileAction writeAction =
                    new WriteUpdatedConfigurationFileAction(inStream, configurationFile, true);
            configurationFile.writeFile(writeAction);
        } finally {
            FileHelper.safeClose(inStream);
        }
    }
}
